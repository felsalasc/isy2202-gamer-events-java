# Gamer Events

Aplicación web y API REST para consultar eventos de la comunidad gamer, crear una
cuenta y personalizar un perfil. Desarrollada con Java y Spring Boot para la
asignatura **Seguridad y Calidad en el Desarrollo de Software (ISY2202)** de
**Duoc UC**.

**Repositorio:** [isy2202-gamer-events-java](https://github.com/felsalasc/isy2202-gamer-events-java) · **Actividad:** Etapa 3

## Funcionalidades

- **[Pública] Inicio:** eventos más recientes y populares, banners comerciales y acceso al inicio de sesión.
- **[Pública] Búsqueda de eventos:** filtros combinables por nombre, videojuego, tipo o categoría, país, ciudad, fecha y modalidad (presencial u online). Muestra un resumen de cada evento.
- **[Privada] Detalle de eventos:** descripción, videojuego, fecha y hora, ubicación o enlace de acceso, organizador, requisitos de participación, información de inscripción e imagen.
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

### 1. Crear el archivo `.env`

El archivo `.env` contiene las contraseñas y el secreto JWT. Está excluido de Git.

```bash
cp .env.example .env              # macOS y Linux
Copy-Item .env.example .env       # Windows PowerShell
```

Completa los valores vacíos (ver [Variables de entorno](#variables-de-entorno)).
Para generar valores seguros:

```bash
openssl rand -base64 48   # JWT_SECRET
openssl rand -base64 24   # DB_PASSWORD y MYSQL_ROOT_PASSWORD (uno distinto para cada una)
```

Define las contraseñas de MySQL **antes del primer arranque**: MySQL solo las
aplica al crear su volumen de datos.

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

Abre [http://localhost/recetas](http://localhost/recetas). También están
disponibles `/` y `/home`.

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

La aplicación se conecta con un usuario que solo tiene permisos sobre su propia
base de datos, nunca como `root`. Evita el carácter `$` en los valores: Docker
Compose lo interpreta como una variable.

## Rutas públicas y privadas

| Funcionalidad | Web | API | Acceso |
| --- | --- | --- | --- |
| Inicio | `GET /`, `/home`, `/recetas` | `GET /api/home` | Público |
| Búsqueda de eventos | `GET /eventos` | `GET /api/events` | Público |
| Detalle de evento | `GET /eventos/{id}` | `GET /api/events/{id}` | **Privado** |
| Inicio de sesión | `/login` (formulario) | `POST /api/auth/login` | Público |
| Registro | `/registro` | — | Público |
| Perfil | `/perfil` | — | **Privado** |
| Cierre de sesión | `POST /logout` | — | Privado |

- **Web:** las rutas privadas requieren una sesión iniciada; sin ella, redirigen a `/login`.
- **API:** las rutas privadas requieren `Authorization: Bearer <token>`; sin token responden `401`. Una sesión web no da acceso a la API.

### Filtros de búsqueda

`/eventos` y `/api/events` aceptan los mismos parámetros. Todos son opcionales y
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
  4 cuentas, seis eventos y tres banners) la primera vez que se crea el volumen.
  Para volver a cargarlos: `docker compose down -v` y `docker compose up -d`
  (se pierden los datos).
- La aplicación no modifica el esquema: arranca con `ddl-auto=validate` y se
  detiene si las tablas no calzan con las entidades.
- La aplicación y MySQL usan la zona horaria `America/Santiago`.

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
- La carga de archivos y Open Session in View están desactivados.

El repositorio no incluye contraseñas reales, archivos `.env` ni bases de datos.

## Pruebas

```bash
./mvnw clean verify          # macOS y Linux
./mvnw.cmd clean verify      # Windows
```

La suite tiene **95 pruebas**. Las pruebas de integración usan H2 en memoria y
no necesitan Docker. Las 7 pruebas de `MySqlIntegrationTests` levantan un MySQL
8.4 real con Testcontainers: verifican tildes, emojis, fechas, tamaño de las
filas y la búsqueda por nombre. Si Docker no está disponible, se omiten en vez de
fallar.

Los resultados de cada ejecución quedan en `target/surefire-reports/`.

## Solución de problemas

| Síntoma | Causa y solución |
| --- | --- |
| La aplicación no muestra eventos o no acepta las cuentas de prueba | El volumen de MySQL se creó antes de los scripts de `db/`. Recréalo con `docker compose down -v` y `docker compose up -d` (se pierden los datos) |
| `Schema-validation: missing column` al arrancar | Las tablas no calzan con las entidades. Recrea el volumen para que MySQL ejecute `db/01-schema.sql` |
| `Access denied for user 'gamer_events'` tras cambiar `DB_PASSWORD` | MySQL conserva la contraseña con la que se creó el volumen. Restaura el valor anterior en `.env`, o borra el volumen con `docker compose down -v` (se pierden los datos) |
| `JWT_SECRET debe tener al menos 32 bytes` | Falta `JWT_SECRET` en `.env` o es demasiado corto |
| Windows: *running scripts is disabled* | Ejecuta `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass` en esa ventana |
| El puerto 80 está ocupado | Detén el otro servicio, o cambia `"80:8080"` en `docker-compose.yml` (por ejemplo, `"8081:8080"`) |
| VS Code marca errores, pero `./mvnw test` pasa | Ejecuta **Java: Clean Java Language Server Workspace** en la paleta de comandos |
| `docker compose` no conecta con Docker | Abre Docker Desktop y espera a que termine de iniciar |

## Entrega académica

```bash
./scripts/package-source.sh      # macOS y Linux
./scripts/package-source.ps1     # Windows
```

Genera `dist/gamer-events-etapa2-fuente.zip` con el código, las pruebas, el
wrapper, la configuración de Docker, los scripts y la documentación. Incluye
`.env.example`, pero nunca `.env`, bases de datos, metadatos de Git ni
artefactos de compilación.

## Documentación

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
