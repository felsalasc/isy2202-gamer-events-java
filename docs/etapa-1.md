# Etapa 1 — Configuración inicial

## Alcance autorizado

Crear y verificar una base MVC para ISY2202 con Java 17, Maven, Spring Boot,
Spring Security, Thymeleaf, Validation, Data JPA y H2. Mostrar únicamente un
inicio básico. Registro, login personalizado, tres usuarios, perfiles, filtros,
detalle de eventos y despliegue pertenecen a etapas posteriores.

## Documentos académicos revisados

- `ISY2202_Exp1_S1_guia_de_aprendizaje.docx`: guía de Spring Initializr,
  controlador MVC, Thymeleaf y configuración de seguridad. Sus ejemplos de
  usuarios son orientativos; no representan la entrega completa.
- `S1_ Instrucciones_especificas_actividad_B.docx`: caso gamer, páginas públicas
  y privadas, CSS, tres usuarios y entrega de código comprimido con enlace a
  una máquina virtual. Exige que `/recetas` muestre el inicio, aunque el nombre
  no coincide con el dominio gamer. Se conserva como alias del inicio.
- `S1_ Pauta de evaluación.docx`: base Maven y Security, HTML vinculado a
  controladores, autorización de rutas, funcionalidades del caso y ejecución.

Los documentos son fuentes de requisitos académicos; los ejemplos de código,
enlaces y pasos de entrega se interpretan dentro del alcance por etapas definido
por el usuario. No se despliega ni publica el proyecto en esta etapa.

## Decisiones técnicas

- Proyecto en la raíz del repositorio existente, inicialmente vacío, sin commits.
- Spring Initializr genera la base con Spring Boot 4.0.9 y Java 17. La API actual
  rechaza 3.5.16; se usa una versión estable disponible y compatible con Java 17.
- Maven Wrapper fija la distribución de Maven y evita depender del PATH.
- JDK Temurin 17 portátil en `.tools/`, excluido de Git. No altera Java 25 del
  sistema. Se verifica SHA-256 contra los metadatos oficiales de Adoptium.
- Paquete raíz `cl.duoc.gamerevents`, con las capas MVC solicitadas. Las capas sin
  comportamiento quedan documentadas con `package-info.java`, sin entidades o
  servicios ficticios.
- H2 en memoria para desarrollo; consola desactivada y Open Session in View
  desactivado. No hay datos persistentes ni credenciales de producción.
- Inicio público en `/`, `/home` y `/recetas`; únicamente GET de esas rutas y
  CSS público. Resto de solicitudes requiere autenticación. CSRF permanece
  activo. En esta etapa se responde 401 a solicitudes privadas, sin implementar
  una pantalla de login ni usuarios anticipadamente.
- BCrypt queda disponible como bean para etapas posteriores. No se generan
  usuarios ni contraseñas temporales en el arranque.

## Plan de implementación y verificación

- [x] Generar la base Maven con Initializr y preparar Java 17 local.
- [x] Crear pruebas de integración del inicio, CSS, rutas protegidas y CSRF;
  ejecutarlas antes de implementar el controlador y la configuración.
- [x] Implementar controlador, plantilla, CSS, configuración y paquetes MVC.
- [x] Ejecutar `./mvnw.cmd clean verify` con Java 17.
- [x] Iniciar el JAR y comprobar por HTTP inicio, CSS y protección de rutas.
- [x] Documentar comandos, resultados y requisitos pendientes en README.

## Criterios de aceptación

El JAR debe compilar y arrancar en Java 17. El inicio debe renderizarse con
Thymeleaf y servir CSS. Una solicitud anónima a una ruta privada debe ser
rechazada; un POST sin token CSRF debe devolver 403. La base de datos H2 debe
inicializarse a través de JPA. El repositorio no debe incluir herramientas
descargadas, artefactos de compilación ni credenciales reales.

## Registro de verificación del 25 de octubre de 2026

Comandos de inspección: `rg --files`, `Get-ChildItem -Force`,
`git status --short --branch`, `git log -3 --oneline`, `java -version`,
`mvn -version` y `Get-Command java,mvn`. Se comprobó que el repositorio no
tenía archivos ni commits, Java del PATH era 25.0.2 y Maven no estaba en PATH.
Los DOCX se leyeron como OOXML en modo solo lectura, sin modificar los originales.

Se descargaron metadatos y el ZIP oficial desde `start.spring.io` y el JDK
desde Adoptium/GitHub. El ZIP del JDK coincidió con SHA-256:
`e53a79c3c3d86865bd7e787903884331068e71321714ffd44f145785affc7cb0`.
Se quitaron dependencias generadas que no se usan en esta etapa (consola H2,
extensiones Thymeleaf Security y starters de prueba de capas todavía vacías).
El POM usa la coordenada de Maven Central `4.0.9`, sin el sufijo `.RELEASE`
emitido por Initializr. El wrapper fija Maven 3.9.14.

Con `JAVA_HOME` apuntando al JDK local:

```powershell
./mvnw.cmd -version
./mvnw.cmd '-Dmaven.repo.local=.tools/m2' '-Dtest=HomeIntegrationTests' test -B -U
./mvnw.cmd '-Dmaven.repo.local=.tools/m2' test -B
./mvnw.cmd '-Dmaven.repo.local=.tools/m2' clean verify -B
& "$env:JAVA_HOME/bin/java.exe" -jar target/gamer-events-0.0.1-SNAPSHOT.jar --server.port=0
```

Resultados:

- Wrapper: Maven 3.9.14 ejecutándose con Temurin 17.0.20.1.
- Prueba previa a implementar: 12 casos, 5 fallos esperados (inicio y CSS
  bloqueados; login automático público). Confirma que las pruebas detectan
  comportamiento ausente en la base generada.
- `clean verify`: **BUILD SUCCESS**, 12 pruebas, 0 fallos, 0 errores, 0 omitidas;
  compilación con `release 17` y generación del JAR ejecutable.
- Arranque del JAR: Tomcat y JPA/H2 inicializados, puerto libre asignado 22941.
- Peticiones reales con `Invoke-WebRequest -SkipHttpErrorCheck`: GET de `/`,
  `/home`, `/recetas` y `/css/styles.css` devuelve 200; GET de `/perfil`,
  `/eventos`, `/login` y `/h2-console` devuelve 401; POST de `/recetas` sin
  CSRF devuelve 403.
- Inspección en el navegador integrado: título, estilo y estados vacíos
  renderizados correctamente en una ventana estrecha.
- Revisión estática independiente: sin hallazgos accionables.
- `git status` confirma que `.tools/` y `target/` no aparecen entre los archivos
  nuevos. No se crean commits ni se publica el repositorio.

Incidencias del entorno resueltas para verificar:

- El entorno restringido bloqueó DNS/descargas y peticiones de socket local;
  las operaciones necesarias se repitieron con autorización fuera del sandbox.
- Una ejecución de pruebas restringida se detuvo al cargar el agente de Java:
  el volcado informó acceso denegado a `javatool` por una tubería de Windows.
  Se interrumpió esa ejecución; `clean verify` fuera del sandbox pasó completo.
- Los intentos de arranque en 8080 y 8081 fallaron por puerto no disponible.
  Se usó `--server.port=0`; el puerto predeterminado del proyecto sigue siendo
  8080 y puede sobrescribirse al ejecutar.

Pendientes: no hay bloqueos de implementación de Etapa 1. La disponibilidad
del puerto 8080 depende del equipo. Las funcionalidades y entregables de
etapas posteriores siguen pendientes de autorización.
