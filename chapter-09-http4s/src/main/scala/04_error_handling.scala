package http4sbasics

import cats.effect._
import cats.data.EitherT
import cats.syntax.all._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.circe._
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._

/*
 * Chapter 09: Advanced Error Handling with EitherT
 *
 * RUST COMPARISON:
 * This pattern combines http4s with the EitherT[IO, Error, Response] pattern
 * learned in Chapter 7. It's similar to Rust's Result-based error handling
 * with custom error types.
 *
 * Rust:
 *   enum AppError {
 *     NotFound(String),
 *     ValidationError(String),
 *     DatabaseError(String),
 *   }
 *   
 *   impl IntoResponse for AppError {
 *     fn into_response(self) -> Response { ... }
 *   }
 *   
 *   async fn handler() -> Result<Json<User>, AppError>
 *
 * Scala:
 *   sealed trait AppError
 *   case class NotFound(msg: String) extends AppError
 *   case class ValidationError(msg: String) extends AppError
 *   
 *   def toResponse[F[_]](error: AppError): Response[F]
 *   
 *   def handler: EitherT[IO, AppError, User]
 */

object AdvancedErrorHandlingExample extends IOApp.Simple {
  
  /*
   * DOMAIN ERRORS
   * 
   * Define application-specific errors as a sealed trait.
   * Similar to Rust's enum for error types.
   */
  
  sealed trait AppError {
    def message: String
  }
  
  object AppError {
    case class NotFound(resource: String, id: String) extends AppError {
      def message: String = s"$resource with id $id not found"
    }
    
    case class ValidationError(field: String, issue: String) extends AppError {
      def message: String = s"Validation failed for $field: $issue"
    }
    
    case class DatabaseError(cause: String) extends AppError {
      def message: String = s"Database error: $cause"
    }
    
    case class Unauthorized(reason: String) extends AppError {
      def message: String = s"Unauthorized: $reason"
    }
    
    case class InternalError(cause: String) extends AppError {
      def message: String = s"Internal error: $cause"
    }
  }
  
  /*
   * DOMAIN MODELS
   */
  
  case class User(id: Long, name: String, email: String, age: Int)
  case class CreateUserRequest(name: String, email: String, age: Int)
  
  // Error response format for JSON
  case class ErrorResponse(
    error: String,
    message: String,
    details: Option[Map[String, String]] = None
  )
  
  implicit val userDecoder: EntityDecoder[IO, User] = jsonOf[IO, User]
  implicit val userEncoder: EntityEncoder[IO, User] = jsonEncoderOf[IO, User]
  implicit val createUserDecoder: EntityDecoder[IO, CreateUserRequest] = 
    jsonOf[IO, CreateUserRequest]
  implicit val errorEncoder: EntityEncoder[IO, ErrorResponse] = 
    jsonEncoderOf[IO, ErrorResponse]
  
  /*
   * CONVERT ERRORS TO HTTP RESPONSES
   * 
   * Map domain errors to appropriate HTTP status codes and response bodies.
   * Similar to implementing IntoResponse for custom error types in Rust.
   */
  
  def errorToResponse(error: AppError): IO[Response[IO]] = {
    val (status, errorType, details) = error match {
      case e: AppError.NotFound =>
        (Status.NotFound, "not_found", Some(Map(
          "resource" -> e.resource,
          "id" -> e.id
        )))
      
      case e: AppError.ValidationError =>
        (Status.BadRequest, "validation_error", Some(Map(
          "field" -> e.field,
          "issue" -> e.issue
        )))
      
      case e: AppError.DatabaseError =>
        (Status.InternalServerError, "database_error", None)
      
      case e: AppError.Unauthorized =>
        (Status.Unauthorized, "unauthorized", None)
      
      case e: AppError.InternalError =>
        (Status.InternalServerError, "internal_error", None)
    }
    
    val response = ErrorResponse(errorType, error.message, details)
    Response[IO](status).withEntity(response.asJson).pure[IO]
  }
  
