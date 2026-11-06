#!/usr/bin/env bash
# Levanta Gamer Events (aplicación + MySQL) con Docker Compose y espera a que responda.
# Requisitos: Docker en ejecución y un archivo .env completo (copia de .env.example).
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [ ! -f .env ]; then
    echo "Falta el archivo .env. Cópialo desde .env.example y completa las contraseñas:" >&2
    echo "  cp .env.example .env" >&2
    exit 1
fi

if ! docker info >/dev/null 2>&1; then
    echo "Docker no está en ejecución. Abre Docker Desktop (o inicia el servicio docker) e inténtalo otra vez." >&2
    exit 1
fi

docker compose up --build -d

echo "Esperando a que la aplicación responda..."
for _ in $(seq 1 60); do
    if curl -fs -o /dev/null http://localhost/api/home; then
        echo
        echo "Gamer Events está listo en http://localhost"
        echo "Usuarios y contraseñas de prueba: ver el informe de entrega"
        echo "Ver el log: docker compose logs -f gamer-events   |   Detener: docker compose down"
        exit 0
    fi
    sleep 3
done

echo "La aplicación no respondió a tiempo. Revisa el log con: docker compose logs gamer-events" >&2
exit 1
