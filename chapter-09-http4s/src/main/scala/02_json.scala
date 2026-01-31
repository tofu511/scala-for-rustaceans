package http4sbasics

import cats.effect._
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
 * Chapter 09: JSON Encoding and Decoding with Circe
 *
 * RUST COMPARISON:
 * circe is similar to Rust's serde_json:
 * - Automatic derives for encoding/decoding (like #[derive(Serialize, Deserialize)])
 * - Type-safe JSON handling
 * - Encoder/Decoder type classes (like serde traits)
 *
 * Rust (serde):
 *   #[derive(Serialize, Deserialize)]
 *   struct User { name: String, age: u32 }
 *   
 *   let json = serde_json::to_string(&user)?;
 *   let user: User = serde_json::from_str(&json)?;
 *
 * Scala (circe):
 *   case class User(name: String, age: Int)
 *   import io.circe.generic.auto._
 *   
 *   user.asJson
 *   decode[User](json)
 */

object JsonExample extends IOApp.Simple {
  
  // Domain models - like Rust structs
  case class User(id: Long, name: String, email: String, age: Int)
  case class CreateUserRequest(name: String, email: String, age: Int)
  case class ErrorResponse(error: String, message: String)
  
  /*
   * JSON ENTITY ENCODERS/DECODERS
   * 
   * EntityEncoder/EntityDecoder bridge circe JSON with http4s.
   * Similar to implementing IntoResponse and FromRequest in axum.
   *
   * Rust (axum with serde_json):
   *   async fn create_user(Json(payload): Json<CreateUserRequest>) -> Json<User>
   *   
   * The Json<T> extractor automatically deserializes the request body.
   */
  
  // Automatic JSON encoders/decoders for all case classes
  implicit val userDecoder: EntityDecoder[IO, User] = jsonOf[IO, User]
  implicit val userEncoder: EntityEncoder[IO, User] = jsonEncoderOf[IO, User]
  implicit val createUserDecoder: EntityDecoder[IO, CreateUserRequest] = 
    jsonOf[IO, CreateUserRequest]
  implicit val errorEncoder: EntityEncoder[IO, ErrorResponse] = 
    jsonEncoderOf[IO, ErrorResponse]
  
  // In-memory "database" (just for demonstration)
  private var users: Map[Long, User] = Map(
    1L -> User(1, "Alice", "alice@example.com", 30),
    2L -> User(2, "Bob", "bob@example.com", 25)
  )
  private var nextId: Long = 3L
  
  /*
   * JSON API ROUTES
   * 
   * GET /users - List all users
   * GET /users/:id - Get user by ID
   * POST /users - Create new user
   * PUT /users/:id - Update user
   * DELETE /users/:id - Delete user
   */
  
  val userRoutes = HttpRoutes.of[IO] {
    
    // GET /users - Return all users as JSON array
    case GET -> Root / "users" =>
      val userList = users.values.toList
      Ok(userList.asJson)
    
    // GET /users/:id - Return specific user
    case GET -> Root / "users" / LongVar(id) =>
      users.get(id) match {
        case Some(user) => Ok(user.asJson)
        case None => NotFound(ErrorResponse(
          "not_found",
          s"User with id $id not found"
        ).asJson)
      }
    
    // POST /users - Create new user from JSON body
    case req @ POST -> Root / "users" =>
      for {
        createReq <- req.as[CreateUserRequest]  // Decode JSON
        newUser = User(nextId, createReq.name, createReq.email, createReq.age)
        _ = {
          users = users + (nextId -> newUser)
          nextId += 1
        }
        response <- Created(newUser.asJson)  // 201 Created with JSON body
      } yield response
    
    // PUT /users/:id - Update existing user
    case req @ PUT -> Root / "users" / LongVar(id) =>
      (for {
        createReq <- req.as[CreateUserRequest]
        result <- users.get(id) match {
          case Some(_) =>
            val updated = User(id, createReq.name, createReq.email, createReq.age)
            users = users + (id -> updated)
            Ok(updated.asJson)
          case None =>
            NotFound(ErrorResponse(
              "not_found",
              s"User with id $id not found"
            ).asJson)
        }
      } yield result)
    
    // DELETE /users/:id - Delete user
    case DELETE -> Root / "users" / LongVar(id) =>
      users.get(id) match {
        case Some(user) =>
          users = users - id
          Ok(Json.obj("message" -> Json.fromString(s"User ${user.name} deleted")))
        case None =>
          NotFound(ErrorResponse(
            "not_found",
            s"User with id $id not found"
          ).asJson)
      }
  }
  
  /*
   * ERROR HANDLING FOR INVALID JSON
   * 
   * When JSON decoding fails, http4s returns a DecodingFailure.
   * We can catch these and return proper error responses.
   *
   * Rust equivalent:
   *   match serde_json::from_str(&body) {
   *     Ok(user) => Ok(user),
   *     Err(e) => Err((StatusCode::BAD_REQUEST, e.to_string()))
   *   }
   */
  
  val safeUserRoutes = HttpRoutes.of[IO] {
    case req @ POST -> Root / "users" / "safe" =>
      req.as[CreateUserRequest].attempt.flatMap {
        case Right(createReq) =>
          val newUser = User(nextId, createReq.name, createReq.email, createReq.age)
          users = users + (nextId -> newUser)
          nextId += 1
          Created(newUser.asJson)
        
        case Left(e: DecodeFailure) =>
          BadRequest(ErrorResponse(
            "invalid_json",
            s"Failed to decode JSON: ${e.getMessage}"
          ).asJson)
        
        case Left(e) =>
          InternalServerError(ErrorResponse(
            "internal_error",
            e.getMessage
          ).asJson)
      }
  }
  
  /*
   * MANUAL JSON CONSTRUCTION
   * 
   * Sometimes you need to build JSON manually without case classes.
   * circe provides Json.obj and Json.arr for this.
   *
   * Rust (serde_json):
   *   json!({
   *     "status": "healthy",
   *     "version": "1.0.0",
   *     "users_count": users.len()
   *   })
   */
  
  val healthRoutes = HttpRoutes.of[IO] {
    case GET -> Root / "health" =>
      val response = Json.obj(
        "status" -> Json.fromString("healthy"),
        "version" -> Json.fromString("1.0.0"),
        "users_count" -> Json.fromInt(users.size),
        "timestamp" -> Json.fromLong(System.currentTimeMillis())
      )
      Ok(response)
  }
  
  val allRoutes = (userRoutes <+> safeUserRoutes <+> healthRoutes).orNotFound
  
  def run: IO[Unit] = {
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(allRoutes)
      .build
      .use { server =>
        IO.println(s"JSON API Server started at ${server.address}") *>
        IO.println("\nTry these commands:") *>
        IO.println("  # List all users") *>
        IO.println("  curl http://localhost:8080/users") *>
        IO.println("\n  # Get specific user") *>
        IO.println("  curl http://localhost:8080/users/1") *>
        IO.println("\n  # Create new user") *>
        IO.println("""  curl -X POST http://localhost:8080/users \
    -H "Content-Type: application/json" \
    -d '{"name":"Charlie","email":"charlie@example.com","age":28}'""") *>
        IO.println("\n  # Update user") *>
        IO.println("""  curl -X PUT http://localhost:8080/users/1 \
    -H "Content-Type: application/json" \
    -d '{"name":"Alice Smith","email":"alice@example.com","age":31}'""") *>
        IO.println("\n  # Delete user") *>
        IO.println("  curl -X DELETE http://localhost:8080/users/2") *>
        IO.println("\n  # Health check") *>
        IO.println("  curl http://localhost:8080/health") *>
        IO.println("\n  # Invalid JSON (error handling)") *>
        IO.println("""  curl -X POST http://localhost:8080/users/safe \
    -H "Content-Type: application/json" \
    -d '{"invalid json'""") *>
        IO.println("\nPress Ctrl+C to stop...") *>
        IO.never
      }
  }
}

/*
 * EXAMPLE USAGE:
 * 
 * $ curl http://localhost:8080/users
 * [
 *   {"id":1,"name":"Alice","email":"alice@example.com","age":30},
 *   {"id":2,"name":"Bob","email":"bob@example.com","age":25}
 * ]
 * 
 * $ curl -X POST http://localhost:8080/users \
 *   -H "Content-Type: application/json" \
 *   -d '{"name":"Charlie","email":"charlie@example.com","age":28}'
 * {"id":3,"name":"Charlie","email":"charlie@example.com","age":28}
 * 
 * $ curl http://localhost:8080/users/999
 * {"error":"not_found","message":"User with id 999 not found"}
 * 
 * $ curl -X POST http://localhost:8080/users/safe \
 *   -H "Content-Type: application/json" \
 *   -d '{"invalid}'
 * {"error":"invalid_json","message":"Failed to decode JSON: ..."}
 */

/*
 * KEY CONCEPTS FOR RUST DEVELOPERS:
 * 
 * 1. **circe Auto Derivation**:
 *    - import io.circe.generic.auto._
 *    - Like #[derive(Serialize, Deserialize)] in Rust
 *    - Works for case classes automatically
 * 
 * 2. **EntityEncoder/EntityDecoder**:
 *    - Bridge between circe and http4s
 *    - EntityEncoder[IO, A]: A => Response[IO]
 *    - EntityDecoder[IO, A]: Request[IO] => IO[A]
 *    - Similar to axum's Json extractor
 * 
 * 3. **req.as[T]**:
 *    - Decode request body to type T
 *    - Returns IO[T] (can fail)
 *    - Like serde_json::from_str in Rust
 * 
 * 4. **.asJson**:
 *    - Convert any value to JSON
 *    - Requires implicit Encoder[A]
 *    - Like serde_json::to_string in Rust
 * 
 * 5. **Error Handling**:
 *    - DecodeFailure for JSON parsing errors
 *    - Use .attempt to catch errors
 *    - Return proper HTTP status codes
 * 
 * 6. **Manual JSON**:
 *    - Json.obj for objects
 *    - Json.arr for arrays
 *    - Json.fromString/fromInt/etc for primitives
 *    - Like serde_json::json! macro
 * 
 * PERFORMANCE NOTE:
 * circe with generic derivation is slower than manual encoders
 * (similar to serde's derive being slower than manual impl).
 * For production, consider circe-derivation or manual instances.
 * 
 * NEXT: 03_middleware.scala covers logging, error handling, and CORS
 */
