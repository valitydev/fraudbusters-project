#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
project=${REGRESSION_PROJECT:-fb-regression}
k6_image=${K6_IMAGE:-grafana/k6:0.54.0}
services=(fraudbusters-api fraudbusters fb-management fb-notificator fb-warehouse wblist trusted-tokens-manager columbus)

compose() {
    local files=(-f infra/compose.yaml)
    if [[ "$(uname -m)" == arm64 || "$(uname -m)" == aarch64 ]]; then files+=(-f infra/compose.arm.yaml); fi
    files+=(-f tests/k6/compose.regression.yaml)
    docker compose -p "$project" --env-file "tests/k6/versions/$1.env" "${files[@]}" --profile core --profile apps "${@:2}"
}

settings=(K6_CASES RUN_ID RATE TARGET_RATE SPIKE_RATE DURATION RAMP_DURATION STEP_DURATION VUS MAX_VUS CONCURRENCY
    WAIT_TIMEOUT_MS POLL_INTERVAL_MS REQUEST_TIMEOUT_MS CASES_MAX_DURATION P95_LIMIT_MS SHARE_ACCEPT SHARE_AMOUNT
    SHARE_BLACK SHARE_VELOCITY SHARE_HISTORY_SEARCH SHARE_PAYOUT_ACCEPT SHARE_PAYOUT_BLACK RECONFIGURE RECONFIGURE_AT
    STRESS_FACTORS K6_PHASE FEATURES FROM_FEATURES FB_MANAGEMENT_URL FB_API_URL FB_INSPECTOR_URL FB_AUTH_URL
    FB_AUTH_REALM FB_AUTH_CLIENT READY_TIMEOUT)
secrets=(FB_TOKEN FB_AUTH_USER FB_AUTH_PASSWORD)

usage() {
    cat >&2 <<'USAGE'
Usage:
  scripts/run-k6.sh up <version>                      start the regression stack from tests/k6/versions/<version>.env
  scripts/run-k6.sh test <version> <suite> <profile>  run tests/k6/suites/<suite> with a profile
  scripts/run-k6.sh lifecycle <version> [<to>]        prepare, restart or upgrade to <to>, verify
  scripts/run-k6.sh down                              remove the regression stack and its volumes
  scripts/run-k6.sh k8s <suite> <profile>             run a suite as a Kubernetes Job; settings come from
                                                      ConfigMap and Secret k6-regression of the namespace
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
            tests/k6/k8s/job.yaml \
            | sed -e "s|__NAME__|$name|g" -e "s|__SUITE__|$suite|g" -e "s|__PROFILE__|$profile|g" \
                -e "s|__IMAGE__|$k6_image|g" -e "s|__CPU__|${K8S_CPU:-1}|g" -e "s|__MEMORY__|${K8S_MEMORY:-1Gi}|g" \
                -e "s|__CONFIG__|$config|g" -e "s|__SECRET__|$secret|g" \
            > "$bundle/job.yaml"
        if [[ "${K8S_DRY_RUN:-}" == 1 ]]; then
            cat "$bundle/job.yaml"
            exit 0
        fi
        "${kube[@]}" get configmap "$config" >/dev/null \
            || { echo "ConfigMap $config with FB_* settings of the environment is required" >&2; exit 2; }
        tar czf "$bundle/tests.tgz" -C tests/k6 --exclude ./results --exclude ./reports .
        created=1
        "${kube[@]}" create configmap "$name" --from-file=tests.tgz="$bundle/tests.tgz" >/dev/null
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
    down)
        any_version=$(ls tests/k6/versions | head -1)
        compose "${any_version%.env}" down -v --remove-orphans
        ;;
    *)
        usage
        ;;
esac
