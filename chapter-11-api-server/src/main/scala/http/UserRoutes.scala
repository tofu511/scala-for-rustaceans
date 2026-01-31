package apiserver.http

import cats.effect._
import cats.syntax.all._
import cats.data.EitherT
import doobie._
import doobie.implicits._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.circe._
import org.http4s.server.middleware._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import apiserver.domain._
import apiserver.service.UserService

/*
 * HTTP LAYER
 * 
 * REST API routes. Translates HTTP requests to service calls and back.
 *
 * RUST COMPARISON:
 * Similar to HTTP handlers in axum:
 * - Define routes
 * - Parse requests
 * - Call service layer
 * - Map errors to HTTP responses
 *
 * Example Rust equivalent (axum):
 *   async fn create_user(
 *     State(service): State<UserService>,
 *     Json(req): Json<CreateUserRequest>
 *   ) -> Result<Json<User>, AppError> {
 *     let user = service.create_user(req).await?;
 *     Ok(Json(user))
 *   }
 */

class UserRoutes(service: UserService[ConnectionIO], xa: Transactor[IO]) {
  
  // JSON encoders/decoders
  implicit val userEncoder: EntityEncoder[IO, User] = jsonEncoderOf[IO, User]
  implicit val createUserDecoder: EntityDecoder[IO, CreateUserRequest] = 
    jsonOf[IO, CreateUserRequest]
  implicit val updateUserDecoder: EntityDecoder[IO, UpdateUserRequest] = 
    jsonOf[IO, UpdateUserRequest]
  implicit val errorEncoder: EntityEncoder[IO, ErrorResponse] = 
    jsonEncoderOf[IO, ErrorResponse]
  implicit val successEncoder: EntityEncoder[IO, SuccessResponse] = 
    jsonEncoderOf[IO, SuccessResponse]
  
  // Convert domain errors to HTTP responses
  private def errorToResponse(error: DomainError): IO[Response[IO]] = {
    val (status, errorType, details) = error match {
      case e: DomainError.UserNotFound =>
        (Status.NotFound, "not_found", Some(Map("id" -> e.id.toString)))
      
      case e: DomainError.ValidationError =>
        (Status.BadRequest, "validation_error", Some(Map("field" -> e.field, "issue" -> e.issue)))
      
      case e: DomainError.DuplicateEmail =>
        (Status.Conflict, "duplicate_email", Some(Map("email" -> e.email)))
      
      case _: DomainError.DatabaseError =>
        (Status.InternalServerError, "database_error", None)
    }
    
    Response[IO](status)
      .withEntity(ErrorResponse(errorType, error.message, details).asJson)
      .pure[IO]
  }
  
  // Routes
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    
    // GET /users - List all users
    case GET -> Root / "users" =>
      service.findAll()
        .value
        .transact(xa)
        .flatMap {
          case Right(users) => Ok(users.asJson)
          case Left(error) => errorToResponse(error)
        }
    
    // GET /users/:id - Get user by ID
    case GET -> Root / "users" / LongVar(id) =>
      service.findById(id)
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
        result <- service.create(createReq).value.transact(xa)
        response <- result match {
          case Right(user) => Created(user.asJson)
          case Left(error) => errorToResponse(error)
        }
      } yield response).handleErrorWith { error =>
        errorToResponse(DomainError.DatabaseError(error.getMessage))
      }
    
    // PUT /users/:id - Update user
    case req @ PUT -> Root / "users" / LongVar(id) =>
      (for {
        updateReq <- req.as[UpdateUserRequest]
        result <- service.update(id, updateReq).value.transact(xa)
        response <- result match {
          case Right(user) => Ok(user.asJson)
          case Left(error) => errorToResponse(error)
        }
      } yield response).handleErrorWith { error =>
        errorToResponse(DomainError.DatabaseError(error.getMessage))
      }
    
    // DELETE /users/:id - Delete user
    case DELETE -> Root / "users" / LongVar(id) =>
      service.delete(id)
        .value
        .transact(xa)
        .flatMap {
          case Right(_) => Ok(SuccessResponse(s"User $id deleted").asJson)
          case Left(error) => errorToResponse(error)
        }
    
    // GET /health - Health check
    case GET -> Root / "health" =>
      Ok(Json.obj(
        "status" -> Json.fromString("healthy"),
        "timestamp" -> Json.fromLong(System.currentTimeMillis())
      ))
  }
  
  // Add middleware
  val routesWithMiddleware: HttpRoutes[IO] = {
    Logger.httpRoutes(
      logHeaders = true,
      logBody = false
    )(routes)
  }
}
