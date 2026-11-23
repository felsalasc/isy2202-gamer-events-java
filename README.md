# Gamer Events

Aplicación web y API REST para consultar eventos de la comunidad gamer, crear una
cuenta y personalizar un perfil. Desarrollada con Java y Spring Boot para la
asignatura **Seguridad y Calidad en el Desarrollo de Software (ISY2202)** de
**Duoc UC**.

**Repositorio:** [isy2202-gamer-events-java](https://github.com/felsalasc/isy2202-gamer-events-java) · **Actividad:** Semana 5, etapa de desarrollo

## Funcionalidades

La ampliación de semana 5 incorpora publicación de eventos propios, galería privada de fotos y videos,
comentarios, valoraciones y eventos compartidos. El inicio de entrega está en `/eventos` y la
búsqueda en `/eventos/buscar`. Consulta [desarrollo de semana 5](docs/semana-5-desarrollo.md)
para las rutas nuevas y la migración de bases existentes antes de actualizar la aplicación.

- **[Pública] Inicio:** eventos más recientes y populares, banners comerciales y acceso al inicio de sesión.
- **[Pública] Búsqueda de eventos:** filtros combinables por nombre, videojuego, tipo o categoría, país, ciudad, fecha y modalidad (presencial u online). Muestra un resumen de cada evento.
- **[Privada] Detalle de eventos:** descripción, videojuego, fecha y hora, ubicación o enlace de acceso, organizador, requisitos de participación, información de inscripción e imagen.
- **[Privada] Publicación:** crear eventos presenciales u online con la cuenta autenticada como propietaria.
- **[Privada] Fotos y videos:** el propietario adjunta PNG/JPEG de hasta 5 MB y MP4 con video H.264 de hasta 20 MB, con un máximo de 20 archivos por evento. Los usuarios autenticados pueden consultar la galería.
- **[Privada] Comentarios y valoraciones:** comentarios de hasta 1000 caracteres y una puntuación actualizable de 1 a 5 por usuario y evento.
- **[Privada] Comunidad y compartir:** el propietario comparte su evento dentro del sitio o abre un enlace para compartirlo en WhatsApp, Facebook o X. El destinatario debe iniciar sesión para acceder al detalle.
- **API REST con JWT:** login que entrega un token, APIs públicas de inicio y búsqueda, y API privada de detalle con `Authorization: Bearer`.
- **Registro, inicio de sesión y perfil:** contraseña almacenada con BCrypt, perfil privado editable y cierre de sesión protegido con CSRF.
- **Interfaz adaptable:** HTML5, CSS3 y plantillas Thymeleaf.

Los datos iniciales (usuarios, eventos y marcas) son ficticios, están
identificados como tales en la interfaz y se cargan con los scripts SQL de `db/`.

## Tecnologías

| Tecnología | Versión o uso |
| --- | --- |
| Java | 17 |
| Spring Boot | 4.0.9 (Spring Web MVC, Security, Data JPA, Validation) |
| MySQL | 8.4, mediante `mysql-connector-j` |
| jjwt | 0.12.5, tokens JWT firmados con HS256 |
| Thymeleaf | Vistas web |
| Docker y Docker Compose | Ejecución de la aplicación y MySQL |
| Maven | 3.9.14 mediante Maven Wrapper |
| JUnit 5, MockMvc, H2 y Testcontainers | Pruebas automatizadas |

## Requisitos

- **Docker Desktop** (Windows y macOS) o Docker Engine con Docker Compose (Linux).
- Git para clonar el repositorio, o el código descargado como ZIP.
- Solo para ejecutar las pruebas: JDK 17 o superior y la variable `JAVA_HOME`.
  No es necesario instalar Maven: el proyecto incluye Maven Wrapper.

Ejecuta todos los comandos desde la raíz del proyecto.

## Ejecución con Docker

La aplicación y MySQL corren en contenedores. MySQL solo es accesible desde la
red interna de Docker; no se publica ningún puerto de la base de datos.

### 1. Crear el archivo `.env` en una instalación nueva

El archivo `.env` contiene las contraseñas y el secreto JWT. Está excluido de Git.

En Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Los valores `JWT_SECRET`, `DB_PASSWORD` y `MYSQL_ROOT_PASSWORD` los generas para
tu instalación. Ejecuta este bloque en PowerShell y copia cada línea resultante
en el campo correspondiente de `.env`:

```powershell
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()

foreach ($nombre in 'JWT_SECRET','DB_PASSWORD','MYSQL_ROOT_PASSWORD') {
    $bytes = New-Object byte[] 48
    $rng.GetBytes($bytes)
    "$nombre=$([Convert]::ToBase64String($bytes))"
}

$rng.Dispose()
```

Mantén también este valor para probar en tu computador:

```dotenv
PUBLIC_BASE_URL=http://localhost
```

En macOS y Linux, copia la plantilla con `cp .env.example .env` y genera un valor
distinto para cada secreto con OpenSSL:

```bash
openssl rand -base64 48   # JWT_SECRET
openssl rand -base64 24   # DB_PASSWORD y MYSQL_ROOT_PASSWORD (uno distinto para cada una)
```

Define las contraseñas de MySQL **antes del primer arranque**: MySQL solo las
aplica al crear su volumen de datos. Si ya tienes un `.env` y una base creada,
conserva sus valores y sigue [Actualizar una instalación existente](#actualizar-una-instalación-existente).
Las contraseñas de MySQL son distintas de la contraseña con la que te registras
en el sitio.

### 2. Levantar la aplicación

**macOS y Linux:**

```bash
./scripts/run.sh
```

**Windows PowerShell:**

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass   # solo si PowerShell bloquea el script
./scripts/run.ps1
```

Los scripts comprueban el `.env` y Docker, ejecutan `docker compose up --build -d`
y esperan a que la aplicación responda. También puedes usar Docker Compose
directamente:

```bash
docker compose up --build -d
```

La primera vez descarga las imágenes y las dependencias, y tarda unos minutos.

### 3. Abrir la aplicación

Abre [http://localhost/eventos](http://localhost/eventos). También están
disponibles `/`, `/home` y `/recetas` como rutas alternativas de inicio.

Puedes crear una cuenta desde [Registro](http://localhost/registro) para probar
las funciones privadas. No necesitas conocer las contraseñas de las cuentas
precargadas para registrar tu propia cuenta y publicar un evento.

| Cuenta de prueba | Rol |
| --- | --- |
| `gamer1@example.test` | USER |
| `gamer2@example.test` | USER |
| `gamer3@example.test` | USER |
| `admin@example.test` | ADMIN |

Las contraseñas no están en el repositorio: se entregan en el informe. En
`db/02-data.sql` solo se guardan sus hashes BCrypt.

### Comandos útiles

```bash
docker compose ps                       # estado; mysql debe aparecer como "healthy"
docker compose logs -f gamer-events     # log de la aplicación (Ctrl+C para salir)
docker compose down                     # detiene y elimina los contenedores; los datos se conservan
docker compose up --build -d            # vuelve a construir tras cambiar el código
```

## Variables de entorno

| Variable | Uso | Requisito |
| --- | --- | --- |
| `JWT_SECRET` | Firma de los tokens JWT (HS256) | Al menos 32 bytes; sin él la aplicación no arranca |
| `DB_NAME` | Nombre de la base de datos | Por defecto `gamer_events` |
| `DB_USERNAME` | Usuario de MySQL de la aplicación | Por defecto `gamer_events` |
| `DB_PASSWORD` | Contraseña de ese usuario | Obligatoria |
| `MYSQL_ROOT_PASSWORD` | Contraseña del administrador de MySQL | Obligatoria; solo para administración |
| `PUBLIC_BASE_URL` | Origen usado para construir los enlaces compartidos | `http://localhost` en local; origen público real al desplegar, sin rutas adicionales |

La aplicación se conecta con un usuario que solo tiene permisos sobre su propia
base de datos, nunca como `root`. Evita el carácter `$` en los valores: Docker
Compose lo interpreta como una variable.

## Rutas públicas y privadas

| Funcionalidad | Web | API | Acceso |
| --- | --- | --- | --- |
| Inicio | `GET /eventos`, `/`, `/home`, `/recetas` | `GET /api/home` | Público |
| Búsqueda de eventos | `GET /eventos/buscar` | `GET /api/events` | Público |
| Detalle de evento | `GET /eventos/{id}` | `GET /api/events/{id}` | **Privado** |
| Publicar evento | `GET /eventos/nuevo`, `POST /eventos/nuevo` | `POST /api/events` | **Privado** |
| Comentarios | Detalle; `POST /eventos/{id}/comentarios` | `GET / POST /api/events/{id}/comments` | **Privado** |
| Valoraciones | Detalle; `POST /eventos/{id}/valoracion` | `GET /api/events/{id}/ratings`, `PUT /api/events/{id}/ratings/me` | **Privado** |
| Adjuntar multimedia | `POST /eventos/{id}/multimedia` | `POST /api/events/{id}/media` | **Solo propietario** |
| Consultar multimedia | `GET /eventos/{id}/multimedia/{mediaId}` | `GET /api/events/{id}/media`, `GET /api/events/{id}/media/{mediaId}` | **Privado** |
| Compartir evento | `POST /eventos/{id}/compartir` | `POST /api/events/{id}/shares` | **Solo propietario** |
| Comunidad | `GET /comunidad` | `GET /api/community` | **Privado** |
| Inicio de sesión | `/login` (formulario) | `POST /api/auth/login` | Público |
| Registro | `/registro` | — | Público |
| Perfil | `/perfil` | — | **Privado** |
| Cierre de sesión | `POST /logout` | — | Privado |

- **Web:** las rutas privadas requieren una sesión iniciada; sin ella, redirigen a `/login`.
- **API:** las rutas privadas requieren `Authorization: Bearer <token>`; sin token responden `401`. Una sesión web no da acceso a la API.

### Filtros de búsqueda

`/eventos/buscar` y `/api/events` aceptan los mismos parámetros. Todos son opcionales y
se combinan:

| Parámetro | Ejemplo | Coincidencia |
| --- | --- | --- |
| `name` | `?name=nexus` | Parcial, sin distinguir mayúsculas |
| `game` | `?game=Valorant` | Exacta |
| `category` | `?category=Esports` | Exacta |
| `country` | `?country=Chile` | Exacta |
| `city` | `?city=Santiago` | Exacta (solo eventos presenciales) |
| `date` | `?date=2026-12-05` | Día del evento (`AAAA-MM-DD`) |
| `modality` | `?modality=ONLINE` | `PRESENCIAL` u `ONLINE` |

## API REST y JWT

### 1. Obtener el token

```bash
curl -X POST http://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"gamer1@example.test","password":"TU_CONTRASEÑA"}'
```

```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

El token se firma con HS256, contiene el correo del usuario (`sub`), el emisor
(`gamer-events`) y el rol, y **expira a los 30 minutos**.

### 2. Usar el token en la API privada

```bash
curl http://localhost/api/events/1 -H "Authorization: Bearer EL_TOKEN"
```

Las APIs públicas no necesitan token:

```bash
curl http://localhost/api/home
curl "http://localhost/api/events?country=Chile&modality=PRESENCIAL"
```

### Postman

1. **Login:** `POST http://localhost/api/auth/login`. En **Body**, elige **raw** y
   **JSON**, y envía el correo y la contraseña. Copia el `token` de la respuesta.
2. **Detalle:** `GET http://localhost/api/events/1`. En **Authorization**, elige
   **Bearer Token** y pega el token, sin la palabra `Bearer`.

Para no copiar el token a mano, en la pestaña **Scripts → Post-response** del
login agrega `pm.environment.set("token", pm.response.json().token);` y usa
`{{token}}` en las demás solicitudes, con un environment seleccionado.

### Respuestas de error

Todos los errores de la API tienen el formato `{"error": "..."}`:

| Código | Mensaje | Cuándo |
| --- | --- | --- |
| `400` | `Solicitud inválida.` | Parámetros, cuerpo JSON o identificador inválidos |
| `401` | `Credenciales inválidas.` | Correo inexistente o contraseña incorrecta en el login |
| `401` | `Se requiere un token Bearer.` | API privada sin token |
| `401` | `Token inválido o expirado.` | Token alterado, mal formado o vencido |
| `404` | `Evento no encontrado.` | El evento no existe |

## Persistencia

| Entorno | Base de datos |
| --- | --- |
| Aplicación (Docker) | MySQL 8.4, en el volumen `mysql-data` |
| Pruebas automatizadas | H2 en memoria y, si Docker está disponible, MySQL 8.4 con Testcontainers |

- Los datos se guardan en un **volumen con nombre** de Docker: sobreviven a
  `docker compose down`, a reinicios y a la reconstrucción de los contenedores.
- MySQL ejecuta `db/01-schema.sql` (creación) y `db/02-data.sql` (inserción de
  4 cuentas, seis eventos y tres banners), seguidos de `03-interactions.sql`,
  `04-community.sql` y `05-demo-owners.sql`, la primera vez que se crea el volumen.
- Los comentarios, valoraciones, eventos compartidos y archivos multimedia se
  guardan en MySQL. Los archivos se almacenan como BLOB, dentro del mismo volumen.
- La aplicación no modifica el esquema: arranca con `ddl-auto=validate` y se
  detiene si las tablas no calzan con las entidades.
- La aplicación y MySQL usan la zona horaria `America/Santiago`.

## Actualizar una instalación existente

Si ya tienes una base de la etapa anterior, los scripts de inicialización no
se volverán a ejecutar automáticamente. Respalda la base y aplica la migración
antes de iniciar la aplicación nueva:

```powershell
docker compose stop gamer-events
docker compose up -d mysql
./scripts/migrate-week5.ps1
docker compose up -d --build
```

En Linux y macOS, sustituye el tercer comando por `bash scripts/migrate-week5.sh`.
Espera a que MySQL esté listo antes de ejecutar la migración. Los scripts pueden
repetirse y conservan los datos existentes. No ejecutes `db/01-schema.sql` sobre
esa base: recrea las tablas. Consulta el [procedimiento de migración](docs/semana-5-desarrollo.md#actualizar-una-instalación-existente-sin-perder-datos).

## Arquitectura

```text
src/main/java/cl/duoc/gamerevents/
├── config/          # Seguridad (cadenas web y API) y JWT
├── controller/      # Controladores web (Thymeleaf) y REST (/api)
├── exception/       # Excepciones y respuestas de error JSON de la API
├── model/           # Entidades JPA, formularios y DTO de la API
├── repository/      # Acceso a datos con Spring Data JPA
└── service/         # Reglas de negocio y transacciones
src/main/resources/
├── templates/       # Plantillas Thymeleaf
├── static/css/      # Estilos compartidos
└── static/images/   # Avatares e imágenes de eventos (SVG propios)
src/test/            # Pruebas unitarias y de integración
scripts/             # Ejecución con Docker y empaquetado
docs/                # Documentación académica y técnica
```

La web y la API usan los mismos servicios: los filtros y los datos son idénticos
en ambas. La API devuelve DTO (`EventSummary`, `EventDetail`, `HomeResponse`),
nunca entidades JPA, para que el listado público no exponga datos del detalle.

## Seguridad

- **Dos cadenas de Spring Security:**
  - `/api/**`: sin sesión ni CSRF, autenticación con JWT y `401` en JSON.
  - Resto de rutas: formulario de login, sesión y CSRF.
- El secreto JWT se lee de `JWT_SECRET`; la aplicación no arranca si falta o es corto.
- Contraseñas con BCrypt. El login de la API responde lo mismo ante un correo
  inexistente o una contraseña incorrecta, y tarda lo mismo en ambos casos.
- Los eventos online solo aceptan enlaces `https://`, que se abren con
  `rel="noopener noreferrer"`.
- La aplicación se conecta a MySQL con un usuario limitado a su base de datos.
  MySQL no publica ningún puerto fuera de Docker.
- Thymeleaf escapa el contenido; el perfil solo modifica la cuenta autenticada.
- La carga de archivos está limitada a fotos PNG/JPEG y videos MP4 validados, en eventos propios y con acceso privado. Open Session in View permanece desactivado.

El repositorio no incluye contraseñas reales, archivos `.env` ni bases de datos.

## Pruebas

### Comprobaciones manuales en el navegador

Con la aplicación ejecutándose, recorre estas páginas:

| Paso | Dónde | Resultado esperado |
| --- | --- | --- |
| 1 | [Inicio](http://localhost/eventos) | Se abre sin iniciar sesión. |
| 2 | [Catálogo](http://localhost/eventos/buscar) | Permite buscar y combinar filtros. |
| 3 | [Registro](http://localhost/registro) y [Login](http://localhost/login) | Permiten crear tu cuenta e iniciar sesión. |
| 4 | [Publicar evento](http://localhost/eventos/nuevo) | Crea un evento futuro y abre su detalle. Para uno online usa un enlace con `https://` en minúsculas. |
| 5 | Detalle del evento creado | Permite adjuntar una foto o un video válido y visualizarlo en la galería. |
| 6 | Detalle del evento creado | Publica comentarios y guarda una valoración de 1 a 5. Cambiar la puntuación actualiza tu valoración, sin duplicarla. |
| 7 | Detalle → Compartir en la comunidad | El evento aparece en [Comunidad](http://localhost/comunidad). |
| 8 | Detalle → Compartir en redes | Abre la red elegida con el enlace del evento; la publicación se confirma allí. |
| 9 | Mismo detalle con otra cuenta | Puede comentar y valorar; no puede adjuntar archivos ni compartir el evento como propio. |
| 10 | Detalle sin sesión, por ejemplo en una ventana privada | Solicita login y, después de iniciar sesión, regresa al evento. |

Comprueba también que se rechacen comentarios vacíos, puntuaciones fuera del
rango y archivos inválidos o demasiado grandes. La validación automatizada de
las vistas no sustituye esta revisión visual del frontend.

### Pruebas automatizadas en la terminal

Desde la raíz del proyecto, en la terminal de VS Code o PowerShell:

```powershell
./mvnw.cmd verify
```

En macOS y Linux usa `./mvnw verify`. El resultado esperado es `BUILD SUCCESS`.
Mantén Docker Desktop abierto para ejecutar también las pruebas con MySQL real.

Las pruebas unitarias y de integración con H2 en memoria no necesitan Docker.
Las pruebas de `MySqlIntegrationTests` levantan un MySQL
8.4 real con Testcontainers: verifican tildes, emojis, fechas, tamaño de las
filas y la búsqueda por nombre. Si Docker no está disponible, se omiten en vez de
fallar.

Semana 5 añade pruebas de comentarios, valoraciones, autoría, multimedia,
eventos compartidos, concurrencia y migración de esquema con MySQL real.

Los resultados de cada ejecución quedan en `target/surefire-reports/`.
La última verificación del desarrollo, registrada el 23 de noviembre de 2026,
incluyó **155 pruebas aprobadas**, sin fallos, errores ni omisiones. Cada nueva
ejecución debe comprobarse con sus propios reportes.

## Solución de problemas

| Síntoma | Causa y solución |
| --- | --- |
| La aplicación no muestra eventos o no acepta las cuentas de prueba | Revisa los logs y el origen del volumen. Las cuentas precargadas solo se crean al inicializarlo; también puedes registrar tu propia cuenta. Conserva el volumen existente |
| `Schema-validation: missing column` o tabla nueva ausente al arrancar | Detén la aplicación y sigue la migración de semana 5 indicada arriba; después reconstruye la aplicación |
| `Access denied for user 'gamer_events'` tras cambiar `DB_PASSWORD` | MySQL conserva la contraseña con la que se creó el volumen. Restaura el valor original en `.env` o cambia la contraseña mediante administración de MySQL |
| `JWT_SECRET debe tener al menos 32 bytes` | Falta `JWT_SECRET` en `.env` o es demasiado corto |
| Windows: *running scripts is disabled* | Ejecuta `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass` en esa ventana |
| El puerto 80 está ocupado | Detén el otro servicio, o cambia `"80:8080"` en `docker-compose.yml` (por ejemplo, `"8081:8080"`) |
| VS Code marca errores, pero `./mvnw test` pasa | Ejecuta **Java: Clean Java Language Server Workspace** en la paleta de comandos |
| `docker compose` no conecta con Docker | Abre Docker Desktop y espera a que termine de iniciar |
| Los enlaces compartidos apuntan a otro equipo o puerto | Ajusta `PUBLIC_BASE_URL` en `.env` y recrea el contenedor de la aplicación con `docker compose up -d` |

## Entrega académica

Esta versión cubre la etapa de desarrollo. El informe, el video, el análisis
de seguridad de semana 5 y la actualización de la máquina virtual se realizan
por separado. Los cambios locales solo estarán en la VM después de desplegarlos.

```bash
./scripts/package-source.sh      # macOS y Linux
./scripts/package-source.ps1     # Windows
```

Genera `dist/gamer-events-semana5-fuente.zip` con el código, las pruebas, el
wrapper, la configuración de Docker, los scripts y la documentación. Incluye
`.env.example`, pero nunca `.env`, bases de datos, metadatos de Git ni
artefactos de compilación.

## Documentación

- [Desarrollo, API y migración de semana 5](docs/semana-5-desarrollo.md)
- [Configuración inicial y verificación (Etapa 1)](docs/etapa-1.md)
- [Etapas funcionales y pruebas (Etapa 1)](docs/etapas-funcionales.md)
- [APIs con JWT, MySQL y funcionalidades del caso (Etapa 2)](docs/etapa-2-auth-jwt.md)
- [Seguridad OWASP Top 10 y análisis con ZAP Proxy (Etapa 3)](docs/etapa-3-owasp-zap.md)
- [Despliegue y entrega](docs/despliegue-y-entrega.md)

## Referencias

- [Spring Boot: requisitos del sistema](https://docs.spring.io/spring-boot/4.0/system-requirements.html)
- [Spring Security: formulario de autenticación](https://docs.spring.io/spring-security/reference/7.0/servlet/authentication/passwords/form.html)
- [Spring Security: protección CSRF](https://docs.spring.io/spring-security/reference/7.0/servlet/exploits/csrf.html)
- [MySQL: imagen oficial de Docker](https://hub.docker.com/_/mysql)
- [Testcontainers: módulo MySQL](https://java.testcontainers.org/modules/databases/mysql/)
