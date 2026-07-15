#!/bin/sh
set -eu

NACOS_ADDR="${NACOS_ADDR:-nacos:8848}"
NACOS_USER="${NACOS_USER:-nacos}"
NACOS_PASS="${NACOS_PASS:-nacos}"
NACOS_IDENTITY_KEY="${NACOS_IDENTITY_KEY:-serverIdentity}"
NACOS_IDENTITY_VALUE="${NACOS_IDENTITY_VALUE:-security}"
CONFIG_FILE="${CONFIG_FILE:-/config/campus-shared.yaml}"
DATA_ID="${DATA_ID:-campus-shared.yaml}"
GROUP="${GROUP:-DEFAULT_GROUP}"
MAX_RETRY="${MAX_RETRY:-30}"

wait_nacos() {
  echo "[nacos-config] waiting for nacos at ${NACOS_ADDR}..."
  retry=0
  while [ "$retry" -lt "$MAX_RETRY" ]; do
    if curl -sf "http://${NACOS_ADDR}/nacos/v1/ns/service/list?pageNo=1&pageSize=1" >/dev/null 2>&1; then
      echo "[nacos-config] nacos is ready"
      return 0
    fi
    retry=$((retry + 1))
    sleep 2
  done
  echo "[nacos-config] nacos not ready after ${MAX_RETRY} attempts" >&2
  return 1
}

extract_access_token() {
  response="$1"
  token=$(printf '%s' "$response" | tr ',' '\n' | sed -n 's/.*"accessToken"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -n 1)
  if [ -n "$token" ]; then
    printf '%s' "$token"
    return 0
  fi
  return 1
}

login_access_token() {
  echo "[nacos-config] trying login for admin API..."
  response=$(curl -fsS -X POST "http://${NACOS_ADDR}/nacos/v3/auth/user/login" \
    -d "username=${NACOS_USER}" \
    -d "password=${NACOS_PASS}" 2>/dev/null) || return 1
  extract_access_token "$response"
}

publish_v3() {
  token="$1"
  echo "[nacos-config] publishing ${DATA_ID} via v3 admin API..."
  # Nacos 3.x v3 admin API expects content as form string, not multipart file
  if [ -n "$token" ]; then
    curl -fsS -X POST "http://${NACOS_ADDR}/nacos/v3/admin/cs/config" \
      -H "${NACOS_IDENTITY_KEY}: ${NACOS_IDENTITY_VALUE}" \
      -H "accessToken: ${token}" \
      --data-urlencode "dataId=${DATA_ID}" \
      --data-urlencode "groupName=${GROUP}" \
      --data-urlencode "contentType=yaml" \
      --data-urlencode "content@${CONFIG_FILE}"
  else
    curl -fsS -X POST "http://${NACOS_ADDR}/nacos/v3/admin/cs/config" \
      -H "${NACOS_IDENTITY_KEY}: ${NACOS_IDENTITY_VALUE}" \
      --data-urlencode "dataId=${DATA_ID}" \
      --data-urlencode "groupName=${GROUP}" \
      --data-urlencode "contentType=yaml" \
      --data-urlencode "content@${CONFIG_FILE}"
  fi
}

publish_v1() {
  token="$1"
  echo "[nacos-config] publishing ${DATA_ID} via v1 API..."
  if [ -n "$token" ]; then
    curl -fsS -X POST "http://${NACOS_ADDR}/nacos/v1/cs/configs" \
      -H "${NACOS_IDENTITY_KEY}: ${NACOS_IDENTITY_VALUE}" \
      -H "accessToken: ${token}" \
      -F "dataId=${DATA_ID}" \
      -F "group=${GROUP}" \
      -F "type=yaml" \
      -F "content@${CONFIG_FILE};type=text/plain"
  else
    curl -fsS -X POST "http://${NACOS_ADDR}/nacos/v1/cs/configs" \
      -H "${NACOS_IDENTITY_KEY}: ${NACOS_IDENTITY_VALUE}" \
      -F "dataId=${DATA_ID}" \
      -F "group=${GROUP}" \
      -F "type=yaml" \
      -F "content@${CONFIG_FILE};type=text/plain"
  fi
}

verify_config() {
  curl -sf "http://${NACOS_ADDR}/nacos/v3/client/cs/config?dataId=${DATA_ID}&groupName=${GROUP}" >/dev/null 2>&1 \
    || curl -sf "http://${NACOS_ADDR}/nacos/v1/cs/configs?dataId=${DATA_ID}&group=${GROUP}" >/dev/null 2>&1
}

wait_nacos

token=""
if ! token=$(login_access_token); then
  echo "[nacos-config] login skipped (console/admin auth disabled for demo)"
  token=""
fi

if ! publish_v3 "$token"; then
  echo "[nacos-config] v3 publish failed, retrying v1 API..." >&2
  publish_v1 "$token"
fi

if verify_config; then
  echo "[nacos-config] verified: ${DATA_ID} exists in ${GROUP}"
else
  echo "[nacos-config] publish finished but verification failed" >&2
  exit 1
fi

echo "[nacos-config] done"
