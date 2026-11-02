# Etapa 2 — APIs con JWT, MySQL y funcionalidades del caso

## Alcance autorizado

Cumplir la pauta de la Etapa 2 dentro del mismo proyecto Spring Boot:

- MySQL como base de datos.
- API de login que entrega un token JWT.
- APIs públicas y privadas según la actividad. Las privadas se acceden con
  `Authorization: Bearer <token>`.
- Funcionalidades del caso completas: inicio, búsqueda y detalle de eventos.

La autenticación sigue el ejemplo del profesor y no se crea un microservicio
aparte. El sitio web con Thymeleaf, su login por formulario, el registro y el
perfil se mantienen.

## Requisitos de la evaluación

1. Backend con Spring Boot, Spring Web, Spring Security, Spring Data JPA y
   MySQL Driver.
2. Protección de URLs: solo son públicas las APIs que la actividad indica; el
   resto son privadas.
3. API de login con al menos 3 usuarios, que retorna un token JWT.
4. Con el token JWT, el usuario autenticado accede a las APIs privadas.
5. Los datos de las APIs se obtienen desde la base de datos.
6. Funcionalidades del caso:
   - **[Pública] Página de inicio:** eventos más recientes y populares, banners
     comerciales y acceso al inicio de sesión.
   - **[Pública] Búsqueda de eventos:** filtrar por nombre, videojuego, tipo o
     categoría, país o ciudad, fecha y modalidad (presencial u online), con un
     resumen de cada evento.
   - **[Privada] Detalle de eventos:** descripción, videojuego, fecha y hora,
     ubicación o enlace de acceso, organizador, requisitos de participación,
     información de inscripción y fotografías.

## Revisión del estado previo (1 de noviembre de 2026)

| Requisito | Estado antes de la etapa |
|---|---|
| Spring Boot, Web (`starter-webmvc`), Security, Data JPA | Cumplido |
| MySQL Driver | Falta: el proyecto usa H2 |
| 3 usuarios | Cumplido: `gamer1`, `gamer2` y `gamer3@example.test`, creados por `DemoDataInitializer` (perfil `demo`) |
| Datos desde la BD | Cumplido: eventos y banners vía JPA |
| Inicio público | Cumplido: próximos, destacados, banners y enlace "Ingresar" |
| Búsqueda pública | **Incumple:** `/eventos` exige sesión |
| Filtros de búsqueda | **Parcial:** categoría, ciudad y fecha; faltan nombre, videojuego, país y modalidad |
| Detalle privado | Cumplido en la web (`/eventos/{id}`) |
| Datos del detalle | **Parcial:** faltan videojuego, enlace de acceso, requisitos, inscripción y fotografías |
| API de login y APIs JWT | Falta |

## Decisiones técnicas

### Autenticación JWT (requisitos 3 y 4)

- Se conservan los nombres de las clases del ejemplo del profesor (`Constants`,
  `JWTAuthenticationConfig`, `JWTAuthorizationFilter`, `LoginController`), así
  el evaluador reconoce la estructura vista en clases. Se usa jjwt 0.12.5.
- Los tokens se firman con HS256. El secreto viene de la variable de entorno
  `JWT_SECRET`, sin constantes en el código, porque un secreto en el
  repositorio permitiría falsificar tokens. La aplicación no arranca si falta o
  tiene menos de 32 bytes, el mínimo para HS256.
- El token lleva `sub` (email), `iss` (`gamer-events`), el claim `authorities`
  (`ROLE_USER`), `iat` y `exp` (30 minutos).
- El login de la API usa `POST /api/auth/login` y no `/login`. Motivo: `/login`
  ya lo ocupa el formulario web, y `formLogin()` de Spring Security procesa ese
  `POST` con sesión, CSRF y redirect, nunca con JSON.
- El login reutiliza los 3 usuarios existentes y compara la contraseña con BCrypt
  (`PasswordEncoder.matches`). Así se cumple "al menos 3 usuarios" con las
  mismas cuentas de la web. Ante un email inexistente o una contraseña
  incorrecta responde 401 con el mismo mensaje, para no revelar qué cuentas
  existen.
- Un token alterado, expirado, malformado o de otro emisor recibe 401 con
  `WWW-Authenticate: Bearer` y un mensaje genérico.

### Protección de URLs (requisito 2)

- Hay dos cadenas de seguridad en la misma aplicación:
  - `/api/**` (primera en orden): sin sesión, sin CSRF y con el filtro JWT. Si
    falta el token responde 401, no redirige, porque el cliente de una API
    espera JSON.
  - Web (resto de rutas): login por formulario, CSRF y logout.
- `JWTAuthorizationFilter` es un `@Component`. Spring Boot registraría su bean
  como filtro global de servlets. Para evitarlo hay un `FilterRegistrationBean`
  desactivado, de modo que el filtro solo actúe dentro de la cadena de la API.
- Las rutas públicas y privadas siguen exactamente la lista del requisito 6:

