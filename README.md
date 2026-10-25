# Gamer Events

Aplicación web para consultar eventos de la comunidad gamer, crear una cuenta y
personalizar un perfil. Desarrollada con Java y Spring Boot para la asignatura
**Seguridad y Calidad en el Desarrollo de Software (ISY2202)** de **Duoc UC**.

**Repositorio:** [isy2202-gamer-events-java](https://github.com/felsalasc/isy2202-gamer-events-java) · **Actividad:** Semana 1

## Funcionalidades

- **Inicio público:** próximos eventos, eventos destacados y publicidad de marcas.
- **Registro e inicio de sesión:** nombre, correo único y contraseña almacenada con BCrypt.
- **Perfil privado:** nombre, alias, avatar, juegos de interés y preferencias de notificaciones.
- **Catálogo privado:** filtros combinables por categoría, fecha y ubicación.
- **Detalle de eventos:** descripción, dirección, fecha, hora, organizadores, servicios y expositores.
- **Cierre de sesión:** formulario POST protegido con CSRF.
- **Interfaz adaptable:** HTML5, CSS3 y plantillas Thymeleaf.

Las preferencias de notificaciones se guardan; el envío de correos no está
implementado. El perfil de demostración contiene eventos y marcas ficticios,
identificados como tales en la interfaz.

## Tecnologías

| Tecnología | Versión o uso |
| --- | --- |
| Java | 17 |
| Spring Boot | 4.0.9 |
| Maven | 3.9.14 mediante Maven Wrapper |
| Spring MVC y Thymeleaf | Controladores y vistas |
| Spring Security | Autenticación, autorización, BCrypt y CSRF |
| Spring Data JPA y H2 | Persistencia local |
| Jakarta Validation | Validación de formularios |
| JUnit 5 y MockMvc | Pruebas de integración |

## Requisitos

- JDK 17 y variable `JAVA_HOME` configurada.
- Git para clonar el repositorio, o descargar el código como ZIP.
- Conexión a Internet para descargar dependencias en la primera compilación.
- PowerShell para los scripts auxiliares de Windows.

Maven Wrapper está incluido; no es necesario instalar Maven globalmente.
Ejecuta los comandos desde la raíz del proyecto.

## Instalación y ejecución

Clona el repositorio y entra en su directorio:

```bash
git clone https://github.com/felsalasc/isy2202-gamer-events-java.git
cd isy2202-gamer-events-java
```

También puedes descargar y descomprimir el ZIP desde GitHub. Luego compila y
ejecuta las pruebas con los comandos correspondientes a tu sistema operativo.

### Windows PowerShell

```powershell
./mvnw.cmd clean verify
& "$env:JAVA_HOME/bin/java.exe" -jar target/gamer-events-0.0.1-SNAPSHOT.jar --server.port=18080
```

### Linux y macOS

```bash
chmod +x mvnw
./mvnw clean verify
"$JAVA_HOME/bin/java" -jar target/gamer-events-0.0.1-SNAPSHOT.jar --server.port=18080
```

Abre [http://localhost:18080/recetas](http://localhost:18080/recetas).
También están disponibles `/` y `/home`. Detén la aplicación con `Ctrl+C`.

Si el puerto 18080 no está disponible, sustituye `--server.port=18080` por
`--server.port=0` en el comando de
arranque. El sistema asignará un puerto libre, visible en el mensaje
`Tomcat started on port ...` del log. En Windows, detén la aplicación antes de
volver a ejecutar `clean verify`, porque el proceso mantiene abierto el JAR.

La ejecución normal permite registrar usuarios y comienza sin eventos ni marcas
precargados. Para explorar el catálogo de ejemplo, utiliza el perfil demo.

## Ejecución desde VS Code

En **Ejecutar y depurar** (`Ctrl+Shift+D`), selecciona **Gamer Events - Demo**
y presiona **F5** para ver los eventos de ejemplo. Abre
[http://localhost:18080/recetas](http://localhost:18080/recetas).
Detén la ejecución anterior con **Shift+F5** antes de iniciar otra.

La configuración de VS Code es local y está excluida de Git. En una clonación
nueva, puedes usar `./scripts/run-demo.ps1 -Port 18080` o crear una configuración
Java con `cl.duoc.gamerevents.GamerEventsApplication` como clase principal,
JDK 17 y los argumentos `--server.port=18080 --spring.profiles.active=demo`.
En este último caso, proporciona `GAMER_DEMO_PASSWORD` mediante un archivo de
entorno local excluido de Git. El modo normal utiliza solo `--server.port=18080`.

Los comandos de este README usan 18080 para evitar el rango que Windows reserva
en algunos equipos. El valor base de `application.properties` sigue siendo 8080;
el argumento de arranque tiene prioridad. Si utilizas `--server.port=0`, sustituye
el argumento 18080 para solicitar un puerto libre.

## Identidad visual

Estilo gamer con fondo negro, tarjetas oscuras, texto blanco y colores de
contraste. Los títulos tienen mayor peso visual; las tarjetas incorporan líneas
y formas geométricas en coral, amarillo y verde suave.

| Color | Hexadecimal | Uso |
| --- | --- | --- |
| Negro | `#090B10` | Fondo principal |
| Grafito | `#131720` | Tarjetas y formularios |
| Blanco | `#F4F5FA` | Texto principal |
| Coral | `#FF6B58` | Botones y acentos |
| Amarillo | `#FFB54D` | Foco de teclado y destacados |
| Verde suave | `#83B6A8` | Enlaces y acentos secundarios |

Las vistas comparten estilos adaptables a pantallas pequeñas y foco visible
para navegación por teclado.

## Demostración

### Windows

Después de compilar:

```powershell
./scripts/run-demo.ps1
```

El script inicia el perfil `demo` en un puerto libre. Genera una contraseña
aleatoria en `.tools/demo-password.txt`, un archivo local excluido de Git y del
ZIP de entrega. Si ya existe la variable `GAMER_DEMO_PASSWORD`, utiliza ese valor.

| Cuenta de prueba | Contraseña |
| --- | --- |
| `gamer1@example.test` | Contraseña local de demostración |
| `gamer2@example.test` | Contraseña local de demostración |
| `gamer3@example.test` | Contraseña local de demostración |

Para solicitar un puerto concreto:

```powershell
./scripts/run-demo.ps1 -Port 18080
```

### Otros entornos

Define `GAMER_DEMO_PASSWORD` en el entorno y ejecuta el JAR con
`--spring.profiles.active=demo`. La contraseña debe tener al menos 12 caracteres
y no superar 72 bytes UTF-8.

```bash
"$JAVA_HOME/bin/java" -jar target/gamer-events-0.0.1-SNAPSHOT.jar --server.port=18080 --spring.profiles.active=demo
```

El inicializador crea tres cuentas, tres eventos y tres anuncios. No duplica
los datos al reiniciar ni cambia las contraseñas de cuentas existentes. Conserva
la contraseña elegida para seguir usando las cuentas creadas.

## Persistencia

| Entorno | Base de datos |
| --- | --- |
| Desarrollo normal | `data/gamerevents.mv.db` |
| Demostración | `data/demo-gamerevents.mv.db` |
| Pruebas automatizadas | H2 en memoria, aislada de los datos locales |

Los registros y perfiles se conservan entre reinicios. Las bases locales están
excluidas de Git. H2 y `ddl-auto=update` se utilizan para desarrollo y evaluación;
la configuración de producción requiere una revisión específica.

## Arquitectura MVC

```text
src/main/java/cl/duoc/gamerevents/
├── config/          # Seguridad y carga de datos demo
├── controller/      # Rutas MVC y recepción de formularios
├── model/           # Entidades y formularios validados
├── repository/      # Acceso a datos mediante Spring Data JPA
└── service/         # Reglas de negocio y transacciones
src/main/resources/
├── templates/       # Plantillas Thymeleaf
├── static/css/      # Estilos compartidos
└── static/images/   # Avatares locales
src/test/            # Pruebas de integración
scripts/             # Ejecución demo y empaquetado
docs/               # Documentación académica y técnica
```

## Seguridad

- Inicio, registro, login y recursos estáticos son públicos; las demás rutas requieren autenticación.
- Las contraseñas se almacenan mediante BCrypt y no se vuelven a mostrar en formularios con errores.
- El correo se normaliza y tiene una restricción única en la base de datos.
- CSRF permanece activo en registro, login, edición de perfil y logout.
- El perfil se obtiene del usuario autenticado; enviar otro identificador no cambia la cuenta editada.
- Los formularios usan objetos específicos y validación del lado servidor.
- Thymeleaf escapa el contenido y los avatares se limitan a opciones locales.
- La consola H2, la carga de archivos y Open Session in View están desactivados.
- El servidor escucha en `127.0.0.1` durante el desarrollo.

No se incluyen contraseñas de acceso reales, bases locales ni archivos de entorno
en el repositorio. El usuario H2 `sa` sin contraseña corresponde exclusivamente
a la configuración local de desarrollo.

## Pruebas

```powershell
./mvnw.cmd clean verify
```

La verificación registrada incluye **27 pruebas aprobadas**, sin fallos ni
 errores: autenticación de tres cuentas demo, registro, unicidad de correo,
BCrypt, CSRF, validación, aislamiento del perfil, logout, filtros, detalle privado
y escape de HTML. Los resultados de cada ejecución quedan en
`target/surefire-reports/`.

## Entrega académica

```powershell
./scripts/package-source.ps1
```

Genera `dist/gamer-events-semana1-fuente.zip` con el código, pruebas, wrapper,
scripts y documentación. Excluye herramientas locales, credenciales, bases de
datos, metadatos Git y artefactos de compilación.

La actividad solicita acceso público mediante `http://[IP]/recetas`. La ruta está
implementada; el despliegue en una máquina virtual está pendiente.

## Documentación

- [Configuración inicial y verificación](docs/etapa-1.md)
- [Etapas funcionales y pruebas](docs/etapas-funcionales.md)
- [Despliegue y entrega](docs/despliegue-y-entrega.md)

## Referencias

- [Spring Boot: requisitos del sistema](https://docs.spring.io/spring-boot/4.0/system-requirements.html)
- [Spring Security: formulario de autenticación](https://docs.spring.io/spring-security/reference/7.0/servlet/authentication/passwords/form.html)
- [Spring Security: protección CSRF](https://docs.spring.io/spring-security/reference/7.0/servlet/exploits/csrf.html)
