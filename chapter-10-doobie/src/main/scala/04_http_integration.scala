package doobiebasics

import cats.effect._
import cats.data.EitherT
import cats.syntax.all._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.hikari.HikariTransactor
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.circe._
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import java.time.LocalDateTime

/*
 * Chapter 10: Integrating Doobie with http4s
 *
 * RUST COMPARISON:
 * This is like integrating sqlx with axum in Rust:
 * - Pass Pool/Transactor to handlers
 * - Use EitherT for typed errors
 * - Map database errors to HTTP responses
 *
 * Rust (axum + sqlx):
 *   #[derive(Clone)]
 *   struct AppState {
 *     pool: PgPool,
 *   }
 *   
 *   async fn get_user(
 *     State(state): State<AppState>,
 *     Path(id): Path<i64>
 *   ) -> Result<Json<User>, AppError> {
 *     let user = sqlx::query_as("SELECT ...")
 *       .fetch_one(&state.pool)
 *       .await?;
 *     Ok(Json(user))
 *   }
 *
 * Scala (http4s + Doobie):
 *   class UserRoutes(xa: Transactor[IO]) {
 *     val routes = HttpRoutes.of[IO] {
 *       case GET -> Root / "users" / LongVar(id) =>
 *         findUser(id).foldF(errorToResponse, user => Ok(user.asJson))
 *     }
 *   }
 */

object HttpIntegrationExample extends IOApp.Simple {
  
  /*
   * DOMAIN MODELS
   */
  
  case class User(
    id: Long,
    name: String,
    email: String,
    age: Int,
    createdAt: LocalDateTime
  )
  
  case class CreateUserRequest(
    name: String,
    email: String,
    age: Int
  )
  
  case class UpdateUserRequest(
    name: String,
    email: String,
    age: Int
  )
  
  /*
   * ERROR TYPES
   */
  
  sealed trait AppError {
    def message: String
  }
  
  object AppError {
    case class NotFound(resource: String, id: Long) extends AppError {
      def message: String = s"$resource with id $id not found"
    }
    
    case class ValidationError(field: String, issue: String) extends AppError {
      def message: String = s"Validation failed for $field: $issue"
    }
    
    case class DuplicateEmail(email: String) extends AppError {
      def message: String = s"User with email $email already exists"
    }
    
    case class DatabaseError(cause: String) extends AppError {
      def message: String = s"Database error: $cause"
    }
  }
  
  case class ErrorResponse(error: String, message: String)
  
  /*
   * JSON CODECS
   */
  
  implicit val userEncoder: EntityEncoder[IO, User] = jsonEncoderOf[IO, User]
  implicit val createUserDecoder: EntityDecoder[IO, CreateUserRequest] = 
    jsonOf[IO, CreateUserRequest]
  implicit val updateUserDecoder: EntityDecoder[IO, UpdateUserRequest] = 
    jsonOf[IO, UpdateUserRequest]
  implicit val errorEncoder: EntityEncoder[IO, ErrorResponse] = 
    jsonEncoderOf[IO, ErrorResponse]
  
  /*
   * REPOSITORY LAYER
   * 
   * Database operations with typed errors.
   * Similar to Rust repository pattern.
   */
  
  object UserRepository {
    
    def findById(id: Long): EitherT[ConnectionIO, AppError, User] = {
      EitherT(
        sql"SELECT id, name, email, age, created_at FROM users WHERE id = $id"
          .query[User]
          .option
          .attemptSql
          .map {
            case Right(Some(user)) => Right(user)
            case Right(None) => Left(AppError.NotFound("User", id))
            case Left(error: Throwable) => Left(AppError.DatabaseError(error.getMessage))
          }
      )
    }
    
    def findAll(): EitherT[ConnectionIO, AppError, List[User]] = {
      EitherT(
        sql"SELECT id, name, email, age, created_at FROM users ORDER BY id"
          .query[User]
          .to[List]
          .attemptSql
          .map {
            case Right(users) => Right(users)
            case Left(error: Throwable) => Left(AppError.DatabaseError(error.getMessage))
          }
      )
    }
    
    def findByEmail(email: String): ConnectionIO[Option[User]] = {
      sql"SELECT id, name, email, age, created_at FROM users WHERE email = $email"
        .query[User]
        .option
    }
    
