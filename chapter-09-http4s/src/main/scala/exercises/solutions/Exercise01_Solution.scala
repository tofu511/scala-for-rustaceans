package http4sbasics.exercises.solutions

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
 * EXERCISE 01 SOLUTION: Book API with http4s
 */

object Exercise01Solution extends IOApp.Simple {
  
  // TASK 1: Domain models
  case class Book(
    id: Long,
    title: String,
    author: String,
    year: Int,
    isbn: String
  )
  
  case class CreateBookRequest(
    title: String,
    author: String,
    year: Int,
    isbn: String
  )
  
  sealed trait BookError {
    def message: String
  }
  
  object BookError {
    case class NotFound(id: Long) extends BookError {
      def message: String = s"Book with id $id not found"
    }
    
    case class ValidationError(field: String, issue: String) extends BookError {
      def message: String = s"Validation failed for $field: $issue"
    }
    
    case class DuplicateISBN(isbn: String) extends BookError {
      def message: String = s"Book with ISBN $isbn already exists"
    }
  }
  
  case class ErrorResponse(error: String, message: String)
  
  // TASK 2: JSON encoders/decoders
  implicit val bookDecoder: EntityDecoder[IO, Book] = jsonOf[IO, Book]
  implicit val bookEncoder: EntityEncoder[IO, Book] = jsonEncoderOf[IO, Book]
  implicit val createBookDecoder: EntityDecoder[IO, CreateBookRequest] = 
    jsonOf[IO, CreateBookRequest]
  implicit val errorEncoder: EntityEncoder[IO, ErrorResponse] = 
    jsonEncoderOf[IO, ErrorResponse]
  
  // In-memory "database"
  private var books: Map[Long, Book] = Map.empty
  private var nextId: Long = 1L
  
  object BookService {
    
    // TASK 3: Find book by ID
    def findBook(id: Long): EitherT[IO, BookError, Book] = {
      EitherT.fromOption[IO](
        books.get(id),
        BookError.NotFound(id)
      )
    }
    
    // TASK 4: Validate create request
    def validateCreateRequest(req: CreateBookRequest): EitherT[IO, BookError, CreateBookRequest] = {
      val validations = List(
        if (req.title.trim.isEmpty)
          Some(BookError.ValidationError("title", "cannot be empty"))
        else None,
        
        if (req.author.trim.isEmpty)
          Some(BookError.ValidationError("author", "cannot be empty"))
        else None,
        
        if (req.year < 1000 || req.year > 2100)
          Some(BookError.ValidationError("year", "must be between 1000 and 2100"))
        else None,
        
        if (req.isbn.trim.isEmpty)
          Some(BookError.ValidationError("isbn", "cannot be empty"))
        else None
      ).flatten
      
      validations.headOption match {
        case Some(error) => EitherT.leftT[IO, CreateBookRequest](error)
        case None => EitherT.rightT[IO, BookError](req)
      }
    }
    
    // TASK 5: Create new book
    def createBook(req: CreateBookRequest): EitherT[IO, BookError, Book] = {
      for {
        validated <- validateCreateRequest(req)
        // Check for duplicate ISBN
        _ <- EitherT.cond[IO](
          !books.values.exists(_.isbn == validated.isbn),
          (),
          BookError.DuplicateISBN(validated.isbn): BookError
        )
        newBook = Book(nextId, validated.title, validated.author, validated.year, validated.isbn)
        _ <- EitherT.right[BookError](IO {
          books = books + (nextId -> newBook)
          nextId += 1
        })
      } yield newBook
    }
    
    // TASK 6: Update book
    def updateBook(id: Long, req: CreateBookRequest): EitherT[IO, BookError, Book] = {
      for {
        _ <- findBook(id)
        validated <- validateCreateRequest(req)
        // Allow updating to same ISBN or check for duplicates
        _ <- EitherT.cond[IO](
          books.get(id).exists(_.isbn == validated.isbn) || 
            !books.values.exists(_.isbn == validated.isbn),
          (),
          BookError.DuplicateISBN(validated.isbn): BookError
        )
        updated = Book(id, validated.title, validated.author, validated.year, validated.isbn)
        _ <- EitherT.right[BookError](IO {
          books = books + (id -> updated)
        })
      } yield updated
    }
    
    // TASK 7: Delete book
    def deleteBook(id: Long): EitherT[IO, BookError, Unit] = {
      for {
        _ <- findBook(id)
        _ <- EitherT.right[BookError](IO {
          books = books - id
        })
      } yield ()
    }
    
    // TASK 8: List all books
    def listBooks(): EitherT[IO, BookError, List[Book]] = {
      EitherT.rightT[IO, BookError](books.values.toList)
    }
  }
  
  // TASK 9: Convert errors to HTTP responses
  def errorToResponse(error: BookError): IO[Response[IO]] = {
    val (status, errorType) = error match {
      case _: BookError.NotFound =>
        (Status.NotFound, "not_found")
      
      case _: BookError.ValidationError =>
        (Status.BadRequest, "validation_error")
      
      case _: BookError.DuplicateISBN =>
        (Status.Conflict, "duplicate_isbn")
    }
    
    val response = ErrorResponse(errorType, error.message)
    Response[IO](status).withEntity(response.asJson).pure[IO]
  }
  
