#!/usr/bin/env bash
# End-to-end smoke: Kafka KRaft + Postgres + services via docker compose,
# place an order through the secured gateway, wait for query-side projection.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8080}"
GATEWAY_API_KEY="${GATEWAY_API_KEY:-smoke-integration-key}"
COMPOSE=(docker compose --profile full)
MAX_WAIT_SECS="${MAX_WAIT_SECS:-240}"
POLL_SECS="${POLL_SECS:-3}"

cleanup() {
  "${COMPOSE[@]}" down -v --remove-orphans >/dev/null 2>&1 || true
}
trap cleanup EXIT

# Work around incomplete Docker FORWARD rules on some hosts (bridge netfilter).
fix_bridge_nf() {
  local path="$1"
  if [[ -e "$path" ]]; then
    if [[ -w "$path" ]]; then
      echo 0 >"$path" || true
    elif command -v sudo >/dev/null 2>&1; then
      echo 0 | sudo tee "$path" >/dev/null 2>&1 || true
    fi
  fi
}
fix_bridge_nf /proc/sys/net/bridge/bridge-nf-call-iptables
fix_bridge_nf /proc/sys/net/bridge/bridge-nf-call-ip6tables

echo "==> Starting full stack (kafka + postgres + apps)"
export GATEWAY_API_KEY
"${COMPOSE[@]}" up --build -d

echo "==> Waiting for gateway health at ${GATEWAY_URL}/actuator/health"
deadline=$((SECONDS + MAX_WAIT_SECS))
until curl -sf "${GATEWAY_URL}/actuator/health" >/dev/null 2>&1; do
  if (( SECONDS >= deadline )); then
    echo "Timed out waiting for gateway health" >&2
    "${COMPOSE[@]}" ps >&2 || true
    "${COMPOSE[@]}" logs --tail=80 >&2 || true
    exit 1
  fi
  sleep "$POLL_SECS"
done

echo "==> Reject without API key (expect 401)"
status="$(curl -s -o /dev/null -w '%{http_code}' -X POST "${GATEWAY_URL}/api/orders" \
  -H 'Content-Type: application/json' \
  -d '{"customerId":"cust-smoke","sku":"SKU-WIDGET-001","quantity":1,"unitPrice":9.99}')"
if [[ "$status" != "401" ]]; then
  echo "Expected 401 without API key, got ${status}" >&2
  exit 1
fi

echo "==> Place order with X-API-Key"
order_json="$(curl -sf -X POST "${GATEWAY_URL}/api/orders" \
  -H 'Content-Type: application/json' \
  -H "X-API-Key: ${GATEWAY_API_KEY}" \
  -d '{"customerId":"cust-smoke","sku":"SKU-WIDGET-001","quantity":1,"unitPrice":9.99}')"
echo "Order response: ${order_json}"

order_id="$(printf '%s' "$order_json" | sed -n 's/.*"orderId"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
if [[ -z "$order_id" ]]; then
  echo "Could not parse orderId from response" >&2
  exit 1
fi
echo "orderId=${order_id}"

echo "==> Poll query side until projection appears"
deadline=$((SECONDS + MAX_WAIT_SECS))
view_json=""
while true; do
  code="$(curl -s -o /tmp/ems-view.json -w '%{http_code}' \
    -H "X-API-Key: ${GATEWAY_API_KEY}" \
    "${GATEWAY_URL}/api/views/orders/${order_id}" || true)"
  if [[ "$code" == "200" ]]; then
    view_json="$(cat /tmp/ems-view.json)"
    break
  fi
  if (( SECONDS >= deadline )); then
    echo "Timed out waiting for query projection (last HTTP ${code})" >&2
    "${COMPOSE[@]}" logs --tail=100 order-service inventory-service query-service >&2 || true
    exit 1
  fi
  sleep "$POLL_SECS"
done

echo "View response: ${view_json}"
if ! printf '%s' "$view_json" | grep -q "$order_id"; then
  echo "Projection payload missing orderId" >&2
  exit 1
fi

# Prefer fulfilment status when inventory has reacted; accept PLACED if lag is still short.
if printf '%s' "$view_json" | grep -Eq '"fulfilmentStatus"[[:space:]]*:[[:space:]]*"(RESERVED|REJECTED|PLACED)"'; then
  echo "==> Smoke OK — order projected on query side"
  exit 0
fi

echo "Unexpected fulfilmentStatus in view" >&2
exit 1