    def create(req: CreateUserRequest): EitherT[ConnectionIO, AppError, User] = {
      EitherT(
        sql"""INSERT INTO users (name, email, age)
              VALUES (${req.name}, ${req.email}, ${req.age})
              RETURNING id, name, email, age, created_at"""
          .query[User]
          .unique
          .attemptSql
          .map {
            case Right(user) => Right(user)
            case Left(error: Throwable) => 
              if (error.getMessage.contains("unique") || error.getMessage.contains("duplicate"))
                Left(AppError.DuplicateEmail(req.email))
              else
                Left(AppError.DatabaseError(error.getMessage))
          }
      )
    }
    
    def update(id: Long, req: UpdateUserRequest): EitherT[ConnectionIO, AppError, User] = {
      EitherT(
        (for {
          // Check if user exists
          _ <- findById(id).value
          // Update
          result <- sql"""UPDATE users 
                SET name = ${req.name}, email = ${req.email}, age = ${req.age}
                WHERE id = $id
                RETURNING id, name, email, age, created_at"""
            .query[User]
            .unique
            .map(user => Right(user): Either[AppError, User])
        } yield result).attemptSql.map {
          case Right(result) => result
          case Left(error: Throwable) => Left(AppError.DatabaseError(error.getMessage))
        }
      )
    }
    
    def delete(id: Long): EitherT[ConnectionIO, AppError, Unit] = {
      for {
        _ <- findById(id)  // Check if exists
        _ <- EitherT.right[AppError](
          sql"DELETE FROM users WHERE id = $id".update.run.void
        )
      } yield ()
    }
  }
  
  /*
   * SERVICE LAYER
   * 
   * Business logic with validation.
   */
  
  object UserService {
    
    def validate(req: CreateUserRequest): Either[AppError, CreateUserRequest] = {
      val errors = List(
        if (req.name.trim.isEmpty) 
          Some(AppError.ValidationError("name", "cannot be empty"))
        else None,
        
        if (!req.email.contains("@")) 
          Some(AppError.ValidationError("email", "must be valid"))
        else None,
        
        if (req.age < 0 || req.age > 150) 
          Some(AppError.ValidationError("age", "must be between 0 and 150"))
        else None
      ).flatten
      
      errors.headOption.map(Left(_)).getOrElse(Right(req))
    }
    
    def createUser(req: CreateUserRequest): EitherT[ConnectionIO, AppError, User] = {
      for {
        validated <- EitherT.fromEither[ConnectionIO](validate(req))
        user <- UserRepository.create(validated)
      } yield user
    }
    
    def updateUser(id: Long, req: UpdateUserRequest): EitherT[ConnectionIO, AppError, User] = {
      for {
        validated <- EitherT.fromEither[ConnectionIO](validate(
          CreateUserRequest(req.name, req.email, req.age)
        ))
        user <- UserRepository.update(id, req)
      } yield user
    }
  }
  
  /*
   * HTTP ROUTES
   * 
   * Expose database operations via REST API.
   */
  
  class UserRoutes(xa: Transactor[IO]) {
    
    def errorToResponse(error: AppError): IO[Response[IO]] = {
      val (status, errorType) = error match {
        case _: AppError.NotFound => (Status.NotFound, "not_found")
        case _: AppError.ValidationError => (Status.BadRequest, "validation_error")
        case _: AppError.DuplicateEmail => (Status.Conflict, "duplicate_email")
        case _: AppError.DatabaseError => (Status.InternalServerError, "database_error")
      }
      
      Response[IO](status)
        .withEntity(ErrorResponse(errorType, error.message).asJson)
        .pure[IO]
    }
    
    val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
      
      // GET /users - List all users
      case GET -> Root / "users" =>
        UserRepository.findAll()
          .value
          .transact(xa)
          .flatMap {
            case Right(users) => Ok(users.asJson)
            case Left(error) => errorToResponse(error)
          }
      
      // GET /users/:id - Get user by ID
      case GET -> Root / "users" / LongVar(id) =>
        UserRepository.findById(id)
          .value
          .transact(xa)
          .flatMap {
            case Right(user) => Ok(user.asJson)
            case Left(error) => errorToResponse(error)
          }
      
      // POST /users - Create user
      case req @ POST -> Root / "users" =>
        (for {
          createReq <- req.as[CreateUserRequest]
          result <- UserService.createUser(createReq).value.transact(xa)
          response <- result match {
            case Right(user) => Created(user.asJson)
            case Left(error) => errorToResponse(error)
          }
        } yield response).handleErrorWith { error =>
          errorToResponse(AppError.DatabaseError(error.getMessage))
        }
      
