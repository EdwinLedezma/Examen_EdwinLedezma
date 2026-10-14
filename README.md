# Examen — Huertos urbanos comunitarios

Java 17 · Spring Boot 3.5.16 · Spring Data JPA · PostgreSQL

La aplicación registra parcelas y mediciones de sensores. Valida y limpia los datos de entrada y devuelve errores personalizados en español.

## Servicios

| Servicio | Puerto | Responsabilidad |
|---|---|---|
| parcel-service | 8081 | Registrar, listar y consultar parcelas |
| monitoring-service | 8082 | Registrar lecturas y calcular alertas y recomendaciones |

Cada servicio tiene su propia base PostgreSQL. El detalle de una parcela consulta por HTTP al servicio de monitoreo para incluir su estado. Al registrar una lectura se verifica por HTTP que la parcela exista.

La consigna detalla Parcel y pide dos microservicios. Se eligió monitoreo como segundo servicio para completar la consulta de estado actual. Se implementan las tres operaciones solicitadas de parcelas; no se incluyen actualización ni eliminación.

## Ejecutar

Se necesita Docker Desktop encendido (o Docker Engine con Compose v2), conexión a Internet y los puertos 8081 y 8082 disponibles. Java y Maven se ejecutan dentro de Docker.

```bash
git clone https://github.com/EdwinLedezma/Examen_EdwinLedezma.git
cd Examen_EdwinLedezma
docker compose up --build -d
```

El primer inicio descarga imágenes y dependencias. Comprueba que las aplicaciones hayan iniciado:

```bash
docker compose ps
docker compose logs parcel-service monitoring-service
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

En PowerShell puedes usar `curl.exe`. Con Python 3 instalado:

```bash
python scripts/wait_for_services.py
python scripts/smoke_test.py
```

Detener conservando los datos: `docker compose down`. `docker compose down -v` también elimina los datos; úsalo únicamente si quieres reiniciar las bases de prueba.

Las credenciales incluidas son de desarrollo. Puedes copiar `.env.example` a `.env` y cambiar la contraseña antes del primer inicio. Cambiarla después no actualiza la contraseña de una base ya inicializada.

## API

| Método | URL | Respuesta |
|---|---|---|
| POST | `http://localhost:8081/api/v1/parcels` | 201, parcela creada y cabecera Location |
| GET | `http://localhost:8081/api/v1/parcels` | 200, listado |
| GET | `http://localhost:8081/api/v1/parcels/{id}` | 200, parcela y estado actual |
| POST | `http://localhost:8082/api/v1/readings` | 201, lectura registrada |
| GET | `http://localhost:8082/api/v1/readings/parcels/{id}/current` | 200, última lectura y alertas |

Ejemplo de registro:

```json
{
  "name": "Huerto Norte",
  "location": {"latitude": 28.63, "longitude": -106.08},
  "soilType": "LIMOSO",
  "ownerOrLead": "Edwin Ledezma"
}
```

Los tipos de suelo son `ARCILLOSO`, `LIMOSO` y `ARENOSO`. El servidor genera `id` y `createdAt`. Copia el UUID devuelto al consultar el detalle o registrar una lectura. Hay ejemplos completos en `examples/requests.http` y una colección importable en `examples/postman_collection.json`.

## Validación y sanitización

- Nombre y responsable: obligatorios, máximo 100 y 120 caracteres.
- Ubicación: objeto obligatorio; latitud entre -90 y 90, longitud entre -180 y 180.
- Lecturas: todos los campos son obligatorios. Humedad de 0 a 100 %, temperatura de -50 a 80 °C, pH de 0 a 14, N/P/K de 0 a 10000 mg/kg y AQI entero de 0 a 500.
- Fecha de medición: ISO-8601, no puede estar en el futuro.
- Los DTO se validan con Jakarta Validation y `@Valid`.
- Antes de validar, jsoup elimina HTML y el sanitizador normaliza Unicode y espacios. Si un texto queda vacío se rechaza.
- Los nombres de parcelas se comparan sin distinguir mayúsculas. Una restricción UNIQUE también impide duplicados concurrentes.
- JPA usa consultas parametrizadas. Los campos desconocidos en JSON se rechazan para impedir que el cliente asigne id o fecha de creación.

Las tablas se inicializan mediante `schema.sql` y Hibernate valida su estructura. En una evolución del proyecto se usarían migraciones versionadas.

## Manejo de errores

`GlobalExceptionHandler` devuelve timestamp, status, code, message, path y errores por campo. No devuelve SQL ni stack traces al cliente.

| Situación | HTTP | Código |
|---|---|---|
| Campos inválidos | 400 | VALIDACION_FALLIDA |
| JSON, UUID, enum o fecha mal formados | 400 | SOLICITUD_INVALIDA |
| Parcela inexistente | 404 | RECURSO_NO_ENCONTRADO |
| Nombre repetido | 409 | PARCELA_DUPLICADA |
| Conflicto en base de datos | 409 | CONFLICTO_DATOS |
| Otro servicio no disponible | 503 | SERVICIO_NO_DISPONIBLE |
| Base no disponible | 503 | BASE_NO_DISPONIBLE |
| Error inesperado | 500 | ERROR_INTERNO |

Las excepciones propias son `NotFoundException`, `DuplicateException` y `DependencyException`, derivadas de `ApiException`. Los clientes HTTP tienen límites de conexión de 2 segundos y lectura de 3 segundos.

## Estado y alertas

El estado puede ser `SIN_DATOS`, `NORMAL` o `ALERTA`. Se usa la lectura con mayor fecha de medición, con UUID como desempate determinista. El endpoint de monitoreo devuelve SIN_DATOS si no encuentra lecturas, incluso para un UUID no registrado; el detalle de parcelas sí comprueba que la parcela exista.

Las reglas de ejemplo generan alertas por humedad menor a 30 % o mayor a 80 %, temperatura fuera de 5 a 35 °C, pH fuera de 5.5 a 7.5, N menor a 20, P menor a 10, K menor a 50 mg/kg o AQI mayor a 100. Son umbrales didácticos, no recomendaciones agronómicas certificadas.

Una humedad de -1 % se rechaza; una de 10 % se guarda y genera una alerta. Las alertas se calculan al consultar la última muestra. No hay push en tiempo real, comprobación de caducidad de mediciones, frontend ni gestión de voluntarios en esta entrega.

## Pruebas

Con JDK 17 y Maven 3.9 o superior:

```bash
mvn clean verify
```

Hay 16 pruebas con MockMvc y H2 en modo PostgreSQL. El script smoke comprueba el flujo entre servicios. GitHub Actions compila, ejecuta las pruebas, inicia Docker Compose y ejecuta el script contra PostgreSQL real. El resultado de cada ejecución se consulta en la pestaña Actions.

## Ramas

`main` inicia con el cascarón de los dos servicios. Desde ahí se crea `develop` y después `feature/validacion-sanitizacion`. La implementación se integra primero en develop y luego en main mediante merges que conservan los commits.

La matriz que relaciona los requisitos con sus archivos está en `docs/CUMPLIMIENTO.md`.
