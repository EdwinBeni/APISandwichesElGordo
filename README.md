# API Sandwiches El Gordo

API REST en Java y Spring Boot para la aplicacion Android de Sandwiches El Gordo. Consulta clientes por codigo, puntos, catalogo, recompensas e historial de pedidos en Oracle Database.

## Requisitos

- JDK 25.
- Acceso a la base Oracle con las tablas del proyecto.
- Wallet de Oracle Autonomous Database para la conexion mTLS actual.
- Maven Wrapper incluido: no es necesario instalar Maven por separado.

## Configuracion local actual

La aplicacion importa el archivo privado:

`C:/Users/PC/Documents/OracleWallets/config-elgordo.properties`

Este archivo debe contener las propiedades `db.url`, `db.user` y `db.password`. Hay una plantilla en `config-elgordo.example.properties`. Ajusta la ruta de `spring.config.import` en `src/main/resources/application.properties` si ejecutas el proyecto en otra computadora.

Extrae el wallet fuera del repositorio y configura su ubicacion mediante `TNS_ADMIN` en la URL JDBC. No subas el wallet ni el archivo privado de conexion a GitHub.

## Ejecutar en Windows

Desde esta carpeta:

```powershell
.\mvnw.cmd spring-boot:run
```

En NetBeans tambien puedes usar Run Maven > Other Goals con `spring-boot:run`.

La API utiliza el puerto 8081. La base de datos y sus datos no se incluyen en este repositorio.

## Consultas

| Metodo | Ruta | Uso |
| --- | --- | --- |
| GET | `/api/clientes/codigo/{codigo}/puntos` | Consultar puntos por codigo, por ejemplo CLI004 |
| GET | `/api/clientes/{id}/puntos` | Consultar puntos por ID interno |
| GET | `/api/productos` | Catalogo de productos activos |
| GET | `/api/recompensas` | Recompensas activas |
| GET | `/api/clientes/codigo/{codigo}/pedidos` | Historial del cliente |

Ejemplo local:

`http://localhost:8081/api/clientes/codigo/CLI004/puntos`

## Acceso desde Android

Android se conecta a la API REST; no se conecta directamente a Oracle. Para las pruebas remotas se utiliza Tailscale Funnel sobre el puerto 8081. La computadora debe permanecer encendida, con Internet, sin suspenderse y con Spring Boot y Funnel funcionando.

Esta version de demostracion permite consultar por codigo sin autenticacion de cliente. Antes de un uso real con datos personales, se debe incorporar autenticacion y autorizacion.
