package http4sbasics

import cats.effect._
import cats.data.{Kleisli, OptionT}
import cats.syntax.all._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.circe._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware._
import com.comcast.ip4s._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import scala.concurrent.duration._

/*
 * Chapter 09: Middleware - Logging, Error Handling, and CORS
 *
 * RUST COMPARISON:
 * http4s middleware is similar to tower middleware in Rust:
 * - Wraps HttpRoutes to add cross-cutting concerns
 * - Request logging (like tower_http::trace)
 * - Error recovery (like tower::ServiceBuilder error handlers)
 * - CORS (like tower_http::cors)
 *
 * Rust (tower/axum):
 *   let app = Router::new()
 *     .route("/", get(handler))
 *     .layer(TraceLayer::new_for_http())
 *     .layer(CorsLayer::permissive());
 *
 * Scala (http4s):
 *   val routes = HttpRoutes.of[IO] { ... }
 *   val withLogging = Logger.httpRoutes(logHeaders = true, logBody = true)(routes)
 *   val withCors = CORS.policy.withAllowOriginAll(withLogging)
 */

object MiddlewareExample extends IOApp.Simple {
  
  case class User(id: Long, name: String)
  case class ErrorResponse(error: String, message: String, timestamp: Long)
  
  implicit val userEncoder: EntityEncoder[IO, User] = jsonEncoderOf[IO, User]
  implicit val errorEncoder: EntityEncoder[IO, ErrorResponse] = 
    jsonEncoderOf[IO, ErrorResponse]
  
  /*
   * BASIC ROUTES WITH POTENTIAL ERRORS
   * 
   * These routes demonstrate different error scenarios:
   * - Success responses
   * - Not found errors
   * - Internal errors (exceptions)
   */
  
  val baseRoutes = HttpRoutes.of[IO] {
    case GET -> Root / "success" =>
      Ok(User(1, "Alice").asJson)
    
    case GET -> Root / "not-found" =>
      NotFound(ErrorResponse(
        "not_found",
        "Resource not found",
        System.currentTimeMillis()
      ).asJson)
    
    case GET -> Root / "error" =>
      // This will throw an exception
      IO.raiseError(new RuntimeException("Something went wrong!"))
    
    case GET -> Root / "timeout" =>
      // Simulates a slow endpoint
      IO.sleep(3.seconds) *> Ok("Finally done!")
  }
  
  /*
   * CUSTOM ERROR HANDLING MIDDLEWARE
   * 
   * Catches exceptions and converts them to proper HTTP error responses.
   * Similar to tower error handling layers in Rust.
   *
   * Rust (tower):
   *   ServiceBuilder::new()
   *     .layer(HandleErrorLayer::new(|err| async move {
   *       (StatusCode::INTERNAL_SERVER_ERROR, err.to_string())
   *     }))
   */
  
  def errorHandler(routes: HttpRoutes[IO]): HttpRoutes[IO] = {
    Kleisli { req: Request[IO] =>
      routes(req).handleErrorWith { error =>
        OptionT.liftF {
          error match {
            case _: java.util.concurrent.TimeoutException =>
              RequestTimeout(ErrorResponse(
                "timeout",
                "Request took too long",
                System.currentTimeMillis()
              ).asJson)
            
            case _ =>
              InternalServerError(ErrorResponse(
                "internal_error",
                error.getMessage,
                System.currentTimeMillis()
              ).asJson)
          }
        }
      }
    }
  }
  
  /*
   * REQUEST LOGGING MIDDLEWARE
   * 
   * http4s provides built-in Logger middleware for request/response logging.
   * Similar to tower_http::trace in Rust.
   *
   * Rust (tower_http):
   *   use tower_http::trace::TraceLayer;
   *   Router::new()
   *     .route("/", get(handler))
   *     .layer(TraceLayer::new_for_http())
   */
  
  // Logger.httpRoutes logs requests and responses
  def withLogging(routes: HttpRoutes[IO]): HttpRoutes[IO] = {
    Logger.httpRoutes(
      logHeaders = true,
      logBody = false,  // Set to true to log request/response bodies (verbose!)
      redactHeadersWhen = _ => false  // Customize header redaction
    )(routes)
  }
  