| Funcionalidad | Web | API | Acceso |
|---|---|---|---|
| Inicio | `GET /` | `GET /api/home` | Público |
| Búsqueda | `GET /eventos` | `GET /api/events` | **Público** (antes privado en la web) |
| Detalle | `GET /eventos/{id}` | `GET /api/events/{id}` | Privado (sesión web o JWT) |
| Login | `/login` | `POST /api/auth/login` | Público |
| Perfil | `/perfil` | — | Privado |

- La búsqueda deja de exigir sesión porque la pauta la marca como pública. El
  listado muestra solo el resumen. El detalle completo queda privado, que es la
  distinción que pide la actividad.

### Modelo de eventos (requisito 6)

Se amplía `GamerEvent` con los datos que la pauta exige y que hoy no existen:

| Campo nuevo | Motivo en la pauta |
|---|---|
| `game` | Filtro y detalle: "videojuego" |
| `country` | Filtro "país o ciudad"; `location` se mantiene como ciudad |
| `modality` (`PRESENCIAL` / `ONLINE`) | Filtro "modalidad (presencial u online)" |
| `accessUrl` | Detalle: "ubicación o enlace de acceso" en eventos online |
| `requirements` | Detalle: "requisitos de participación" |
| `registrationInfo` | Detalle: "información de inscripción" |
| `imageUrl` | Detalle: "fotografías" |

- La búsqueda filtra por nombre (`title`, coincidencia parcial sin distinguir
  mayúsculas), videojuego, categoría, país o ciudad, fecha y modalidad. Todos
  los filtros son opcionales y se combinan.
- "Populares" se representa con el indicador `featured` existente.
- Los datos de demo incluyen eventos presenciales y online de distintos países,
  con imágenes servidas desde `static/images`, para que todos los filtros
  tengan resultados que mostrar al evaluar.
- La web y la API leen el mismo servicio y repositorio. Así los datos de ambas
  salen de la BD (requisito 5) sin duplicar lógica.
- La API devuelve DTOs, no entidades JPA: un resumen en la búsqueda y el
  detalle completo en `/{id}`. Así el listado público no expone los campos que
  pertenecen al detalle privado.

### Base de datos (requisitos 1 y 5)

- MySQL en la ejecución local y en `docker-compose`, con el driver
  `mysql-connector-j`, porque la pauta exige MySQL Driver.
- Las credenciales de MySQL se leen de variables de entorno, igual que
  `JWT_SECRET`.
- Las pruebas automatizadas siguen con H2 en memoria, para no depender de un
  servidor MySQL al ejecutar `./mvnw test`.

## Plan de implementación y verificación

- [x] **Parte 1 — Base JWT y `JWT_SECRET`** (commit `b313127`). Requisitos 3 y 4.
  - [x] Dependencias jjwt en `pom.xml`.
  - [x] `Constants`, `JWTAuthenticationConfig` y `JWTAuthorizationFilter`.
  - [x] `JWT_SECRET` en `application.properties`, `.env.example`,
    `docker-compose.yml` y los scripts `run-demo`.
  - [x] Desactivar el registro global del filtro.
  - [x] `JwtTokenTests`: secreto ausente o corto y contenido del token.
- [x] **Parte 2 — Login de la API y dos cadenas de seguridad.** Requisitos 2 a 4.
  - [x] 2.1 Separar `SecurityConfig` en una cadena `/api/**` (stateless, JWT,
    401) y la cadena web. Pruebas sin token, con token inválido y con token
    válido; pruebas web existentes en verde (commit `dfc9b9f`).
  - [x] 2.2 `LoginController` con `POST /api/auth/login` y BCrypt. Pruebas de
    login correcto, contraseña incorrecta, usuario inexistente y uso del token
    (commit `164409d`). Los errores de la API se responden desde
    `exception/ApiExceptionHandler` con el formato `{"error": "..."}`.
- [x] **Parte 3 — Modelo de eventos completo y búsqueda pública.** Requisito 6.
  - [x] 3.1 Modelo y datos de demo: enum `Modality` guardado como texto, campos
    nuevos en `GamerEvent`, fotos del evento y unos 6 eventos de demo
    (presenciales y online, varios países y videojuegos). Actualizar el
    `insert` SQL del helper de `EventJourneysTests`.
  - [x] 3.2 Búsqueda pública: `record EventFilter` con nombre (coincidencia
    parcial), videojuego, categoría, país, ciudad, fecha y modalidad;
    formulario en `events.html`; `/eventos` público. La prueba que exigía
    `/eventos` privado se actualiza porque el requisito cambió.
  - [x] 3.3 Detalle completo: videojuego, modalidad, dirección o enlace,
    requisitos, inscripción e imagen del evento en `event-detail.html`;
    `/eventos/{id}` sigue privado.
- [x] **Parte 4 — APIs públicas y privadas.** Requisitos 2, 4, 5 y 6.
  - [x] 4.1 `GET /api/home` y `GET /api/events` públicos, con DTO de resumen
    (`EventSummary`, `HomeResponse`, `BrandAdResponse`) y los mismos filtros de
    la búsqueda web. Errores 400 de la API con formato `{"error": "..."}`.
  - [x] 4.2 `GET /api/events/{id}` privado con JWT, con DTO de detalle y
    `EventNotFoundException` (404) compartida por la web y la API.
  - [x] Pruebas: acceso anónimo a las públicas, 401 sin token en el detalle,
    200 con token y 404 para un evento inexistente.
