# Matriz de cumplimiento

| Requisito | Implementación | Cómo demostrarlo |
|---|---|---|
| Java 17+ y Spring Boot | Maven raíz y módulos | `mvn clean verify` |
| Ambos microservicios base | Parcelas + monitoreo | Dos aplicaciones independientes y Dockerfiles |
| Compose | compose.yaml | `docker compose up --build -d` |
| PostgreSQL, opción B | Dos bases independientes | schema.sql por servicio y volúmenes persistentes |
| Entidad Parcel | Parcel.java | UUID, nombre, GPS, suelo, responsable y fecha |
| POST /api/v1/parcels | ParcelController.create | Creación con 201 y Location |
| GET /api/v1/parcels | ParcelController.all | Lista de parcelas |
| GET /api/v1/parcels/{id} | ParcelController.detail | Datos de parcela y estado de sensores |
| Validación | DTO y Jakarta Validation | Campos vacíos, GPS inválido, enum y UUID |
| Sanitización | TextSanitizer | HTML y espacios normalizados antes de validar |
| Excepciones personalizadas | ApiException y tres subclases | 404, 409 y 503 |
| Mensajes personalizados | GlobalExceptionHandler y anotaciones | JSON y mensajes en español |
| Pruebas | MockMvc, H2 y script conjunto | tests y scripts/smoke_test.py |

## Guion para explicar la solución

1. La solicitud JSON se convierte en un DTO; su constructor limpia el texto.
2. Bean Validation comprueba campos obligatorios y límites.
3. El controlador entrega los datos al servicio de aplicación.
4. El servicio aplica reglas de negocio y usa el repositorio JPA.
5. PostgreSQL almacena los datos y aplica restricciones de integridad.
6. Si algo falla, una excepción viaja al manejador global, que devuelve HTTP y un mensaje claro.
7. El detalle de parcela consulta al otro microservicio; las lecturas originan alertas y recomendaciones según reglas didácticas.

No confundir datos inválidos con alertas: humedad de -1 % es inválida (400); humedad de 10 % es válida, se almacena y produce una alerta.
