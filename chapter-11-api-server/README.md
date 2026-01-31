# Chapter 11: Complete API Server

A production-ready REST API server bringing together everything from previous chapters.

## What This Chapter Covers

This is a **synthesis chapter** that demonstrates:
- **Layered architecture** (Domain → Repository → Service → HTTP)
- **http4s** for HTTP server (Chapter 9)
- **Doobie** for database access (Chapter 10)
- **Flyway** for migrations (Chapter 10)
- **Cats-Effect** for effects (Chapters 7-8)
- **EitherT** for typed errors (Chapters 6-7)
- **Configuration management** with typesafe-config
- **Docker Compose** for local PostgreSQL

## Architecture

```
┌─────────────────────────────────────────────┐
│              HTTP Layer                      │
│  - UserRoutes (HTTP → Service)              │
│  - Error mapping (Domain → HTTP)            │
│  - JSON encoding/decoding                   │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│             Service Layer                    │
│  - UserService                              │
│  - Business logic                           │
│  - Validation                               │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│           Repository Layer                   │
│  - UserRepository                           │
│  - Database operations (Doobie)             │
│  - Returns EitherT[ConnectionIO, E, A]      │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│            Domain Layer                      │
│  - Models (User, CreateUserRequest, etc.)   │
│  - Errors (DomainError)                     │
│  - Pure domain logic                        │
└─────────────────────────────────────────────┘
```

## Project Structure

```
chapter-11-api-server/
├── docker-compose.yml           # PostgreSQL setup
├── build.sbt                    # Dependencies
├── src/main/
│   ├── resources/
│   │   ├── application.conf     # Configuration
│   │   ├── logback.xml          # Logging config
│   │   └── db/migration/
│   │       └── V1__create_users_table.sql
│   └── scala/
│       ├── Main.scala           # Application entry point
│       ├── config/
│       │   └── AppConfig.scala  # Configuration loading
│       ├── domain/
│       │   └── Models.scala     # Domain models and errors
│       ├── repository/
│       │   └── UserRepository.scala  # Database layer
│       ├── service/
│       │   └── UserService.scala     # Business logic
│       └── http/
│           └── UserRoutes.scala      # HTTP routes
```

## Quick Start

### 1. Start PostgreSQL

```bash
cd chapter-11-api-server
docker-compose up -d

# Check it's running
docker-compose ps
```

### 2. Run the Server

```bash
sbt run
```

The server will:
1. Load configuration
2. Run Flyway migrations
3. Create database connection pool
4. Start HTTP server on port 8080

### 3. Test the API

```bash
# Health check
curl http://localhost:8080/health

# Create a user
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com","age":30}'

# List all users
curl http://localhost:8080/users

# Get specific user
curl http://localhost:8080/users/1

# Update user
curl -X PUT http://localhost:8080/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice Smith","email":"alice@example.com","age":31}'

# Delete user
curl -X DELETE http://localhost:8080/users/1
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/health` | Health check |
| GET | `/users` | List all users |
| GET | `/users/:id` | Get user by ID |
| POST | `/users` | Create user |
| PUT | `/users/:id` | Update user |
| DELETE | `/users/:id` | Delete user |

## Rust Comparison

This architecture is similar to a typical Rust web application:

### Rust (axum + sqlx)

```rust
// main.rs
#[tokio::main]
async fn main() -> Result<()> {
    let config = Config::load()?;
    
    // Migrations
    sqlx::migrate!().run(&pool).await?;
    
    // Create app state
    let app_state = AppState {
        pool: pool.clone(),
        config,
    };
    
    // Build app
    let app = Router::new()
        .route("/users", get(list_users).post(create_user))
        .route("/users/:id", get(get_user).put(update_user).delete(delete_user))
        .with_state(app_state);
    
    // Start server
    let listener = tokio::net::TcpListener::bind("0.0.0.0:8080").await?;
    axum::serve(listener, app).await?;
    Ok(())
}

// repository.rs
impl UserRepository {
    async fn find_by_id(&self, id: i64) -> Result<User, DomainError> {
        sqlx::query_as("SELECT * FROM users WHERE id = $1")
            .bind(id)
            .fetch_one(&self.pool)
            .await
            .map_err(|_| DomainError::UserNotFound(id))
    }
}

// service.rs
impl UserService {
    async fn create_user(&self, req: CreateUserRequest) -> Result<User, DomainError> {
        self.validate(&req)?;
        self.repository.create(req).await
    }
}

// handler.rs
async fn create_user(
    State(service): State<UserService>,
    Json(req): Json<CreateUserRequest>
) -> Result<Json<User>, AppError> {
    let user = service.create_user(req).await?;
    Ok(Json(user))
}
```

### Scala (http4s + Doobie)

The structure is remarkably similar! Key differences:
- **Explicit effects**: IO[A] vs implicit async/await
- **EitherT**: Type-safe errors in function signatures
- **Transactor**: Explicit `.transact(xa)` vs implicit pool
- **For-comprehension**: Instead of ? operator