  /*
   * TIMEOUT MIDDLEWARE
   * 
   * Automatically times out requests that take too long.
   * Similar to tower::timeout in Rust.
   *
   * Rust (tower):
   *   ServiceBuilder::new()
   *     .layer(TimeoutLayer::new(Duration::from_secs(2)))
   */
  
  def withTimeout(routes: HttpRoutes[IO]): HttpRoutes[IO] = {
    Timeout.httpRoutes[IO](
      timeout = 2.seconds  // Fail requests that take longer than 2 seconds
    )(routes)
  }
  
  /*
   * CORS MIDDLEWARE
   * 
   * Adds Cross-Origin Resource Sharing headers.
   * Similar to tower_http::cors in Rust.
   *
   * Rust (tower_http):
   *   use tower_http::cors::{CorsLayer, Any};
   *   CorsLayer::new()
   *     .allow_origin(Any)
   *     .allow_methods(Any)
   */
  
  def withCors(routes: HttpRoutes[IO]): HttpRoutes[IO] = {
    CORS.policy
      .withAllowOriginAll
      .withAllowMethodsAll
      .withAllowHeadersAll
      .apply(routes)
  }
  
  /*
   * CUSTOM AUTHENTICATION MIDDLEWARE
   * 
   * Checks for an API key in the Authorization header.
   * Similar to custom tower middleware in Rust.
   *
   * Rust (tower):
   *   async fn auth_middleware(req: Request, next: Next) -> Result<Response> {
   *     if req.headers().get("authorization").is_none() {
   *       return Err(StatusCode::UNAUTHORIZED);
   *     }
   *     Ok(next.run(req).await)
   *   }
   */
  
  def requireAuth(routes: HttpRoutes[IO]): HttpRoutes[IO] = {
    Kleisli { req: Request[IO] =>
      req.headers.get(org.typelevel.ci.CIString("Authorization")) match {
        case Some(authHeader) if authHeader.head.value.startsWith("Bearer ") =>
          // Token present, proceed with request
          routes(req)
        
        case _ =>
          // No valid token, return 401
          OptionT.liftF(
            Unauthorized(
              org.http4s.headers.`WWW-Authenticate`(
                org.http4s.Challenge("Bearer", "api")
              ),
              ErrorResponse(
                "unauthorized",
                "Missing or invalid Authorization header",
                System.currentTimeMillis()
              ).asJson
            )
          )
      }
    }
  }
  
  /*
   * REQUEST ID MIDDLEWARE
   * 
   * Adds a unique request ID to each request for tracing.
   * Useful for correlating logs across services.
   *
   * Rust equivalent:
   *   Use uuid crate and add X-Request-ID header
   */
  
  def withRequestId(routes: HttpRoutes[IO]): HttpRoutes[IO] = {
    Kleisli { req: Request[IO] =>
      for {
        requestId <- OptionT.liftF(IO(java.util.UUID.randomUUID().toString))
        _ <- OptionT.liftF(IO.println(s"[Request ID: $requestId] ${req.method} ${req.uri}"))
        response <- routes(req)
        modifiedResponse = response.putHeaders(Header.Raw(org.typelevel.ci.CIString("X-Request-ID"), requestId))
      } yield modifiedResponse
    }
  }
  
  /*
   * COMPOSING MIDDLEWARE
   * 
   * Middleware can be composed by applying them in sequence.
   * The order matters - outermost middleware runs first.
   *
   * Rust (tower):
   *   ServiceBuilder::new()
   *     .layer(layer1)
   *     .layer(layer2)
   *     .layer(layer3)
   */
  
  // Public routes - no auth required
  val publicRoutes = HttpRoutes.of[IO] {
    case GET -> Root / "public" / "health" =>
      Ok("Healthy!")
  }
  
  // Protected routes - auth required
  val protectedRoutes = HttpRoutes.of[IO] {
    case GET -> Root / "api" / "profile" =>
      Ok(User(1, "Alice").asJson)
  }
  
