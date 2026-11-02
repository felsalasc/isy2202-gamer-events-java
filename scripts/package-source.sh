#!/usr/bin/env bash
set -euo pipefail

# Obtener directorio raíz del proyecto
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

DIST_DIR="$ROOT_DIR/dist"
ZIP_FILE="$DIST_DIR/gamer-events-etapa2-fuente.zip"

mkdir -p "$DIST_DIR"
rm -f "$ZIP_FILE"

echo "Empaquetando entrega limpia en: $ZIP_FILE"

# Archivos y carpetas específicos a empaquetar
ITEMS=(
    "pom.xml"
    "mvnw"
    "mvnw.cmd"
    ".gitignore"
    ".gitattributes"
    "README.md"
    "Dockerfile"
    "docker-compose.yml"
    ".dockerignore"
    ".env.example"
    "src"
    "docs"
    ".mvn"
    "scripts"
)

# Empaquetar excluyendo archivos ocultos del sistema como .DS_Store
zip -r -q "$ZIP_FILE" "${ITEMS[@]}" -x "*.DS_Store" "*__MACOSX*"

echo "Listo. Archivo generado exitosamente:"
ls -lh "$ZIP_FILE"
