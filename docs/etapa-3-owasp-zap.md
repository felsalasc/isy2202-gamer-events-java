# Etapa 3 — Seguridad OWASP Top 10 y análisis con ZAP Proxy

Plan de trabajo ActividadA Sumativa S3.

## Alcance

- Se trabaja sobre la misma aplicación de las etapas 1 y 2 (eventos gamer).
- Se analiza la aplicación con ZAP Proxy antes y después de las correcciones.

## Decisiones tomadas

| Tema | Decisión |
| --- | --- |
| Orden de trabajo | Primero ZAP sobre la versión actual (línea base), después los cambios y un segundo escaneo |
| Datos iniciales | Script SQL (`db/`) con esquema e inserción. Se elimina `DemoDataInitializer` y el perfil `demo` |
| Contraseñas de usuarios | Se insertan ya hasheadas con BCrypt. El texto plano solo va en el informe entregado en Duoc |
| Contraseñas | Una contraseña compartida por los 3 USER y otra distinta para el ADMIN. Ambas largas y aleatorias |
| `.env` | Solo lo que leen la app y MySQL: `JWT_SECRET`, `DB_*`, `MYSQL_ROOT_PASSWORD` |
| Contraseña escrita en el repo | Se quita la contraseña demo escrita en `scripts/azure-user-data.sh` y en `.env.example` |
| `azure-user-data.sh` | Se mantiene y se corrige. Se usará para volver a desplegar la VM con los cambios |
| `run-demo` | Se mantiene, renombrado a `run.sh` / `run.ps1`, sin referencias a `GAMER_DEMO_PASSWORD` |
| Pantalla admin | `/admin` con un listado de usuarios de solo lectura (datos ficticios). Sin edición |
| HTTPS | No se implementa. Se documenta en el informe como riesgo aceptado (cookie sin `Secure`, sin HSTS) |
| VM de entrega | `http://20.80.36.171` |

## Fase 0 — Escaneo inicial con ZAP (antes de cambiar código)

Objetivo: evidencia del estado actual para el informe ("antes").

1. Instalar ZAP (zaproxy.org) y documentar con capturas la instalación y la
   configuración.
2. Escanear la VM de entrega `http://20.80.36.171`, que tiene desplegada la
   versión actual. No se levanta la aplicación en local. La VM no se
   redespliega hasta terminar todos los escaneos de esta fase.
3. **Frontend (sitio web Thymeleaf):**
   - Escaneo anónimo: Automated Scan sobre `http://20.80.36.171`.
   - Escaneo autenticado: crear un Context con `http://20.80.36.171.*`,
     excluir `http://20.80.36.171/logout.*` y configurar la autenticación
     por formulario (`/login`, campos `email` y `password`) con un usuario de
     prueba y modo de usuario forzado, para que ZAP también recorra las
     páginas privadas.
   - Ejecutar Spider, luego Active Scan.
4. **Backend (API REST con JWT):**
   - Obtener un token con `POST http://20.80.36.171/api/auth/login`.
   - Enviar las peticiones de la API (Postman o curl) a través del proxy de
     ZAP, o agregar una regla Replacer que inserte
     `Authorization: Bearer <token>`.
   - Ejecutar Active Scan sobre `/api/**`.
5. Exportar los reportes HTML de cada escaneo a `docs/zap/antes/`.
6. Revisar la lista de alertas: criticidad, si corresponde a OWASP Top 10, y
   si es un hallazgo real o un falso positivo.

### Resultados — escaneo anónimo (ZAP 2.17.0, 05-11-2026, VM `20.80.36.171`)

| # | Alerta | Riesgo | OWASP 2021 | Clasificación |
| --- | --- | --- | --- | --- |
| 1 | Cabecera Content Security Policy (CSP) no configurada (10038) | Medio | A05 | Real. Se corrige en Fase 3 |
| 2 | Atributo de elemento HTML controlable por el usuario (XSS potencial) en `/registro` (10031) | Informativo | A03 | Falso positivo: Thymeleaf escapa la salida y las contraseñas no se reflejan |
| 3 | Petición de autenticación identificada (10111) | Informativo | — | No es vulnerabilidad |
| 4 | Respuesta de gestión de sesión identificada (10112) | Informativo | — | No es vulnerabilidad |

Evidencia del falso positivo 2: con `name="><script>alert(1)</script>` la
respuesta contiene `value="&quot;&gt;&lt;script&gt;alert(1)&lt;/script&gt;"`,
y los campos `password` y `confirmPassword` vuelven sin `value`.

Pendientes: escaneo autenticado del sitio y escaneo de la API con JWT.

## Fase 1 — Usuarios y script SQL (retroalimentación semanas 1 y 2)

1. Crear `db/01-schema.sql` con las tablas `gamer_users` (con columna
   `role`), eventos y `brand_ads`, idénticas a las entidades JPA.
