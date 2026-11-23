# Semana 5 Diagnóstico y primer bloque de código

Estado posterior: el usuario aprobó avanzar y luego acotó el trabajo a la etapa de desarrollo. El alcance y uso del código implementado están en `semana-5-desarrollo.md`. Este documento conserva el diagnóstico inicial; sus pendientes iniciales no describen el estado final.

Revisión del 23 de noviembre de 2026. El objetivo solicitado es comenzar por el código pendiente del ejercicio, conservando la aplicación Gamer Events. El informe académico y el video se abordarán después.

## Base revisada

- Repositorio: https://github.com/felsalasc/isy2202-gamer-events-java
- `origin/master`: `1a1a3bc581bd4f81bef342664145bc6e6127b280`.
- Copia de trabajo local: `a9ef97e`, sin cambios de usuario al comenzar. Se descargaron las referencias remotas para revisar la versión actual; no se integraron todavía en la rama local.
- Se leyeron guía, pauta y formato de respuesta de semana 5. Sus instrucciones se consideran requisitos académicos, no autorización para publicar, entregar documentos, grabar ni operar la máquina virtual.

La revisión fue de código y documentos. No se ejecutó la suite actual ni un análisis de vulnerabilidades; las cifras y resultados históricos del README no constituyen verificación de esta revisión.

## Requisitos y faltantes

| Requisito | Evidencia en la versión remota | Trabajo pendiente |
| --- | --- | --- |
| Spring Boot, Web, Security, JPA y Thymeleaf | Dependencias, controladores, vistas y configuración presentes | Conservar y verificar al implementar |
| MySQL y datos desde BD | Driver, entidades, repositorios, scripts SQL y Docker presentes | Añadir tablas y migraciones para las nuevas funciones |
| Login JWT y tres usuarios | Login REST, filtro JWT y datos SQL con tres USER y un ADMIN | Reutilizar y verificar con pruebas |
| Fotos y videos en eventos, acceso privado | Solo existe `imageUrl` y una imagen estática por evento; multipart está desactivado | Persistencia de medios, carga validada, consulta privada y galería |
| Compartir eventos propios dentro del sitio y en redes | No hay modelo ni controladores de publicaciones compartidas; el evento no tiene propietario | Definir autoría, permisos, publicación interna y enlaces de compartir |
| Comentarios y valoraciones privados | No hay entidades, tablas, formularios ni endpoints | Implementar web, API y persistencia |
| Inicio público en `/eventos` | `/eventos` muestra la búsqueda; el home está en `/`, `/home` y `/recetas` | Ajustar el enrutamiento para cumplir el formato de entrega y actualizar enlaces y pruebas |
| Análisis estático de frontend y backend | No hay configuración SAST ni Dependency-Check en Maven | Configurar análisis reproducible, guardar línea base, corregir hallazgos reales y repetir |
| Aplicación pública en VM | Hay scripts y documentación de un despliegue anterior | Verificar y redesplegar cuando el código esté listo; no se comprobó la VM en esta revisión |

La guía ejemplifica Dependency-Check, que analiza dependencias (SCA). La actividad también solicita análisis estático de código. ZAP, documentado en la etapa anterior, realiza análisis dinámico (DAST); por sí solo no acredita ese requisito. La versión de Dependency-Check mostrada en la guía es un ejemplo y deberá verificarse antes de instalarla. No se deben introducir vulnerabilidades deliberadamente para obtener evidencias.

## Orden propuesto

1. Integrar la versión remota mediante avance rápido, comprobando otra vez que no haya cambios locales.
2. Comentarios y valoraciones privados, con API JWT y vistas Thymeleaf.
3. Autoría de eventos y permisos para gestionar contenido propio. Los eventos históricos requieren una asignación explícita mediante migración; no se debe inferir un autor desde el texto de organizadores.
4. Fotos y videos mediante almacenamiento persistente, validación de tamaño y contenido, nombres generados y acceso autenticado. No basta con habilitar multipart ni con aceptar URLs arbitrarias.
5. Compartir dentro del sitio y mediante enlaces a redes sociales. Compartir un enlace no debe hacer público el detalle privado ni incluir JWT o datos privados. Publicar automáticamente en cuentas sociales no se presume parte del alcance.
6. Ajustar `/eventos`, completar análisis estático y SCA, corregir hallazgos y verificar la aplicación.

## Diseño del primer bloque para aprobación

### Objetivo y alcance

Permitir que un usuario autenticado comente y valore un evento existente desde su detalle y desde la API. La aplicación mantiene Java 17, Spring Boot, MySQL y Thymeleaf. Se reutilizan las dos cadenas de seguridad existentes: sesión y CSRF para web, JWT para API. Este primer bloque no incluye archivos multimedia, cambios visuales generales, publicación externa ni despliegue.

### Alternativas

- Solo API: menor trabajo inicial, pero la función no se podría demostrar desde las páginas Thymeleaf.
- Solo formularios web: permite la demostración visual, pero deja pendiente el requisito de APIs privadas con JWT.
- Web y API sobre un mismo servicio: recomendada; mantiene las reglas y la persistencia iguales en ambos accesos.

### Comportamiento propuesto

- El detalle privado muestra comentarios escapados como texto, autor visible por alias y fecha, promedio y cantidad de valoraciones, y dos formularios separados para comentar y valorar.
- Se permiten varios comentarios por usuario, de 1 a 1000 caracteres después de quitar espacios en los extremos.
- La valoración es un entero de 1 a 5. Cada usuario tiene una valoración por evento y puede actualizarla; una restricción única en BD evita duplicados.
- Una tabla de comentarios y otra de valoraciones referencian evento y usuario. Las relaciones no se serializan directamente en respuestas JSON; se usan DTO.
- El servidor obtiene el usuario desde la autenticación. El cliente nunca elige el autor mediante un identificador enviado en el formulario o JSON.
- API propuesta: `GET/POST /api/events/{id}/comments`, `GET /api/events/{id}/ratings` para el resumen y `PUT /api/events/{id}/ratings/me` para crear o actualizar la valoración propia. Todas son privadas.
- Web propuesta: `POST /eventos/{id}/comentarios` y `POST /eventos/{id}/valoracion`, con validación y CSRF. Los errores deben mostrarse en el detalle conservando los datos válidos del formulario.
- Las consultas de comentarios se paginan y ordenan de forma estable; el resumen de valoraciones se calcula en BD.
- Eventos inexistentes producen 404; datos inválidos, 400 en API; ausencia de JWT, 401 en API. La web mantiene sus redirecciones de login.
- Se prepara una migración SQL incremental para las tablas nuevas, además del esquema para instalaciones nuevas. No se requiere borrar el volumen MySQL existente.

### Comprobación prevista

Pruebas de autenticación y CSRF, rechazo de texto vacío y puntuaciones inválidas, aislamiento del autor, escape de contenido HTML, actualización sin duplicados, cálculo del promedio, eventos inexistentes y renderizado del detalle. Ejecutar la suite existente después del cambio e informar por separado las pruebas MySQL omitidas si Docker no está disponible.

### Estado

Diagnóstico completado. Diseño propuesto, pendiente de aprobación antes de escribir código funcional. No se modificó la aplicación ni se ejecutaron escaneos.
