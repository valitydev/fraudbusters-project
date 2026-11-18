#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
project=${REGRESSION_PROJECT:-fb-regression}
k6_image=${K6_IMAGE:-grafana/k6:0.54.0}
services=(fraudbusters-api fraudbusters fb-management fb-notificator fb-warehouse wblist trusted-tokens-manager columbus)
series_steps_default=(payments:smoke payouts:smoke payments:functional payouts:functional mixed:functional
    payments:baseline payments:load payments:spike payments:stress mixed:load)
load_profiles=(baseline load stress spike soak)

is_arm() {
    [[ "$(uname -m)" == arm64 || "$(uname -m)" == aarch64 ]]
}

infra_directory_of() {
    local commit
    commit=$(sed -n 's/^INFRA_COMMIT=//p' "tests/k6/versions/$1.env")
    if [[ -z "$commit" ]]; then
        echo infra
        return
    fi
    local directory="tests/k6/.infra/$commit"
    if [[ ! -f "$directory/infra/compose.yaml" ]]; then
        rm -rf "$directory"
        mkdir -p "$directory"
        git archive "$commit" infra | tar -x -C "$directory" \
            || { echo "Cannot read infra of commit $commit" >&2; exit 2; }
    fi
    echo "$directory/infra"
}

keep_existing_services() {
    awk 'NR == FNR { existing[$0] = 1; next }
        /^[^ ]/ { shown = 1 }
        /^  [^ #]/ { name = $1; sub(/:$/, "", name); shown = (name in existing) }
        shown' "$1" "$2" > "$3"
}

compose() {
    local infra
    infra=$(infra_directory_of "$1")
    local files=(-f "$infra/compose.yaml")
    if is_arm; then files+=(-f "$infra/compose.arm.yaml"); fi
    local overrides="tests/k6/.infra/overrides/$project"
    mkdir -p "$overrides"
    docker compose -p "$project" --env-file "tests/k6/versions/$1.env" "${files[@]}" --profile core --profile apps \
        config --services > "$overrides/services"
    local regression_files=(tests/k6/compose.regression.yaml)
    if is_arm; then regression_files+=(tests/k6/compose.regression.arm.yaml); fi
    local source target
    for source in "${regression_files[@]}"; do
        target="$overrides/$(basename "$source")"
        keep_existing_services "$overrides/services" "$source" "$target"
        if grep -q '^  [^ #]' "$target"; then files+=(-f "$target"); fi
    done
    docker compose -p "$project" --env-file "tests/k6/versions/$1.env" "${files[@]}" --profile core --profile apps "${@:2}"
}

remove_stack() {
    compose "$1" down -v --remove-orphans
    local volumes
    volumes=$(docker volume ls -q --filter "label=com.docker.compose.project=$project")
    if [[ -n "$volumes" ]]; then
        docker volume rm $volumes >/dev/null
    fi
}

series_log() {
    echo "$(date -u +%FT%TZ) $*" | tee -a "$series_directory/series.log"
}

sample_container_stats() {
    echo 'time,container,cpu,memory' > "$1"
    while true; do
        local containers
        containers=$(docker ps -q --filter "label=com.docker.compose.project=$project" || true)
        if [[ -n "$containers" ]]; then
            docker stats --no-stream --format '{{.Name}},{{.CPUPerc}},{{.MemUsage}}' $containers 2>/dev/null \
                | sed "s/^/$(date -u +%H:%M:%S),/" >> "$1" || true
        fi
        sleep "${STATS_INTERVAL:-15}"
    done
}

summarize_clickhouse_queries() {
    compose "$1" exec -T clickhouse sh -c \
        'clickhouse-client -u "${CLICKHOUSE_USER:-default}" --password "${CLICKHOUSE_PASSWORD:-}" -n --query "$0"' "
        SYSTEM FLUSH LOGS;
        SELECT
            version() AS server_version,
            count() AS queries,
            countIf(query ILIKE 'SELECT%') AS selects,
            countIf(query ILIKE 'INSERT%') AS inserts,
            round(avgIf(query_duration_ms, query ILIKE 'SELECT%'), 2) AS select_avg_ms,
            round(quantileIf(0.95)(query_duration_ms, query ILIKE 'SELECT%'), 2) AS select_p95_ms,
            max(memory_usage) AS max_query_memory
        FROM system.query_log
        WHERE type = 'QueryFinish'
        FORMAT Vertical"
}

run_series_step() {
    local version=$1 suite=$2 profile=$3
    series_run=$((series_run + 1))
    local label
    label=$(printf '%02d-%s-%s-%s' "$series_run" "$suite" "$profile" "$version")
    series_log "START $label"
    remove_stack "$version" >> "$series_directory/$label.compose.log" 2>&1 || true
    if ! "$0" up "$version" >> "$series_directory/$label.compose.log" 2>&1; then
        series_log "UP FAILED $label, see $label.compose.log"
        remove_stack "$version" >> "$series_directory/$label.compose.log" 2>&1 || true
        return 1
    fi
    sample_container_stats "$series_directory/$label.stats.csv" &
    local sampler=$!
    local status=0
    "$0" test "$version" "$suite" "$profile" > "$series_directory/$label.k6.log" 2>&1 || status=$?
    kill "$sampler" 2>/dev/null || true
    wait "$sampler" 2>/dev/null || true
    summarize_clickhouse_queries "$version" > "$series_directory/$label.clickhouse.txt" 2>&1 || true
    remove_stack "$version" >> "$series_directory/$label.compose.log" 2>&1 || true
    series_results=$(sed -n 's/^Results: //p' "$series_directory/$label.k6.log" | tail -1)
    printf '%s\t%s\t%s\t%s\t%s\t%s\n' "$label" "$version" "$suite" "$profile" "$status" "$series_results" \
        >> "$series_directory/runs.tsv"
    series_log "DONE $label status=$status $series_results"
}

compare_series_pair() {
    local label=$1 reference_results=$2 candidate_results=$3
    if [[ -z "$reference_results" || -z "$candidate_results" ]]; then
        series_log "COMPARE SKIPPED $label: a run has no results"
        return 1
    fi
    if node tests/k6/tools/compare.mjs "$reference_results" "$candidate_results" > "$series_directory/$label.compare.md"; then
        printf '%s\t%s\n' "$label" "no regressions" >> "$series_directory/compares.tsv"
        series_log "COMPARE $label: no regressions"
        return 0
    fi
    printf '%s\t%s\n' "$label" "regressions" >> "$series_directory/compares.tsv"
    series_log "COMPARE $label: regressions, see $label.compare.md"
    return 1
}

write_series_summary() {
    {
        echo "# Серия $1 → $2"
        echo
        echo '| Прогон | Версия | Набор | Профиль | Код выхода | Результаты |'
        echo '| --- | --- | --- | --- | --- | --- |'
        if [[ -f "$series_directory/runs.tsv" ]]; then
            while IFS=$'\t' read -r label version suite profile status results; do
                echo "| $label | $version | $suite | $profile | $status | ${results:+$results/report.md} |"
            done < "$series_directory/runs.tsv"
        fi
        echo
        echo '| Сравнение | Итог |'
        echo '| --- | --- |'
        if [[ -f "$series_directory/compares.tsv" ]]; then
            while IFS=$'\t' read -r label outcome; do
                echo "| [$label]($label.compare.md) | $outcome |"
            done < "$series_directory/compares.tsv"
        fi
    } > "$series_directory/summary.md"
}

is_load_profile() {
    local profile
    for profile in "${load_profiles[@]}"; do
        [[ "$profile" == "$1" ]] && return 0
    done
    return 1
}

settings=(K6_CASES RUN_ID RATE TARGET_RATE SPIKE_RATE DURATION RAMP_DURATION STEP_DURATION VUS MAX_VUS CONCURRENCY
    WAIT_TIMEOUT_MS POLL_INTERVAL_MS REQUEST_TIMEOUT_MS CASES_MAX_DURATION P95_LIMIT_MS SHARE_ACCEPT SHARE_AMOUNT
    SHARE_BLACK SHARE_VELOCITY SHARE_HISTORY_SEARCH SHARE_PAYOUT_ACCEPT SHARE_PAYOUT_BLACK RECONFIGURE RECONFIGURE_AT
    STRESS_FACTORS K6_PHASE FEATURES FROM_FEATURES FB_MANAGEMENT_URL FB_API_URL FB_INSPECTOR_URL FB_AUTH_URL
    FB_AUTH_REALM FB_AUTH_CLIENT READY_TIMEOUT K6_GLOBAL_CASES)
secrets=(FB_TOKEN FB_AUTH_USER FB_AUTH_PASSWORD)

usage() {
    cat >&2 <<'USAGE'
Usage:
  scripts/run-k6.sh up <version>                      start the regression stack from tests/k6/versions/<version>.env
  scripts/run-k6.sh test <version> <suite> <profile>  run tests/k6/suites/<suite> with a profile
  scripts/run-k6.sh lifecycle <version> [<to>]        prepare, restart or upgrade to <to>, verify
  scripts/run-k6.sh series <reference> <candidate> [<suite>:<profile> ...]
                                                      run each step on fresh stacks of both versions and compare them
  scripts/run-k6.sh down [<version>]                  remove the regression stack and its volumes
  scripts/run-k6.sh k8s <suite> <profile>             run a suite as a Kubernetes Job; settings come from
                                                      ConfigMap and Secret k6-regression of the namespace;
                                                      K6_TESTS_IMAGE runs the tests built into that image
USAGE
    exit 2
}

version_file() {
    [[ -f "tests/k6/versions/$1.env" ]] || { echo "Unknown version $1; see tests/k6/versions" >&2; exit 2; }
}

features_of() {
    sed -n 's/^FEATURES=//p' "tests/k6/versions/$1.env"
}

wait_ready() {
    local deadline=$((SECONDS + ${READY_TIMEOUT:-600}))
    until docker run --rm --network "${project}_default" -v "$PWD/tests/k6:/k6:ro" "$k6_image" \
        run --quiet --log-format=raw /k6/tools/ready.js >/dev/null 2>&1; do
        (( SECONDS < deadline )) || { echo "Stack $project is not ready after ${READY_TIMEOUT:-600}s" >&2; exit 1; }
        sleep 10
    done
}

case "${1:-}" in
    up)
        version=${2:?Usage: run-k6.sh up <version>}
        version_file "$version"
        compose "$version" up -d "${services[@]}"
        wait_ready
        echo "Regression stack $project is ready with version $version"
        ;;
    test)
        version=${2:?Usage: run-k6.sh test <version> <suite> <profile>}
        suite=${3:?Usage: run-k6.sh test <version> <suite> <profile>}
        profile=${4:?Usage: run-k6.sh test <version> <suite> <profile>}
        version_file "$version"
        [[ -f "tests/k6/suites/$suite/main.js" ]] || { echo "Unknown suite $suite" >&2; exit 2; }
        results="tests/k6/results/$suite-$profile-$version-$(date -u +%Y%m%dT%H%M%SZ)"
        mkdir -p "$results"
        FEATURES=$(features_of "$version")
        K6_GLOBAL_CASES=${K6_GLOBAL_CASES:-on}
        passthrough=()
        for name in "${settings[@]}" "${secrets[@]}"; do
            if [[ -n "${!name+set}" ]]; then passthrough+=(-e "$name=${!name}"); fi
        done
        set +e
        docker run --rm --network "${project}_default" -u "$(id -u):$(id -g)" \
            -v "$PWD/tests/k6:/k6:ro" -v "$PWD/$results:/results" \
            -e K6_PROFILE="$profile" -e VERSION="$version" -e RESULTS_DIR=/results ${passthrough[@]+"${passthrough[@]}"} \
            "$k6_image" run --quiet --log-format=raw "/k6/suites/$suite/main.js" 2>&1 | tee "$results/run.log"
        status=${PIPESTATUS[0]}
        set -e
        node tests/k6/tools/report.mjs "$results" | tee "$results/report.md"
        echo "Results: $results"
        exit "$status"
        ;;
    lifecycle)
        from=${2:?Usage: run-k6.sh lifecycle <version> [<to-version>]}
        to=${3:-$from}
        version_file "$from"
        version_file "$to"
        [[ "$(infra_directory_of "$from")" == "$(infra_directory_of "$to")" ]] \
            || { echo "Versions $from and $to use different INFRA_COMMIT; lifecycle needs one infrastructure" >&2; exit 2; }
        export RUN_ID=${RUN_ID:-lc$(date +%H%M%S)}
        export FROM_FEATURES
        FROM_FEATURES=$(features_of "$from")
        set +e
        K6_PHASE=prepare "$0" test "$from" lifecycle functional
        prepared=$?
        set -e
        (( prepared == 0 )) || { echo "Prepare phase failed, verify is skipped" >&2; exit "$prepared"; }
        if [[ "$to" == "$from" ]]; then
            compose "$from" restart "${services[@]}"
        else
            compose "$to" up -d "${services[@]}"
        fi
        wait_ready
        K6_PHASE=verify "$0" test "$to" lifecycle functional
        ;;
    k8s)
        suite=${2:?Usage: run-k6.sh k8s <suite> <profile>}
        profile=${3:?Usage: run-k6.sh k8s <suite> <profile>}
        [[ -f "tests/k6/suites/$suite/main.js" ]] || { echo "Unknown suite $suite" >&2; exit 2; }
        label=${VERSION:-k8s}
        config=${K8S_CONFIG:-k6-regression}
        secret=${K8S_SECRET:-k6-regression}
        kube=(kubectl)
        [[ -n "${K8S_NAMESPACE:-}" ]] && kube+=(-n "$K8S_NAMESPACE")
        name=$(printf 'k6-%s-%s-%s' "$suite" "$profile" "$(date -u +%m%d%H%M%S)" | tr '[:upper:]_' '[:lower:]-')
        results="tests/k6/results/$suite-$profile-$label-$(date -u +%Y%m%dT%H%M%SZ)"
        job_template=tests/k6/k8s/job.yaml
        job_image=$k6_image
        if [[ -n "${K6_TESTS_IMAGE:-}" ]]; then
            job_template=tests/k6/k8s/job-image.yaml
            job_image=$K6_TESTS_IMAGE
        fi
        bundle=$(mktemp -d)
        created=
        cleanup() {
            rm -rf "$bundle"
            [[ -z "$created" || "${K8S_KEEP:-}" == 1 ]] && return
            "${kube[@]}" delete job "$name" --ignore-not-found --wait=false >/dev/null 2>&1 || true
            "${kube[@]}" delete configmap "$name" --ignore-not-found >/dev/null 2>&1 || true
        }
        trap cleanup EXIT
        env_entry() { printf '            - name: %s\n              value: "%s"\n' "$1" "${2//\"/\\\"}"; }
        {
            env_entry SUITE "$suite"
            env_entry K6_PROFILE "$profile"
            env_entry VERSION "$label"
            env_entry RESULTS_DIR /results
            for key in "${settings[@]}"; do
                if [[ -n "${!key+set}" ]]; then env_entry "$key" "${!key}"; fi
            done
        } > "$bundle/env.yaml"
        awk -v env_file="$bundle/env.yaml" '/^__ENV__$/ { while ((getline line < env_file) > 0) print line; next } { print }' \
            "$job_template" \
            | sed -e "s|__NAME__|$name|g" -e "s|__SUITE__|$suite|g" -e "s|__PROFILE__|$profile|g" \
                -e "s|__IMAGE__|$job_image|g" -e "s|__CPU__|${K8S_CPU:-1}|g" -e "s|__MEMORY__|${K8S_MEMORY:-1Gi}|g" \
                -e "s|__CONFIG__|$config|g" -e "s|__SECRET__|$secret|g" \
            > "$bundle/job.yaml"
        if [[ "${K8S_DRY_RUN:-}" == 1 ]]; then
            cat "$bundle/job.yaml"
            exit 0
        fi
        "${kube[@]}" get configmap "$config" >/dev/null \
            || { echo "ConfigMap $config with FB_* settings of the environment is required" >&2; exit 2; }
        created=1
        if [[ -z "${K6_TESTS_IMAGE:-}" ]]; then
            tar czf "$bundle/tests.tgz" -C tests/k6 --exclude ./results --exclude ./reports .
            "${kube[@]}" create configmap "$name" --from-file=tests.tgz="$bundle/tests.tgz" >/dev/null
        fi
        "${kube[@]}" apply -f "$bundle/job.yaml" >/dev/null
        echo "Job $name in namespace ${K8S_NAMESPACE:-of the current context}"
        pod=
        until [[ -n "$pod" ]]; do
            pod=$("${kube[@]}" get pods -l "job-name=$name" -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || true)
            [[ -n "$pod" ]] || sleep 5
        done
        "${kube[@]}" wait --for=condition=Ready "pod/$pod" --timeout="${K8S_START_TIMEOUT:-15m}" >/dev/null
        "${kube[@]}" logs -f "$pod" -c k6 &
        logs=$!
        until "${kube[@]}" exec "$pod" -c k6 -- test -f /results/done >/dev/null 2>&1; do
            phase=$("${kube[@]}" get pod "$pod" -o jsonpath='{.status.phase}' 2>/dev/null || true)
            [[ "$phase" == Failed || "$phase" == Succeeded ]] && { echo "Pod $pod ended as $phase" >&2; break; }
            sleep 15
        done
        kill "$logs" 2>/dev/null || true
        mkdir -p "$results"
        "${kube[@]}" cp -c k6 "$pod:/results/." "$results" >/dev/null
        status=$(cat "$results/exit-code" 2>/dev/null || echo 1)
        node tests/k6/tools/report.mjs "$results" | tee "$results/report.md"
        echo "Results: $results"
        exit "$status"
        ;;
    series)
        reference=${2:?Usage: run-k6.sh series <reference> <candidate> [<suite>:<profile> ...]}
        candidate=${3:?Usage: run-k6.sh series <reference> <candidate> [<suite>:<profile> ...]}
        version_file "$reference"
        version_file "$candidate"
        steps=("${@:4}")
        if (( ${#steps[@]} == 0 )); then steps=("${series_steps_default[@]}"); fi
        for step in "${steps[@]}"; do
            [[ "$step" == *:* && -f "tests/k6/suites/${step%%:*}/main.js" ]] \
                || { echo "Step $step must be <suite>:<profile> with a suite from tests/k6/suites" >&2; exit 2; }
        done
        series_directory="tests/k6/results/_series-$reference-$candidate-$(date -u +%Y%m%dT%H%M%SZ)"
        mkdir -p "$series_directory"
        series_run=0
        pair=0
        stopped=
        series_log "SERIES $reference → $candidate: ${steps[*]}"
        for step in "${steps[@]}"; do
            suite=${step%%:*}
            profile=${step#*:}
            pairs=1
            if is_load_profile "$profile"; then pairs=${LOAD_PAIRS:-2}; fi
            for (( repeat = 1; repeat <= pairs; repeat++ )); do
                if (( pair % 2 == 0 )); then order=("$reference" "$candidate"); else order=("$candidate" "$reference"); fi
                pair=$((pair + 1))
                reference_results=
                candidate_results=
                for version in "${order[@]}"; do
                    run_series_step "$version" "$suite" "$profile" || { stopped="stand of $version did not start"; break; }
                    if [[ "$version" == "$reference" ]]; then reference_results=$series_results; fi
                    if [[ "$version" == "$candidate" ]]; then candidate_results=$series_results; fi
                done
                [[ -z "$stopped" ]] || break
                if ! compare_series_pair "$(printf '%02d-%s-%s' "$pair" "$suite" "$profile")" \
                    "$reference_results" "$candidate_results"; then
                    if ! is_load_profile "$profile" && [[ "${SERIES_CONTINUE:-}" != 1 ]]; then
                        stopped="regression in $suite $profile"
                        break
                    fi
                fi
            done
            [[ -z "$stopped" ]] || break
        done
        write_series_summary "$reference" "$candidate"
        if [[ -n "$stopped" ]]; then
            series_log "SERIES STOPPED: $stopped"
            exit 1
        fi
        series_log "SERIES COMPLETE: $series_directory/summary.md"
        ;;
    down)
        version=${2:-$(ls tests/k6/versions | head -1)}
        remove_stack "${version%.env}"
        ;;
    *)
        usage
        ;;
esac
