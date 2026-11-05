# Despliegue y entrega académica

Guía para publicar Gamer Events en una máquina virtual de Azure con Docker
Compose. Escrita después del despliegue real del 2 de noviembre de 2026, en la
VM `event-gamer-sumativa2`.

## Arquitectura en la VM

```text
Internet ──► puerto 80 de la VM ──► contenedor gamer-events (Spring Boot, puerto 8080)
                                          │  red interna de Docker
                                          ▼
                                    contenedor mysql (MySQL 8.4, sin puerto publicado)
                                          │
                                          ▼
                                    volumen mysql-data (disco de la VM)
```

- Solo el puerto **80** queda expuesto. MySQL es accesible únicamente desde la
  aplicación, a través de la red interna de Docker.
- Los datos viven en el volumen `mysql-data`: sobreviven a reinicios de los
  contenedores, de Docker y de la VM.
- Los secretos (`JWT_SECRET` y las contraseñas de MySQL) se generan dentro de la
  VM y quedan en `/opt/isy2202-gamer-events-java/.env`, legible solo por root.

## 1. Crear la máquina virtual

En el portal de Azure, **Virtual machines → Create**:

| Opción | Valor recomendado |
| --- | --- |
| Imagen | Ubuntu Server LTS (22.04 o 24.04) |
| Tamaño | B2s (2 vCPU, 4 GB). Una B1s (1 GB) funciona gracias al swap del script, pero compila mucho más lento |
| Puertos de entrada | **HTTP (80)** desde Internet. **SSH (22)** solo desde tu IP |
| Puerto 3306 (MySQL) | **No abrir** |

### Custom data

En **Advanced → Custom data**, pega el contenido de
[`scripts/azure-user-data.sh`](../scripts/azure-user-data.sh). Cópialo desde
GitHub en vista **Raw** o desde el archivo abierto en VS Code. Nunca desde un
editor de texto enriquecido (RTF, Word o TextEdit en modo RTF).

El script se ejecuta una sola vez, como root, en el primer arranque:

1. Crea 2 GB de swap si la VM no tiene.
2. Instala Docker, Docker Compose, Git, curl y openssl.
3. Clona el repositorio en `/opt/isy2202-gamer-events-java` (rama `master`).
4. Crea `.env` con secretos aleatorios, solo si no existe.
5. Ejecuta `docker compose up -d --build`.
6. Espera a que la aplicación responda y registra el resultado.