      // PUT /users/:id - Update user
      case req @ PUT -> Root / "users" / LongVar(id) =>
        (for {
          updateReq <- req.as[UpdateUserRequest]
          result <- UserService.updateUser(id, updateReq).value.transact(xa)
          response <- result match {
            case Right(user) => Ok(user.asJson)
            case Left(error) => errorToResponse(error)
          }
        } yield response).handleErrorWith { error =>
          errorToResponse(AppError.DatabaseError(error.getMessage))
        }
      
      // DELETE /users/:id - Delete user
      case DELETE -> Root / "users" / LongVar(id) =>
        UserRepository.delete(id)
          .value
          .transact(xa)
          .flatMap {
            case Right(_) => Ok(Json.obj("message" -> Json.fromString(s"User $id deleted")))
            case Left(error) => errorToResponse(error)
          }
    }
  }
  
  /*
   * APPLICATION SETUP
   */
  
  val dbUrl = "jdbc:postgresql://localhost:5432/testdb"
  val dbUser = "postgres"
  val dbPassword = "password"
  
  def createTransactor(): Resource[IO, HikariTransactor[IO]] = {
    for {
      ec <- ExecutionContexts.fixedThreadPool[IO](10)
      xa <- HikariTransactor.newHikariTransactor[IO](
        driverClassName = "org.postgresql.Driver",
        url = dbUrl,
        user = dbUser,
        pass = dbPassword,
        connectEC = ec
      )
    } yield xa
  }
  
  def run: IO[Unit] = {
    createTransactor().use { xa =>
      val userRoutes = new UserRoutes(xa)
      
      EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(userRoutes.routes.orNotFound)
        .build
        .use { server =>
          IO.println(s"✅ User API started at ${server.address}") *>
          IO.println("\nTest commands:") *>
          IO.println("""  curl -X POST http://localhost:8080/users -H "Content-Type: application/json" -d '{"name":"Alice","email":"alice@example.com","age":30}'""") *>
          IO.println("  curl http://localhost:8080/users") *>
          IO.println("  curl http://localhost:8080/users/1") *>
          IO.println("""  curl -X PUT http://localhost:8080/users/1 -H "Content-Type: application/json" -d '{"name":"Alice Smith","email":"alice@example.com","age":31}'""") *>
          IO.println("  curl -X DELETE http://localhost:8080/users/1") *>
          IO.println("\nPress Ctrl+C to stop...") *>
          IO.never
        }
    }.handleErrorWith { error =>
      IO.println(s"Error: ${error.getMessage}") *>
      IO.println("\nMake sure PostgreSQL is running and migrations are applied.")
    }
  }
}

/*
 * KEY CONCEPTS FOR RUST DEVELOPERS:
 * 
 * 1. **Layered Architecture**:
 *    - Repository: Database operations
 *    - Service: Business logic and validation
 *    - Routes: HTTP handling
 *    - Similar to Rust web app structure
 * 
 * 2. **Transactor as Dependency**:
 *    - Pass Transactor[IO] to routes
 *    - Like passing Arc<PgPool> in Rust
 * 
 * 3. **EitherT Through Layers**:
 *    - EitherT[ConnectionIO, E, A] in repository
 *    - .transact(xa) in route layer
 *    - Type-safe error propagation
 * 
 * 4. **.mapK(xa.trans)**:
 *    - Transform ConnectionIO to IO
 *    - When composing EitherT operations
 * 
 * 5. **Error to HTTP Response**:
 *    - Centralized error mapping
 *    - Consistent error format
 *    - Like IntoResponse in axum
 * 
 * PRODUCTION PATTERN:
 * 
 * Repository Layer:
 *   - Pure database operations
 *   - Returns EitherT[ConnectionIO, DomainError, Result]
 * 
 * Service Layer:
 *   - Business logic
 *   - Validation
 *   - Composes repository operations
 * 
 * HTTP Layer:
 *   - Takes Transactor as dependency
 *   - .transact(xa) to execute queries
 *   - .foldF() to handle errors
 *   - Maps to HTTP responses
 * 
 * This is THE recommended pattern for production Scala web services!
 * 
 * NEXT: Exercises to practice Doobie with http4s
 */
