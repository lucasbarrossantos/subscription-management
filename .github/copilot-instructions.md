# GitHub Copilot Instructions - Subscription Management

## Project Overview
This is a subscription management system built with **Spring Boot** following **Hexagonal Architecture** (Ports & Adapters). The system manages user subscriptions with multiple plan tiers and integrates with an external wallet service for payment processing.

## Architecture Principles

### Hexagonal Architecture (Ports & Adapters)
The project strictly follows hexagonal architecture with clear separation of concerns:

```
core/
  ├── domain/          # Business entities (Subscription, User, enums)
  ├── port/
  │   ├── in/          # Input ports (use case interfaces)
  │   └── out/         # Output ports (repository, external service interfaces)
  ├── usecase/         # Business logic implementation
  └── exception/       # Business exceptions

adapter/
  ├── datasource/      # Database adapters (JPA repositories, entities, mappers)
  ├── http/            # REST API adapters (controllers, DTOs, mappers)
  └── integration/     # External service adapters (Feign clients)
```

**Key Rules:**
1. **Core never depends on adapters** - dependencies point inward
2. **Domain objects are framework-agnostic** - no JPA annotations in domain
3. **Use cases implement input ports** - annotated with `@Service`
4. **Adapters implement output ports** - annotated with `@Component`
5. **Mappers convert between layers** - use MapStruct for adapters

## Coding Standards

### 1. Package Structure Rules

#### Core Layer (`core/`)
- **Domain (`domain/`)**: Pure POJOs with Lombok (`@Data`, `@Builder`, `@AllArgsConstructor`, `@NoArgsConstructor`)
- **Ports (`port/in/` and `port/out/`)**: Interfaces only, no implementation
- **Use Cases (`usecase/`)**: Business logic, implements input ports, uses output ports
- **Exceptions (`exception/`)**: Custom business exceptions extending `BusinessException`

#### Adapter Layer (`adapter/`)
- **Datasource (`datasource/database/`)**: 
  - `entity/` - JPA entities with framework annotations
  - `repository/` - Spring Data JPA repositories
  - `mapper/` - MapStruct mappers between entities and domain
  - Database adapters implement repository ports

- **HTTP (`http/`)**: 
  - `controller/` - REST controllers (`@RestController`, `@RequestMapping`)
  - `dto/` - Request/Response DTOs (use Java records when possible)
  - `mapper/` - MapStruct mappers between DTOs and domain
  - `exception/` - Exception handlers (`@RestControllerAdvice`)

- **Integration (`integration/`)**: 
  - Feign clients for external APIs (`@FeignClient`)
  - DTOs for external services
  - Adapters implementing external service ports

### 2. Naming Conventions

#### Classes
- **Use Cases**: `{Action}{Entity}UseCase` (e.g., `CreateSubscriptionUseCase`)
- **Input Ports**: `{Action}{Entity}Port` (e.g., `CreateSubscriptionPort`)
- **Output Ports**: `{Entity}RepositoryPort`, `{Service}Port` (e.g., `SubscriptionRepositoryPort`, `WalletPort`)
- **Adapters**: `{Entity}DatabaseAdapter`, `{Service}Adapter` (e.g., `SubscriptionDatabaseAdapter`, `WalletAdapter`)
- **Controllers**: `{Entity}Controller` (e.g., `SubscriptionController`)
- **DTOs**: `{Entity}Request`, `{Entity}Response` (e.g., `SubscriptionRequest`, `SubscriptionResponse`)
- **Entities**: `{Entity}Entity` (e.g., `SubscriptionEntity`)
- **Exceptions**: `{Reason}Exception` (e.g., `UserNotFoundException`, `ActiveSubscriptionAlreadyExistsException`)

#### Methods
- **Use Cases**: `execute()` for main operation
- **Repositories**: Standard Spring Data conventions (`findById`, `findByUserIdAndStatus`, custom queries with `@Query`)
- **Controllers**: HTTP method names (`create`, `update`, `delete`, `findById`)

### 3. Dependency Injection
- Use **constructor injection** with Lombok `@AllArgsConstructor` or `@RequiredArgsConstructor`
- Never use field injection (`@Autowired` on fields)
- Inject ports/interfaces, not concrete implementations

### 4. Logging
- Use SLF4J with Lombok `@Slf4j`
- Log at appropriate levels:
  - `log.info()` - Business operations, successful flows
  - `log.warn()` - Recoverable issues, business warnings
  - `log.error()` - Exceptions, failures with stack traces
