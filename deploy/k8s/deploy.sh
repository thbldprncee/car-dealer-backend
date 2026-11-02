#!/usr/bin/env bash
# Deploys the whole system to a local Kubernetes cluster (kind).
#
#   deploy/k8s/deploy.sh                 build images from the working tree and deploy them
#   IMAGES=pull deploy/k8s/deploy.sh     deploy the images published to GHCR instead
#   deploy/k8s/deploy.sh test            run the HTTP scenario against the cluster
#   deploy/k8s/deploy.sh rollout-check   restart both services under load, count failed requests
#   deploy/k8s/deploy.sh down            delete the cluster
#
# Needs Docker, kind and kubectl. Services are reachable on the same host ports as with
# docker compose's alternative ports: order-service :18080, storage-service :18081, Keycloak :18180.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
K8S="$ROOT/deploy/k8s"
CLUSTER=car-dealer
NS=car-dealer
SERVICES=(storage-service order-service)
KUBECTL=(kubectl --context "kind-$CLUSTER")

log() { printf '\n[%s] %s\n' "$(date +%H:%M:%S)" "$*"; }
image() { echo "ghcr.io/thbldprncee/car-dealer-$1:latest"; }
k() { "${KUBECTL[@]}" -n "$NS" "$@"; }

create_cluster() {
  if kind get clusters 2>/dev/null | grep -qx "$CLUSTER"; then
    log "Cluster $CLUSTER already exists"
  else
    log "Creating cluster $CLUSTER"
    kind create cluster --config "$K8S/kind-cluster.yaml" --wait 120s
  fi
}

load_images() {
  if [[ "${IMAGES:-build}" == pull ]]; then
    log "Pulling the published images"
    for s in "${SERVICES[@]}"; do docker pull -q "$(image "$s")"; done
  else
    log "Building images from the working tree"
    docker compose -f "$ROOT/docker-compose.yml" build "${SERVICES[@]}"
  fi
  # The node cannot see the host's images; kind copies them into its containerd. Public images
  # (PostgreSQL, Kafka, Keycloak, Liquibase) are pulled by the node itself.
  for s in "${SERVICES[@]}"; do kind load docker-image "$(image "$s")" --name "$CLUSTER"; done
}

# Three steps, like a CD pipeline: infrastructure, then migrations, then a rollout of services
# that expect the new schema. Migrations run once per deploy instead of inside every replica.
deploy() {
  local existed=false
  k get deployment order-service > /dev/null 2>&1 && existed=true

  log "1/3 Infrastructure: PostgreSQL, Kafka, Keycloak"
  "${KUBECTL[@]}" apply -f "$K8S/infra/namespace.yaml"
  k create configmap keycloak-realm \
    --from-file=car-dealer-realm.json="$ROOT/docker/keycloak/car-dealer-realm.json" \
    --dry-run=client -o yaml | k apply -f -
  "${KUBECTL[@]}" apply -k "$K8S/infra"
  for sts in order-db storage-db kafka; do k rollout status "statefulset/$sts" --timeout=300s; done

  log "2/3 Database migrations"
  # A Job's pod template is immutable and a finished Job never runs again, so every deploy
  # replaces the Jobs.
  k delete job order-db-migrate storage-db-migrate --ignore-not-found
  "${KUBECTL[@]}" apply -k "$K8S/migrations"
  if ! k wait --for=condition=complete job/order-db-migrate job/storage-db-migrate --timeout=300s; then
    k logs job/order-db-migrate --all-containers --tail=50 || true
    k logs job/storage-db-migrate --all-containers --tail=50 || true
    echo "Migrations failed; the services were not touched." >&2
    exit 1
  fi

  log "3/3 Services"
  "${KUBECTL[@]}" apply -k "$K8S/services"
  # The image tag stays "latest", so a rebuilt image needs an explicit rolling restart.
  if $existed; then
    for s in "${SERVICES[@]}"; do k rollout restart "deployment/$s"; done
  fi
  k rollout status deployment/keycloak --timeout=600s
  for s in "${SERVICES[@]}"; do k rollout status "deployment/$s" --timeout=600s; done
  k get pods -o wide
  cat <<EOF

Deployed. From the host:
  order-service    http://localhost:18080/swagger-ui.html
  storage-service  http://localhost:18081/swagger-ui.html
  Keycloak         http://localhost:18180 (admin / admin)
EOF
}

# Clients run in containers on kind's network and reach the services through the node's
# NodePorts; Keycloak's issuer stays http://localhost:18180 because KC_HOSTNAME fixes it.
run_scenario() {
  log "HTTP scenario against the cluster"
  docker run --rm --network kind -v "$ROOT/http":/workdir jetbrains/intellij-http-client \
    -v http-client.env.json -e kind car-dealer.http
}

rollout_check() {
  log "Rolling restart of both services under load"
  docker run --rm --network kind -v "$K8S":/scripts:ro -e DURATION=150s \
    grafana/k6:0.54.0 run --quiet --no-color /scripts/rollout-check.js &
  local k6_pid=$!
  sleep 20
  for s in "${SERVICES[@]}"; do
    k rollout restart "deployment/$s"
    k rollout status "deployment/$s" --timeout=300s
  done
  wait "$k6_pid"
}

case "${1:-deploy}" in
  deploy) create_cluster; load_images; deploy ;;
  test) run_scenario ;;
  rollout-check) rollout_check ;;
  down) kind delete cluster --name "$CLUSTER" ;;
  *) echo "usage: $0 [deploy|test|rollout-check|down]" >&2; exit 2 ;;
esac
