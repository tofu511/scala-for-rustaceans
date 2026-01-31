# Chapter 09: Exercises Quick Reference

## Exercise 01: Book API

**File:** `src/main/scala/exercises/Exercise01_BookAPI.scala`  
**Solution:** `src/main/scala/exercises/solutions/Exercise01_Solution.scala`  
**Estimated Time:** 45-60 minutes

### Objectives
- Build a complete REST API with CRUD operations
- Handle JSON encoding/decoding with circe
- Implement typed error handling with EitherT
- Practice http4s routing patterns

### Tasks Overview

**Part 1: Domain Models (Tasks 1-2)**
- Define `Book` case class with id, title, author, year, isbn
- Define `CreateBookRequest` (without id)
- Add JSON encoders/decoders

**Part 2: Service Layer (Tasks 3-8)**
- `findBook`: Look up book by ID
- `validateCreateRequest`: Validate all fields
- `createBook`: Create with validation and duplicate check
- `updateBook`: Update existing book
- `deleteBook`: Remove book
- `listBooks`: Get all books

**Part 3: HTTP Layer (Tasks 9-10)**
- `errorToResponse`: Map errors to HTTP statuses
- Implement all CRUD routes

### How to Run

**1. Implement the exercise:**
```bash
cd chapter-09-http4s
# Edit: src/main/scala/exercises/Exercise01_BookAPI.scala
# Replace all ??? with working code
sbt compile
```

**2. Run your implementation:**
```bash
sbt "runMain http4sbasics.exercises.Exercise01BookAPI"
```

**3. Test with curl (in another terminal):**
```bash
# Create a book
curl -X POST http://localhost:8080/books \
  -H "Content-Type: application/json" \
  -d '{"title":"The Rust Book","author":"Steve Klabnik","year":2018,"isbn":"978-1593278281"}'

# List all books
curl http://localhost:8080/books

# Get specific book
curl http://localhost:8080/books/1

# Update book
curl -X PUT http://localhost:8080/books/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"The Rust Programming Language","author":"Steve Klabnik","year":2023,"isbn":"978-1718503106"}'

# Delete book
curl -X DELETE http://localhost:8080/books/1

# Test validation errors
curl -X POST http://localhost:8080/books \
  -H "Content-Type: application/json" \
  -d '{"title":"","author":"","year":3000,"isbn":""}'

# Test not found
curl http://localhost:8080/books/999
```

**4. Run the solution to compare:**
```bash
sbt "runMain http4sbasics.exercises.solutions.Exercise01Solution"
```

### Expected Output

**After creating a book:**
```json
{
  "id": 1,
  "title": "The Rust Book",
  "author": "Steve Klabnik",
  "year": 2018,
  "isbn": "978-1593278281"
}
```

**After listing books:**
```json
[
  {
    "id": 1,
    "title": "The Rust Book",
    "author": "Steve Klabnik",
    "year": 2018,
    "isbn": "978-1593278281"
  }
]
```

**Validation error:**
```json
{
  "error": "validation_error",
  "message": "Validation failed for title: cannot be empty"
}
```

**Not found error:**
```json
{
  "error": "not_found",
  "message": "Book with id 999 not found"
}
```

**Duplicate ISBN error:**
```json
{
  "error": "duplicate_isbn",
  "message": "Book with ISBN 978-1593278281 already exists"
}
```

## Implementation Hints

### Task 1-2: Domain Models and JSON

```scala
// Define the models
case class Book(
  id: Long,
  title: String,
  author: String,
  year: Int,
  isbn: String
)

// JSON codecs
import io.circe.generic.auto._
import org.http4s.circe._

implicit val bookDecoder: EntityDecoder[IO, Book] = jsonOf[IO, Book]
implicit val bookEncoder: EntityEncoder[IO, Book] = jsonEncoderOf[IO, Book]
```

### Task 3: Find Book

```scala
def findBook(id: Long): EitherT[IO, BookError, Book] = {
  EitherT.fromOption[IO](
    books.get(id),
    BookError.NotFound(id)
  )
}
```

### Task 4: Validation

```scala
def validateCreateRequest(req: CreateBookRequest): EitherT[IO, BookError, CreateBookRequest] = {
  val errors = List(
    if (req.title.trim.isEmpty) Some(BookError.ValidationError("title", "cannot be empty")) else None,
    // ... more validations
  ).flatten
  
  errors.headOption match {
    case Some(error) => EitherT.leftT[IO, CreateBookRequest](error)
    case None => EitherT.rightT[IO, BookError](req)
  }
}
```

### Task 5: Create Book

```scala
def createBook(req: CreateBookRequest): EitherT[IO, BookError, Book] = {
  for {
    validated <- validateCreateRequest(req)
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
```

### Task 9: Error to Response

