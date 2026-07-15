#!/bin/bash
set -euo pipefail

ROOT_PWD="${MYSQL_ROOT_PASSWORD:-relife_root}"
MASTER_HOST="${MYSQL_MASTER_HOST:-mysql}"
SLAVE_HOST="${MYSQL_SLAVE_HOST:-mysql-slave}"

echo "[replica-setup] waiting for master ${MASTER_HOST}..."
until mysqladmin ping -h "${MASTER_HOST}" -uroot -p"${ROOT_PWD}" --silent 2>/dev/null; do
  sleep 2
done

echo "[replica-setup] waiting for slave ${SLAVE_HOST}..."
until mysqladmin ping -h "${SLAVE_HOST}" -uroot -p"${ROOT_PWD}" --silent 2>/dev/null; do
  sleep 2
done

IO_RUNNING=$(mysql -h "${SLAVE_HOST}" -uroot -p"${ROOT_PWD}" -N -e \
  "SHOW REPLICA STATUS\G" 2>/dev/null | awk -F': ' '/Replica_IO_Running/{print $2}' | tr -d ' \r' || true)
SQL_RUNNING=$(mysql -h "${SLAVE_HOST}" -uroot -p"${ROOT_PWD}" -N -e \
  "SHOW REPLICA STATUS\G" 2>/dev/null | awk -F': ' '/Replica_SQL_Running/{print $2}' | tr -d ' \r' || true)

if [[ "${IO_RUNNING}" == "Yes" && "${SQL_RUNNING}" == "Yes" ]]; then
  echo "[replica-setup] replication already running"
  exit 0
fi

echo "[replica-setup] configuring replication ${MASTER_HOST} -> ${SLAVE_HOST}"
mysql -h "${SLAVE_HOST}" -uroot -p"${ROOT_PWD}" <<EOF
STOP REPLICA;
RESET REPLICA ALL;
CHANGE REPLICATION SOURCE TO
  SOURCE_HOST='${MASTER_HOST}',
  SOURCE_USER='repl',
  SOURCE_PASSWORD='repl123',
  SOURCE_AUTO_POSITION=1;
START REPLICA;
EOF

mysql -h "${SLAVE_HOST}" -uroot -p"${ROOT_PWD}" -e "SHOW REPLICA STATUS\G"
echo "[replica-setup] done"