- Include relevant context (IDs, amounts, statuses)
- Log before and after critical operations

**Example:**
```java
log.info("Creating subscription for user {} - plan: {}", userId, plan);
// operation
log.info("Subscription created successfully - id: {}", subscription.getId());
```

### 5. Exception Handling

#### Business Exceptions
- Create custom exceptions extending `BusinessException`
- Use descriptive names and messages
- Throw from use cases, catch in controllers

**Example:**
```java
public class SubscriptionNotFoundException extends BusinessException {
    public SubscriptionNotFoundException(String message) {
        super(message);
    }
}
```

#### Global Exception Handler
- Use `@RestControllerAdvice` for centralized exception handling
- Return consistent error responses with appropriate HTTP status codes
- Log exceptions with full context

### 6. Database Practices

#### Entities
- Use JPA annotations on entities, not domain objects
- Always use UUIDs for IDs (`@GeneratedValue(strategy = GenerationType.AUTO)`)
- Use enums with `@Enumerated(EnumType.STRING)` for type safety
- Create proper indexes for query performance
- Use meaningful constraint names

#### Repositories
- Extend `JpaRepository<Entity, UUID>`
- Use method naming conventions for simple queries
- Use `@Query` for complex queries
- Always use `Pageable` for batch operations

#### Migrations
- Use Flyway with versioned migrations (`V{number}__{description}.sql`)
- Never modify existing migrations
- Create new migration for schema changes
- Include indexes in migrations

### 7. External Service Integration

#### Feign Clients
- Use `@FeignClient` with proper configuration
- Define DTOs for external service contracts
- Implement adapters that handle exceptions and translate to domain exceptions
- Log all external calls (request and response)

**Example:**
```java
@FeignClient(name = "wallet-api", url = "${integrations.wallet.api.url}")
public interface WalletFeignClient {
    @PostMapping("/wallets/{userId}/transactions")
    WalletTransactionResponse createTransaction(
        @PathVariable UUID userId, 
        @RequestBody WalletTransactionRequest request
    );
}
```

#### Error Handling
- Catch `FeignException` in adapters
- Map to business exceptions with meaningful messages
- Handle specific HTTP status codes appropriately

### 8. DTOs and Mappers

#### DTOs
- Use Java **records** for immutable DTOs
- Name request/response fields in Portuguese for API contract (e.g., `usuarioId`, `plano`)
- Keep domain names in English internally

**Example:**
```java
public record SubscriptionRequest(
    UUID usuarioId,
    TypePlan plano
) {}
```

#### MapStruct Mappers
- Use `@Mapper(componentModel = "spring")` for Spring integration
- Use `@Mapping` annotations for field name differences
- Ignore auto-generated fields with `@Mapping(target = "...", ignore = true)`
- Create custom mapping methods with `@Named` when needed

### 9. API Design

#### REST Controllers
- Use proper HTTP methods (POST for create, DELETE for cancel, GET for read, PUT for update)
- Return appropriate status codes (201 Created, 204 No Content, 200 OK)
- Use `ResponseEntity<T>` for explicit control
- Create URIs for created resources using `ServletUriComponentsBuilder`
- Use `@Valid` for request validation

**Example:**
```java
@PostMapping
public ResponseEntity<SubscriptionResponse> create(@Valid @RequestBody SubscriptionRequest request) {
    Subscription subscription = mapper.toDomain(request);
    Subscription created = useCase.execute(subscription);
    SubscriptionResponse response = mapper.toResponse(created);
    
    URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id())
            .toUri();
    
    return ResponseEntity.created(uri).body(response);
}
```

### 10. Business Logic Rules

#### Subscriptions
- Only one ACTIVE subscription per user at a time
- Support plan changes with financial adjustments:
  - **Upgrade** (cheaper → expensive): Charge only the difference
  - **Downgrade** (expensive → cheaper): Refund the difference
  - **Same price**: No financial transaction
- Track renewal attempts (max 3)
- Suspend subscription after 3 failed renewal attempts

#### Financial Transactions
- Always validate wallet balance before operations
- Use `BigDecimal` for monetary values
- Include descriptive messages in transactions
- Handle insufficient balance gracefully

### 11. Testing Considerations
When writing tests:
- Mock output ports in use case tests
- Test business rules thoroughly
- Use `@SpringBootTest` for integration tests
- Test exception scenarios
- Verify logging output in critical paths

