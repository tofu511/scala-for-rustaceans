package http4sbasics.exercises

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
 * EXERCISE 01: Build a Book API with http4s
 *
 * OBJECTIVES:
 * - Implement REST API routes (GET, POST, PUT, DELETE)
 * - Handle JSON encoding/decoding with circe
 * - Use EitherT for typed error handling
 * - Add middleware for logging
 *
 * RUST COMPARISON:
 * This is similar to building a REST API with axum in Rust:
 * - Define domain models (like Rust structs)
 * - Create routes with pattern matching
 * - Handle errors with Result types
 * - Add middleware for cross-cutting concerns
 *
 * TASKS:
 * 1. Complete the Book model and CreateBookRequest
 * 2. Implement BookService methods with typed errors
 * 3. Create HTTP routes for CRUD operations
 * 4. Add error handling middleware
 */

object Exercise01BookAPI extends IOApp.Simple {
  
  // TASK 1: Complete the domain models
  // Hint: A book should have id, title, author, year, and isbn
  case class Book(
    id: Long = 0,
    title: String = "",
    author: String = "",
    year: Int = 0,
    isbn: String = ""
  )
  
  case class CreateBookRequest(
    title: String = "",
    author: String = "",
    year: Int = 0,
    isbn: String = ""
  )
  
  // Error types for the API
  sealed trait BookError {
    def message: String
  }
  
  object BookError {
    case class NotFound(id: Long) extends BookError {
      def message: String = ???
    }
    
    case class ValidationError(field: String, issue: String) extends BookError {
      def message: String = ???
    }
    
    case class DuplicateISBN(isbn: String) extends BookError {
      def message: String = ???
    }
  }
  
  case class ErrorResponse(error: String, message: String)
  
  // TASK 2: Add implicit encoders/decoders for JSON
  // Hint: Use jsonOf[IO, T] for decoders and jsonEncoderOf[IO, T] for encoders
  implicit val bookDecoder: EntityDecoder[IO, Book] = ???
  implicit val bookEncoder: EntityEncoder[IO, Book] = ???
  implicit val createBookDecoder: EntityDecoder[IO, CreateBookRequest] = ???
  implicit val errorEncoder: EntityEncoder[IO, ErrorResponse] = ???
  
  // In-memory "database"
  private var books: Map[Long, Book] = Map.empty
  private var nextId: Long = 1L
  
  object BookService {
    
    // TASK 3: Implement findBook
    // Hint: Use EitherT.fromOption with BookError.NotFound
    def findBook(id: Long): EitherT[IO, BookError, Book] = {
      ???
    }
    
    // TASK 4: Implement validation
    // Hint: Check that title and author are not empty, year is reasonable
    def validateCreateRequest(req: CreateBookRequest): EitherT[IO, BookError, CreateBookRequest] = {
      ???
    }
    
    // TASK 5: Implement createBook
    // Hint: First validate, then check for duplicate ISBN, then create
    def createBook(req: CreateBookRequest): EitherT[IO, BookError, Book] = {
      ???
    }
    
    // TASK 6: Implement updateBook
    // Hint: Find book first, validate request, then update
    def updateBook(id: Long, req: CreateBookRequest): EitherT[IO, BookError, Book] = {
      ???
    }
    
    // TASK 7: Implement deleteBook
    def deleteBook(id: Long): EitherT[IO, BookError, Unit] = {
      ???
    }
    
    // TASK 8: Implement listBooks
    def listBooks(): EitherT[IO, BookError, List[Book]] = {
      ???
    }
  }
  
  // TASK 9: Convert errors to HTTP responses
  // Hint: Pattern match on error type and return appropriate status
  def errorToResponse(error: BookError): IO[Response[IO]] = {
    ???
  }
  
  // TASK 10: Implement HTTP routes
  val bookRoutes = HttpRoutes.of[IO] {
    // GET /books - List all books
    case GET -> Root / "books" =>
      ???
    
    // GET /books/:id - Get book by ID
    case GET -> Root / "books" / LongVar(id) =>
      ???
    
    // POST /books - Create new book
    case req @ POST -> Root / "books" =>
      ???
    
    // PUT /books/:id - Update book
    case req @ PUT -> Root / "books" / LongVar(id) =>
      ???
    
    // DELETE /books/:id - Delete book
    case DELETE -> Root / "books" / LongVar(id) =>
      ???
  }
  
  def run: IO[Unit] = {
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(bookRoutes.orNotFound)
      .build
      .use { server =>
        IO.println(s"Book API started at ${server.address}") *>
        IO.println("Test with:") *>
        IO.println("""  curl -X POST http://localhost:8080/books -H "Content-Type: application/json" -d '{"title":"The Rust Book","author":"Steve Klabnik","year":2018,"isbn":"978-1593278281"}'""") *>
        IO.println("  curl http://localhost:8080/books") *>
        IO.never
      }
  }
}

/*
 * HOW TO RUN:
 * 
 * 1. Replace all ??? with working code
 * 2. Compile:
 *    cd chapter-09-http4s
 *    sbt compile
 * 
 * 3. Run the server:
 *    sbt "runMain http4sbasics.exercises.Exercise01BookAPI"
 * 
 * 4. Test with curl:
 *    # Create a book
 *    curl -X POST http://localhost:8080/books \
 *      -H "Content-Type: application/json" \
 *      -d '{"title":"The Rust Book","author":"Steve Klabnik","year":2018,"isbn":"978-1593278281"}'
 *    
 *    # List all books
 *    curl http://localhost:8080/books
 *    
 *    # Get specific book
 *    curl http://localhost:8080/books/1
 *    
 *    # Update book
 *    curl -X PUT http://localhost:8080/books/1 \
 *      -H "Content-Type: application/json" \
 *      -d '{"title":"The Rust Programming Language","author":"Steve Klabnik","year":2023,"isbn":"978-1718503106"}'
 *    
 *    # Delete book
 *    curl -X DELETE http://localhost:8080/books/1
 *    
 *    # Test validation (should fail)
 *    curl -X POST http://localhost:8080/books \
 *      -H "Content-Type: application/json" \
 *      -d '{"title":"","author":"","year":3000,"isbn":""}'
 * 
 * EXPECTED OUTPUT:
 * 
 * After creating a book:
 * {"id":1,"title":"The Rust Book","author":"Steve Klabnik","year":2018,"isbn":"978-1593278281"}
 * 
 * After listing books:
 * [{"id":1,"title":"The Rust Book","author":"Steve Klabnik","year":2018,"isbn":"978-1593278281"}]
 * 
 * After validation error:
 * {"error":"validation_error","message":"..."}
 */

// See solutions/Exercise01_Solution.scala for complete implementation
