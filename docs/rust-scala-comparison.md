# Rust ⟷ Scala Comparison Guide

A comprehensive comparison of Rust and Scala concepts for developers transitioning between the languages.

## Memory Management

### Rust: Ownership and Borrowing
```rust
// Ownership: value is moved
let s1 = String::from("hello");
let s2 = s1; // s1 is no longer valid

// Borrowing: temporary access
fn calculate_length(s: &String) -> usize {
    s.len()
} // s goes out of scope but doesn't own the data
```

### Scala: Garbage Collection
```scala
// All values are references (for objects)
val s1 = "hello"
val s2 = s1 // Both s1 and s2 are valid

// No explicit borrowing needed
def calculateLength(s: String): Int = {
  s.length
} // GC handles memory cleanup
```

**Key Difference**: Rust uses compile-time memory management (ownership), while Scala uses runtime garbage collection. This means Rust has zero-cost abstractions and predictable performance, but Scala has simpler code without lifetime annotations.

## Type System

### Rust: Generics with Traits
```rust
// Generic function with trait bound
fn print_it<T: Display>(value: T) {
    println!("{}", value);
}

// Trait definition
trait Drawable {
    fn draw(&self);
}

// Trait implementation
impl Drawable for Circle {
    fn draw(&self) { /* ... */ }
}
```

### Scala: Generics with Type Classes
```scala
// Generic function with type class constraint (implicit)
def printIt[T: Show](value: T): Unit = {
  println(value.show)
}

// Or explicitly:
def printIt[T](value: T)(implicit ev: Show[T]): Unit = {
  println(ev.show(value))
}

// Trait definition
trait Drawable {
  def draw(): Unit
}

// Trait implementation (type class pattern)
implicit val circleDrawable: Drawable[Circle] = new Drawable[Circle] {
  def draw(): Unit = { /* ... */ }
}
```

**Key Difference**: Both have traits, but Scala's implicit system enables the type class pattern, which provides more flexibility in adding behavior to existing types.

## Error Handling

### Rust: Result and Option
```rust
// Result for fallible operations
fn divide(a: i32, b: i32) -> Result<i32, String> {
    if b == 0 {
        Err("division by zero".to_string())
    } else {
        Ok(a / b)
    }
}

// Using ? operator for propagation
fn compute() -> Result<i32, String> {
    let x = divide(10, 2)?;
    let y = divide(x, 3)?;
    Ok(y)
}

// Option for nullable values
fn find_user(id: u32) -> Option<User> {
    // ...
}
```

### Scala: Either, Try, and Option
```scala
// Either for fallible operations (Right = success, Left = error)
def divide(a: Int, b: Int): Either[String, Int] = {
  if (b == 0) {
    Left("division by zero")
  } else {
    Right(a / b)
  }
}

// Using for-comprehension for propagation
def compute(): Either[String, Int] = {
  for {
    x <- divide(10, 2)
    y <- divide(x, 3)
  } yield y
}

// Try for exception-based errors
def parseNumber(s: String): Try[Int] = Try(s.toInt)

// Option for nullable values
def findUser(id: Int): Option[User] = {
  // ...
}
```

**Key Difference**: Very similar concepts! `Result<T, E>` ≈ `Either[E, T]`, `Option<T>` ≈ `Option[T]`. Scala's `for-comprehension` is like Rust's `?` operator but more general.

## Async Programming

### Rust: async/await with Future
```rust
use tokio::time::{sleep, Duration};

async fn fetch_user(id: u32) -> Result<User, Error> {
    sleep(Duration::from_secs(1)).await;
    // Fetch user from database
    Ok(user)
}

async fn get_user_profile(id: u32) -> Result<Profile, Error> {
    let user = fetch_user(id).await?;
    let profile = fetch_profile(user.id).await?;
    Ok(profile)
}

#[tokio::main]
async fn main() {
    let profile = get_user_profile(123).await;
}
```