El script es **100 % ASCII y con saltos de línea LF** (Unix). Ver
[Problema conocido](#problema-conocido-el-script-no-se-ejecuta).

## 2. Esperar y verificar

La primera vez tarda entre **10 y 20 minutos**: instala Docker, descarga MySQL y
compila la aplicación dentro de la VM. Que el portal muestre la VM **En
ejecución** solo significa que está encendida, no que la aplicación esté lista.

Desde el navegador, con **http** y no https:

```text
http://IP-PUBLICA/recetas
```

Desde una terminal:

```bash
IP=IP-PUBLICA
curl -s http://$IP/api/home | head -c 200; echo                    # API pública: JSON con eventos
curl -s -o /dev/null -w "%{http_code}\n" http://$IP/api/events/1    # API privada sin token: 401
```

Cuentas de prueba: `gamer1@example.test`, `gamer2@example.test`,
`gamer3@example.test` (USER) y `admin@example.test` (ADMIN). MySQL las crea con
`db/02-data.sql` la primera vez que se crea el volumen `mysql-data`. Las
contraseñas se entregan en el informe; el repositorio solo tiene sus hashes.

En una VM que ya tenía la versión anterior, el volumen existe y los scripts no
se ejecutan. Hay que recrearlo una vez (se borran los datos):

```bash
cd /opt/isy2202-gamer-events-java
sudo git pull --ff-only
sudo docker compose down -v
sudo docker compose up -d --build
```

## 3. Diagnóstico

Por SSH en la VM:

```bash
sudo cloud-init status --long                     # ¿se ejecutó el custom data?
sudo tail -30 /var/log/gamer-events-setup.log     # pasos del script y resultado
cd /opt/isy2202-gamer-events-java
sudo docker compose ps                            # mysql "healthy", gamer-events "Up"
sudo docker compose logs --tail=50 gamer-events   # log de la aplicación
free -h                                           # memoria disponible
curl -s http://localhost/api/home | head -c 200   # ¿responde dentro de la VM?
```

| Síntoma | Causa probable |
| --- | --- |
| No existe `/var/log/gamer-events-setup.log` | El custom data no se ejecutó. Ver [Problema conocido](#problema-conocido-el-script-no-se-ejecuta) |
| Responde dentro de la VM, pero no desde afuera | Falta la regla de entrada del puerto 80 en **Networking** |
| El navegador no conecta y `curl http://...` sí | El navegador cambió a `https://`. Escribe `http://` explícitamente |
| `gamer-events` se reinicia una y otra vez | Revisa `docker compose logs gamer-events`: suele faltar memoria o haber un error en `.env` |

## Problema conocido: el script no se ejecuta

**Síntoma** (despliegue del 2 de noviembre de 2026): la VM aparece **En
ejecución**, pero la IP no responde y no existe el log del script.

```text
$ sudo cloud-init status --long
extended_status: degraded done
WARNING: Failed calling handler ShellScriptPartHandler ... part-001
```

En `/var/log/cloud-init.log`:

```text
UnicodeEncodeError: 'utf-8' codec can't encode character '\udcf3' in position 39: surrogates not allowed
```

**Causa:** la versión de entonces del script tenía tildes en los comentarios.
El texto llegó a Azure en codificación Latin-1, y no en UTF-8. El byte `0xF3`
(la "ó" de "configuración", en la posición 39) no es UTF-8 válido. cloud-init
no pudo guardar el script en el disco y nunca lo ejecutó.

**Corrección:** el script actual no tiene ningún carácter fuera de ASCII, y
`.gitattributes` fija `eol=lf` para los `.sh`. Así no depende de la codificación
con que se copie y pegue.

**Recuperación sin recrear la VM:** cloud-init no vuelve a intentar el script.
Hay que ejecutarlo a mano, y es seguro repetirlo:

```bash
curl -fsSL https://raw.githubusercontent.com/felsalasc/isy2202-gamer-events-java/master/scripts/azure-user-data.sh -o /tmp/setup.sh
head -2 /tmp/setup.sh      # debe empezar con #!/bin/bash
sudo bash /tmp/setup.sh
```

## 4. Operación

**Actualizar la aplicación** después de un merge a `master`:

```bash
cd /opt/isy2202-gamer-events-java
sudo git pull
sudo docker compose up -d --build     # los datos de MySQL se conservan
```

**Reiniciar o detener:**

```bash
sudo docker compose restart gamer-events
sudo docker compose down              # detiene; los datos se conservan
sudo docker compose up -d
```

> **Advertencia:** nunca ejecutes `docker compose down -v` en la VM: elimina el
> volumen de MySQL y todos los datos.

**Respaldo de la base de datos:**

```bash
cd /opt/isy2202-gamer-events-java
sudo docker compose exec -T mysql sh -c \
  'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysqldump -u root --single-transaction "$MYSQL_DATABASE"' \
  > respaldo-$(date +%F).sql
tail -1 respaldo-*.sql      # debe terminar en "-- Dump completed on ..."
```

- `MYSQL_PWD` evita escribir la contraseña en el comando. Las variables
  `$MYSQL_ROOT_PASSWORD` y `$MYSQL_DATABASE` se leen dentro del contenedor.
- `--single-transaction` copia un estado consistente sin detener la aplicación.

**Restaurar un respaldo** (reemplaza las tablas actuales por las del archivo):

```bash
cd /opt/isy2202-gamer-events-java
sudo docker compose exec -T mysql sh -c \
  'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -u root "$MYSQL_DATABASE"' < respaldo-AAAA-MM-DD.sql
```

Ambos comandos se probaron el 2 de noviembre de 2026 contra el stack local.
Se restauró en una base temporal y coincidieron los conteos de las 3 tablas, el
hash BCrypt y los bytes UTF-8 de los textos con tilde.

El volumen sobrevive a reinicios, pero no a la eliminación de la VM o de su
disco. Guarda los respaldos fuera de la VM, por ejemplo con
`scp usuario@IP:/opt/isy2202-gamer-events-java/respaldo-*.sql .`.

**Consultar los datos** con el cliente de MySQL del contenedor:

```bash
sudo docker compose exec mysql sh -c \
  'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -u root --default-character-set=utf8mb4 "$MYSQL_DATABASE"'
```

Sin `--default-character-set=utf8mb4`, ese cliente usa `latin1` y muestra
"Valpara�so". Es solo un problema de visualización: los datos guardados son
correctos.

**Contraseñas:** MySQL aplica `DB_PASSWORD` y `MYSQL_ROOT_PASSWORD` solo al
crear el volumen. No edites esos valores en `.env` después del primer arranque:
la aplicación fallaría con `Access denied`.

## 5. Seguridad del despliegue

- SSH restringido a tu IP. Solo el puerto 80 queda abierto a Internet.
- MySQL no publica puertos. La aplicación usa un usuario limitado a su base de
  datos, no `root`.
- `.env` tiene permisos `600` y no se comparte. Para evaluar solo hacen falta
  las cuentas de prueba del informe.
- Los contenedores se reinician solos (`restart: unless-stopped`), también
  después de un reinicio de la VM.
- La aplicación se sirve por HTTP, sin cifrado, porque así lo pide la actividad
  (`http://[IP]/recetas`). Con usuarios reales haría falta HTTPS: un dominio, un
  proxy inverso y un certificado.

## Entrega del código fuente

```bash
./scripts/package-source.sh      # macOS y Linux
./scripts/package-source.ps1     # Windows
```

Genera `dist/gamer-events-etapa2-fuente.zip` con el código, las pruebas, el
wrapper, la configuración de Docker, los scripts y la documentación. Incluye
`.env.example`, pero nunca `.env`, bases de datos, metadatos de Git ni
artefactos de compilación.

## Evidencia de evaluación (Etapa 2)

| Requisito de la pauta | Evidencia |
| --- | --- |
| Spring Boot, Web, Security, Data JPA y MySQL Driver | `pom.xml`; MySQL 8.4 en `docker-compose.yml` |
| Protección de URLs: solo las APIs indicadas son públicas | `SecurityConfig` (cadenas web y `/api/**`); `ApiSecurityTests`, `EventApiPublicTests` y `EventApiDetailTests` |
| API de login con 3 usuarios que retorna un JWT | `POST /api/auth/login`; `ApiLoginTests` y `SeedDataMySqlTests` |
| Acceso a las APIs privadas con el token | `GET /api/events/{id}`; `EventApiDetailTests` (login → token → detalle) |
| Datos desde la base de datos | MySQL en Docker; `MySqlIntegrationTests` (Testcontainers) |
| [Pública] Inicio | `/`, `/recetas` y `GET /api/home`; `HomeIntegrationTests` y `EventApiPublicTests` |
| [Pública] Búsqueda con 6 filtros | `/eventos` y `GET /api/events`; `EventSearchTests` |
| [Privada] Detalle completo | `/eventos/{id}` y `GET /api/events/{id}`; `EventDetailTests` y `EventApiDetailTests` |

Las rutas públicas y privadas y el uso del token están en el
[README](../README.md#rutas-públicas-y-privadas).