  // TASK 10: HTTP routes
  val bookRoutes = HttpRoutes.of[IO] {
    
    // GET /books - List all books
    case GET -> Root / "books" =>
      BookService.listBooks()
        .foldF(errorToResponse, books => Ok(books.asJson))
    
    // GET /books/:id - Get book by ID
    case GET -> Root / "books" / LongVar(id) =>
      BookService.findBook(id)
        .foldF(errorToResponse, book => Ok(book.asJson))
    
    // POST /books - Create new book
    case req @ POST -> Root / "books" =>
      (for {
        createReq <- EitherT.liftF[IO, BookError, CreateBookRequest](
          req.as[CreateBookRequest]
        )
        book <- BookService.createBook(createReq)
      } yield book)
        .foldF(errorToResponse, book => Created(book.asJson))
    
    // PUT /books/:id - Update book
    case req @ PUT -> Root / "books" / LongVar(id) =>
      (for {
        createReq <- EitherT.liftF[IO, BookError, CreateBookRequest](
          req.as[CreateBookRequest]
        )
        book <- BookService.updateBook(id, createReq)
      } yield book)
        .foldF(errorToResponse, book => Ok(book.asJson))
    
    // DELETE /books/:id - Delete book
    case DELETE -> Root / "books" / LongVar(id) =>
      BookService.deleteBook(id)
        .foldF(
          errorToResponse,
          _ => Ok(Json.obj("message" -> Json.fromString(s"Book $id deleted")))
        )
  }
  
  def run: IO[Unit] = {
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(bookRoutes.orNotFound)
      .build
      .use { server =>
        IO.println(s"✅ Book API Solution started at ${server.address}") *>
        IO.println("\n=== Test Commands ===") *>
        IO.println("\n1. Create books:") *>
        IO.println("""curl -X POST http://localhost:8080/books -H "Content-Type: application/json" -d '{"title":"The Rust Book","author":"Steve Klabnik","year":2018,"isbn":"978-1593278281"}'""") *>
        IO.println("""curl -X POST http://localhost:8080/books -H "Content-Type: application/json" -d '{"title":"Programming Rust","author":"Jim Blandy","year":2021,"isbn":"978-1492052593"}'""") *>
        IO.println("\n2. List books:") *>
        IO.println("curl http://localhost:8080/books") *>
        IO.println("\n3. Get specific book:") *>
        IO.println("curl http://localhost:8080/books/1") *>
        IO.println("\n4. Update book:") *>
        IO.println("""curl -X PUT http://localhost:8080/books/1 -H "Content-Type: application/json" -d '{"title":"The Rust Programming Language","author":"Steve Klabnik","year":2023,"isbn":"978-1718503106"}'""") *>
        IO.println("\n5. Delete book:") *>
        IO.println("curl -X DELETE http://localhost:8080/books/2") *>
        IO.println("\n6. Test errors:") *>
        IO.println("curl http://localhost:8080/books/999  # Not found") *>
        IO.println("""curl -X POST http://localhost:8080/books -H "Content-Type: application/json" -d '{"title":"","author":"","year":3000,"isbn":""}'  # Validation error""") *>
        IO.println("\nPress Ctrl+C to stop...") *>
        IO.never
      }
  }
}

/*
 * EXAMPLE OUTPUT:
 * 
 * $ curl -X POST http://localhost:8080/books \
 *   -H "Content-Type: application/json" \
 *   -d '{"title":"The Rust Book","author":"Steve Klabnik","year":2018,"isbn":"978-1593278281"}'
 * 
 * {"id":1,"title":"The Rust Book","author":"Steve Klabnik","year":2018,"isbn":"978-1593278281"}
 * 
 * $ curl http://localhost:8080/books
 * 
 * [{"id":1,"title":"The Rust Book","author":"Steve Klabnik","year":2018,"isbn":"978-1593278281"}]
 * 
 * $ curl http://localhost:8080/books/999
 * 
 * {"error":"not_found","message":"Book with id 999 not found"}
 * 
 * $ curl -X POST http://localhost:8080/books \
 *   -H "Content-Type: application/json" \
 *   -d '{"title":"The Rust Book","author":"Steve Klabnik","year":2018,"isbn":"978-1593278281"}'
 * 
 * {"error":"duplicate_isbn","message":"Book with ISBN 978-1593278281 already exists"}
 */

/*
 * KEY LEARNING POINTS:
 * 
 * 1. **Domain Modeling**: Case classes for data, sealed traits for errors
 * 2. **JSON Handling**: circe with generic.auto for automatic derives
 * 3. **Typed Errors**: EitherT[IO, Error, A] for type-safe error handling
 * 4. **REST API**: Pattern matching for route definitions
 * 5. **Validation**: Check business rules before operations
 * 6. **Error Mapping**: Convert domain errors to HTTP responses
 * 7. **Composition**: Use for-comprehension to chain operations
 * 
 * This pattern (EitherT + typed errors + error mapping) is THE
 * recommended approach for production Scala web services.
 */
