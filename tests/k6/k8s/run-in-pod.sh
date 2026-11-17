#!/bin/sh
set -u
finish() {
    echo "$1" > /results/exit-code
    touch /results/done
    sleep "${HOLD_SECONDS:-3600}"
    exit "$1"
}
deadline=$(( $(date +%s) + ${READY_TIMEOUT:-600} ))
until k6 run --quiet --log-format=raw /k6/tools/ready.js >/dev/null 2>&1; do
    if [ "$(date +%s)" -ge "$deadline" ]; then
        echo "Environment is not ready after ${READY_TIMEOUT:-600}s: management, inspection or history do not answer"
        finish 1
    fi
    sleep 10
done
{ k6 run --quiet --log-format=raw "/k6/suites/${SUITE}/main.js"; echo $? > /results/k6-exit-code; } 2>&1 | tee /results/run.log
finish "$(cat /results/k6-exit-code)"
