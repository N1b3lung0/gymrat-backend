# CLAUDE.md

## Índice

| # | Sección |
|---|---------|
| 1 | [Rol y Contexto](#rol-y-contexto) |
| 2 | [Stack Tecnológico](#stack-tecnológico) |
| 3 | [Estructura Multi-Módulo Gradle](#estructura-multi-módulo-gradle) |
| 4 | [Gestión de Dependencias: Version Catalogs](#gestión-de-dependencias-version-catalogs) |
| 5 | [DDD: Lenguaje Ubicuo](#ddd-lenguaje-ubicuo) |
| 6 | [DDD: Entidades, Aggregates y Value Objects](#ddd-entidades-aggregates-y-value-objects) |
| 7 | [DDD: Factory Methods en Aggregates](#ddd-factory-methods-en-aggregates) |
| 8 | [DDD: Domain Events](#ddd-domain-events) |
| 9 | [CQRS: Separación Command / Query](#cqrs-separación-command--query) |
| 10 | [Gestión de Transacciones](#gestión-de-transacciones) |
| 11 | [Jerarquía de Excepciones](#jerarquía-de-excepciones) |
| 12 | [Migraciones de Base de Datos: Flyway](#migraciones-de-base-de-datos-flyway) |
| 13 | [N+1 y Fetch Strategy en JPA](#n1-y-fetch-strategy-en-jpa) |
| 14 | [Auditoría sin contaminar el dominio](#auditoría-createdat-updatedat-createdby-sin-contaminar-el-dominio) |
| 15 | [Optimistic Locking](#optimistic-locking-concurrencia-sin-bloqueos) |
| 16 | [Soft Delete](#soft-delete-convención-única) |
| 17 | [Seguridad: Spring Security sin contaminar el dominio](#seguridad-spring-security-sin-contaminar-el-dominio) |
| 18 | [Convenciones de API REST](#convenciones-de-api-rest) |
| 19 | [OpenAPI / Swagger](#openapi--swagger-convenciones-de-documentación) |
| 20 | [Observabilidad](#observabilidad) |
| 21 | [Calidad de Código: Tooling](#calidad-de-código-tooling) |
| 22 | [Configuración Externalizada (12-Factor)](#configuración-externalizada-12-factor) |
| 23 | [Desacoplamiento de Spring: Registro de Beans](#desacoplamiento-de-spring-registro-de-beans) |
| 24 | [Nomenclatura](#nomenclatura) |
| 25 | [Testing](#testing) |
| 26 | [Pipeline CI](#pipeline-ci-checks-obligatorios-antes-de-merge) |
| 27 | [Lo que NUNCA se debe hacer](#lo-que-nunca-se-debe-hacer) |
| 28 | [Respuestas de Claude](#respuestas-de-claude-cómo-trabajar-conmigo) |

---

## Rol y Contexto

Eres un asistente de desarrollo para un **Senior Backend Developer** experto en arquitectura de software.
Este proyecto aplica **Arquitectura Hexagonal**, **Clean Architecture**, **Clean Code**, **CQRS** y **Domain-Driven Design (DDD)**.
Asume siempre el máximo nivel de conocimiento técnico. No expliques conceptos básicos salvo que se te pida explícitamente.

---

## Stack Tecnológico

| Componente        | Versión   |
|-------------------|-----------|
| Java              | 25        |
| Spring Boot       | 4.0.3     |
| Gradle            | 9.4.0     |
| Gradle DSL        | Kotlin    |
| Gestión deps      | Version Catalogs (`gradle/libs.versions.toml`) |
| Migraciones BD    | Flyway    |
| Observabilidad    | Micrometer + OpenTelemetry |
| Calidad de código | Spotless + Checkstyle |
| Seguridad         | Spring Security 7     |

### Características de Java 25 a usar activamente

- **Records** para Value Objects, Commands, Queries y DTOs de respuesta.
- **Sealed classes + pattern matching** para modelar variantes del dominio (tipos algebraicos).
- **`switch` expressions con pattern matching** en lugar de cadenas if/else o instanceof.
- **Text blocks** para queries JPQL/SQL en tests, mensajes de error estructurados o JSON fixtures.
- **Virtual Threads (Project Loom)** — usar en lugar de `CompletableFuture` cuando la lógica es secuencial.
- **Sequenced Collections** donde aplique (`SequencedList`, `SequencedMap`).
- `var` en variables locales de tipo obvio; no usar en tipos genéricos complejos o lambdas.

---

## Estructura Multi-Módulo Gradle

El proyecto se organiza en módulos Gradle independientes. Las fronteras arquitectónicas se refuerzan **en compilación**: `domain` y `application` no pueden importar infraestructura aunque quisieran.

```
root/
├── gradle/
│   └── libs.versions.toml          ← Version Catalog centralizado
├── settings.gradle.kts             ← Declara todos los módulos
├── build.gradle.kts                ← Convenciones comunes (plugins, Java toolchain)
│
├── domain/                         ← Módulo: núcleo puro. CERO dependencias externas.
│   └── src/main/java/com/{ctx}/domain/
│       ├── model/                  ← Entidades, Aggregates, Value Objects
│       ├── service/                ← Domain Services
│       ├── event/                  ← Domain Events (sealed interfaces + records)
│       ├── exception/              ← Excepciones de dominio
│       └── repository/             ← Interfaces de repositorio (Output Ports de dominio)
│
├── application/                    ← Módulo: casos de uso. Depende solo de :domain
│   └── src/main/java/com/{ctx}/application/
│       ├── port/
│       │   ├── input/              ← Use Case interfaces — Command side y Query side separados
│       │   └── output/             ← Contratos de infraestructura (Driven Ports)
│       ├── command/                ← Command Handlers (CQRS write side)
│       ├── query/                  ← Query Handlers (CQRS read side)
│       ├── dto/                    ← Commands, Queries, Responses (Records)
│       └── mapper/                 ← Mappers dominio ↔ DTO (sin frameworks de mapeo)
│
└── infrastructure/                 ← Módulo: adaptadores. Depende de :application y :domain
    └── src/main/java/com/{ctx}/infrastructure/
        ├── adapter/
        │   ├── input/
        │   │   ├── rest/           ← Controllers REST + Request/Response DTOs
        │   │   └── messaging/      ← Consumers de eventos (Kafka, RabbitMQ…)
        │   └── output/
        │       ├── persistence/    ← JPA Adapters, Entities, Spring Repositories
        │       ├── messaging/      ← Producers de eventos + Outbox
        │       └── external/       ← Clientes HTTP externos
        └── config/                 ← @Configuration, registro de beans de aplicación
```

### `settings.gradle.kts`

```kotlin
rootProject.name = "my-app"
include(":domain", ":application", ":infrastructure")
```

### `application/build.gradle.kts`

```kotlin
dependencies {
    implementation(project(":domain"))   // única dependencia permitida
}
```

### `infrastructure/build.gradle.kts`

```kotlin
dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))
    // aquí van Spring Boot, JPA, Kafka, etc.
}
```

**Regla absoluta de dependencias:**
```
:infrastructure → :application → :domain
                                    ↑
                              (núcleo puro)
```

---

## Gestión de Dependencias: Version Catalogs

Todas las versiones se declaran en `gradle/libs.versions.toml`. **Nunca escribir versiones hardcodeadas en ningún `build.gradle.kts`.**

```toml
[versions]
java                 = "25"
spring-boot          = "4.0.3"
spring-dependency    = "1.1.x"
flyway               = "10.x"
mapstruct            = "1.6.x"
lombok               = "1.18.x"
archunit             = "1.3.x"
testcontainers       = "1.20.x"
micrometer-tracing   = "1.x"
spotless             = "6.x"
checkstyle           = "10.x"

[libraries]
spring-boot-web          = { module = "org.springframework.boot:spring-boot-starter-web" }
spring-boot-jpa          = { module = "org.springframework.boot:spring-boot-starter-data-jpa" }
spring-boot-validation   = { module = "org.springframework.boot:spring-boot-starter-validation" }
spring-boot-actuator     = { module = "org.springframework.boot:spring-boot-starter-actuator" }
spring-boot-test         = { module = "org.springframework.boot:spring-boot-starter-test" }
flyway-core              = { module = "org.flywaydb:flyway-core",                       version.ref = "flyway" }
flyway-postgres          = { module = "org.flywaydb:flyway-database-postgresql",        version.ref = "flyway" }
lombok                   = { module = "org.projectlombok:lombok",                       version.ref = "lombok" }
mapstruct                = { module = "org.mapstruct:mapstruct",                        version.ref = "mapstruct" }
mapstruct-processor      = { module = "org.mapstruct:mapstruct-processor",              version.ref = "mapstruct" }
archunit                 = { module = "com.tngtech.archunit:archunit-junit5",           version.ref = "archunit" }
testcontainers-junit     = { module = "org.testcontainers:junit-jupiter",               version.ref = "testcontainers" }
testcontainers-pg        = { module = "org.testcontainers:postgresql",                  version.ref = "testcontainers" }
micrometer-otlp          = { module = "io.micrometer:micrometer-registry-otlp",        version.ref = "micrometer-tracing" }
micrometer-tracing-otel  = { module = "io.micrometer:micrometer-tracing-bridge-otel",  version.ref = "micrometer-tracing" }

[bundles]
spring-web      = ["spring-boot-web", "spring-boot-validation", "spring-boot-actuator"]
observability   = ["micrometer-otlp", "micrometer-tracing-otel"]
testing         = ["spring-boot-test", "archunit"]
testcontainers  = ["testcontainers-junit", "testcontainers-pg"]

[plugins]
spring-boot       = { id = "org.springframework.boot",        version.ref = "spring-boot" }
spring-dependency = { id = "io.spring.dependency-management", version.ref = "spring-dependency" }
spotless          = { id = "com.diffplug.spotless",           version.ref = "spotless" }
```

---

## DDD: Lenguaje Ubicuo

El Lenguaje Ubicuo es **la regla más importante** del proyecto. Todo nombre en el código debe provenir del glosario del dominio acordado con el negocio, no de la tecnología.

### Reglas

- **Glosario primero.** Antes de crear una clase de dominio, el término debe estar definido y acordado. Si no tiene nombre en el negocio, no tiene clase en el dominio.
- **Un concepto, un nombre.** No mezclar sinónimos: si el negocio dice `Order`, el código dice `Order` — no `Purchase`, no `Cart`, no `Transaction`.
- **El dominio manda.** Si el negocio cambia el nombre de un concepto, se refactoriza el código.
- **Sin jerga técnica en el dominio.** `OrderManager`, `OrderProcessor`, `OrderHelper`, `OrderUtil` no son nombres de dominio — son síntomas de lógica de negocio mal ubicada.
- **Los métodos hablan el idioma del negocio.** `order.ship()`, `order.cancel(reason)`, `order.applyDiscount(coupon)` — no `order.setStatus(SHIPPED)` ni `order.updateData(dto)`.

```java
// ❌ Jerga técnica infiltrada en el dominio
public class OrderProcessor {
    public void processOrderData(OrderDTO dto) { ... }
}

// ✅ Lenguaje del negocio
public class Order {
    public void ship(TrackingNumber trackingNumber) { ... }
    public void cancel(CancellationReason reason) { ... }
}
```

---

## DDD: Entidades, Aggregates y Value Objects

### Entidades y Aggregates

- Las entidades tienen **identidad** modelada como Value Object, nunca `UUID` o `Long` en crudo.
- Los Aggregates protegen sus invariantes — toda modificación de estado pasa por métodos del Aggregate Root.
- Solo el Aggregate Root es persistido/recuperado por el repositorio. Nunca persistir entidades hijas directamente.
- No exponer colecciones internas mutables: devolver `List.copyOf()` o `Collections.unmodifiableList()`.

### Value Objects

Siempre inmutables. Usar `record`. Igualdad basada en valor. Validación en constructor.

```java
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        Objects.requireNonNull(amount, "amount required");
        Objects.requireNonNull(currency, "currency required");
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new NegativeMoneyException(amount);
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency))
            throw new CurrencyMismatchException(this.currency, other.currency);
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

---

## DDD: Factory Methods en Aggregates

### Regla: constructor privado para reconstitución, `create()` estático para negocio

El Aggregate tiene dos formas de instanciarse con propósitos completamente distintos:

- **`create()` estático** — punto de entrada de negocio. Aplica invariantes, genera el ID, emite el Domain Event inicial. Es el único camino para crear un Aggregate nuevo.
- **Constructor de paquete/privado** — exclusivo para el mapper de persistencia. Reconstituye el Aggregate desde la base de datos sin disparar lógica de negocio ni eventos.

```java
// domain/model/Order.java
public class Order {

    private final OrderId id;
    private final CustomerId customerId;
    private OrderStatus status;
    private final List<OrderLine> lines;
    private final List<OrderEvent> domainEvents = new ArrayList<>();

    // ✅ Factory Method — único punto de entrada de negocio
    public static Order create(CustomerId customerId, List<OrderLineRequest> lineRequests) {
        Objects.requireNonNull(customerId, "customerId required");
        if (lineRequests == null || lineRequests.isEmpty())
            throw new EmptyOrderException();

        var order = new Order(OrderId.generate(), customerId, OrderStatus.PENDING, new ArrayList<>());
        lineRequests.forEach(req -> order.addLine(req.productId(), req.quantity(), req.unitPrice()));
        order.domainEvents.add(new OrderPlaced(order.id, customerId, Instant.now()));
        return order;
    }

    // ✅ Constructor de reconstitución — solo para el mapper de persistencia
    Order(OrderId id, CustomerId customerId, OrderStatus status, List<OrderLine> lines) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.lines = new ArrayList<>(lines);
    }
}
```

```java
// infrastructure/adapter/output/persistence/mapper/OrderPersistenceMapper.java
@Component
public class OrderPersistenceMapper {

    public Order toDomain(OrderEntity entity) {
        // ✅ Usa el constructor de reconstitución — sin eventos, sin validaciones de creación
        return new Order(
            OrderId.of(entity.getId()),
            CustomerId.of(entity.getCustomerId()),
            OrderStatus.valueOf(entity.getStatus()),
            entity.getLines().stream().map(this::toLinesDomain).toList()
        );
    }
}
```

### Cuándo usar un `Builder` en lugar de Factory Method

Solo si el Aggregate tiene **más de 4-5 parámetros opcionales** en su creación. En ese caso, el Builder también es estático y anidado en la clase del Aggregate, nunca en una clase separada.

```java
// ✅ Builder anidado para Aggregates con muchos parámetros opcionales
var order = Order.builder()
    .customerId(customerId)
    .shippingAddress(address)
    .coupon(coupon)         // opcional
    .notes(notes)           // opcional
    .build();               // build() aplica invariantes y emite evento
```

---

## DDD: Domain Events

### Modelado

Los Domain Events son hechos del pasado. Siempre inmutables, siempre con timestamp.

```java
// domain/event/OrderEvent.java
public sealed interface OrderEvent permits OrderPlaced, OrderShipped, OrderCancelled {}

public record OrderPlaced(OrderId orderId, CustomerId customerId, Instant occurredOn) implements OrderEvent {}
public record OrderShipped(OrderId orderId, TrackingNumber trackingNumber, Instant occurredOn) implements OrderEvent {}
public record OrderCancelled(OrderId orderId, CancellationReason reason, Instant occurredOn) implements OrderEvent {}
```

### Ciclo de vida: producción y recolección

Los eventos se **acumulan en el Aggregate Root** y se **publican en la capa de aplicación** tras persistir. El dominio nunca publica eventos directamente.

```java
// domain/model/Order.java
public class Order {
    private final List<OrderEvent> domainEvents = new ArrayList<>();

    public void ship(TrackingNumber trackingNumber) {
        this.status = OrderStatus.SHIPPED;
        domainEvents.add(new OrderShipped(this.id, trackingNumber, Instant.now()));
    }

    public List<OrderEvent> pullDomainEvents() {
        var events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }
}
```

```java
// application/command/ShipOrderHandler.java
public class ShipOrderHandler implements ShipOrderUseCase {

    private final OrderRepositoryPort repository;
    private final DomainEventPublisherPort eventPublisher;

    @Override
    public void execute(ShipOrderCommand command) {
        Order order = repository.findById(command.orderId()).orElseThrow(...);
        order.ship(command.trackingNumber());
        repository.save(order);
        order.pullDomainEvents().forEach(eventPublisher::publish);  // siempre después del save
    }
}
```

### Estrategia de publicación

- **Síncrona (dentro de la transacción):** Output Port `DomainEventPublisherPort`. La implementación decide el mecanismo (Spring Events, Kafka, etc.).
- **Outbox Pattern** para eventos críticos (garantía de al menos una entrega): persistir el evento en la misma transacción que el aggregate, publicar asíncronamente con un poller o CDC.
- **Nunca** publicar eventos antes de confirmar la persistencia del aggregate.

---

## CQRS: Separación Command / Query

### Principio

- **Command side:** modifica estado, usa el modelo de dominio rico, trabaja con Aggregates.
- **Query side:** solo lectura, usa proyecciones planas optimizadas (JDBC, JPQL con `@Query`, vistas), sin pasar por el modelo de dominio.
- Los dos lados comparten la misma base de datos (CQRS lógico), no infraestructura separada, salvo que escale a necesitarlo.

### Estructura en `:application`

```
application/
├── port/input/
│   ├── command/               ← Un interface por caso de uso de escritura
│   │   ├── PlaceOrderUseCase.java
│   │   └── ShipOrderUseCase.java
│   └── query/                 ← Un interface por caso de uso de lectura
│       ├── GetOrderByIdUseCase.java
│       └── ListOrdersByCustomerUseCase.java
├── command/                   ← Handlers de escritura
│   ├── PlaceOrderHandler.java
│   └── ShipOrderHandler.java
└── query/                     ← Handlers de lectura
    ├── GetOrderByIdHandler.java
    └── ListOrdersByCustomerHandler.java
```

### Query side: sin Aggregates

Los Query Handlers no cargan Aggregates. Trabajan directamente con proyecciones.

```java
// application/port/output/OrderQueryPort.java
public interface OrderQueryPort {
    Optional<OrderDetailView> findDetailById(OrderId id);
    Page<OrderSummaryView> findByCustomer(CustomerId customerId, Pageable pageable);
}

// application/dto/OrderDetailView.java
public record OrderDetailView(String orderId, String customerName, BigDecimal total,
                               String status, List<OrderLineView> lines) {}
```

La implementación en infraestructura puede usar JDBC, `@Query` con proyecciones de Spring Data o cualquier mecanismo optimizado, sin tocar el modelo de dominio.

---

## Gestión de Transacciones

`@Transactional` no existe en el dominio ni en la capa de aplicación. Toda gestión transaccional vive en infraestructura.

### Patrón: decorador transaccional en infraestructura

```java
// infrastructure/config/OrderConfig.java
@Configuration
public class OrderConfig {

    @Bean
    public PlaceOrderUseCase placeOrderUseCase(OrderRepositoryPort repository,
                                               DomainEventPublisherPort publisher,
                                               MeterRegistry meterRegistry,
                                               PlatformTransactionManager txManager) {
        var handler = new PlaceOrderHandler(repository, publisher, meterRegistry);
        return new TransactionalPlaceOrderUseCase(handler, txManager);
    }
}
```

```java
// infrastructure/adapter/output/persistence/TransactionalPlaceOrderUseCase.java
public class TransactionalPlaceOrderUseCase implements PlaceOrderUseCase {

    private final PlaceOrderUseCase delegate;
    private final PlatformTransactionManager txManager;

    @Override
    public OrderId execute(PlaceOrderCommand command) {
        return new TransactionTemplate(txManager).execute(status -> delegate.execute(command));
    }
}
```

**Alternativa aceptable:** `@Transactional` únicamente en métodos del **JPA Adapter** cuando la transacción no necesita abarcar más de una operación de persistencia.

**Nunca:** `@Transactional` en clases de `application/` ni de `domain/`.

---

## Jerarquía de Excepciones

Una jerarquía consistente garantiza que el `GlobalExceptionHandler` mapee siempre el HTTP status correcto sin ambigüedad.

### Categorías y HTTP status mapping

```
DomainException (RuntimeException)
├── NotFoundException          → 404 Not Found
├── BusinessRuleViolationException → 422 Unprocessable Entity
├── ConflictException          → 409 Conflict
└── UnauthorizedException      → 403 Forbidden

ApplicationException (RuntimeException)
└── UseCaseValidationException → 400 Bad Request

InfrastructureException (RuntimeException)
└── ExternalServiceException   → 502 Bad Gateway
```

### Implementación base en el dominio

```java
// domain/exception/DomainException.java
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) { super(message); }
    protected DomainException(String message, Throwable cause) { super(message, cause); }
}

// domain/exception/NotFoundException.java
public abstract class NotFoundException extends DomainException {
    protected NotFoundException(String message) { super(message); }
}

// domain/exception/BusinessRuleViolationException.java
public abstract class BusinessRuleViolationException extends DomainException {
    protected BusinessRuleViolationException(String message) { super(message); }
}

// domain/exception/ConflictException.java
public abstract class ConflictException extends DomainException {
    protected ConflictException(String message) { super(message); }
}
```

### Excepciones concretas de dominio extienden la categoría correcta

```java
// domain/exception/OrderNotFoundException.java
public class OrderNotFoundException extends NotFoundException {
    public OrderNotFoundException(OrderId id) {
        super("Order not found: " + id);
    }
}

// domain/exception/InsufficientStockException.java
public class InsufficientStockException extends BusinessRuleViolationException {
    public InsufficientStockException(ProductId productId, int requested, int available) {
        super("Insufficient stock for product %s: requested=%d available=%d"
            .formatted(productId, requested, available));
    }
}

// domain/exception/OrderConcurrentModificationException.java
public class OrderConcurrentModificationException extends ConflictException {
    public OrderConcurrentModificationException(OrderId id, Throwable cause) {
        super("Order modified concurrently: " + id);
    }
}
```

### GlobalExceptionHandler mapeado por categoría

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleViolationException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, ex);
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        return problem(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedException ex) {
        return problem(HttpStatus.FORBIDDEN, ex);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ProblemDetail handleExternalService(ExternalServiceException ex) {
        log.error("External service failure", ex);
        return problem(HttpStatus.BAD_GATEWAY, ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, "Validation failed");
        problem.setProperty("violations", ex.getBindingResult().getFieldErrors().stream()
            .map(e -> Map.of("field", e.getField(), "message", e.getDefaultMessage()))
            .toList());
        return problem;
    }

    private ProblemDetail problem(HttpStatus status, Exception ex) {
        var problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problem.setType(URI.create("https://api.myapp.com/errors/" +
            ex.getClass().getSimpleName().toLowerCase().replace("exception", "")));
        return problem;
    }
}
```

---

## Migraciones de Base de Datos: Flyway

- **`ddl-auto` siempre en `validate` o `none` en producción.** Nunca `create`, `create-drop` ni `update` fuera de tests.
- Todas las migraciones en `infrastructure/src/main/resources/db/migration/`.
- Nomenclatura: `V{version}__{descripcion_en_snake_case}.sql` → `V1__create_orders_table.sql`.
- Las migraciones son **inmutables** una vez aplicadas. Para corregir errores, nueva migración.
- En tests con Testcontainers, Flyway se ejecuta automáticamente — no usar `@Sql` para esquema.

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: false
  jpa:
    hibernate:
      ddl-auto: validate
```

---

## Convenciones de API REST

### Problem Details (RFC 9457)

Todos los errores devuelven `application/problem+json`. Usar `ProblemDetail` de Spring 6+.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail handleOrderNotFound(OrderNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create("https://api.myapp.com/errors/order-not-found"));
        problem.setTitle("Order Not Found");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, "Validation failed");
        problem.setProperty("violations", ex.getBindingResult().getFieldErrors().stream()
                .map(e -> Map.of("field", e.getField(), "message", e.getDefaultMessage()))
                .toList());
        return problem;
    }
}
```

### Versionado de API

- Versionar en la URL: `/api/v1/orders`, `/api/v2/orders`.
- Una versión por `@RequestMapping` de clase en el controller.
- Nunca versionar por header o query param.
- Mantener la versión anterior al menos un ciclo de release tras deprecarla.

### Paginación

Nunca devolver un array crudo para colecciones paginadas. Respuesta estandarizada:

```java
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }
}
```

### Convenciones generales

- `POST` para crear → `201 Created` + `Location` header.
- `PUT` para reemplazar completo, `PATCH` para actualización parcial.
- `DELETE` exitoso → `204 No Content`.
- IDs en la URL siempre como `String` (UUID como string), nunca claves numéricas internas.
- Validaciones Bean Validation únicamente en DTOs de infraestructura (request), nunca en dominio.

---

## Observabilidad

### Structured Logging

- **Nunca concatenar strings en logs.** Usar siempre parámetros con `{}`.
- Logs en **formato JSON en producción** (Logback + `logstash-logback-encoder`).
- Incluir siempre `traceId` y `spanId` (propagados automáticamente por Micrometer Tracing vía MDC).
- Nivel por defecto `INFO`. `DEBUG` solo en desarrollo, nunca habilitado por defecto en producción.

```java
// ✅
log.info("Order placed orderId={} customerId={}", order.getId(), command.customerId());

// ❌
log.info("Order " + orderId + " placed for customer " + customerId);
```

### Métricas con Micrometer

- Registrar métricas de negocio en los Command/Query Handlers, no en infraestructura.
- `MeterRegistry` inyectado como dependencia desde config.
- Nombrar métricas en snake_case con prefijo de contexto: `orders.placed.total`, `orders.cancelled.total`.

```java
public class PlaceOrderHandler implements PlaceOrderUseCase {
    private final MeterRegistry meterRegistry;

    @Override
    public OrderId execute(PlaceOrderCommand command) {
        var orderId = // ... lógica
        meterRegistry.counter("orders.placed.total").increment();
        return orderId;
    }
}
```

### Tracing con OpenTelemetry

- Configurar exclusivamente en `infrastructure/config/`.
- Los spans se propagan automáticamente con Micrometer Tracing + Spring Boot Actuator.
- Añadir atributos de negocio relevantes al span activo en los handlers.

### Actuator

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
  endpoint:
    health:
      show-details: when-authorized
```

---

## Calidad de Código: Tooling

### Spotless (formato automático)

```kotlin
spotless {
    java {
        googleJavaFormat("1.22.0")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        ktlint()
    }
}
```

- `./gradlew spotlessCheck` en CI — falla el build si el formato no es correcto.
- `./gradlew spotlessApply` para formatear localmente antes de commit.

### Checkstyle

Reglas mínimas a activar:
- Sin imports con `*`.
- Javadoc obligatorio en interfaces públicas de puertos (`port/input/` y `port/output/`).
- Longitud máxima de línea: 120 caracteres.
- Sin bloques `catch` vacíos.

### ArchUnit: Tests de Arquitectura Obligatorios

```java
@AnalyzeClasses(packages = "com.{ctx}")
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainIsIsolated =
        noClasses().that().resideInAPackage("..domain..")
                   .should().dependOnClassesThat()
                   .resideInAnyPackage("org.springframework..", "jakarta.persistence..",
                                       "..application..", "..infrastructure..");

    @ArchTest
    static final ArchRule applicationDoesNotUseInfrastructure =
        noClasses().that().resideInAPackage("..application..")
                   .should().dependOnClassesThat()
                   .resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule springAnnotationsOnlyInInfrastructure =
        noClasses().that().resideInAnyPackage("..domain..", "..application..")
                   .should().beAnnotatedWith(Service.class)
                   .orShould().beAnnotatedWith(Component.class)
                   .orShould().beAnnotatedWith(Repository.class)
                   .orShould().beAnnotatedWith(Transactional.class);

    @ArchTest
    static final ArchRule commandHandlersImplementCommandPorts =
        classes().that().resideInAPackage("..application.command..")
                 .should().implement(resideInAPackage("..application.port.input.command.."));

    @ArchTest
    static final ArchRule queryHandlersImplementQueryPorts =
        classes().that().resideInAPackage("..application.query..")
                 .should().implement(resideInAPackage("..application.port.input.query.."));
}
```

---

## Desacoplamiento de Spring: Registro de Beans

El dominio y la aplicación **no tienen ninguna anotación Spring**. Todo se registra desde `infrastructure/config/`.

```java
@Configuration
public class OrderConfig {

    @Bean
    public PlaceOrderUseCase placeOrderUseCase(OrderRepositoryPort repository,
                                               DomainEventPublisherPort publisher,
                                               MeterRegistry meterRegistry,
                                               PlatformTransactionManager txManager) {
        var handler = new PlaceOrderHandler(repository, publisher, meterRegistry);
        return new TransactionalPlaceOrderUseCase(handler, txManager);
    }

    @Bean
    public GetOrderByIdUseCase getOrderByIdUseCase(OrderQueryPort queryPort) {
        return new GetOrderByIdHandler(queryPort);
    }
}
```

---

## Nomenclatura

| Elemento                  | Convención                                   | Ejemplo                          |
|---------------------------|----------------------------------------------|----------------------------------|
| Entidad / Aggregate Root  | Sustantivo singular del dominio              | `Order`, `Product`               |
| Value Object              | Sustantivo descriptivo del concepto          | `Money`, `TrackingNumber`        |
| Domain Event              | Sustantivo + participio pasado               | `OrderPlaced`, `OrderShipped`    |
| Domain Service            | Sustantivo + "Service"                       | `PricingService`                 |
| Domain Exception          | Sustantivo + "Exception"                     | `InsufficientStockException`     |
| Command                   | Verbo imperativo + "Command"                 | `PlaceOrderCommand`              |
| Query                     | "Find/Get/List" + sustantivo + "Query"       | `FindOrderByIdQuery`             |
| Command Use Case (port)   | Verbo imperativo + "UseCase"                 | `PlaceOrderUseCase`              |
| Query Use Case (port)     | "Get/List/Find" + sustantivo + "UseCase"     | `GetOrderByIdUseCase`            |
| Command Handler           | Verbo + "Handler"                            | `PlaceOrderHandler`              |
| Query Handler             | "Get/List/Find" + sustantivo + "Handler"     | `GetOrderByIdHandler`            |
| Response / View           | Sustantivo + "Response" / "View"             | `OrderDetailView`                |
| Output Port (repo)        | Sustantivo + "RepositoryPort"                | `OrderRepositoryPort`            |
| Output Port (query)       | Sustantivo + "QueryPort"                     | `OrderQueryPort`                 |
| Output Port (events)      | Sustantivo + "PublisherPort"                 | `DomainEventPublisherPort`       |
| JPA Entity                | Sustantivo + "Entity"                        | `OrderEntity`                    |
| Spring Data Repository    | "Spring" + Sustantivo + "Repository"         | `SpringOrderRepository`          |
| JPA Adapter               | Sustantivo + "JpaAdapter"                    | `OrderJpaAdapter`                |
| REST Controller           | Sustantivo + "Controller"                    | `OrderController`                |
| Mapper de persistencia    | Sustantivo + "PersistenceMapper"             | `OrderPersistenceMapper`         |
| @Configuration de beans   | Sustantivo + "Config"                        | `OrderConfig`                    |

---

## Testing

### Pirámide de Tests

```
           /‾‾‾‾‾‾‾‾‾‾‾\
          /  E2E / IT    \      ← Testcontainers + Spring Boot Test (pocos, lentos)
         /‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾\
        /  Integration     \   ← @WebMvcTest, @DataJpaTest (capa a capa)
       /‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾\
      /   Unit Tests         \  ← JUnit 5 + Mockito (mayoría — dominio sin mocks)
     /‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾\
    /  Architecture Tests      \ ← ArchUnit (siempre en CI)
   /‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾\
```

### Convenciones

- Patrón **Given / When / Then** con comentarios en todos los tests.
- Nombre: `should{Resultado}_when{Condicion}()`.
- Tests de dominio **sin Mockito** — el dominio es puro y testeable sin mocks.
- Tests de Command/Query Handlers mockean los Output Ports.
- `@WebMvcTest` para controllers — nunca levantar el contexto completo para tests REST.
- `@DataJpaTest` + Testcontainers para los JPA Adapters.
- Nunca `@SpringBootTest` para tests unitarios.

---

## Seguridad: Spring Security sin contaminar el dominio

El dominio y la aplicación **nunca conocen a Spring Security**. El contexto de seguridad es un detalle de infraestructura.

### Principio: el dominio recibe identidad, no contexto de seguridad

El dominio trabaja con Value Objects de identidad propios (`UserId`, `TenantId`), nunca con `Authentication`, `Principal`, `SecurityContext` ni `UserDetails`.

```java
// ❌ Spring Security infiltrado en el dominio
public class OrderService {
    public void placeOrder(PlaceOrderCommand cmd) {
        var user = SecurityContextHolder.getContext().getAuthentication(); // NUNCA
    }
}

// ✅ El dominio recibe lo que necesita como parámetro
public record PlaceOrderCommand(CustomerId customerId, List<OrderLineCommand> lines) {}
```

### Patrón: resolver la identidad en el adaptador de entrada

El controller (infraestructura) extrae la identidad del `SecurityContext` y la convierte a un Value Object del dominio antes de construir el Command o Query.

```java
// infrastructure/adapter/input/rest/OrderController.java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final AuthenticatedUserResolver userResolver;  // helper de infraestructura

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<OrderResponse> place(@Valid @RequestBody PlaceOrderRequest request,
                                               Authentication authentication) {
        var customerId = userResolver.resolveCustomerId(authentication);  // conversión aquí
        var command = new PlaceOrderCommand(customerId, request.lines());
        var orderId = placeOrderUseCase.execute(command);
        return ResponseEntity.created(URI.create("/api/v1/orders/" + orderId)).build();
    }
}
```

```java
// infrastructure/adapter/input/rest/AuthenticatedUserResolver.java
@Component
public class AuthenticatedUserResolver {

    public CustomerId resolveCustomerId(Authentication authentication) {
        var subject = (String) authentication.getPrincipal();
        return CustomerId.of(UUID.fromString(subject));
    }
}
```

### Autorización: en infraestructura, no en el dominio

- Usar `@PreAuthorize` o `SecurityFilterChain` en la capa de infraestructura para autorización basada en roles.
- La autorización basada en **reglas de negocio** (p.ej. "solo el propietario puede cancelar su pedido") sí puede vivir en el dominio como lógica de negocio, pero recibiendo el `UserId` como parámetro, sin tocar `SecurityContext`.

```java
// ✅ Autorización de negocio en el dominio
public class Order {
    public void cancel(UserId requestedBy, CancellationReason reason) {
        if (!this.customerId.equals(requestedBy) && !this.assignedOperatorId.equals(requestedBy)) {
            throw new UnauthorizedCancellationException(requestedBy, this.id);
        }
        // ... lógica de cancelación
    }
}
```

### Configuración de Spring Security

- Toda la configuración de `SecurityFilterChain`, JWT, OAuth2, etc. en `infrastructure/config/SecurityConfig.java`.
- Los beans de Spring Security **nunca** se inyectan fuera de `infrastructure/`.

```java
// infrastructure/config/SecurityConfig.java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .build();
    }
}
```

---

## Diseño de Aggregates: Reglas de Oro

Estas reglas previenen los problemas más frecuentes en proyectos DDD a medida que el modelo crece.

### Regla 1: Referenciar otros Aggregates solo por ID

Un Aggregate nunca contiene una referencia directa a otro Aggregate. Solo guarda su ID como Value Object. La carga de Aggregates relacionados es responsabilidad de la capa de aplicación.

```java
// ❌ Referencia directa a otro Aggregate
public class Order {
    private Customer customer;  // Order no debe cargar Customer completo
}

// ✅ Referencia por ID
public class Order {
    private final CustomerId customerId;  // solo el ID
}
```

### Regla 2: Mantener los Aggregates pequeños

Un Aggregate debe contener **solo lo necesario para proteger sus invariantes**. Si un campo o colección no participa en ninguna regla de negocio del Aggregate, probablemente no pertenece ahí.

Señales de que un Aggregate es demasiado grande:
- Las transacciones tardan más de lo esperado.
- Los tests requieren construir objetos muy complejos.
- Múltiples casos de uso distintos modifican siempre el mismo Aggregate.

### Regla 3: Un Aggregate por transacción

Una transacción modifica **un solo Aggregate Root**. Si dos Aggregates deben cambiar juntos, la coordinación ocurre mediante Domain Events y consistencia eventual.

```java
// ❌ Dos Aggregates en la misma transacción
repository.save(order);
repository.save(inventory);  // transacción que abarca dos Aggregates

// ✅ Order emite evento, Inventory reacciona de forma eventual
order.confirm();
repository.save(order);
order.pullDomainEvents().forEach(eventPublisher::publish);
// InventoryReservationHandler escucha OrderConfirmed y actualiza Inventory en su propia transacción
```

### Regla 4: Los invariantes del Aggregate se protegen siempre en el Aggregate Root

Nunca permitir que código externo al Aggregate manipule directamente su estado interno o sus colecciones hijas.

```java
// ❌ El handler manipula directamente el estado interno
order.getLines().add(new OrderLine(...));   // rompe el encapsulamiento

// ✅ El Aggregate Root controla sus invariantes
order.addLine(productId, quantity, unitPrice);  // el Aggregate valida y modifica
```

---

## Idempotencia

Toda operación que pueda ser reintentada (por un cliente HTTP, por un consumer de mensajería, por Resilience4j) debe ser idempotente o estar protegida contra ejecuciones duplicadas.

### Idempotencia en Commands de API REST

Usar una clave de idempotencia enviada por el cliente en el header `Idempotency-Key`. El adaptador de entrada la extrae y la incluye en el Command. La capa de aplicación delega la comprobación a un Output Port.

```java
// infrastructure/adapter/input/rest/OrderController.java
@PostMapping
public ResponseEntity<OrderResponse> place(
        @Valid @RequestBody PlaceOrderRequest request,
        @RequestHeader("Idempotency-Key") UUID idempotencyKey,
        Authentication authentication) {
    var command = new PlaceOrderCommand(customerId, request.lines(), idempotencyKey);
    // ...
}
```

```java
// application/port/output/IdempotencyPort.java
public interface IdempotencyPort {
    Optional<OrderId> findProcessedCommand(UUID idempotencyKey);
    void markAsProcessed(UUID idempotencyKey, OrderId result);
}
```

```java
// application/command/PlaceOrderHandler.java
public class PlaceOrderHandler implements PlaceOrderUseCase {

    private final IdempotencyPort idempotency;

    @Override
    public OrderId execute(PlaceOrderCommand command) {
        return idempotency.findProcessedCommand(command.idempotencyKey())
                .orElseGet(() -> {
                    var orderId = processNewOrder(command);
                    idempotency.markAsProcessed(command.idempotencyKey(), orderId);
                    return orderId;
                });
    }
}
```

### Idempotencia en consumers de mensajería

Todo Message Consumer debe protegerse frente a mensajes duplicados. Patrón recomendado: tabla de mensajes procesados con el `messageId` como clave única.

```java
// infrastructure/adapter/input/messaging/OrderEventConsumer.java
@Component
public class OrderEventConsumer {

    private final ProcessedMessageRepository processedMessages;
    private final ShipOrderUseCase shipOrderUseCase;

    public void onOrderReadyToShip(OrderReadyToShip event, String messageId) {
        if (processedMessages.existsById(messageId)) {
            log.info("Duplicate message ignored messageId={}", messageId);
            return;
        }
        shipOrderUseCase.execute(new ShipOrderCommand(event.orderId(), event.trackingNumber()));
        processedMessages.markProcessed(messageId);
    }
}
```

**Regla:** el `messageId` debe persistirse **en la misma transacción** que el efecto de negocio, nunca en una transacción separada.

---

## Configuración Externalizada (12-Factor)

### Principio

La configuración que varía entre entornos (URLs, credenciales, timeouts, feature flags) nunca vive en el código fuente. Siempre en variables de entorno o en un config server.

### `@ConfigurationProperties` solo en infraestructura

Encapsular la configuración en clases `@ConfigurationProperties` tipadas. **Nunca** inyectar `@Value` o `Environment` en dominio ni aplicación.

```java
// infrastructure/config/properties/DatabaseProperties.java
@ConfigurationProperties(prefix = "app.database")
public record DatabaseProperties(
        String url,
        String username,
        @DurationUnit(ChronoUnit.SECONDS) Duration connectionTimeout,
        int maxPoolSize
) {}
```

```java
// infrastructure/config/properties/ExternalApiProperties.java
@ConfigurationProperties(prefix = "app.external.payment")
public record ExternalApiProperties(
        String baseUrl,
        String apiKey,
        @DurationUnit(ChronoUnit.MILLIS) Duration readTimeout
) {}
```

```java
// infrastructure/config/InfrastructureConfig.java
@Configuration
@EnableConfigurationProperties({DatabaseProperties.class, ExternalApiProperties.class})
public class InfrastructureConfig { ... }
```

### Profiles

| Profile     | Uso                                              |
|-------------|--------------------------------------------------|
| `local`     | Desarrollo local — H2 o Docker Compose           |
| `test`      | Tests de integración — Testcontainers            |
| `staging`   | Entorno de pre-producción                        |
| `prod`      | Producción — sin valores por defecto en secrets  |

- Los secrets (`apiKey`, passwords, tokens) **nunca** tienen valor por defecto en `application.yml`. Si no están definidos, la app falla al arrancar.
- Usar `spring.config.import` para cargar desde Vault, AWS Secrets Manager o similar en producción.

```yaml
# application.yml — valores seguros por defecto solo para local/test
app:
  external:
    payment:
      base-url: ${PAYMENT_API_URL}          # obligatorio — falla si no está
      api-key: ${PAYMENT_API_KEY}           # obligatorio — nunca hardcodeado
      read-timeout: ${PAYMENT_TIMEOUT:5s}   # con fallback seguro
```

---

## OpenAPI / Swagger: Convenciones de Documentación

### Configuración base en infraestructura

```java
// infrastructure/config/OpenApiConfig.java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("My App API")
                .version("v1")
                .description("API del contexto de {dominio}"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme().type(SecurityScheme.Type.HTTP)
                        .scheme("bearer").bearerFormat("JWT")));
    }
}
```

### Convenciones de anotaciones en Controllers

- `@Tag` a nivel de clase para agrupar endpoints por recurso.
- `@Operation` en cada método con `summary` (una línea) y `description` (opcional, solo si aporta).
- `@ApiResponse` para documentar explícitamente los códigos de error relevantes.
- Los `@Schema` van en los Request/Response DTOs de infraestructura, **nunca en el dominio**.

```java
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Gestión del ciclo de vida de pedidos")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    @PostMapping
    @Operation(summary = "Crear un nuevo pedido")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pedido creado"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Pedido duplicado (Idempotency-Key ya procesada)")
    })
    public ResponseEntity<OrderResponse> place(...) { ... }
}
```

```java
// infrastructure/adapter/input/rest/dto/PlaceOrderRequest.java
public record PlaceOrderRequest(

    @Schema(description = "Lista de líneas del pedido", minLength = 1)
    @NotEmpty List<OrderLineRequest> lines
) {}
```

### Qué documentar y qué no

| Documentar siempre                              | No documentar                              |
|-------------------------------------------------|--------------------------------------------|
| Todos los endpoints públicos                    | Endpoints de Actuator (ya tienen su UI)    |
| Códigos de error de negocio (404, 409, 422)    | Errores genéricos de servidor (500)        |
| Headers obligatorios (`Idempotency-Key`, etc.) | Detalles de implementación interna         |
| Estructura de `ProblemDetail` en errores        | Clases de dominio o Application Services  |

---

## Pipeline CI: Checks Obligatorios antes de Merge

Todo PR debe pasar estos checks en orden. Si uno falla, no se mergea.

```
1. ./gradlew spotlessCheck          ← Formato de código
2. ./gradlew checkstyleMain         ← Reglas de estilo
3. ./gradlew :domain:test           ← Tests de dominio (sin Spring, rápidos)
4. ./gradlew :application:test      ← Tests de aplicación (Mockito, sin Spring)
5. ./gradlew :infrastructure:test   ← Tests de integración (Testcontainers)
6. ./gradlew test -ParchUnit        ← Tests de arquitectura (ArchUnit)
7. ./gradlew :infrastructure:test   ← Tests E2E / Spring Boot Test (lentos, al final)
```

### Reglas del pipeline

- Los pasos 1–4 deben completarse en **menos de 2 minutos**. Si no, hay un problema de diseño (tests unitarios que levantan Spring).
- Testcontainers en paso 5 puede reutilizar el contenedor entre tests (`@Testcontainers` con `reuse = true` en local).
- **El build de `main` nunca puede estar en rojo.** Si se rompe, es prioridad máxima antes que cualquier feature.
- La cobertura no es una métrica de CI — ArchUnit y la pirámide de tests son la garantía real de calidad.

### Ejemplo de workflow GitHub Actions

```yaml
name: CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '25'
          distribution: 'temurin'
          cache: 'gradle'

      - name: Format check
        run: ./gradlew spotlessCheck

      - name: Style check
        run: ./gradlew checkstyleMain

      - name: Unit tests (domain + application)
        run: ./gradlew :domain:test :application:test

      - name: Architecture tests
        run: ./gradlew :infrastructure:test --tests "*ArchitectureTest"

      - name: Integration & E2E tests
        run: ./gradlew :infrastructure:test
        env:
          TESTCONTAINERS_REUSE_ENABLE: true
```

---

## N+1 y Fetch Strategy en JPA

### Regla base: todo `LAZY`, `@EntityGraph` explícito cuando se necesite

```java
// ✅ Por defecto: todas las asociaciones en LAZY
@Entity
public class OrderEntity {

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OrderLineEntity> lines = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;  // si se necesita (normalmente solo el ID basta)
}
```

**Nunca** usar `FetchType.EAGER`. Causa N+1 silenciosos y carga datos que nadie necesita.

### Cuándo y cómo usar `@EntityGraph`

Usar `@EntityGraph` en el `Spring*Repository` cuando un caso de uso concreto necesita el Aggregate completo con sus colecciones en una sola query.

```java
// infrastructure/adapter/output/persistence/repository/SpringOrderRepository.java
public interface SpringOrderRepository extends JpaRepository<OrderEntity, UUID> {

    // Carga el aggregate completo para operaciones de escritura que necesitan las líneas
    @EntityGraph(attributePaths = {"lines", "lines.product"})
    Optional<OrderEntity> findWithLinesById(UUID id);

    // Query simple sin colecciones — para reconstitución ligera
    Optional<OrderEntity> findById(UUID id);  // heredado, sin EntityGraph
}
```

```java
// infrastructure/adapter/output/persistence/OrderJpaAdapter.java
@Override
public Optional<Order> findById(OrderId id) {
    // Elegir el método según lo que el caso de uso necesita
    return repository.findWithLinesById(id.value()).map(mapper::toDomain);
}
```

### Read side: sin `@EntityGraph`, proyecciones directas

Los Query Handlers del lado CQRS nunca cargan Aggregates completos. Usan proyecciones JPQL o Spring Data Projections que traen solo los campos necesarios en una query, eliminando el problema N+1 por diseño.

```java
// infrastructure/adapter/output/persistence/repository/SpringOrderRepository.java
@Query("""
    SELECT new com.example.application.dto.OrderSummaryView(
        o.id, o.status, o.createdAt, c.name, SUM(l.unitPrice * l.quantity)
    )
    FROM OrderEntity o
    JOIN CustomerEntity c ON c.id = o.customerId
    JOIN OrderLineEntity l ON l.order = o
    WHERE o.customerId = :customerId
    GROUP BY o.id, o.status, o.createdAt, c.name
    """)
Page<OrderSummaryView> findSummariesByCustomer(@Param("customerId") UUID customerId, Pageable pageable);
```

### Detectar N+1 en desarrollo

Activar en el profile `local`:

```yaml
spring:
  jpa:
    properties:
      hibernate:
        generate_statistics: true
logging:
  level:
    org.hibernate.stat: DEBUG
```

Si una query genera más de una sentencia SQL por registro, hay un N+1. Añadir `@EntityGraph` o reescribir como proyección.

---

## Auditoría: createdAt, updatedAt, createdBy sin contaminar el dominio

Los metadatos de auditoría son un **detalle de persistencia**, no un concepto del dominio. El Aggregate nunca conoce `@CreatedDate`, `@LastModifiedBy` ni `@EntityListeners`.

### Patrón: auditoría solo en la Entity JPA

```java
// infrastructure/adapter/output/persistence/entity/OrderEntity.java
@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)  // solo aquí
public class OrderEntity {

    @Id
    private UUID id;

    // ... campos de negocio

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(nullable = false)
    private String updatedBy;
}
```

```java
// infrastructure/config/PersistenceConfig.java
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class PersistenceConfig {

    @Bean
    public AuditorAware<String> auditorProvider(AuthenticatedUserResolver resolver) {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(resolver::resolveUsername);
    }
}
```

### Cuándo la auditoría SÍ pertenece al dominio

Si el negocio necesita razonar sobre quién hizo qué y cuándo (p.ej. auditoría de cumplimiento, trazabilidad legal), entonces es un concepto de dominio explícito y se modela como Domain Event o como un Value Object de auditoría propio:

```java
// domain/model/AuditTrail.java  — solo si el negocio lo requiere explícitamente
public record AuditTrail(UserId performedBy, Instant occurredOn, String action) {}
```

En ese caso, no usar `@CreatedBy` de Spring Data — modelarlo como parte del Aggregate y persistirlo como campo normal.

**Regla:** si el negocio nunca pregunta por esos datos en sus reglas, es auditoría técnica y va solo en la `*Entity`. Si el negocio los necesita para tomar decisiones, es dominio.

---

## Optimistic Locking: concurrencia sin bloqueos

### Principio

`@Version` es un detalle de persistencia JPA. **Nunca** en el Aggregate del dominio. La entidad JPA lleva la versión; el Aggregate la desconoce.

```java
// ✅ Solo en la Entity JPA
@Entity
public class OrderEntity {
    @Version
    private Long version;
    // ...
}

// ✅ El Aggregate no sabe nada de versiones
public class Order {
    // sin @Version, sin campo version
}
```

### Traducir la excepción de JPA a excepción de dominio

Spring lanza `ObjectOptimisticLockingFailureException` cuando hay conflicto. Debe traducirse a una excepción de dominio significativa **en el JPA Adapter**, antes de que suba a la capa de aplicación.

```java
// infrastructure/adapter/output/persistence/OrderJpaAdapter.java
@Override
public void save(Order order) {
    try {
        var entity = mapper.toEntity(order);
        repository.save(entity);
    } catch (ObjectOptimisticLockingFailureException ex) {
        throw new OrderConcurrentModificationException(order.getId(), ex);
    }
}
```

```java
// domain/exception/OrderConcurrentModificationException.java
public class OrderConcurrentModificationException extends RuntimeException {
    public OrderConcurrentModificationException(OrderId id, Throwable cause) {
        super("Order was modified concurrently: " + id, cause);
    }
}
```

```java
// infrastructure/adapter/input/rest/GlobalExceptionHandler.java
@ExceptionHandler(OrderConcurrentModificationException.class)
public ProblemDetail handleConcurrentModification(OrderConcurrentModificationException ex) {
    var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    problem.setType(URI.create("https://api.myapp.com/errors/concurrent-modification"));
    problem.setTitle("Concurrent Modification");
    return problem;
}
```

### Cuándo aplicar optimistic locking

Aplicar en Aggregates que:
- Son modificados frecuentemente por múltiples usuarios o procesos concurrentes.
- Tienen operaciones de lectura-modificación-escritura (leer, calcular, guardar).

No aplicar en entidades de solo escritura o con baja contención — añade overhead innecesario.

---

## Soft Delete: convención única

### Decisión de diseño: siempre un detalle de persistencia

El borrado lógico **no es un concepto del dominio** en este proyecto. El dominio solo conoce estados de negocio explícitos (`CANCELLED`, `CLOSED`, `ARCHIVED`). La columna `deleted_at` o `active` es un detalle del adaptador de persistencia.

**Consecuencia directa:** los repositorios del dominio nunca devuelven registros "borrados". El filtrado es responsabilidad del JPA Adapter, invisible para el dominio y la aplicación.

### Implementación

```java
// infrastructure/adapter/output/persistence/entity/OrderEntity.java
@Entity
@Table(name = "orders")
@SQLRestriction("deleted_at IS NULL")  // filtro automático en todas las queries
@SQLDelete(sql = "UPDATE orders SET deleted_at = NOW() WHERE id = ?")  // override del DELETE
public class OrderEntity {

    // ... campos de negocio y auditoría

    @Column(name = "deleted_at")
    private Instant deletedAt;  // null = activo, non-null = borrado
}
```

```java
// infrastructure/adapter/output/persistence/OrderJpaAdapter.java
@Override
public void deleteById(OrderId id) {
    // Spring Data ejecutará el @SQLDelete en lugar del DELETE físico
    repository.deleteById(id.value());
}
```

### Recuperar registros borrados (operaciones de administración)

Si hay casos de uso administrativos que necesitan ver registros borrados, usar una query nativa explícita que ignore el `@SQLRestriction`, **nunca** exponer esto a través del Output Port estándar — crear un Output Port separado para operaciones de administración.

```java
// application/port/output/OrderAdminQueryPort.java
public interface OrderAdminQueryPort {
    List<OrderSummaryView> findAllIncludingDeleted(Pageable pageable);
}
```

### Reglas del Soft Delete

- La columna se llama siempre `deleted_at` (timestamp), nunca `is_deleted` (boolean) — el timestamp es más informativo y permite auditoría.
- Usar `@SQLRestriction` de Hibernate 6+ en lugar del deprecado `@Where`.
- Las migraciones Flyway deben añadir índice parcial en producción: `CREATE INDEX ON orders (id) WHERE deleted_at IS NULL`.
- **Nunca** exponer `deletedAt` fuera de la `*Entity` JPA. El Aggregate no tiene ese campo.

---

## Lo que NUNCA se debe hacer

```java
// ❌ Constructor público en Aggregate usado para creación de negocio
new Order(id, customerId, status, lines);   // usar Order.create(customerId, lines)

// ❌ Excepción concreta capturada en GlobalExceptionHandler en lugar de categoría
@ExceptionHandler(OrderNotFoundException.class)   // capturar NotFoundException, no la concreta
@ExceptionHandler(ProductNotFoundException.class) // mismo status, duplicar handlers no escala

// ❌ FetchType.EAGER en cualquier asociación JPA
@OneToMany(fetch = FetchType.EAGER)  // siempre LAZY + @EntityGraph explícito si se necesita

// ❌ Auditoría técnica en el Aggregate del dominio
public class Order {
    @CreatedDate private Instant createdAt;       // detalle de persistencia
    @LastModifiedBy private String updatedBy;     // detalle de persistencia
}

// ❌ @Version en el Aggregate del dominio
public class Order {
    @Version private Long version;   // solo en OrderEntity
}

// ❌ Soft delete como campo del dominio
public class Order {
    private boolean deleted;         // el dominio usa estados de negocio: CANCELLED, CLOSED…
}

// ❌ ObjectOptimisticLockingFailureException llegando al controller sin traducir
// (debe convertirse a excepción de dominio en el JPA Adapter)

// ❌ Spring Security en dominio o aplicación
var auth = SecurityContextHolder.getContext().getAuthentication(); // en dominio o app

// ❌ @Value o Environment fuera de infraestructura
@Value("${app.timeout}") private Duration timeout;  // en dominio o app

// ❌ Sin protección de idempotencia en endpoints que crean recursos
// (POST sin Idempotency-Key cuando hay riesgo de reintento)

// ❌ Dos Aggregates modificados en la misma transacción
repository.save(order);
repository.save(inventory);

// ❌ Anotaciones Spring en dominio o aplicación
@Service public class PlaceOrderHandler { ... }
@Transactional public class PlaceOrderHandler { ... }

// ❌ JPA Entity en el dominio
@Entity public class Order { ... }

// ❌ Spring Data en el dominio
public interface OrderRepository extends JpaRepository<Order, UUID> { ... }

// ❌ El controller llama al Handler directamente (saltarse el puerto)
@Autowired private PlaceOrderHandler handler;   // debe ser PlaceOrderUseCase

// ❌ Versiones hardcodeadas en build.gradle.kts
implementation("org.mapstruct:mapstruct:1.6.0")  // usar libs.mapstruct

// ❌ ddl-auto destructivo fuera de tests
spring.jpa.hibernate.ddl-auto=create-drop        // usar validate o none

// ❌ Devolver null desde consultas
public Order findById(OrderId id) { return null; }  // usar Optional<Order>

// ❌ Logs con concatenación
log.info("Order " + orderId + " placed");            // usar parámetros {}

// ❌ Lógica de negocio en el controller
if (request.amount() <= 0) throw new RuntimeException("invalid"); // va en el dominio

// ❌ Publicar Domain Events antes de persistir
eventPublisher.publish(event);
repository.save(order);  // orden incorrecto: primero save, luego publish

// ❌ Jerga técnica en el dominio
public class OrderProcessor { }
public void processOrderData(OrderDTO dto) { }
```

---

## Respuestas de Claude: Cómo Trabajar Conmigo

- **No explicar** conceptos básicos de Java, Spring, DDD, CQRS o arquitectura hexagonal — soy Senior.
- **Dar código completo y funcional**, nunca fragmentos con `// ...resto del código`.
- Respetar **todas** las convenciones de nomenclatura de este documento en el código generado.
- Antes de sugerir una solución, verificar que encaja en la capa y módulo correctos.
- Si varias soluciones son válidas, presentar las alternativas con trade-offs y recomendar una.
- Los comentarios en código solo cuando explican **el porqué**, nunca el qué.
- Al modificar código existente, no tocar lo que no fue pedido.
- Si una petición viola las reglas arquitectónicas de este documento, **advertirlo** y proponer la alternativa correcta antes de implementar.
- Generar siempre el test unitario correspondiente junto con el código de producción.
- Al generar una clase nueva, indicar en qué módulo Gradle y paquete exacto debe vivir.
- Cuando generes un endpoint REST, incluir siempre: anotaciones OpenAPI, extracción de identidad del `Authentication`, y el header `Idempotency-Key` si el endpoint crea o modifica recursos.
- Cuando generes un Command Handler, evaluar siempre si necesita protección de idempotencia.
- Nunca resolver `SecurityContextHolder` fuera de `infrastructure/`.