- [x] **Parte 5 — H2 a MySQL.** Requisitos 1 y 5.
  - [x] 5.1 Driver `mysql-connector-j` (`runtime`) y H2 solo para pruebas
    (`test`). Conexión por variables de entorno (`DB_HOST`, `DB_PORT`,
    `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`). El perfil `demo` ya no cambia la
    base, solo carga los datos de ejemplo.
  - [x] 5.2 Servicio `mysql:8.4` en `docker-compose.yml` con volumen con nombre,
    healthcheck y sin puerto publicado (destino final: VM de Azure). La app
    espera a que MySQL esté sano y corre con `TZ=America/Santiago`: dentro de
    un contenedor la JVM usa UTC por defecto, y `LocalDateTime.now()` decide
    qué eventos son próximos. Arranque real con los 3 usuarios.
  - [x] 5.3 Pruebas contra MySQL real con Testcontainers (se omiten si no hay
    Docker): escape del `LIKE`, tildes y UTF-8 sin parámetros en la URL, y
    tamaño de las filas.
- [ ] **Parte 6 — Documentación.**
  - [ ] README: variables de entorno, ejecución, ejemplos de login y uso del
    token, tabla de rutas públicas y privadas.
  - [ ] Cierre de este registro de verificación.

## Criterios de aceptación

- `POST /api/auth/login` con uno de los 3 usuarios devuelve un token HS256; con
  credenciales inválidas devuelve 401.
- `GET /` (inicio), `/eventos` (búsqueda), `/api/home` y `/api/events`
  responden sin autenticación.
- Una petición a `/api/events/{id}` sin token o con token inválido recibe 401,
  nunca un redirect a `/login`. Con un token válido devuelve el detalle
  completo.
- La búsqueda filtra por nombre, videojuego, tipo o categoría, país o ciudad,
  fecha y modalidad.
- El detalle muestra descripción, videojuego, fecha y hora, ubicación o enlace,
  organizador, requisitos, inscripción y fotografía.
- Todos los datos provienen de MySQL al ejecutar la aplicación.
- El login web, el registro y el perfil siguen funcionando con CSRF.
- La aplicación no arranca sin `JWT_SECRET`.
- El repositorio no incluye secretos reales ni archivos `.env`.

## Registro de verificación del 1 de noviembre de 2026

Estado al terminar la Parte 1, en la rama `feature/jwt-auth`:

```bash
./mvnw -q -B test
```

Resultados:

- **30 pruebas, 0 fallos, 0 errores, 0 omitidas.**
  - `HomeIntegrationTests`: 10.
  - `UserJourneysTests`: 12.
  - `EventJourneysTests`: 4.
  - `JwtTokenTests`: 3.
  - `DemoDataTests`: 1.
- Las pruebas corren con H2 en memoria (`jdbc:h2:mem:demo-tests`).
- El `java` del PATH del equipo es 27.0.1. El proyecto declara
  `java.version` 17.

## Registro de verificación de la Parte 2 (1 de noviembre de 2026)

Pruebas nuevas: `ApiSecurityTests` (8) y `ApiLoginTests` (9). Antes de
implementar, `ApiSecurityTests` falló 8 de 8: había un redirect 302 a `/login`
en vez de 401, y la sesión web autenticaba `/api/**`. `ApiLoginTests` falló 8 de
9 con 404, porque el endpoint no existía. `loginOnlyAcceptsPost` ya pasaba
gracias a la cadena del paso 2.1.

```bash
./mvnw -q -B test
```

Resultados:

- **47 pruebas, 0 fallos, 0 errores, 0 omitidas.**
  - `HomeIntegrationTests`: 10.
  - `UserJourneysTests`: 12.
  - `ApiLoginTests`: 9.
  - `ApiSecurityTests`: 8.
  - `EventJourneysTests`: 4.
  - `JwtTokenTests`: 3.
  - `DemoDataTests`: 1.
- Las pruebas siguen en verde después de mover el manejo de errores a
  `exception/` (`InvalidCredentialsException`, `ApiError` y
  `ApiExceptionHandler`), sin modificarlas.
- Aplicación real con el perfil `demo` y `curl`:
  - `gamer1`, `gamer2` y `gamer3` obtienen token (200).
  - Contraseña incorrecta: 401 con `WWW-Authenticate: Bearer` y
    `{"error":"Credenciales inválidas."}`.
  - Cuerpo vacío: 400.
  - `/api/**` sin token: 401 `{"error":"Se requiere un token Bearer."}`.
  - Token inválido: 401 `{"error":"Token inválido o expirado."}`, en UTF-8.
  - El token obtenido pasa la cadena `/api/**`: 404, porque aún no hay
    endpoints.
  - `GET /perfil` sigue redirigiendo a `/login`.

Pendientes: las Partes 3 a 6.
