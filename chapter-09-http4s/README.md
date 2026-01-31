# Chapter 09: HTTP with http4s

This chapter covers building HTTP servers with http4s, Scala's functional HTTP library.

## What is http4s?

http4s is a purely functional, streaming HTTP library for Scala built on top of Cats-Effect. It's similar to Rust's web frameworks like axum or actix-web, but with a functional programming approach.

**Rust Comparison:**
```rust
// Rust (axum)
use axum::{Router, routing::get};

async fn handler() -> &'static str {
    "Hello, World!"
}

let app = Router::new().route("/", get(handler));
```

```scala
// Scala (http4s)
import org.http4s._
import org.http4s.dsl.io._

val routes = HttpRoutes.of[IO] {
  case GET -> Root => Ok("Hello, World!")
}
```

## Key Concepts

### 1. HttpRoutes[F[_]]

Routes are defined as `HttpRoutes[F[_]]` which is a function `Request[F] => OptionT[F, Response[F]]`.

**Rust Comparison:**
- Similar to `Router` in axum or `App` in actix-web
- Pattern matching for routing (vs method-based routing)
- Effect-polymorphic (F[_] can be any effect type, usually IO)

```scala
val routes = HttpRoutes.of[IO] {
  case GET -> Root / "hello" / name =>
    Ok(s"Hello, $name!")
}
```

### 2. HTTP DSL

http4s provides a DSL for pattern matching on HTTP methods and paths:

```scala
case GET -> Root / "users" / IntVar(id) =>
  // Match GET /users/123 and extract 123 as Int
  
case req @ POST -> Root / "users" =>
  // Match POST /users with request body access
  
case GET -> Root / "search" :? QueryParam(term) =>
  // Match GET /search?term=scala
```

**Rust Comparison:**
```rust
// axum
Router::new()
  .route("/users/:id", get(get_user))
  .route("/users", post(create_user))
  .route("/search", get(search))
```

### 3. JSON with Circe

http4s integrates with circe for JSON encoding/decoding:

```scala
import io.circe.generic.auto._
import org.http4s.circe._

case class User(id: Long, name: String)

implicit val userDecoder: EntityDecoder[IO, User] = jsonOf[IO, User]
implicit val userEncoder: EntityEncoder[IO, User] = jsonEncoderOf[IO, User]

val routes = HttpRoutes.of[IO] {
  case req @ POST -> Root / "users" =>
    for {
      user <- req.as[User]  // Decode JSON
      response <- Created(user.asJson)  // Encode to JSON
    } yield response
}
```

**Rust Comparison:**
```rust
// axum with serde
#[derive(Deserialize, Serialize)]
struct User {
    id: u64,
    name: String,
}

async fn create_user(Json(user): Json<User>) -> Json<User> {
    Json(user)
}
```

### 4. Middleware

Middleware wraps routes to add cross-cutting concerns:

```scala
import org.http4s.server.middleware._

val routes = HttpRoutes.of[IO] { ... }

val withLogging = Logger.httpRoutes(logHeaders = true, logBody = false)(routes)
val withCors = CORS.policy.withAllowOriginAll(withLogging)
val withTimeout = Timeout.httpRoutes[IO](2.seconds)(withCors)
```

**Rust Comparison:**
```rust
// tower/axum middleware
use tower::ServiceBuilder;

let app = Router::new()
    .route("/", get(handler))
    .layer(ServiceBuilder::new()
        .layer(TraceLayer::new_for_http())
        .layer(CorsLayer::permissive())
        .layer(TimeoutLayer::new(Duration::from_secs(2)))
    );
```

### 5. Typed Errors with EitherT

Production pattern for error handling:

```scala
sealed trait AppError
case class NotFound(id: Long) extends AppError
case class ValidationError(msg: String) extends AppError

def findUser(id: Long): EitherT[IO, AppError, User] = ???

val routes = HttpRoutes.of[IO] {
  case GET -> Root / "users" / LongVar(id) =>
    findUser(id).foldF(
      error => errorToResponse(error),
      user => Ok(user.asJson)
    )
}
```

