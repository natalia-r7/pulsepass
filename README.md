# PulsePass

PulsePass es una plataforma para la gestión de eventos, artistas, usuarios y entradas.

El proyecto fue desarrollado con Java 21 y Spring Boot 4.1.1, utilizando PostgreSQL, Spring Data JPA, Flyway y Testcontainers.

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

Las relaciones principales son:

* Un lugar puede tener muchos eventos.
* Un evento puede tener varios artistas y un artista puede participar en varios eventos.
* Un usuario tiene un perfil.
* Un usuario puede tener varias entradas.
* Un evento puede tener varias entradas.

## Persistencia y migraciones

La estructura de la base de datos es administrada mediante Flyway.

Migraciones implementadas:

* `V1__create_schema.sql`: crea las tablas, relaciones, restricciones e índices principales.
* `V2__insert_initial_artists.sql`: registra los artistas iniciales.
* `V3__add_streaming_url_to_event.sql`: agrega el campo `streaming_url` a los eventos.

Hibernate utiliza:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Por lo tanto, Hibernate valida el esquema creado por Flyway sin modificarlo.

## Repositorios

Se implementaron repositorios utilizando `JpaRepository`:

* `VenueRepository`
* `EventRepository`
* `ArtistRepository`
* `UserRepository`
* `UserProfileRepository`
* `TicketRepository`

También se implementaron consultas mediante:

* Query Methods de Spring Data JPA.
* Consultas JPQL.
* Consultas para eventos, artistas y entradas.

## Pruebas

El proyecto utiliza Testcontainers para ejecutar las pruebas sobre una instancia real de PostgreSQL mediante Docker.

Las pruebas verifican, entre otros aspectos:

* Carga inicial de artistas.
* Búsqueda de artistas.
* Búsqueda de eventos por código.
* Eventos publicados ordenados por fecha.
* Búsqueda de eventos por lugar.
* Búsqueda de eventos por artista.
* Búsqueda de entradas por usuario y estado.
* Consultas JPQL.

Para ejecutar todas las pruebas:

```bash
./mvnw test
```

Resultado actual:

```text
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Compilación

Para limpiar y compilar el proyecto:

```bash
./mvnw clean compile
```

Para ejecutar las pruebas:

```bash
./mvnw test
```

## Requisitos

Para ejecutar las pruebas se necesita:

* Java 21
* Docker Desktop
* Maven Wrapper incluido en el proyecto

No es necesario tener PostgreSQL instalado localmente, ya que Testcontainers crea un contenedor PostgreSQL para las pruebas.