  /*
   * BUSINESS LOGIC WITH TYPED ERRORS
   * 
   * Service layer that returns EitherT[IO, AppError, A]
   * Similar to Rust functions returning Result<T, AppError>
   */
  
  // Simulated database
  private var users: Map[Long, User] = Map(
    1L -> User(1, "Alice", "alice@example.com", 30),
    2L -> User(2, "Bob", "bob@example.com", 25)
  )
  private var nextId: Long = 3L
  
  object UserService {
    
    // Find user by ID
    def findUser(id: Long): EitherT[IO, AppError, User] = {
      EitherT.fromOption[IO](
        users.get(id),
        AppError.NotFound("User", id.toString)
      )
    }
    
    // Validate user creation request
    def validateCreateRequest(req: CreateUserRequest): EitherT[IO, AppError, CreateUserRequest] = {
      val validations = List(
        if (req.name.trim.isEmpty)
          Some(AppError.ValidationError("name", "cannot be empty"))
        else None,
        
        if (!req.email.contains("@"))
          Some(AppError.ValidationError("email", "must be valid email"))
        else None,
        
        if (req.age < 0 || req.age > 150)
          Some(AppError.ValidationError("age", "must be between 0 and 150"))
        else None
      ).flatten
      
      validations.headOption match {
        case Some(error) => EitherT.leftT[IO, CreateUserRequest](error)
        case None => EitherT.rightT[IO, AppError](req)
      }
    }
    
    // Create new user
    def createUser(req: CreateUserRequest): EitherT[IO, AppError, User] = {
      for {
        validated <- validateCreateRequest(req)
        // Check if email already exists
        _ <- EitherT.cond[IO](
          !users.values.exists(_.email == validated.email),
          (),
          AppError.ValidationError("email", "already exists"): AppError
        )
        newUser = User(nextId, validated.name, validated.email, validated.age)
        _ <- EitherT.right[AppError](IO {
          users = users + (nextId -> newUser)
          nextId += 1
        })
      } yield newUser
    }
    
    // Update user
    def updateUser(id: Long, req: CreateUserRequest): EitherT[IO, AppError, User] = {
      for {
        _ <- findUser(id)  // Check if exists
        validated <- validateCreateRequest(req)
        updated = User(id, validated.name, validated.email, validated.age)
        _ <- EitherT.right[AppError](IO {
          users = users + (id -> updated)
        })
      } yield updated
    }
    
    // Delete user
    def deleteUser(id: Long): EitherT[IO, AppError, Unit] = {
      for {
        _ <- findUser(id)  // Check if exists
        _ <- EitherT.right[AppError](IO {
          users = users - id
        })
      } yield ()
    }
    
    // List all users
    def listUsers(): EitherT[IO, AppError, List[User]] = {
      EitherT.rightT[IO, AppError](users.values.toList)
    }
  }
  
  /*
   * HTTP ROUTES WITH ERROR HANDLING
   * 
   * Routes call service methods and handle errors uniformly.
   * Similar to axum handlers that return Result<Json<T>, AppError>
   */
  
  val userRoutes = HttpRoutes.of[IO] {
    
    // GET /users - List all users
    case GET -> Root / "users" =>
      UserService.listUsers()
        .foldF(errorToResponse, users => Ok(users.asJson))
    
    // GET /users/:id - Get user by ID
    case GET -> Root / "users" / LongVar(id) =>
      UserService.findUser(id)
        .foldF(errorToResponse, user => Ok(user.asJson))
    
    // POST /users - Create new user
    case req @ POST -> Root / "users" =>
      (for {
        createReq <- EitherT.liftF[IO, AppError, CreateUserRequest](
          req.as[CreateUserRequest]
        )
        user <- UserService.createUser(createReq)
      } yield user)
        .foldF(errorToResponse, user => Created(user.asJson))
    
    // PUT /users/:id - Update user
    case req @ PUT -> Root / "users" / LongVar(id) =>
      (for {
        createReq <- EitherT.liftF[IO, AppError, CreateUserRequest](
          req.as[CreateUserRequest]
        )
        user <- UserService.updateUser(id, createReq)
      } yield user)
        .foldF(errorToResponse, user => Ok(user.asJson))
    
    // DELETE /users/:id - Delete user
    case DELETE -> Root / "users" / LongVar(id) =>
      UserService.deleteUser(id)
        .foldF(
          errorToResponse,
          _ => Ok(Json.obj("message" -> Json.fromString(s"User $id deleted")))
        )
  }
  