```scala
def errorToResponse(error: BookError): IO[Response[IO]] = {
  val (status, errorType) = error match {
    case _: BookError.NotFound => (Status.NotFound, "not_found")
    case _: BookError.ValidationError => (Status.BadRequest, "validation_error")
    case _: BookError.DuplicateISBN => (Status.Conflict, "duplicate_isbn")
  }
  
  Response[IO](status)
    .withEntity(ErrorResponse(errorType, error.message).asJson)
    .pure[IO]
}
```

### Task 10: HTTP Routes

```scala
val bookRoutes = HttpRoutes.of[IO] {
  // GET /books
  case GET -> Root / "books" =>
    BookService.listBooks()
      .foldF(errorToResponse, books => Ok(books.asJson))
  
  // POST /books
  case req @ POST -> Root / "books" =>
    (for {
      createReq <- EitherT.liftF[IO, BookError, CreateBookRequest](req.as[CreateBookRequest])
      book <- BookService.createBook(createReq)
    } yield book)
      .foldF(errorToResponse, book => Created(book.asJson))
}
```

## Key Concepts

### 1. EitherT Pattern
```scala
// Service layer returns EitherT[IO, Error, Result]
def operation(): EitherT[IO, AppError, Result]

// HTTP layer uses .foldF to handle both cases
operation().foldF(
  error => errorToResponse(error),    // Left case
  result => Ok(result.asJson)          // Right case
)
```

### 2. Validation
```scala
// Collect all validation errors
val errors: List[Option[Error]] = List(
  if (condition1) Some(Error1) else None,
  if (condition2) Some(Error2) else None
).flatten

// Return first error or success
errors.headOption match {
  case Some(err) => EitherT.leftT(err)
  case None => EitherT.rightT(validatedValue)
}
```

### 3. EitherT Composition
```scala
for {
  step1 <- operation1()  // EitherT[IO, E, A]
  step2 <- operation2(step1)  // EitherT[IO, E, B]
  step3 <- operation3(step2)  // EitherT[IO, E, C]
} yield step3
// Short-circuits on first Left (error)
```

### 4. JSON Handling
```scala
// Decode request body
createReq <- EitherT.liftF[IO, Error, Request](req.as[Request])

// Encode response body
Ok(result.asJson)
```

## Rust Comparison

```rust
// Rust (axum)
#[derive(Deserialize, Serialize)]
struct Book {
    id: u64,
    title: String,
    author: String,
    year: u32,
    isbn: String,
}

enum BookError {
    NotFound(u64),
    ValidationError(String),
    DuplicateISBN(String),
}

impl IntoResponse for BookError {
    fn into_response(self) -> Response {
        match self {
            BookError::NotFound(id) => 
                (StatusCode::NOT_FOUND, format!("Book {} not found", id)).into_response(),
            // ...
        }
    }
}

async fn create_book(Json(req): Json<CreateBookRequest>) -> Result<Json<Book>, BookError> {
    let book = validate_and_create(req)?;
    Ok(Json(book))
}
```

## Common Mistakes

❌ **Forgetting to lift IO into EitherT**
```scala
createReq <- req.as[CreateBookRequest]  // Wrong! Type mismatch
```

✅ **Use EitherT.liftF**
```scala
createReq <- EitherT.liftF[IO, BookError, CreateBookRequest](req.as[CreateBookRequest])
```

❌ **Not using foldF**
```scala
BookService.createBook(req).flatMap(book => Created(book.asJson))  // Wrong!
```

✅ **Use foldF to handle both cases**
```scala
BookService.createBook(req).foldF(errorToResponse, book => Created(book.asJson))
```

❌ **Incorrect status constructors**
```scala
Status.NotFound(errorResponse.asJson)  // Wrong! Status is not callable
```

✅ **Use Response constructor**
```scala
Response[IO](Status.NotFound).withEntity(errorResponse.asJson).pure[IO]
```

## Testing Checklist

- [ ] Can create a book with valid data
- [ ] Can list all books
- [ ] Can get a specific book by ID
- [ ] Can update an existing book
- [ ] Can delete a book
- [ ] Returns 404 for non-existent book
- [ ] Returns 400 for validation errors (empty fields, invalid year)
- [ ] Returns 409 for duplicate ISBN
- [ ] All error responses have proper format

## Key Takeaways

✅ **REST API Pattern**: GET/POST/PUT/DELETE with pattern matching  
✅ **JSON Handling**: circe with generic.auto for automatic derives  
✅ **Typed Errors**: EitherT[IO, Error, A] for type-safe error handling  
✅ **Validation**: Check business rules, return first error  
✅ **Error Mapping**: Convert domain errors to HTTP responses  
✅ **Composition**: Use for-comprehension to chain operations  
✅ **Production Pattern**: This is THE recommended approach for Scala web services  

**Next:** Chapter 10 covers database access with Doobie and migrations with Flyway!
