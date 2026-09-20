# PulsePass

PulsePass es una plataforma para la gestión de eventos, artistas, usuarios y entradas.

El proyecto fue desarrollado con **Java 21** y **Spring Boot 4.1.1**, utilizando **PostgreSQL**, **Spring Data JPA**, **Flyway** y **Testcontainers**.

## Tecnologías

* Java 21
* Spring Boot 4.1.1
* Spring Data JPA
* PostgreSQL
* Flyway
* Maven
* Testcontainers
* JUnit

## Modelo de datos

El proyecto contiene las siguientes entidades:

* `Venue`
* `Event`
* `Artist`
* `User`
* `UserProfile`
* `Ticket`

Además, se utilizan los siguientes enumerados:

* `EventCategory`
* `EventStatus`
* `TicketType`
* `TicketStatus`

### Relaciones principales

* Un `Venue` puede tener muchos `Event`.
* Un `Event` pertenece a un `Venue`.
* Un `Event` puede tener varios `Artist` y un `Artist` puede participar en varios `Event`, mediante la relación `event_artists`.
* Un `User` tiene un único `UserProfile`.
* Un `User` puede tener varios `Ticket`.
* Un `Event` puede tener varios `Ticket`.
* Cada `Ticket` pertenece a un `User` y a un `Event`.

## Persistencia y migraciones

La estructura de la base de datos es administrada mediante **Flyway**.

Las migraciones implementadas son:

* `V1__create_schema.sql`: crea las siete tablas principales, relaciones, restricciones e índices.
* `V2__insert_initial_artists.sql`: registra los artistas iniciales.
* `V3__add_streaming_url_to_event.sql`: agrega el campo `streaming_url` a la tabla `events`.

Entre las restricciones implementadas se encuentran:

* Códigos únicos para `Venue`, `Event`, `Artist`, `User` y `Ticket`.
* Capacidad de los lugares mayor que cero.
* Precio de los tickets mayor o igual a cero.
* Edad mínima de los eventos mayor o igual a cero.
* Clave foránea y restricción única para la relación `User` - `UserProfile`.
* Clave primaria compuesta para `event_artists`.
* Claves foráneas entre eventos, lugares, usuarios, artistas y tickets.
* Valores permitidos para las categorías, estados y tipos definidos mediante `CHECK`.

Hibernate utiliza:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Por lo tanto, **Flyway es responsable de crear y evolucionar el esquema**, mientras Hibernate únicamente valida que las entidades coincidan con la estructura de la base de datos.

## Repositorios

Se implementaron repositorios utilizando `JpaRepository`:

* `VenueRepository`
* `EventRepository`
* `ArtistRepository`
* `UserRepository`
* `UserProfileRepository`
* `TicketRepository`

Los repositorios utilizan:

* Query Methods de Spring Data JPA para consultas simples.
* Consultas JPQL para consultas que requieren joins, filtros combinados o agregaciones.

### Consultas principales

#### EventRepository

* Buscar un evento por `eventCode`.
* Buscar eventos por estado y ordenarlos por fecha.
* Buscar eventos por código del lugar.
* Buscar eventos asociados a un artista.
* Contar tickets con estado `PAID` de un evento.
* Buscar eventos por ciudad y artista.
* Buscar eventos publicados posteriores a una fecha, en una ciudad y cuyo artista contenga un texto, ignorando mayúsculas/minúsculas y evitando duplicados.

La consulta de recomendación utiliza `DISTINCT` y ordena los resultados cronológicamente.

#### TicketRepository

* Buscar un ticket por `ticketCode`.
* Buscar tickets por correo del usuario.
* Buscar tickets por correo y estado.
* Buscar tickets por correo y estado mediante JPQL.
* Buscar tickets `PAID` de un evento mediante su `eventCode`.
* Buscar tickets cuyo evento sea posterior a una fecha y ordenarlos cronológicamente.

#### UserRepository

* Buscar un usuario mediante su correo electrónico ignorando mayúsculas y minúsculas.

#### ArtistRepository

* Buscar un artista mediante su `stageName`.

## Pruebas de integración

El proyecto utiliza **Testcontainers** para ejecutar las pruebas sobre una instancia real de **PostgreSQL** mediante Docker.

Las pruebas utilizan:

```java
@Testcontainers
```

y un contenedor:

```text
postgres:16-alpine
```

De esta forma, las pruebas no dependen de tener PostgreSQL instalado localmente.

Actualmente se cuenta con **13 pruebas de integración**, que verifican diferentes aspectos del proyecto, entre ellos:

* Carga inicial de los artistas mediante Flyway.
* Búsqueda de artistas por nombre artístico.
* Búsqueda de eventos por código.
* Búsqueda de eventos publicados ordenados por fecha.
* Búsqueda de eventos por código de lugar.
* Búsqueda de eventos por artista.
* Búsqueda de tickets por usuario y estado.
* Consulta JPQL de tickets por usuario y estado.
* Consulta de tickets `PAID` mediante `eventCode`.
* Consulta de eventos recomendados por fecha, ciudad y texto del artista.
* Consulta de tickets cuyos eventos son posteriores a una fecha.
* Prueba de relaciones entre las entidades.
* Validación de una restricción `UNIQUE` utilizando `saveAndFlush()`.

Las consultas que requieren relaciones entre entidades se prueban utilizando una base de datos PostgreSQL real proporcionada por Testcontainers.

## Compilación y ejecución

Para limpiar y compilar el proyecto:

```bash
./mvnw clean compile
```

Para ejecutar todas las pruebas:

```bash
./mvnw clean test
```

La ejecución de las pruebas requiere que Docker Desktop esté instalado y en ejecución.

## Resultado de las pruebas

La última ejecución de:

```bash
./mvnw clean test
```

finalizó correctamente con:

```text
Tests run: 22, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS
```

La prueba de restricción `UNIQUE` genera intencionalmente una excepción de PostgreSQL al intentar guardar un registro duplicado. Esta excepción es capturada por la prueba, por lo que el resultado final es exitoso.

## Requisitos

Para ejecutar el proyecto y sus pruebas se necesita:

* Java 21
* Docker Desktop
* Maven Wrapper incluido en el proyecto

No es necesario tener PostgreSQL instalado localmente, ya que **Testcontainers crea automáticamente un contenedor PostgreSQL para las pruebas**.

## Alcance del proyecto

El alcance del proyecto se centra en:

* Modelado de entidades.
* Persistencia con Spring Data JPA.
* Creación y evolución del esquema mediante Flyway.
* Consultas mediante Query Methods y JPQL.
* Pruebas de integración con PostgreSQL y Testcontainers.
* Validación de restricciones de la base de datos.

No forman parte del alcance actual:

* Autenticación y autorización.
* Procesamiento de pagos.
* Frontend.
* Controladores REST.
* Servicios de negocio.
* Integración con proveedores externos.

## Estructura principal

```text
src/
├── main/
│   ├── java/
│   │   └── com/pulsepass/
│   │       ├── entity/
│   │       ├── repository/
│   │       └── PulsepassApplication.java
│   │
│   └── resources/
│       ├── db/
│       │   └── migration/
│       │       ├── V1__create_schema.sql
│       │       ├── V2__insert_initial_artists.sql
│       │       └── V3__add_streaming_url_to_event.sql
│       │
│       └── application.properties
│
└── test/
    └── java/
        └── com/pulsepass/
            └── PulsepassApplicationTests.java
```
