#!/bin/bash
# Archivo en ASCII y con saltos de linea LF (Unix): asi cloud-init lo guarda y ejecuta sin errores.
# Gamer Events: configuracion inicial de una VM Ubuntu en Azure (campo "Custom data" o "User data").
# Azure lo ejecuta una sola vez, como root, en el primer arranque de la VM.
# Log completo: /var/log/gamer-events-setup.log   (ver con: sudo cat /var/log/gamer-events-setup.log)
#
# No contiene secretos: JWT_SECRET y las contrasenas de MySQL se generan dentro de la VM.
# Para ver las contrasenas despues:  sudo cat /opt/isy2202-gamer-events-java/.env
set -euo pipefail
exec > >(tee -a /var/log/gamer-events-setup.log) 2>&1

REPO_URL="https://github.com/felsalasc/isy2202-gamer-events-java.git"
APP_DIR="/opt/isy2202-gamer-events-java"
# Contrasena de las 3 cuentas demo (ficticias). Minimo 12 caracteres y sin el caracter "$".
DEMO_PASSWORD="EvaluacionDuoc2026!"

echo "== $(date) inicio de la configuracion"

# 1. Memoria de intercambio (swap) de 2 GB si la VM no tiene: compilar la imagen con Maven y correr
#    MySQL y la JVM a la vez puede agotar la RAM de una VM pequena.
if [ -z "$(swapon --show)" ]; then
    fallocate -l 2G /swapfile
    chmod 600 /swapfile
    mkswap /swapfile
    swapon /swapfile
    echo '/swapfile none swap sw 0 0' >> /etc/fstab
    echo "== swap de 2 GB creado"
fi

# 2. Docker, Docker Compose, Git, curl y openssl.
export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y docker.io docker-compose-v2 git curl openssl
systemctl enable --now docker

# 3. Codigo: clonar la primera vez; si ya existe, traer los cambios.
if [ -d "$APP_DIR/.git" ]; then
    git -C "$APP_DIR" pull --ff-only
else
    git clone "$REPO_URL" "$APP_DIR"
fi
cd "$APP_DIR"

# 4. Archivo .env con secretos generados en la VM. Solo se crea si no existe: MySQL guarda las
#    contrasenas del primer arranque y cambiarlas despues provoca "Access denied".
if [ ! -f .env ]; then
    umask 077   # .env legible solo por root
    cat > .env <<EOF
GAMER_DEMO_PASSWORD=${DEMO_PASSWORD}
JWT_SECRET=$(openssl rand -base64 48 | tr -d '\n')
DB_NAME=gamer_events
DB_USERNAME=gamer_events
DB_PASSWORD=$(openssl rand -base64 24 | tr -d '\n')
MYSQL_ROOT_PASSWORD=$(openssl rand -base64 24 | tr -d '\n')
EOF
    echo "== .env creado con secretos nuevos"
else
    echo "== .env existente: se conservan sus valores"
fi

# 5. Construir y levantar la aplicacion y MySQL.
docker compose up -d --build

# 6. Esperar a que la aplicacion responda (la primera compilacion tarda varios minutos).
for _ in $(seq 1 100); do
    if curl -fs -o /dev/null http://localhost/api/home; then
        echo "== $(date) Gamer Events listo en http://IP-PUBLICA-DE-LA-VM/recetas"
        exit 0
    fi
    sleep 6
done

echo "== La aplicacion no respondio a tiempo. Revisa: cd $APP_DIR && sudo docker compose logs"
exit 1
