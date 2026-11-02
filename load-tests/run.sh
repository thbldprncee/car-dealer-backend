#!/usr/bin/env bash
# Runs the same k6 load tests against the monolith (commit 5baba7b) and the microservices (HEAD).
#
#   load-tests/run.sh                  # both architectures
#   load-tests/run.sh monolith         # one of them
#   READ_RATES="200 500" PURCHASE_RATES="5 10" load-tests/run.sh microservices
#
# Results go to load-tests/results/<arch>/: k6 summaries (JSON), docker stats samples (CSV)
# and, for the microservices, saga timings from order_db. summarize.py turns them into tables.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LT="$ROOT/load-tests"
K6_IMAGE=grafana/k6:0.54.0
MONOLITH_COMMIT=5baba7b

# An empty list skips that test: PURCHASE_RATES="" load-tests/run.sh
READ_RATES="${READ_RATES-250 500 1000 2000 3000}"
READ_DURATION="${READ_DURATION:-40s}"
PURCHASE_RATES="${PURCHASE_RATES-5 10 20 40}"
PURCHASE_DURATION="${PURCHASE_DURATION:-60s}"
DRAIN_TIMEOUT="${DRAIN_TIMEOUT:-300}"

log() { printf '\n[%s] %s\n' "$(date +%H:%M:%S)" "$*"; }

compose() {
  case "$ARCH" in
    monolith) docker compose -p lt-mono -f "$LT/monolith/docker-compose.yml" "$@" ;;
    microservices) ORDER_SERVICE_PORT="${ORDER_SERVICE_PORT:-18080}" \
      docker compose -p lt-micro -f "$ROOT/docker-compose.yml" -f "$LT/microservices-limits.yml" "$@" ;;
  esac
}

psql_in() { # service db [psql args...]
  local service=$1 db=$2; shift 2
  compose exec -T "$service" psql -U postgres -d "$db" -v ON_ERROR_STOP=1 "$@"
}

build_monolith() {
  local src; src="$(mktemp -d)"
  git -C "$ROOT" archive "$MONOLITH_COMMIT" | tar -x -C "$src"
  cp "$LT/monolith/Dockerfile" "$src/Dockerfile.load"
  docker build -q -f "$src/Dockerfile.load" -t "car-dealer-monolith:$MONOLITH_COMMIT" "$src"
  rm -rf "$src"
}

seed() { # Cars 1..30000 are bought by purchase.js, 900001..901000 are read by catalog.js.
  local service db
  for target in $SEED_TARGETS; do
    service=${target%%:*}; db=${target##*:}
    psql_in "$service" "$db" -q -v from=1 -v to=30000 -f - < "$LT/seed-cars.sql"
    psql_in "$service" "$db" -q -v from=900001 -v to=901000 -f - < "$LT/seed-cars.sql"
  done
}

k6() { # script name [env...]
  local script=$1 name=$2; shift 2
  local env=(-e "ARCH=$ARCH")
  for kv in "$@"; do env+=(-e "$kv"); done
  docker run --rm --network "$NETWORK" -v "$LT/k6":/scripts:ro -v "$OUT":/out \
    "${env[@]}" "$K6_IMAGE" run --quiet --no-color \
    ${name:+--summary-export "/out/$name.json"} "/scripts/$script" \
    | grep -E '✗|↳|http_req_duration\.|http_req_failed|dropped_iterations|purchase_ok|purchase_sync' || true
}

sample_stats() { # name: samples CPU/memory of the stack's containers every 2 s until stopped
  local file="$OUT/$1.stats.csv"
  echo "time,container,cpu_percent,mem" > "$file"
  while true; do
    docker stats --no-stream --format '{{.Name}},{{.CPUPerc}},{{.MemUsage}}' \
      | grep "^$PROJECT-" | sed "s/^/$(date +%s),/" >> "$file" || true
    sleep 2
  done
}

measured() { # name script [env...]: one k6 run with resource sampling around it
  local name=$1; shift
  sample_stats "$name" & local sampler=$!
  k6 "$1" "$name" "${@:2}"
  kill "$sampler"; wait "$sampler" 2>/dev/null || true
}

wait_for_saga() { # until no order paid in this run is still waiting for the warehouse
  local since=$1 waited=0 pending
  while (( waited < DRAIN_TIMEOUT )); do
    pending=$(psql_in order-db order_db -tA -c \
      "select count(*) from in_stock_car_orders o join outbox_events e on e.aggregate_id = o.id
       and e.topic = 'order.sent-for-approval' where e.created_at >= '$since' and o.status = 'PAID'")
    [[ "$pending" == 0 ]] && { echo "  saga drained in ${waited}s"; return; }
    sleep 5; waited=$((waited + 5))
  done
  echo "  saga NOT drained after ${DRAIN_TIMEOUT}s: $pending orders still PAID"
}

run_arch() {
  ARCH=$1
  OUT="$LT/results/$ARCH"
  mkdir -p "$OUT"  # a repeated step overwrites its own files, other steps are kept
  case "$ARCH" in
    monolith)
      PROJECT=lt-mono; READ_PATHS="direct"; SEED_TARGETS="db:car_dealer"
      log "Building the monolith from $MONOLITH_COMMIT"; build_monolith ;;
    microservices)
      PROJECT=lt-micro; READ_PATHS="direct grpc"; SEED_TARGETS="storage-db:storage_db"
      log "Building the microservices"; compose build -q ;;
  esac
  NETWORK="${PROJECT}_default"

  log "$ARCH: fresh stack"
  compose down -v --remove-orphans >/dev/null 2>&1 || true
  compose up -d --wait >/dev/null
  seed

  log "$ARCH: warm-up (JIT, connection pools), not recorded"
  k6 catalog.js "" RATE=300 DURATION=30s
  k6 purchase.js "" RATE=5 DURATION=30s OFFSET=29000
  [[ "$ARCH" == microservices ]] && k6 catalog.js "" RATE=300 DURATION=20s READ_PATH=grpc

  for path in $READ_PATHS; do
    for rate in $READ_RATES; do
      log "$ARCH: read ($path) at $rate req/s for $READ_DURATION"
      measured "read-$path-$rate" catalog.js RATE="$rate" DURATION="$READ_DURATION" READ_PATH="$path"
      sleep 5
    done
  done

  local offset=0 since
  for rate in $PURCHASE_RATES; do
    log "$ARCH: purchase at $rate flows/s for $PURCHASE_DURATION"
    since=$(date -u +%Y-%m-%dT%H:%M:%SZ)
    measured "purchase-$rate" purchase.js RATE="$rate" DURATION="$PURCHASE_DURATION" OFFSET="$offset"
    offset=$((offset + rate * ${PURCHASE_DURATION%s} + 100))
    if [[ "$ARCH" == microservices ]]; then
      wait_for_saga "$since"
      psql_in order-db order_db -A -F, -v since="$since" -f - < "$LT/saga-report.sql" \
        | head -2 > "$OUT/purchase-$rate.saga.csv"
      cat "$OUT/purchase-$rate.saga.csv"
    fi
    sleep 5
  done

  log "$ARCH: stopping the stack"
  compose down -v >/dev/null 2>&1
}

ARCHS=("${@:-monolith microservices}")
for arch in ${ARCHS[*]}; do run_arch "$arch"; done
log "Done. Summary: python3 $LT/summarize.py"
