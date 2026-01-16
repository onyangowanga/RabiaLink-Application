# Copilot Instructions for RabiaLinkApp

## Project Overview
RabiaLinkApp is a Spring Boot 4.0.1 supply management system (Java 17) with a domain-driven architecture organized into functional modules: **orders**, **products**, and **clients**. Each module follows a strict layered pattern: controller → service → repository → domain. The system uses PostgreSQL with JPA/Hibernate for persistence.

## Architecture & Module Structure

### Three Core Domains
1. **Orders** (`orders/`) - Customer order management with parent-child relationships
   - Controllers handle REST endpoints (`/api/orders`)
   - Services implement business logic with transaction management (`@Transactional`)
   - Domains: `Order` (parent) and `OrderItem` (children, cascade delete/persist)
   - Status tracking via `OrderStatus` enum (CREATED, PENDING, SHIPPED, etc.)

2. **Products** (`products/`) - Inventory management
   - Simple CRUD operations, minimal business logic
   - Used as references in orders (OrderItem references Product by ID)

3. **Clients** (`clients/`) - Customer account management
   - Referenced by orders (Order.client is @ManyToOne)
   - Validation: orders fail if client not found

### Data Flow Example: Creating an Order
```
OrderController.createOrder() 
  → OrderServiceImpl.createOrder(OrderRequest)
    → Validate client exists (ClientRepository)
    → Create Order + OrderItems, validate products exist (ProductRepository)
    → Persist Order (cascade saves items)
    → Return OrderResponse (DTO transformation)
```

## Key Patterns & Conventions

### DTO Pattern (Strict Boundary)
- **Request DTOs** (`*Request.java`): Use `@NotEmpty`, `@Valid` for nested validation
  - Example: `OrderRequest` contains `List<OrderItemRequest>` - nested validation applies
- **Response DTOs** (`*Response.java`): Mapped from domain entities, exclude sensitive/internal fields
- **Never return domain entities directly** in controllers - always map to DTOs

### Service Layer Rules
- **Always use `@Service` + `@Transactional`** for order operations
- Constructor injection only (no `@Autowired` on fields)
- Throw `ResourceNotFoundException` for not-found queries (centrally handled by `GlobalExceptionHandler`)
- Service methods return DTOs, not domain entities

### Domain Entities
- Use `@Entity`, `@Table` with explicit names
- Relationships: `Order` uses `@OneToMany(mappedBy="order", cascade=CascadeType.ALL)` for items
- Use `@Enumerated(EnumType.STRING)` for enums like `OrderStatus`
- Include `@PrePersist` for timestamps and default status (see `Order.java`)

### Exception Handling
- `GlobalExceptionHandler` centrally catches:
  - `MethodArgumentNotValidException` → 400 with field-level errors (HashMap format)
  - `ResourceNotFoundException` → 404 with error message
- Controllers don't handle exceptions; service layer throws and handler catches

### Validation Strategy
- **Request level**: Use Jakarta validation annotations (`@NotEmpty`, `@Valid`)
- **Business logic level**: Service validates existence of foreign keys before operations
- Always wrap optional results: `findById().orElseThrow()`

## Build & Runtime

### Maven Commands
```bash
./mvnw clean install        # Full build with tests
./mvnw spring-boot:run      # Start server (http://localhost:8080)
./mvnw test                 # Run tests only
```

### Database Setup
- PostgreSQL required (localhost:5432, database: `rabiya_supply`)
- Credentials in `application.yml` (postgres/JonaMia)
- Hibernate auto-creates/updates schema with `ddl-auto: update`
- Enable `show-sql: true` for debugging SQL queries

### Dependencies to Know
- **Spring Data JPA**: Repository interfaces auto-implement CRUD
- **Lombok**: Used for annotations (though not imported in current code, available in deps)
- **Jakarta Validation**: `jakarta.validation.*` (not `javax.*`)
- **PostgreSQL Driver**: Runtime dependency

## Common Workflows

### Adding a New CRUD Module (e.g., Inventory)
1. Create `inventory/` folder with subfolders: `domain/`, `repository/`, `service/`, `controller/`, `dto/`
2. Define domain entity with `@Entity`, relationships
3. Create repository extending `JpaRepository<Entity, ID>`
4. Create service interface → impl with `@Service`, `@Transactional`
5. Create controller with `@RestController`, constructor-injected service
6. Create request/response DTOs with validation annotations

### Cross-Module References
- Use repository to fetch and validate references (e.g., OrderService validates `clientId` exists)
- Never assume parent entity exists; always throw `ResourceNotFoundException`
- Use cascade operations for parent-child (Order→OrderItems), not for sibling references

### Testing Approach
- Test files in `src/test/java/com/codewith/RabiaLinkApp/`
- Use Spring test fixtures and `@DataJpaTest` for repository tests
- Mock external services; use real repositories for integration tests

## Code Organization
```
src/main/java/com/codewith/RabiaLinkApp/
├── RabiaLinkAppApplication.java          # Spring Boot entry point
├── common/exception/                       # Shared exception handling
├── orders/
│   ├── controller/OrderController.java    # REST endpoints
│   ├── service/{OrderService, impl/}      # Business logic
│   ├── domain/{Order, OrderItem, etc.}    # JPA entities
│   ├── repository/                        # Spring Data interfaces
│   └── dto/                                # Request/Response objects
├── products/                               # Similar structure
└── clients/                                # Similar structure
```

## Important Notes for AI Agents
1. **Always check `GlobalExceptionHandler`** when adding new error scenarios
2. **Preserve cascade settings** in relationships; changing them affects data integrity
3. **DTOs are the contract**: Modify responses carefully; clients depend on structure
4. **Transactionality**: Order creation involves multiple entities; never split across non-transactional methods
5. **PostgreSQL dialect**: Some SQL features differ from H2/MySQL; test with actual database