### 12. Configuration
- Externalize all configurations in `application.yaml`
- Use Spring profiles for different environments
- Use `@Value` or `@ConfigurationProperties` for property injection
- Never hardcode URLs, credentials, or business constants

**Example:**
```yaml
integrations:
  wallet:
    api:
      url: http://localhost:8081
```

### 13. Code Quality

#### General Practices
- Keep methods small and focused (single responsibility)
- Use meaningful variable and method names
- Avoid magic numbers - use constants
- Don't repeat yourself (DRY principle)
- Write self-documenting code with clear names
- Add comments only when the "why" is not obvious

#### Lombok Usage
- Use `@Data` for domain objects (or `@Getter/@Setter` individually)
- Use `@Builder` for objects with many fields
- Use `@AllArgsConstructor` with Spring components
- Use `@Slf4j` for logging
- Avoid `@ToString` and `@EqualsAndHashCode` on entities with relationships

### 14. Domain-Driven Design Patterns

#### Value Objects
Use enums for type-safe value objects:
```java
@Getter
public enum TypePlan {
    BASIC(new BigDecimal("19.90"), "Plano BASICO"),
    PREMIUM(new BigDecimal("39.90"), "Plano PREMIUM"),
    FAMILY(new BigDecimal("59.90"), "Plano FAMILIA");

    private final BigDecimal price;
    private final String description;
}
```

#### Aggregates
- `User` and `Subscription` are separate aggregates
- Reference by ID, not by object when crossing aggregate boundaries
- Load related data explicitly when needed

## Technology Stack
- **Java 21**
- **Spring Boot 3.2+**
- **Spring Data JPA** with Hibernate
- **PostgreSQL** database
- **Flyway** for database migrations
- **MapStruct** for object mapping
- **Lombok** for boilerplate reduction
- **OpenFeign** for HTTP clients
- **Maven** for build management

## When Generating New Code

1. **Identify the layer**: Core (domain/use case) or Adapter (datasource/http/integration)
2. **Follow the flow**: Controller → Use Case → Repository/External Service
3. **Create ports first**: Define interfaces before implementations
4. **Use existing patterns**: Follow naming conventions and structure from existing code
5. **Add proper logging**: Info for happy path, error for exceptions
6. **Handle exceptions**: Create custom exceptions, handle in adapters
7. **Write clean code**: Small methods, clear names, proper separation of concerns
8. **Create migrations**: Always include database changes in Flyway migrations

## Common Patterns in This Project

### Creating a New Feature
1. Define domain object in `core/domain/`
2. Create input port in `core/port/in/`
3. Create output port(s) in `core/port/out/`
4. Implement use case in `core/usecase/`
5. Create JPA entity in `adapter/datasource/database/entity/`
6. Create repository in `adapter/datasource/database/repository/`
7. Implement database adapter in `adapter/datasource/database/`
8. Create DTOs in `adapter/http/dto/`
9. Create controller in `adapter/http/controller/`
10. Create Flyway migration if needed

### Integrating External Service
1. Create port interface in `core/port/out/`
2. Create Feign client in `adapter/integration/{service}/`
3. Create DTOs for external service
4. Implement adapter with exception handling
5. Use adapter in use cases

## Anti-Patterns to Avoid

❌ **Don't** put business logic in controllers or adapters
❌ **Don't** use domain objects directly in controllers (use DTOs)
❌ **Don't** use JPA entities in use cases (use domain objects)
❌ **Don't** inject adapters directly, always use ports
❌ **Don't** catch generic `Exception` unless necessary
❌ **Don't** ignore exceptions or log without rethrowing
❌ **Don't** use field injection
❌ **Don't** hardcode configuration values
❌ **Don't** create circular dependencies between layers
❌ **Don't** expose internal exceptions to API clients

## Best Practices Checklist

✅ Use constructor injection with Lombok
✅ Log all important operations with context
✅ Validate input in controllers with `@Valid`
✅ Create custom exceptions for business errors
✅ Use BigDecimal for money calculations
✅ Create indexes for frequently queried fields
✅ Use transactions for multi-step operations
✅ Return appropriate HTTP status codes
✅ Use meaningful names for all identifiers
✅ Keep use cases focused on single responsibility
✅ Test business logic thoroughly
✅ Document public APIs with clear examples
✅ Use enums for fixed sets of values
✅ Handle pagination for large result sets
✅ Use batch processing for bulk operations

---

**Remember**: The goal is maintainable, testable, and scalable code that clearly expresses business intent while maintaining proper architectural boundaries.
