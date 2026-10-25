# Despliegue y entrega académica

El código fuente se empaqueta con `./scripts/package-source.ps1`. El ZIP en
`dist/gamer-events-semana1-fuente.zip` incluye código, wrapper, pruebas, scripts
y documentación. Excluye `.tools`, `target`, `data`, `.git` y credenciales locales.
No contiene los documentos originales de Duoc ni instala Java por sí mismo.

## Reproducir la aplicación

1. Instalar un JDK 17 y configurar `JAVA_HOME`.
2. Descomprimir y ejecutar `./mvnw.cmd clean verify` en Windows; en Linux,
   `chmod +x mvnw` y `./mvnw clean verify`.
3. En Windows, ejecutar `./scripts/run-demo.ps1` para una demostración con tres
   cuentas, eventos y publicidad. El script genera una contraseña aleatoria
   local y la conserva en `.tools/demo-password.txt`. Las tres cuentas la usan;
   son cuentas ficticias, exclusivamente para evaluación.
4. En Linux o sin el script: definir `GAMER_DEMO_PASSWORD` en el entorno con una
   contraseña de al menos 12 caracteres y hasta 72 bytes, y ejecutar:

```sh
java -jar target/gamer-events-0.0.1-SNAPSHOT.jar --spring.profiles.active=demo --server.port=8080
```

La contraseña del entorno solo se utiliza al crear las cuentas demo que no
existen. No reemplaza contraseñas de cuentas existentes. Mantener el mismo valor
entre reinicios. El registro web permite crear más usuarios operativos.

## Máquina virtual de la actividad

Las instrucciones académicas exigen un enlace `http://[IP]/recetas`. Aún no se
ha proporcionado una máquina virtual ni un repositorio remoto; no hay despliegue
o publicación realizados.

Para la VM: ejecutar el JAR como usuario sin privilegios con Java 17; configurar
un servicio que conserve el directorio de trabajo y las variables de entorno;
mantener `data/` en un volumen persistente y con permisos limitados. Usar un
proxy inverso en el puerto 80 que reenvíe a `127.0.0.1:8080` y conserve la ruta
`/recetas`. Para uso con personas y datos reales, configurar HTTPS en el proxy,
cookies seguras y una base de datos de producción con migraciones; H2 con
`ddl-auto=update` se entrega únicamente para desarrollo y evaluación local.

La preferencia de notificaciones se guarda, pero no existe envío de correos.
La actividad solicita la preferencia, no una integración de mensajería.

## Evidencia de evaluación

- Maven, Spring Security, Thymeleaf y controladores: POM y capas MVC.
- Rutas públicas: inicio, registro, login y recursos estáticos.
- Rutas privadas: perfil, listado y detalle. Una sesión anónima se redirige a login.
- BCrypt y tres usuarios: prueba `DemoDataTests` y perfil demo.
- Formularios, CSRF y logout: `UserJourneysTests`.
- Filtros, detalle, estado vacío y escape HTML: `EventJourneysTests`.
- CSS común y avatares locales en todas las páginas.
- Revisar manualmente la accesibilidad desde la VM antes de subir el enlace.