### Scala: IO with Cats-Effect
```scala
import cats.effect.IO
import scala.concurrent.duration._

def fetchUser(id: Int): IO[User] = {
  IO.sleep(1.second) *>
    IO {
      // Fetch user from database
      user
    }
}

def getUserProfile(id: Int): IO[Profile] = {
  for {
    user <- fetchUser(id)
    profile <- fetchProfile(user.id)
  } yield profile
}

object Main extends IOApp.Simple {
  def run: IO[Unit] = {
    getUserProfile(123).flatMap { profile =>
      IO.println(profile)
    }
  }
}
```

**Key Difference**: Rust's `Future` evaluates eagerly when `await` is called. Scala's `IO` is lazy and referentially transparent - it's a description of a computation that runs when explicitly executed. This makes `IO` more composable for functional programming.

## Concurrency

### Rust: Spawning Tasks
```rust
use tokio::task;

async fn concurrent_tasks() {
    let handle1 = task::spawn(async {
        expensive_computation1().await
    });
    
    let handle2 = task::spawn(async {
        expensive_computation2().await
    });
    
    let (result1, result2) = tokio::join!(handle1, handle2);
}

// Shared mutable state
use std::sync::Arc;
use tokio::sync::Mutex;

let counter = Arc::new(Mutex::new(0));
```

### Scala: Fibers with Cats-Effect
```scala
import cats.effect.IO
import cats.effect.kernel.Ref

def concurrentTasks(): IO[Unit] = {
  for {
    fiber1 <- expensiveComputation1().start
    fiber2 <- expensiveComputation2().start
    result1 <- fiber1.joinWithNever
    result2 <- fiber2.joinWithNever
  } yield ()
}

// Or using parallel execution
import cats.syntax.parallel._

val result: IO[(Result1, Result2)] = 
  (expensiveComputation1(), expensiveComputation2()).parTupled

// Shared mutable state
val counter: IO[Ref[IO, Int]] = Ref.of[IO, Int](0)
```

**Key Difference**: Rust's tasks are OS-thread-backed (with work-stealing). Cats-Effect's Fibers are green threads managed by the runtime. `Arc<Mutex<T>>` ≈ `Ref[IO, T]` for thread-safe state.

## Resource Management

### Rust: RAII and Drop
```rust
struct FileHandle {
    path: String,
}

impl Drop for FileHandle {
    fn drop(&mut self) {
        println!("Closing file: {}", self.path);
    }
}

// Automatic cleanup when value goes out of scope
{
    let file = FileHandle { path: "data.txt".to_string() };
    // Use file
} // file.drop() called automatically
```

### Scala: Resource from Cats-Effect
```scala
import cats.effect.{IO, Resource}
import java.io.FileInputStream

def fileResource(path: String): Resource[IO, FileInputStream] = {
  Resource.make(
    IO(new FileInputStream(path))  // acquire
  )(fis =>
    IO(fis.close())                 // release
  )
}

// Automatic cleanup even if errors occur
fileResource("data.txt").use { fis =>
  // Use file
  IO(/* read from fis */)
} // fis.close() called automatically
```

**Key Difference**: Rust uses RAII (Resource Acquisition Is Initialization) with compile-time guarantees. Scala's `Resource` provides similar safety but at runtime, with better error handling (bracket semantics).

## Collections

### Rust: Owned Collections
```rust
let mut vec = vec![1, 2, 3];
vec.push(4);

let mapped: Vec<i32> = vec.iter().map(|x| x * 2).collect();
let filtered: Vec<i32> = vec.into_iter().filter(|x| x % 2 == 0).collect();
```

### Scala: Immutable Collections by Default
```scala
val list = List(1, 2, 3)
val newList = list :+ 4  // Returns new list

val mapped = list.map(_ * 2)
val filtered = list.filter(_ % 2 == 0)

// Mutable collections available but discouraged
import scala.collection.mutable
val buffer = mutable.ArrayBuffer(1, 2, 3)
buffer += 4  // Mutates in place
```

**Key Difference**: Rust collections are mutable by default (`mut`), Scala collections are immutable by default. Scala creates new collections on modifications, Rust modifies in place.

