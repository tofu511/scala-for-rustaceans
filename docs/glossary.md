# Scala and Functional Programming Glossary

A glossary of Scala and functional programming terms with explanations for Rust developers.

## Core Scala Concepts

### Val vs Var
- **val**: Immutable binding (like Rust's `let` without `mut`)
- **var**: Mutable binding (like Rust's `let mut`)

```scala
val x = 5        // immutable, like: let x = 5;
var y = 10       // mutable, like: let mut y = 10;
y = 15           // OK
// x = 10        // ERROR: reassignment to val
```

### Case Class
A class that automatically provides:
- Constructor parameters as fields
- `equals`, `hashCode`, `toString` implementations
- Pattern matching support
- `copy` method for creating modified copies

Similar to Rust structs with `#[derive(Debug, Clone, PartialEq)]`

```scala
case class User(id: Int, name: String, email: String)

val user = User(1, "Alice", "alice@example.com")
val updated = user.copy(email = "newalice@example.com")
```

### Sealed Trait
A trait that can only be extended in the same file. Enables exhaustive pattern matching.

Similar to Rust's enums.

```scala
sealed trait Result[+A]
case class Success[A](value: A) extends Result[A]
case class Failure(error: String) extends Result[Nothing]
```

### Companion Object
An object with the same name as a class, defined in the same file. Used for factory methods and static-like functionality.

Similar to Rust's `impl` blocks without `&self` parameter.

```scala
case class User(id: Int, name: String)

object User {
  def fromId(id: Int): Option[User] = {
    // Factory method
  }
}
```

### For-Comprehension
Syntactic sugar for chaining `flatMap`, `map`, and `filter` operations.

Similar to Rust's `?` operator and async/await, but more general.

```scala
for {
  x <- Some(1)
  y <- Some(2)
} yield x + y
// Desugars to: Some(1).flatMap(x => Some(2).map(y => x + y))
```

### Implicit
(Scala 2.x) Mechanism for automatic parameter passing and type conversions.

Used for:
- Type class instances
- Extension methods
- Automatic conversions

```scala
implicit val orderingInt: Ordering[Int] = Ordering.Int

def sort[A](list: List[A])(implicit ord: Ordering[A]): List[A] = {
  list.sorted(ord)
}

// ord is passed automatically
sort(List(3, 1, 2))
```

### Context Bound
Shorthand syntax for implicit parameters.

```scala
def sort[A: Ordering](list: List[A]): List[A] = {
  list.sorted
}
// Equivalent to:
def sort[A](list: List[A])(implicit ord: Ordering[A]): List[A]
```

## Functional Programming Concepts

### Referential Transparency (RT)
An expression is referentially transparent if it can be replaced with its value without changing program behavior.

```scala
// Referentially transparent
val x = 1 + 1
val y = 2
// x and y are interchangeable

// NOT referentially transparent
var counter = 0
def increment(): Int = {
  counter += 1
  counter
}
val a = increment()  // 1
val b = increment()  // 2
// a and b are different!
```

### Side Effect
An operation that interacts with the world outside the function (I/O, mutation, exceptions).

- Reading/writing files
- Network requests
- Throwing exceptions
- Modifying mutable state
- Printing to console

Pure functional programming aims to make side effects explicit and controlled.

### Pure Function
A function that:
1. Always returns the same output for the same input
2. Has no side effects

```scala
// Pure
def add(a: Int, b: Int): Int = a + b

// Impure (side effect: printing)
def addAndLog(a: Int, b: Int): Int = {
  println(s"Adding $a and $b")
  a + b
}
```

### Higher-Kinded Type (HKT)
A type that abstracts over type constructors.

Think of it as generics for generics. `F[_]` means "a type constructor that takes one type parameter".

```scala
// F[_] is a higher-kinded type
trait Functor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}

// F can be List, Option, Either[E, _], etc.
```

Rust doesn't have direct support for HKTs (yet), but GATs (Generic Associated Types) provide similar capabilities.

### Type Class
A pattern for ad-hoc polymorphism. Allows adding behavior to types without modifying them.

Similar to Rust traits, but implemented differently (via implicits).

```scala
// Type class definition
trait Show[A] {
  def show(value: A): String
}

// Type class instance
implicit val showInt: Show[Int] = new Show[Int] {
  def show(value: Int): String = value.toString
}

// Type class usage
def print[A: Show](value: A): Unit = {
  println(implicitly[Show[A]].show(value))
}
```

### Functor
A type class for types that can be mapped over.

Has one operation: `map[A, B](fa: F[A])(f: A => B): F[B]`

```scala
// List is a Functor
List(1, 2, 3).map(_ * 2)  // List(2, 4, 6)

// Option is a Functor
Some(5).map(_ * 2)  // Some(10)

// Either is a Functor
Right(5).map(_ * 2)  // Right(10)
```

Similar to Rust's `Iterator::map`, `Option::map`, `Result::map`.

### Applicative
A type class that extends Functor with ability to:
- Lift values into the context: `pure[A](a: A): F[A]`
- Apply wrapped functions: `ap[A, B](ff: F[A => B])(fa: F[A]): F[B]`

Allows independent computations to be combined.

```scala
import cats.syntax.apply._

val result: Option[Int] = (Some(1), Some(2), Some(3)).mapN(_ + _ + _)
// Some(6)
```

### Monad
A type class for sequencing computations.

Has two key operations:
- `pure[A](a: A): F[A]` - wrap a value
- `flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]` - sequence computations

```scala
// Option is a Monad
for {
  x <- Some(1)
  y <- Some(2)
} yield x + y  // Some(3)

// Chaining dependent computations
def findUser(id: Int): Option[User] = ???
def findAddress(user: User): Option[Address] = ???

findUser(123).flatMap(user => findAddress(user))
```

Similar to Rust's `and_then` for `Option` and `Result`.

### Semigroup
A type class for types with an associative binary operation.

```scala
trait Semigroup[A] {
  def combine(x: A, y: A): A
}

// Example: String concatenation
"hello".combine(" ").combine("world")  // "hello world"
```

### Monoid
A Semigroup with an identity element (`empty`).

```scala
trait Monoid[A] extends Semigroup[A] {
  def empty: A
}

// Example: Int addition
// combine = +, empty = 0
1.combine(2).combine(0)  // 3
```

Similar to Rust's `Default` + associative operation.

### MonadError
A Monad that can handle errors.

```scala
trait MonadError[F[_], E] extends Monad[F] {
  def raiseError[A](e: E): F[A]
  def handleErrorWith[A](fa: F[A])(f: E => F[A]): F[A]
}

// Either[String, *] has MonadError[Either[String, *], String]
```

### Effect
An `effect` is a description of a computation that may have side effects, without actually performing them.

```scala
// This creates a description, doesn't print yet
val effect: IO[Unit] = IO.println("Hello")

// This runs the effect
effect.unsafeRunSync()  // NOW it prints
```

## Cats-Effect Concepts

### IO[A]
A data type that describes a computation that:
- May perform side effects
- Returns a value of type `A`
- Is referentially transparent
- Is lazy (doesn't run until explicitly executed)

```scala
val io: IO[Int] = IO(println("Computing...")).as(42)
// Nothing happens yet

io.unsafeRunSync()  // Now it runs
```

Similar to Rust's `Future`, but lazy and pure.

### IOApp
A trait for creating applications with `IO` as the main effect type.

```scala
object Main extends IOApp.Simple {
  def run: IO[Unit] = {
    IO.println("Hello from IOApp!")
  }
}
```

Similar to `#[tokio::main]` in Rust.

### Fiber
A lightweight thread managed by Cats-Effect runtime.

```scala
for {
  fiber <- IO.println("Background task").start
  _ <- IO.println("Main task")
  _ <- fiber.join
} yield ()
```

Similar to Rust's `tokio::spawn`.

### Resource[F, A]
A data type for safe resource management with automatic cleanup.

```scala
Resource.make(
  acquire = IO(new FileInputStream("file.txt"))
)(
  release = fis => IO(fis.close())
)
```

Similar to Rust's RAII and `Drop` trait.

### Ref[F, A]
Thread-safe mutable reference in an effect type.

```scala
for {
  ref <- Ref.of[IO, Int](0)
  _ <- ref.update(_ + 1)
  value <- ref.get
} yield value  // 1
```

Similar to Rust's `Arc<Mutex<T>>`, but for effectful contexts.

### Deferred[F, A]
A purely functional synchronization primitive that represents a single value that may not be available yet.

Like a one-shot channel or a promise.

```scala
for {
  deferred <- Deferred[IO, Int]
  fiber <- deferred.complete(42).start
  value <- deferred.get  // Blocks until completed
} yield value
```

Similar to Rust's `oneshot` channel from tokio.

## http4s Concepts

### HttpRoutes[F]
A service that handles HTTP requests in effect type `F`.

```scala
val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
  case GET -> Root / "hello" =>
    Ok("Hello, World!")
}
```

### EntityEncoder / EntityDecoder
Type classes for encoding/decoding HTTP bodies.

```scala
// Automatic JSON encoding with circe
import org.http4s.circe.CirceEntityEncoder._
case class User(id: Int, name: String)

Ok(User(1, "Alice"))  // Automatically encodes to JSON
```

## Doobie Concepts

### ConnectionIO[A]
A program that interacts with a database and produces a value of type `A`.

```scala
val query: ConnectionIO[List[User]] = 
  sql"SELECT id, name FROM users".query[User].to[List]
```

### Transactor[F]
Interprets `ConnectionIO` programs into effect type `F`.

```scala
val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
  "org.postgresql.Driver",
  "jdbc:postgresql://localhost/mydb",
  "user",
  "password"
)

query.transact(xa): IO[List[User]]
```

## Testing Concepts

### Property-Based Testing
Testing with randomly generated inputs to verify properties hold for all inputs.

```scala
property("reversing twice returns original") = forAll { (list: List[Int]) =>
  list.reverse.reverse == list
}
```

Similar to Rust's `proptest` and `quickcheck`.

### Generator (ScalaCheck)
Generates random values for property testing.

```scala
val genPositiveInt: Gen[Int] = Gen.posNum[Int]
val genUser: Gen[User] = for {
  id <- Gen.posNum[Int]
  name <- Gen.alphaStr
} yield User(id, name)
```

### Arbitrary (ScalaCheck)
Type class providing default generators for types.

```scala
implicit val arbUser: Arbitrary[User] = Arbitrary(genUser)
```

---

## Quick Reference

| Term | One-Line Explanation |
|------|---------------------|
| **val** | Immutable binding |
| **var** | Mutable binding |
| **case class** | Data class with built-in methods |
| **sealed trait** | Sum type (like Rust enum) |
| **for-comprehension** | Syntactic sugar for flatMap/map |
| **implicit** | Automatic parameter passing |
| **Type Class** | Pattern for ad-hoc polymorphism |
| **Functor** | Things you can map over |
| **Monad** | Things you can flatMap |
| **IO[A]** | Lazy, referentially transparent effect |
| **Fiber** | Lightweight thread |
| **Resource** | Automatic resource cleanup |
| **Ref** | Thread-safe mutable state |
