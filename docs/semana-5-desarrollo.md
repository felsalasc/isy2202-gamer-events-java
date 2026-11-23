# Desarrollo de semana 5

La etapa de desarrollo incorpora comentarios, valoraciones, fotos, videos y eventos compartidos. El análisis académico de seguridad, el informe, el video y el despliegue se trabajan por separado.

## Funciones

- `/eventos`: inicio público solicitado en la entrega. `/eventos/buscar`: catálogo público con los filtros existentes.
- `/eventos/nuevo`: publicar un evento presencial u online. El propietario se obtiene de la cuenta autenticada.
- Detalle privado `/eventos/{id}`: comentar, valorar de 1 a 5, consultar la galería y, para el propietario, agregar archivos y compartir.
- `/comunidad`: eventos compartidos dentro del sitio. Los enlaces externos abren WhatsApp, Facebook o X; el usuario confirma la publicación en la red elegida. Los destinatarios deben iniciar sesión para acceder al detalle.
- PNG y JPEG hasta 5 MB, MP4 con video H.264 hasta 20 MB, máximo 20 archivos por evento. Fotos decodificadas y recodificadas; videos con comprobación de estructura y pista de video. No se realiza transcodificación ni análisis antivirus.
- Archivos almacenados como BLOB en MySQL, conservados en el volumen existente y servidos por rutas autenticadas; nunca se usan nombres aportados por el cliente como rutas de disco.
- Una valoración por usuario/evento, actualizable; comentarios paginados de 20 en 20, hasta 1000 caracteres. Las respuestas públicas de eventos no exponen comentarios, archivos ni enlaces privados de acceso online.

## API privada con JWT

Además de las rutas existentes:

| Método | Ruta | Operación |
| --- | --- | --- |
| POST | `/api/events` | Publicar un evento propio |
| GET / POST | `/api/events/{id}/comments` | Consultar página `?page=0` / comentar con `{"text":"Nos vemos"}` |
| GET | `/api/events/{id}/ratings` | Cantidad y promedio |
| PUT | `/api/events/{id}/ratings/me` | Crear o actualizar con `{"score":5}` |
| GET / POST | `/api/events/{id}/media` | Listar / adjuntar un archivo multipart en campo `file` |
| GET | `/api/events/{id}/media/{mediaId}` | Obtener archivo privado, soporta Range |
| POST | `/api/events/{id}/shares` | Compartir un evento propio dentro del sitio |
| GET | `/api/community` | Consultar página de eventos compartidos |

Todas requieren `Authorization: Bearer <token>`. Los formularios web conservan sesión y CSRF.

## Actualizar una instalación existente sin perder datos

No ejecutes `db/01-schema.sql` ni borres el volumen existente: el primer script recrea las tablas. Los scripts de inicialización de MySQL solo se ejecutan automáticamente sobre un volumen nuevo.

1. Respalda la base y detén la aplicación mientras mantienes MySQL: `docker compose stop gamer-events`.
2. Ejecuta `./scripts/migrate-week5.ps1` en PowerShell o `bash scripts/migrate-week5.sh` en Linux. Aplican el cambio de propietario y crean las tablas nuevas; pueden repetirse.
3. Configura `PUBLIC_BASE_URL` en `.env` con el origen real, por ejemplo `http://IP-DE-LA-VM` o `http://localhost` en local, sin rutas adicionales. Este valor construye los enlaces compartidos.
4. Ejecuta `docker compose up -d --build`.

Una instalación nueva ejecuta automáticamente los SQL de `db/` en orden. El archivo `05-demo-owners.sql` asigna los seis eventos ficticios conocidos a las tres cuentas de demostración; los eventos históricos ajenos a esa lista conservan propietario vacío y nadie puede apropiárselos desde la API.

## Verificación y entrega del código

Verificación del 23 de noviembre de 2026: compilación `verify` aprobada y 155 pruebas aprobadas en total, sin fallos, errores ni omisiones. Incluye la migración de la base anterior aplicada dos veces sin perder sus 4 usuarios y 6 eventos. Los videos de prueba son archivos sintéticos generados para las pruebas.

`./mvnw.cmd verify` (Windows) o `./mvnw verify` (Linux). Con Docker disponible también se prueban el esquema SQL y las operaciones nuevas contra MySQL 8.4 real. Sin Docker esas pruebas se omiten y deben informarse como omitidas.

`./scripts/package-source.ps1` o `bash scripts/package-source.sh` empaquetan el código e incluyen los scripts de base de datos. No incluyen `.env`, contraseñas, bases locales ni artefactos de compilación.
