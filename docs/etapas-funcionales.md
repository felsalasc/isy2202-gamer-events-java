# Etapas funcionales — Semana 1

Autorizadas por el usuario el 25 de octubre de 2026 al solicitar continuar con
las siguientes etapas. Se mantienen Java 17, Boot 4.0.9 y Maven Wrapper.

## Plan

1. Registro, login y logout: entidad `GamerUser`, repositorio, formularios DTO,
   servicio transaccional y controladores. Correo normalizado y único en base
   de datos; BCrypt; contraseña de 12 caracteres como mínimo y 72 bytes como
   máximo. Formularios con CSRF y errores sin mostrar contraseñas.
2. Perfil privado del principal autenticado: nombre, alias, avatar de una
   lista local, juegos y notificaciones. No aceptar identificadores de otros
   usuarios ni vincular formularios directamente a entidades.
3. Eventos persistidos: próximos, destacados, categoría/fecha/ubicación y
   detalle privado con dirección, horario, organizadores, servicios y expositores.
   Publicidad almacenada en JPA. Datos ficticios identificados en perfil demo.
4. H2 en archivo para desarrollo y H2 en memoria aislado para pruebas.
   Perfil `demo` opcional con tres cuentas; contraseña proporcionada mediante
   variable de entorno, nunca almacenada en código ni documentación pública.
5. Verificar primero pruebas fallidas y luego los flujos completos. Revisar
   seguridad, ejecutar `clean verify`, arrancar JAR y comprobar desde navegador.
   Preparar ZIP de fuente y guía de despliegue; no publicar sin destino definido.

## Archivos afectados

`config/SecurityConfig`, `config/DemoDataInitializer`, entidades y formularios
en `model`, repositorios JPA en `repository`, servicios en `service`,
controladores de autenticación/perfil/eventos y `HomeController`, plantillas
de inicio/login/registro/perfil/eventos/detalle/errores y fragmentos comunes,
CSS, avatares SVG locales, configuración principal/demo/pruebas, pruebas de
integración, README y scripts de ejecución/entrega.

## Verificación prevista

Registro y unicidad incluso con mayúsculas; BCrypt sin contraseña en claro;
login real de tres cuentas; rechazo de contraseña errónea; CSRF en registro,
perfil, login y logout; sesión invalidada al salir; perfil limitado al principal;
validación de avatar y longitudes; filtros combinados; detalles/404; escape HTML;
inicio público sin filtrar el detalle privado. El ZIP excluye herramientas,
bases de datos, Git, contraseñas y logs.

## Referencias técnicas

- https://docs.spring.io/spring-security/reference/7.0/servlet/authentication/passwords/form.html
- https://docs.spring.io/spring-security/reference/7.0/servlet/exploits/csrf.html

Los documentos académicos ya revisados en `etapa-1.md` siguen siendo la fuente
de requisitos. No se agregan flujos de administración o inscripción a eventos,
ya que no se solicitan para esta actividad.

## Resultado de verificación

25 de octubre de 2026: `./mvnw.cmd -Dmaven.repo.local=.tools/m2 clean verify -B`
terminó con BUILD SUCCESS: 27 pruebas, 0 fallos, 0 errores y 0 omitidas.
Las pruebas verificaron tres cuentas demo autenticables, registro y unicidad,
BCrypt, validación Unicode, CSRF, aislamiento de perfil, logout, filtros,
detalles privados y escape HTML. El hallazgo de revisión sobre la hora pública
se reprodujo con una prueba fallida y se corrigió; la hora queda en el detalle.

El JAR inició con Java 17 mediante `scripts/run-demo.ps1`, perfil demo, H2 en
archivo y puerto asignado 3122. Se inspeccionaron visualmente inicio y registro
en el navegador integrado. Los recorridos de autenticación y edición fueron
comprobados por las pruebas de integración, no por interacción manual completa.

Los scripts PowerShell pasaron análisis sintáctico. La entrega se empaqueta con
`scripts/package-source.ps1`. GitHub y VM siguen pendientes de sus destinos.