## Pattern Matching

### Rust: Match Expressions
```rust
enum Message {
    Quit,
    Move { x: i32, y: i32 },
    Write(String),
}

match msg {
    Message::Quit => println!("Quit"),
    Message::Move { x, y } => println!("Move to {}, {}", x, y),
    Message::Write(text) => println!("Write: {}", text),
}

// If let for single pattern
if let Message::Write(text) = msg {
    println!("{}", text);
}
```

### Scala: Match Expressions
```scala
sealed trait Message
case object Quit extends Message
case class Move(x: Int, y: Int) extends Message
case class Write(text: String) extends Message

msg match {
  case Quit => println("Quit")
  case Move(x, y) => println(s"Move to $x, $y")
  case Write(text) => println(s"Write: $text")
}

// Pattern matching in val
val Move(x, y) = msg

// Pattern matching with Option
userOpt match {
  case Some(user) => println(user.name)
  case None => println("No user")
}
```

**Key Difference**: Very similar! Both use `match`, both have exhaustiveness checking. Scala's `case class` is like Rust's `enum` variants with named fields.

## Build Tools and Dependencies

### Rust: Cargo
```toml
# Cargo.toml
[package]
name = "my-project"
version = "0.1.0"
edition = "2021"

[dependencies]
tokio = { version = "1.0", features = ["full"] }
serde = { version = "1.0", features = ["derive"] }
```

```bash
cargo build    # Compile project
cargo run      # Run project
cargo test     # Run tests
cargo add tokio  # Add dependency
```

### Scala: sbt
```scala
// build.sbt
name := "my-project"
version := "0.1.0"
scalaVersion := "2.13.13"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.5.0",
  "io.circe" %% "circe-generic" % "0.14.6"
)
```

```bash
sbt compile    # Compile project
sbt run        # Run project
sbt test       # Run tests
# No direct equivalent to cargo add, edit build.sbt manually
```

**Key Difference**: Cargo is faster and more user-friendly. sbt is more powerful but has a steeper learning curve. The `%%` in sbt handles Scala version compatibility automatically.

## Testing

### Rust: Built-in Testing
```rust
#[test]
fn test_addition() {
    assert_eq!(2 + 2, 4);
}

#[test]
fn test_division() {
    let result = divide(10, 2).unwrap();
    assert_eq!(result, 5);
}

// Property-based testing with proptest
use proptest::prelude::*;

proptest! {
    #[test]
    fn test_reversing_twice(ref s in ".*") {
        let reversed_twice: String = s.chars().rev().collect::<String>()
                                       .chars().rev().collect();
        prop_assert_eq!(&reversed_twice, s);
    }
}
```

### Scala: ScalaTest
```scala
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CalculatorSpec extends AnyFlatSpec with Matchers {
  "Addition" should "work correctly" in {
    2 + 2 shouldBe 4
  }
  
  "Division" should "return correct result" in {
    val result = divide(10, 2)
    result shouldBe Right(5)
  }
}

// Property-based testing with ScalaCheck
import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

object StringSpec extends Properties("String") {
  property("reversing twice") = forAll { (s: String) =>
    s.reverse.reverse == s
  }
}
```

**Key Difference**: Rust has built-in testing, Scala uses external libraries. Both have excellent property-based testing tools (proptest vs ScalaCheck).

## Summary

| Aspect | Rust | Scala |
|--------|------|-------|
| **Memory** | Manual (ownership) | GC |
| **Performance** | Extremely fast | Fast (JVM) |
| **Safety** | Compile-time | Mix of compile/runtime |
| **Concurrency** | Fearless (ownership) | Safe (immutability) |
| **Learning Curve** | Steep (ownership) | Moderate (FP concepts) |
| **Ecosystem** | Growing rapidly | Mature (JVM) |
| **Use Cases** | Systems, performance-critical | Backend services, data processing |

Both languages emphasize immutability and functional programming, making the transition smoother than you might expect!