**Rust Comparison:**
```rust
// axum with custom error type
enum AppError {
    NotFound(u64),
    ValidationError(String),
}

impl IntoResponse for AppError {
    fn into_response(self) -> Response { ... }
}

async fn get_user(id: u64) -> Result<Json<User>, AppError> {
    // ...
}
```

## Files in This Chapter

### Examples
1. **01_basic_routes.scala** - Basic routing, path variables, query parameters
2. **02_json.scala** - JSON encoding/decoding with circe
3. **03_middleware.scala** - Logging, CORS, authentication, error handling
4. **04_error_handling.scala** - Advanced error handling with EitherT

### Exercises
- **Exercise01_BookAPI.scala** - Build a complete REST API for books

## Running the Examples

Start a server:
```bash
cd chapter-09-http4s

# Basic routes example
sbt "runMain http4sbasics.BasicRoutesExample"

# JSON example
sbt "runMain http4sbasics.JsonExample"

# Middleware example
sbt "runMain http4sbasics.MiddlewareExample"

# Error handling example
sbt "runMain http4sbasics.AdvancedErrorHandlingExample"
```

Test with curl (in another terminal):
```bash
# GET request
curl http://localhost:8080/hello/Alice

# POST with JSON
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com","age":30}'

# With authorization
curl -H "Authorization: Bearer secret-token" \
  http://localhost:8080/api/profile
```

Stop the server with **Ctrl+C**.

## Key Differences from Rust

| Feature | Rust (axum) | Scala (http4s) |
|---------|-------------|----------------|
| **Routing** | Method-based: `Router::new().route()` | Pattern matching: `case GET -> Root / "path"` |
| **Handler** | `async fn() -> impl IntoResponse` | `Request[F] => F[Response[F]]` |
| **JSON** | `Json<T>` extractor | `req.as[T]` with implicit decoder |
| **Error** | `Result<T, E>` with `impl IntoResponse` | `EitherT[F, E, T]` with `.foldF()` |
| **Middleware** | `tower::Layer` | `HttpRoutes[F] => HttpRoutes[F]` |
| **Effect** | `async/await` (implicit Future) | Explicit `F[_]` (usually IO) |
| **Server** | `axum::serve()` | `EmberServerBuilder` with Resource |

## Common Patterns

### 1. CRUD API
```scala
val routes = HttpRoutes.of[IO] {
  case GET -> Root / "items" => 
    listItems().flatMap(items => Ok(items.asJson))
  
  case GET -> Root / "items" / LongVar(id) =>
    findItem(id).foldF(errorToResponse, item => Ok(item.asJson))
  
  case req @ POST -> Root / "items" =>
    (for {
      createReq <- EitherT.liftF(req.as[CreateItemRequest])
      item <- createItem(createReq)
    } yield item).foldF(errorToResponse, item => Created(item.asJson))
  
  case req @ PUT -> Root / "items" / LongVar(id) =>
    (for {
      updateReq <- EitherT.liftF(req.as[UpdateItemRequest])
      item <- updateItem(id, updateReq)
    } yield item).foldF(errorToResponse, item => Ok(item.asJson))
  
  case DELETE -> Root / "items" / LongVar(id) =>
    deleteItem(id).foldF(
      errorToResponse,
      _ => NoContent()
    )
}
```

### 2. Middleware Stack
```scala
def buildApp(routes: HttpRoutes[IO]): HttpApp[IO] = {
  val withErrorHandler = errorHandler(routes)
  val withAuth = requireAuth(withErrorHandler)
  val withTimeout = Timeout.httpRoutes[IO](30.seconds)(withAuth)
  val withLogging = Logger.httpRoutes(logHeaders = true, logBody = false)(withTimeout)
  val withCors = CORS.policy.withAllowOriginAll(withLogging)
  
  withCors.orNotFound
}
```

