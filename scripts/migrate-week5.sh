#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
for file in db/migrations/2026-11-23-owner.sql db/03-interactions.sql db/04-community.sql db/05-demo-owners.sql; do
    docker compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysql --default-character-set=utf8mb4 -u "$MYSQL_USER" "$MYSQL_DATABASE"' < "$file"
done
echo 'Esquema de semana 5 actualizado; se conservaron los datos existentes.'