2. Crear `db/02-data.sql` con:
   - `gamer1`, `gamer2` y `gamer3@example.test` con rol USER.
   - `admin@example.test` con rol ADMIN.
   - Los 6 eventos y las 3 marcas que hoy crea `DemoDataInitializer`.
3. Montar `./db` en `/docker-entrypoint-initdb.d` del contenedor MySQL en
   `docker-compose.yml`.
4. Cambiar `spring.jpa.hibernate.ddl-auto` de `update` a `validate`.
5. Eliminar `DemoDataInitializer`, `application-demo.properties`,
   `GAMER_DEMO_PASSWORD` y `SPRING_PROFILES_ACTIVE=demo`.
6. Ajustar `DemoDataTests` y los tests que dependen del perfil `demo`.

Nota: el script de `/docker-entrypoint-initdb.d` solo corre si el volumen
`mysql-data` está vacío. En la VM hay que recrearlo una vez
(`docker compose down -v` y luego `up`), lo que borra los datos actuales.

## Fase 2 — Rol ADMIN (retroalimentación semana 2)

1. Enum `Role { USER, ADMIN }` y campo `role` en `GamerUser`. Los usuarios
   registrados desde `/registro` quedan como USER.
2. `userDetailsService` usa el rol real del usuario, no `"USER"` fijo.
3. El JWT incluye el rol real del usuario, no `ROLE_USER` fijo.
4. Página privada `/admin`, solo para ADMIN: listado de usuarios (nombre,
   correo y rol), de solo lectura.
5. API `GET /api/admin/users`, solo para ADMIN.
6. `@EnableMethodSecurity` y reglas `hasRole("ADMIN")` en ambas cadenas.
7. Enlace "Administración" en el menú, visible solo para ADMIN.
8. Tests: USER recibe 403 y ADMIN recibe 200, en la web y en la API.

## Fase 3 — Correcciones OWASP

La lista definitiva sale de la Fase 0. Hallazgos esperados:

| Hallazgo | Origen | OWASP 2021 | Corrección |
| --- | --- | --- | --- |
| Falta Content Security Policy | ZAP (alerta 10038, riesgo medio) | A05 | Cabecera CSP en `SecurityConfig` |
| Session ID en la URL (`;jsessionid=`) | Preventivo (ZAP no lo reportó) | A07 | `server.servlet.session.tracking-modes=cookie` |
| Faltan Permissions-Policy y Referrer-Policy | Preventivo (ZAP no lo reportó) | A05 | Cabeceras en `SecurityConfig` |
| Cookie sin Secure, sin HSTS (solo HTTP) | Revisión manual de cabeceras | A02 | No se corrige: riesgo aceptado y documentado (sin HTTPS) |
| 404 sin la página de error del sitio (con sesión) | Revisión manual | A05 | Revisar el manejo de errores web |
| Rol fijo `USER` en sesión y JWT | Revisión de código | A01 | Fase 2 |
| Contraseña en texto plano en el repo | Revisión de código | A07 | Fase 1 |
| Hibernate modifica el esquema en producción | Revisión de código | A05 | Fase 1 (`ddl-auto=validate`) |
| Sin límite de intentos de login | Revisión de código | A07 | Bloqueo tras 5 fallos por 15 minutos (web y API) |
| Sin registro de eventos de seguridad | Revisión de código | A09 | Log de logins exitosos, fallidos y accesos denegados |
| Contenedor corre como root | Revisión de código | A05 | `USER` no-root en el `Dockerfile` |

Cada corrección se documenta con: alerta o hallazgo, explicación, cambio
realizado (archivo y línea) y evidencia (test o nuevo escaneo).

## Fase 4 — Escaneo final con ZAP

1. Redesplegar la VM `http://20.80.36.171` con los cambios y repetir
   exactamente los escaneos de la Fase 0 sobre ella.
2. Exportar los reportes a `docs/zap/despues/`.
3. Tabla comparativa antes/después por alerta.

## Fase 5 — Entrega

- [ ] ZIP con frontend, backend y script SQL (`scripts/package-source.sh`).
- [ ] Informe: instalación y configuración de ZAP, evidencia de ejecución,
      listado y criticidad de vulnerabilidades, mapeo a OWASP Top 10,
      explicación y corrección de cada una, evidencia de corrección, y tabla
      de usuarios de prueba con sus contraseñas.
- [ ] Reportes de ZAP antes y después.
- [ ] VM `http://20.80.36.171` redesplegada con `scripts/azure-user-data.sh`
      corregido (recrear el volumen `mysql-data` para que cargue el script SQL).
- [ ] Video por Teams: demo del sitio (público, privado, USER vs ADMIN, API
      con JWT) y explicación del cumplimiento de OWASP Top 10.

## Pendiente de respuesta del profesor

- Confirmar que el caso es eventos gamer y no recetas.
- Confirmar que el análisis con ZAP es sobre la aplicación de la asignatura,
  con las mejoras sugeridas.
- Versión de OWASP Top 10 a usar (2021 o 2025).