### 3. Query Parameters
```scala
object PageParam extends OptionalQueryParamDecoderMatcher[Int]("page")
object LimitParam extends OptionalQueryParamDecoderMatcher[Int]("limit")

val routes = HttpRoutes.of[IO] {
  case GET -> Root / "items" :? PageParam(page) +& LimitParam(limit) =>
    val pageNum = page.getOrElse(1)
    val limitNum = limit.getOrElse(10)
    listItems(pageNum, limitNum).flatMap(items => Ok(items.asJson))
}
```

## Production Patterns

### Layered Architecture
```scala
// Domain layer
sealed trait DomainError
case class User(id: Long, name: String)

// Service layer  
trait UserService[F[_]] {
  def findUser(id: Long): EitherT[F, DomainError, User]
  def createUser(req: CreateUserRequest): EitherT[F, DomainError, User]
}

// HTTP layer
class UserRoutes[F[_]: Monad](service: UserService[F]) {
  val routes = HttpRoutes.of[F] {
    case GET -> Root / "users" / LongVar(id) =>
      service.findUser(id).foldF(errorToResponse, user => Ok(user.asJson))
  }
}
```

### Error Handling
```scala
// Typed domain errors
sealed trait AppError
case class NotFound(resource: String, id: String) extends AppError
case class ValidationError(field: String, issue: String) extends AppError

// Convert to HTTP
def errorToResponse(error: AppError): IO[Response[IO]] = {
  val (status, errorType) = error match {
    case _: NotFound => (Status.NotFound, "not_found")
    case _: ValidationError => (Status.BadRequest, "validation_error")
  }
  
  Response[IO](status)
    .withEntity(ErrorResponse(errorType, error.message).asJson)
    .pure[IO]
}
```

## Tips for Rust Developers

1. **Pattern Matching vs Methods**: http4s uses pattern matching for routing, which is more declarative than method-based routing

2. **Explicit Effects**: Unlike Rust's `async/await` which is implicit, http4s makes effects explicit with `F[_]`

3. **Entity Codecs**: Need explicit `EntityEncoder` and `EntityDecoder` instances (like implementing `IntoResponse` and `FromRequest`)

4. **Resource Management**: Use `.build.use { server => ... }` pattern (similar to Rust's RAII/Drop)

5. **Middleware Composition**: Apply middleware by wrapping routes (similar to tower layers)

6. **Type Safety**: Leverage EitherT for typed errors (similar to Result but in async context)

## Common Mistakes

❌ **Forgetting implicit codecs**
```scala
// Won't compile without implicit EntityEncoder[IO, User]
Ok(user.asJson)
```

✅ **Provide implicit codecs**
```scala
implicit val userEncoder: EntityEncoder[IO, User] = jsonEncoderOf[IO, User]
Ok(user.asJson)
```

❌ **Not handling errors**
```scala
service.findUser(id).map(user => Ok(user.asJson))  // Wrong! Returns EitherT
```

✅ **Use foldF for EitherT**
```scala
service.findUser(id).foldF(errorToResponse, user => Ok(user.asJson))
```

❌ **Blocking IO**
```scala
val user = fetchFromDatabase()  // Blocking!
Ok(user.asJson)
```

✅ **Use IO**
```scala
fetchFromDatabase().flatMap(user => Ok(user.asJson))
```

## Testing

http4s routes can be tested without starting a server:

```scala
import org.http4s.implicits._

val request = Request[IO](Method.GET, uri"/users/1")
val response = routes.orNotFound.run(request).unsafeRunSync()

assert(response.status == Status.Ok)
```

## Next Steps

- **Chapter 10**: Connect to PostgreSQL with Doobie and manage migrations with Flyway
- **Chapter 11**: Build a complete API server with layered architecture

## Resources

- [http4s Documentation](https://http4s.org/)
- [Circe Documentation](https://circe.github.io/circe/)
- [Ember Server](https://http4s.org/v0.23/docs/integrations.html#ember)