## Configuration

Configuration in `src/main/resources/application.conf`:

```hocon
database {
  url = "jdbc:postgresql://localhost:5432/apidb"
  url = ${?DATABASE_URL}  # Override with env var
  
  user = "apiuser"
  password = "apipass"
  pool-size = 10
}

server {
  host = "0.0.0.0"
  port = 8080
  port = ${?SERVER_PORT}  # Override with env var
}
```

Override with environment variables:
```bash
DATABASE_URL=jdbc:postgresql://prod-db:5432/proddb \
SERVER_PORT=9000 \
sbt run
```

## Key Patterns Demonstrated

### 1. Layered Architecture

Each layer has a single responsibility:
- **Domain**: Pure models and errors (no dependencies)
- **Repository**: Database operations (Doobie)
- **Service**: Business logic and validation
- **HTTP**: Request/response handling (http4s)

### 2. Dependency Injection

```scala
// Wire up layers in Main.scala
val repository = new DoobieUserRepository
val service = new UserServiceImpl(repository)
val routes = new UserRoutes(service, xa)
```

Similar to Rust's manual DI or using a framework like axum's State.

### 3. Error Handling

```scala
// Repository returns EitherT[ConnectionIO, DomainError, User]
def findById(id: Long): EitherT[ConnectionIO, DomainError, User]

// Service composes repository operations
def create(req: CreateUserRequest): EitherT[ConnectionIO, DomainError, User] = {
  for {
    validated <- EitherT.fromEither(validate(req))
    user <- repository.create(validated)
  } yield user
}

// HTTP layer executes and maps to responses
service.create(req)
  .value
  .transact(xa)
  .flatMap {
    case Right(user) => Created(user.asJson)
    case Left(error) => errorToResponse(error)
  }
```

### 4. Resource Management

```scala
createTransactor(config).use { xa =>
  EmberServerBuilder
    .build
    .use { server =>
      IO.never  // Run until interrupted
    }
}
```

Ensures proper cleanup (like Rust's Drop/RAII).

### 5. Migration at Startup

```scala
def run: IO[Unit] = {
  for {
    config <- AppConfig.load[IO]
    _ <- runMigrations(config)  // Apply migrations first
    _ <- createTransactor(config).use { xa =>
      // Start server
    }
  } yield ()
}
```

Similar to sqlx::migrate!().run(&pool) in Rust.

## Testing

Run tests with:
```bash
sbt test
```

## Production Deployment

### Environment Variables

```bash
export DATABASE_URL=jdbc:postgresql://prod-db:5432/proddb
export DATABASE_USER=produser
export DATABASE_PASSWORD=secretpassword
export SERVER_PORT=8080
```

### Docker

```dockerfile
FROM hseeberger/scala-sbt:eclipse-temurin-17.0.5_1.8.2_2.13.10 as build
WORKDIR /app
COPY . .
RUN sbt assembly

FROM eclipse-temurin:17-jre-alpine
COPY --from=build /app/target/scala-2.13/api-server.jar /app.jar
CMD ["java", "-jar", "/app.jar"]
```

### Scaling

- **Horizontal**: Run multiple instances behind a load balancer
- **Database**: Connection pooling (HikariCP) handles concurrent requests
- **Monitoring**: Add metrics with Micrometer or Prometheus

## Common Issues

### PostgreSQL Connection Error

```
Error: Connection refused
```

**Solution**: Make sure PostgreSQL is running:
```bash
docker-compose up -d
docker-compose logs postgres
```

### Migration Error

```
Error: Migration failed
```

**Solution**: Check migration files are valid SQL. Drop database and retry:
```bash
docker-compose down -v
docker-compose up -d
sbt run
```

### Port Already in Use

```
Error: Address already in use
```

**Solution**: Change port in application.conf or use environment variable:
```bash
SERVER_PORT=9000 sbt run
```

## What You've Learned

By completing this chapter, you've seen how to:

✅ **Structure** a Scala web application with layered architecture  
✅ **Integrate** http4s, Doobie, Flyway, and Cats-Effect  
✅ **Handle errors** with typed EitherT throughout all layers  
✅ **Manage configuration** with typesafe-config  
✅ **Run migrations** automatically at startup  
✅ **Deploy** with Docker and Docker Compose  
✅ **Test** the API with curl  

This is a **production-ready pattern** used in real Scala services!

## Next Steps

You've completed the core curriculum! Optional chapters:

- **Chapter 12**: Property-based testing with ScalaCheck
- **Chapter 13**: Advanced Cats-Effect (fs2 streaming, more concurrency)
- **Chapter 14**: Advanced type-level programming (Tagless Final)
- **Chapter 15**: Production considerations (logging, metrics, monitoring)

## Resources

- [http4s Documentation](https://http4s.org/)
- [Doobie Documentation](https://tpolecat.github.io/doobie/)
- [Cats-Effect Documentation](https://typelevel.org/cats-effect/)
- [Typesafe Config](https://github.com/lightbend/config)