  // Combine public and protected routes with different middleware
  val allRoutes = {
    val publicWithMiddleware = 
      withLogging(publicRoutes)
    
    val protectedWithMiddleware = 
      withRequestId(
        withTimeout(
          errorHandler(
            requireAuth(protectedRoutes)
          )
        )
      )
    
    // Also apply middleware to base routes for demonstration
    val baseWithMiddleware =
      withLogging(
        withTimeout(
          errorHandler(baseRoutes)
        )
      )
    
    // Combine all routes and apply CORS
    withCors(publicWithMiddleware <+> protectedWithMiddleware <+> baseWithMiddleware).orNotFound
  }
  
  def run: IO[Unit] = {
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(allRoutes)
      .build
      .use { server =>
        IO.println(s"Server with middleware started at ${server.address}") *>
        IO.println("\nPublic endpoint (no auth):") *>
        IO.println("  curl http://localhost:8080/public/health") *>
        IO.println("\nProtected endpoint (requires auth):") *>
        IO.println("  curl http://localhost:8080/api/profile") *>
        IO.println("""  curl -H "Authorization: Bearer secret-token" http://localhost:8080/api/profile""") *>
        IO.println("\nError handling:") *>
        IO.println("  curl http://localhost:8080/success") *>
        IO.println("  curl http://localhost:8080/not-found") *>
        IO.println("  curl http://localhost:8080/error") *>
        IO.println("\nTimeout (takes >2s, will timeout):") *>
        IO.println("  curl http://localhost:8080/timeout") *>
        IO.println("\nPress Ctrl+C to stop...") *>
        IO.never
      }
  }
}

/*
 * EXAMPLE OUTPUT:
 * 
 * $ curl http://localhost:8080/public/health
 * Healthy!
 * 
 * $ curl http://localhost:8080/api/profile
 * {"error":"unauthorized","message":"Missing or invalid Authorization header","timestamp":1234567890}
 * 
 * $ curl -H "Authorization: Bearer secret-token" http://localhost:8080/api/profile
 * {"id":1,"name":"Alice"}
 * 
 * $ curl http://localhost:8080/error
 * {"error":"internal_error","message":"Something went wrong!","timestamp":1234567890}
 * 
 * $ curl http://localhost:8080/timeout
 * {"error":"timeout","message":"Request took too long","timestamp":1234567890}
 * 
 * Server logs will show:
 * [Request ID: 123e4567-e89b-12d3-a456-426614174000] GET /api/profile
 */

/*
 * KEY CONCEPTS FOR RUST DEVELOPERS:
 * 
 * 1. **Middleware as HttpRoutes[F] => HttpRoutes[F]**:
 *    - Function that wraps routes
 *    - Similar to tower::Layer in Rust
 *    - Can inspect/modify requests and responses
 * 
 * 2. **Built-in Middleware**:
 *    - Logger: Request/response logging (like tower_http::trace)
 *    - CORS: Cross-origin headers (like tower_http::cors)
 *    - Timeout: Request timeouts (like tower::timeout)
 * 
 * 3. **Custom Middleware with Kleisli**:
 *    - Kleisli[OptionT[F, *], Request[F], Response[F]]
 *    - Can short-circuit (return OptionT.none for 404)
 *    - Can modify request/response
 * 
 * 4. **Error Handling**:
 *    - .handleErrorWith to catch exceptions
 *    - Convert to proper HTTP responses
 *    - Similar to tower error handling
 * 
 * 5. **Middleware Composition**:
 *    - Apply in sequence: f(g(h(routes)))
 *    - Order matters! Outer middleware runs first
 *    - Like tower::ServiceBuilder
 * 
 * 6. **Authentication**:
 *    - Check headers in middleware
 *    - Return 401 Unauthorized if missing
 *    - Pass through if valid
 * 
 * COMMON MIDDLEWARE PATTERNS:
 * - Logging: Track all requests/responses
 * - Auth: Validate tokens/credentials
 * - CORS: Handle cross-origin requests
 * - Timeout: Prevent slow requests
 * - Request ID: Trace requests across services
 * - Error handling: Convert exceptions to responses
 * - Metrics: Count requests, measure latency
 * 
 * NEXT: 04_error_handling.scala covers advanced error handling patterns
 */
