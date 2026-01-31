package http4sbasics

import cats.effect._
import cats.syntax.semigroupk._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s._

/*
 * Chapter 09: http4s Basics - Routes and Request/Response Handling
 *
 * RUST COMPARISON:
 * http4s is similar to Rust's axum or actix-web frameworks:
 * - Routes defined with pattern matching (like axum's Router)
 * - Request/Response types (like axum's Request/Response)
 * - Middleware support (like tower middleware)
 * - Async by default with IO (like tokio futures)
 *
 * Key Differences:
 * Rust (axum):
 *   async fn handler() -> impl IntoResponse
 *   Router::new().route("/path", get(handler))
 *
 * Scala (http4s):
 *   def handler[F[_]]: HttpRoutes[F]
 *   HttpRoutes pattern matching with DSL
 */

object BasicRoutesExample extends IOApp.Simple {
  
  /*
   * HTTP ROUTES WITH DSL
   * 
   * The http4s-dsl provides pattern matching for HTTP methods and paths.
   * This is similar to axum's routing but uses Scala pattern matching.
   *
   * Rust equivalent with axum:
   *   async fn root() -> &'static str { "Hello, World!" }
   *   async fn hello(Path(name): Path<String>) -> String { 
   *     format!("Hello, {}!", name)
   *   }
   *   Router::new()
   *     .route("/", get(root))
   *     .route("/hello/:name", get(hello))
   */
  
  val helloRoutes = HttpRoutes.of[IO] {
    // GET /
    case GET -> Root =>
      Ok("Hello, World!")
    
    // GET /hello/:name
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name!")
    
    // GET /api/users/:id
    case GET -> Root / "api" / "users" / IntVar(userId) =>
      // IntVar extracts and parses Int from path
      Ok(s"User ID: $userId")
    
    // POST /echo with body
    case req @ POST -> Root / "echo" =>
      for {
        body <- req.as[String]
        response <- Ok(s"Echo: $body")
      } yield response
  }
  
  /*
   * QUERY PARAMETERS
   * 
   * Query parameters are extracted using matchers.
   * Similar to axum's Query extractor.
   *
   * Rust (axum):
   *   async fn search(Query(params): Query<HashMap<String, String>>) -> String {
   *     params.get("q").unwrap_or(&"".to_string()).clone()
   *   }
   */
  
  // Query parameter matcher
  object NameQueryParam extends QueryParamDecoderMatcher[String]("name")
  object AgeQueryParam extends OptionalQueryParamDecoderMatcher[Int]("age")
  
  val queryRoutes = HttpRoutes.of[IO] {
    // GET /greet?name=Alice
    case GET -> Root / "greet" :? NameQueryParam(name) =>
      Ok(s"Hello, $name!")
    
    // GET /profile?name=Alice&age=30
    case GET -> Root / "profile" :? NameQueryParam(name) +& AgeQueryParam(maybeAge) =>
      val ageStr = maybeAge.map(age => s", age $age").getOrElse("")
      Ok(s"Profile: $name$ageStr")
  }
  
  /*
   * HTTP STATUS CODES AND HEADERS
   * 
   * http4s provides constructor methods for all standard HTTP status codes.
   * Headers can be added using .putHeaders()
   *
   * Rust (axum):
   *   (StatusCode::NOT_FOUND, "Not found")
   *   (StatusCode::OK, [("Content-Type", "application/json")], body)
   */
  
  val statusRoutes = HttpRoutes.of[IO] {
    case GET -> Root / "success" =>
      Ok("Success!") // 200 OK
    
    case GET -> Root / "created" =>
      Created("Resource created") // 201 Created
    
    case GET -> Root / "not-found" =>
      NotFound("Resource not found") // 404 Not Found
    
    case GET -> Root / "error" =>
      InternalServerError("Something went wrong") // 500 Internal Server Error
    
    case GET -> Root / "custom-header" =>
      Ok("With header")
        .map(_.putHeaders(Header.Raw(org.typelevel.ci.CIString("X-Custom"), "custom-value")))
  }
  
  /*
   * COMBINING ROUTES
   * 
   * Multiple HttpRoutes can be combined using <+>
   * This is similar to merging routers in axum.
   *
   * Rust (axum):
   *   let app = Router::new()
   *     .merge(hello_routes)
   *     .merge(api_routes);
   */
  
  val allRoutes = 
    helloRoutes <+> queryRoutes <+> statusRoutes
  
  val httpApp = allRoutes.orNotFound  // .orNotFound provides 404 for unmatched routes
  
  /*
   * RUNNING THE SERVER
   * 
   * EmberServer is the built-in http4s server (powered by Cats-Effect).
   * Similar to running axum with tokio.
   *
   * Rust (axum):
   *   let listener = tokio::net::TcpListener::bind("0.0.0.0:8080").await?;
   *   axum::serve(listener, app).await?;
   */
  
  def run: IO[Unit] = {
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpApp)
      .build
      .use { server =>
        IO.println(s"Server started at ${server.address}") *>
        IO.println("Try:") *>
        IO.println("  curl http://localhost:8080/") *>
        IO.println("  curl http://localhost:8080/hello/Alice") *>
        IO.println("  curl http://localhost:8080/api/users/123") *>
        IO.println("  curl -X POST http://localhost:8080/echo -d 'Hello'") *>
        IO.println("  curl http://localhost:8080/greet?name=Bob") *>
        IO.println("  curl http://localhost:8080/profile?name=Charlie&age=25") *>
        IO.println("  curl http://localhost:8080/not-found") *>
        IO.println("Press Ctrl+C to stop...") *>
        IO.never  // Run forever until interrupted
      }
  }
}

/*
 * EXAMPLE OUTPUT:
 * 
 * When you run this and use curl:
 * 
 * $ curl http://localhost:8080/
 * Hello, World!
 * 
 * $ curl http://localhost:8080/hello/Alice
 * Hello, Alice!
 * 
 * $ curl http://localhost:8080/api/users/123
 * User ID: 123
 * 
 * $ curl -X POST http://localhost:8080/echo -d 'Test message'
 * Echo: Test message
 * 
 * $ curl http://localhost:8080/greet?name=Bob
 * Hello, Bob!
 * 
 * $ curl http://localhost:8080/profile?name=Charlie&age=25
 * Profile: Charlie, age 25
 * 
 * $ curl http://localhost:8080/not-found
 * Resource not found
 */

/*
 * KEY CONCEPTS FOR RUST DEVELOPERS:
 * 
 * 1. **HttpRoutes[F[_]]**: Like axum's Router, but effect-polymorphic
 *    - F[_] is typically IO (like Rust's Future)
 *    - Pattern matching for routing (vs function handlers)
 * 
 * 2. **DSL Pattern Matching**:
 *    - GET -> Root / "path" (vs .route("/path", get(handler)))
 *    - More declarative than imperative routing
 * 
 * 3. **Path Variables**:
 *    - IntVar, LongVar for typed extraction
 *    - String variables via pattern matching
 * 
 * 4. **Query Parameters**:
 *    - QueryParamDecoderMatcher for required params
 *    - OptionalQueryParamDecoderMatcher for optional
 *    - :? operator for query extraction
 * 
 * 5. **Status Constructors**:
 *    - Ok(), Created(), NotFound(), etc.
 *    - Similar to StatusCode::OK in Rust
 * 
 * 6. **Resource Management**:
 *    - .build.use { server => ... }
 *    - Ensures proper cleanup (like Rust's Drop)
 * 
 * NEXT: 02_json.scala covers JSON encoding/decoding with circe
 */
