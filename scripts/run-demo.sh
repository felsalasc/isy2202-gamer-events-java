#!/usr/bin/env bash
set -e

PORT="${1:-18080}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

mkdir -p .tools
if [ -z "$GAMER_DEMO_PASSWORD" ]; then
    if [ ! -f .tools/demo-password.txt ]; then
        head -c 24 /dev/urandom | base64 | tr -d '\n' > .tools/demo-password.txt
    fi
    export GAMER_DEMO_PASSWORD="$(cat .tools/demo-password.txt)"
    echo "Contraseña de demostración en .tools/demo-password.txt: $GAMER_DEMO_PASSWORD"
fi

echo "Usuarios: gamer1@example.test, gamer2@example.test, gamer3@example.test"

JAVA_BIN="${JAVA_HOME:+$JAVA_HOME/bin/java}"
JAVA_BIN="${JAVA_BIN:-/Library/Java/JavaVirtualMachines/jdk-27.jdk/Contents/Home/bin/java}"

"$JAVA_BIN" -jar target/gamer-events-0.0.1-SNAPSHOT.jar --spring.profiles.active=demo --server.port="$PORT"
