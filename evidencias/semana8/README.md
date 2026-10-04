# Evidencias de ejecucion: Sumativa 3, semana 8

Estos archivos contienen resultados obtenidos durante la ejecucion en Ubuntu. Las pruebas 03 y 04 corresponden a etapas previas de este mismo desarrollo de semana 8; no son entregas de semanas anteriores.

| Grupo | Resumen principal | Resultado registrado |
|---|---|---|
| OAuth2 | [03_pruebas_oauth2.txt](03_pruebas_oauth2.txt) | 18 comprobaciones correctas |
| Spring Cloud y resiliencia | [04_pruebas_cloud_resiliencia.txt](04_pruebas_cloud_resiliencia.txt) | 20 comprobaciones correctas |
| Docker y JMS | [05_pruebas_docker_jms.txt](05_pruebas_docker_jms.txt) | 25 comprobaciones correctas |

## Que demuestra cada grupo

- `03_*`: emision de JWT para los tres canales; acceso autorizado; rechazo sin token, por firma alterada, cliente incorrecto, permiso ajeno y token fijo anterior. Incluye respuestas 200, 400, 401 y 403.
- `04_*`: acceso protegido a Config Server, configuracion central, registros Eureka y caida/recuperacion de la API. Los tres BFF responden HTTP 503, abren el circuito y vuelven a HTTP 200 con circuito cerrado.
- `05_*`: compilacion de ocho modulos, construccion de imagenes, nueve contenedores saludables, OAuth2 en Docker, eventos JMS procesados, consumidor detenido/reiniciado y recuperacion del circuito web.

## Archivos complementarios

- `03_resultados_oauth2.json`, `04_resultados_cloud.json`, `05_resultados_docker_jms.json`: detalle estructurado de cada comprobacion.
- `04_caida_*.json`: respuestas reales de degradacion y estado del circuito.
- `04_eureka_registros.json`, `05_eureka.json`: servicios registrados.
- `05_compilacion_jms.log`: ultima compilacion Maven.
- `05_construccion_imagenes.log`, `05_imagenes_docker.txt`: construccion e imagenes de los ocho servicios.
- `05_estado_docker.txt`, `05_logs_docker.log`: estado y registros de los contenedores.
- `05_evento_*.json`: identificador, publicacion y registro real del procesamiento JMS.

Los resumenes deben leerse junto con sus registros: la prueba de fallos provoca desconexiones y mensajes de error deliberadamente. El resultado final de cada comprobacion figura en los TXT y JSON. Los secretos y cualquier JWT completo se ocultan al preparar el paquete.

No se incluyen capturas adicionales porque la pauta permite entregar salidas de consola. No se agregan resultados de carga, disponibilidad en nube ni pruebas de produccion que no fueron realizadas.

## Ajuste verificado durante la revision final

En los registros anteriores se detectaron errores HTTP 401 de replicacion: el servidor Eureka unico trataba su URL predeterminada como otro nodo. Se agregaron su hostname explicito y una URL propia coherente con ese hostname.

Los archivos anteriores se conservan como historial real de la ejecucion. La comprobacion posterior esta en [07_revision_eureka.txt](07_revision_eureka.txt), `07_resultados_eureka.json`, `07_eureka_corregido.log`, `07_eureka_registros.json` y `07_estado_docker.txt`. Verifica autenticacion, cinco servicios registrados UP, consultas de los tres BFF, ausencia de intentos de replicacion propios y nueve contenedores saludables.

Referencia del ajuste: https://docs.spring.io/spring-cloud-netflix/reference/4.3/spring-cloud-netflix.html#_standalone_mode