  /*
   * SIMULATE DATABASE ERRORS
   * 
   * Endpoint that demonstrates database error handling
   */
  
  val errorDemoRoutes = HttpRoutes.of[IO] {
    case GET -> Root / "demo" / "db-error" =>
      val error: AppError = AppError.DatabaseError("Connection timeout")
      EitherT.leftT[IO, User](error)
        .foldF(errorToResponse, user => Ok(user.asJson))
    
    case GET -> Root / "demo" / "unauthorized" =>
      val error: AppError = AppError.Unauthorized("Invalid API key")
      EitherT.leftT[IO, User](error)
        .foldF(errorToResponse, user => Ok(user.asJson))
  }
  
  val allRoutes = (userRoutes <+> errorDemoRoutes).orNotFound
  
  def run: IO[Unit] = {
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(allRoutes)
      .build
      .use { server =>
        IO.println(s"Server with typed errors started at ${server.address}") *>
        IO.println("\nValid requests:") *>
        IO.println("  curl http://localhost:8080/users") *>
        IO.println("  curl http://localhost:8080/users/1") *>
        IO.println("""  curl -X POST http://localhost:8080/users -H "Content-Type: application/json" -d '{"name":"Charlie","email":"charlie@example.com","age":28}'""") *>
        IO.println("\nValidation errors:") *>
        IO.println("""  curl -X POST http://localhost:8080/users -H "Content-Type: application/json" -d '{"name":"","email":"invalid","age":200}'""") *>
        IO.println("\nNot found:") *>
        IO.println("  curl http://localhost:8080/users/999") *>
        IO.println("\nError demos:") *>
        IO.println("  curl http://localhost:8080/demo/db-error") *>
        IO.println("  curl http://localhost:8080/demo/unauthorized") *>
        IO.println("\nPress Ctrl+C to stop...") *>
        IO.never
      }
  }
}

/*
 * KEY CONCEPTS FOR RUST DEVELOPERS:
 * 
 * 1. **Typed Errors with EitherT**:
 *    - EitherT[IO, AppError, A] ≈ Result<A, AppError> in async context
 *    - Type-safe error handling at compile time
 *    - Can compose operations with for-comprehension
 * 
 * 2. **Error to Response Mapping**:
 *    - Similar to impl IntoResponse for AppError in Rust
 *    - Centralized error-to-HTTP conversion
 *    - Consistent error response format
 * 
 * 3. **Service Layer**:
 *    - Business logic returns EitherT
 *    - Validation errors vs domain errors
 *    - Composable with flatMap/for-comprehension
 * 
 * 4. **.foldF() Pattern**:
 *    - foldF(onError, onSuccess)
 *    - Similar to Rust's match on Result
 *    - Converts EitherT to IO[Response]
 * 
 * 5. **Validation Composition**:
 *    - Validate multiple fields
 *    - Return first error (short-circuit)
 *    - For accumulating errors, use Validated (Chapter 6)
 * 
 * 6. **Benefits Over Exception-Based**:
 *    - Type safety - errors in function signature
 *    - Exhaustive handling - compiler checks
 *    - Composition - easy to chain operations
 *    - Testing - errors are values, easy to test
 * 
 * PRODUCTION PATTERN:
 * This is THE recommended pattern for Scala web services:
 * - Typed errors in domain layer (EitherT[F, DomainError, A])
 * - Convert to HTTP responses at route layer
 * - Consistent error format across API
 * - Similar to Rust's Result<T, E> pattern
 * 
 * NEXT: Exercises to practice http4s routes, JSON, and error handling
 */
