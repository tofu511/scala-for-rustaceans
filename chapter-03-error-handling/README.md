# Chapter 03: Error Handling

Learn how Scala handles errors with Either, Try, and Future, and how they compare to Rust's error handling approaches.

## Overview

This chapter covers three core patterns for error handling in Scala:

1. **Either[L, R]** - Type-safe error handling (like Rust's `Result<T, E>`)
2. **Try[A]** - Exception handling (like catching panics, but for control flow)
3. **Future[A]** - Async computation (like Rust's `Future`, but eager!)

## Learning Objectives

- Understand when to use Either, Try, or Future
- Chain error-prone operations with flatMap and for-comprehensions
- Convert between different error types
- Recognize the limitations of Future (setting up Chapter 04)

## Prerequisites

- Completed Chapter 02 (Scala Fundamentals)
- Familiar with Rust's `Result<T, E>` and `Option<T>`
- Basic understanding of async concepts

## Quick Start

```bash
# Compile the project
sbt compile

# Run examples
sbt "runMain errorhandling.EitherExamples"
sbt "runMain errorhandling.TryExamples"
sbt "runMain errorhandling.FutureExamples"

# Work on exercises
cd src/main/scala/exercises
# Edit Exercise01_Either.scala, uncomment tests, and run:
sbt "runMain errorhandling.exercises.Exercise01Either"
```

## Content Structure

### Example Files

Located in `src/main/scala/`:

1. **01_either.scala** - Either for typed errors
   - Basic Either usage
   - Pattern matching
   - map, flatMap, fold
   - Validation with Either
   - Converting between Either, Option
   - Practical examples

2. **02_try.scala** - Try for exception handling
   - Try[A] basics
   - Success and Failure
   - recover and recoverWith
   - Converting to/from Option and Either
   - When to use Try vs Either

3. **03_future.scala** - Future for async operations
   - Creating and chaining Futures
   - Sequential vs parallel execution
   - Error handling in futures
   - Callbacks and completion
   - **The referential transparency problem** (teaser for Chapter 04)

### Exercises

Located in `src/main/scala/exercises/`:

- **Exercise01_Either.scala** - Build a validation system
- **Exercise02_Try.scala** - Handle exceptions functionally
- **Exercise03_Future.scala** - Async operations and parallelism

See [EXERCISES.md](EXERCISES.md) for detailed instructions.

## Key Concepts

### Either[L, R]

Use Either when you want type-safe errors with specific error types:

```scala
def divide(a: Int, b: Int): Either[String, Int] = {
  if (b == 0) Left("Division by zero")
  else Right(a / b)
}

// Chain operations
val result = for {
  x <- divide(10, 2)  // Right(5)
  y <- divide(x, 2)   // Right(2)
  z <- divide(y, 2)   // Right(1)
} yield z
```

**Rust comparison**:
```rust
fn divide(a: i32, b: i32) -> Result<i32, String> {
    if b == 0 { 
        Err("Division by zero".to_string()) 
    } else { 
        Ok(a / b) 
    }
}
```

### Try[A]

Use Try when working with exception-throwing code (e.g., Java libraries):

```scala
def parseInt(s: String): Try[Int] = Try(s.toInt)

val result = for {
  a <- parseInt("10")
  b <- parseInt("20")
} yield a + b  // Success(30)
```

**Rust comparison**: Rust doesn't have exceptions for control flow. Try is similar to `std::panic::catch_unwind()`, but used much more commonly in Scala for Java interop.

### Future[A]

Use Future for async operations:

```scala
def fetchUser(id: Int): Future[User] = Future {
  // Async database call
  User(id, "Alice")
}

// Parallel execution
val userFuture = fetchUser(1)
val ordersFuture = fetchOrders(1)

for {
  user <- userFuture
  orders <- ordersFuture
} yield (user, orders)
```

**Rust comparison**:
```rust
async fn fetch_user(id: i32) -> User {
    // Async database call
    User { id, name: "Alice" }
}

// In Rust
let (user, orders) = tokio::join!(
    fetch_user(1),
    fetch_orders(1)
);
```

**Key difference**: Scala's Future is **eager** (starts immediately), while Rust's Future is **lazy** (starts when awaited).

### For-Comprehensions

For-comprehensions work with Either, Try, and Future:

```scala
// With Either
for {
  a <- Right(10)
  b <- Right(20)
} yield a + b  // Right(30)

// With Try
for {
  a <- Try("10".toInt)
  b <- Try("20".toInt)
} yield a + b  // Success(30)

// With Future
for {
  a <- Future(10)
  b <- Future(20)
} yield a + b  // Future(30)
```

**Rust comparison**: Similar to `?` operator and `async/await`:
```rust
// ? operator (Result)
let a = parse_int("10")?;
let b = parse_int("20")?;
Ok(a + b)

// async/await (Future)
let a = async_compute().await;
let b = async_compute().await;
a + b
```

## When to Use What?

| Type | Use When | Rust Equivalent |
|------|----------|-----------------|
| `Either[L, R]` | Need specific error types | `Result<T, E>` |
| `Try[A]` | Working with Java exceptions | `catch_unwind` (rarely used) |
| `Future[A]` | Async operations | `Future` (but eager!) |
| `Option[A]` | Value may be absent | `Option<T>` |

## Common Patterns

### Error Recovery

```scala
// Either
val result = divide(10, 0).fold(
  error => 0,           // Handle error
  value => value * 2    // Handle success
)

// Try
val result = Try("abc".toInt).getOrElse(0)

// Future
val result = fetchUser(id).recover {
  case _: NotFoundException => User.default
}
```

### Converting Between Types

```scala
val either: Either[String, Int] = Right(42)
val tryResult: Try[Int] = either.toTry
val option: Option[Int] = tryResult.toOption

// Or chain them
Right(42).toTry.toOption  // Some(42)
```

## Important Note: Future's Problem

The Future example demonstrates a critical issue with referential transparency:

```scala
var counter = 0
def increment(): Future[Int] = Future {
  counter += 1
  counter
}

// These are NOT equivalent!
val f1 = increment()
val result1 = for { a <- f1; b <- f1 } yield (a, b)  // (1, 1)

val result2 = for { 
  a <- increment()
  b <- increment() 
} yield (a, b)  // (2, 3) - different!
```

This breaks referential transparency because Future executes immediately and has side effects. We'll explore this problem in depth in Chapter 04 and learn how IO[A] solves it in Chapter 07.

## Exercises

Complete the three exercises to practice:

1. **Exercise 01**: Build a validation system with Either
2. **Exercise 02**: Handle exceptions with Try
3. **Exercise 03**: Work with async operations using Future

See [EXERCISES.md](EXERCISES.md) for instructions.

## Rust Developer Notes

### Key Differences from Rust

1. **Either vs Result**: Scala's Either is more general (not specifically for errors), but commonly used for error handling

2. **Try**: Rust doesn't need Try because it doesn't have exceptions. In Rust, you'd use `Result` everywhere

3. **Future**: 
   - Scala: Eager (starts immediately), blocking Await
   - Rust: Lazy (doesn't start until awaited), non-blocking .await

4. **For-comprehensions**: Similar to `?` operator, but more general (works with any type that has flatMap)

### Similarities to Rust

1. **Chaining**: Both use flatMap/? for error propagation
2. **Pattern matching**: Both support matching on Either/Result
3. **Type safety**: Both enforce handling errors at compile time
4. **Composition**: Both allow building complex operations from simple ones

## Next Steps

After completing this chapter:

1. **Chapter 04**: Understand referential transparency problems with Future
2. **Chapter 05**: Learn about Cats and type classes
3. **Chapter 06**: Advanced error handling with Cats
4. **Chapter 07**: Learn IO[A] which solves Future's problems

## Resources

- [Scala Either docs](https://www.scala-lang.org/api/current/scala/util/Either.html)
- [Scala Try docs](https://www.scala-lang.org/api/current/scala/util/Try.html)
- [Scala Future docs](https://www.scala-lang.org/api/current/scala/concurrent/Future.html)
- [Rust Error Handling](https://doc.rust-lang.org/book/ch09-00-error-handling.html)
- [Rust Async Book](https://rust-lang.github.io/async-book/)

## Troubleshooting

**Problem**: "ExecutionContext not found" error with Future
```scala
import scala.concurrent.ExecutionContext.Implicits.global
```

**Problem**: "Await.result blocks forever"
- Check if your Future actually completes
- Increase timeout: `Await.result(future, 10.seconds)`
- Note: Avoid Await in production code!

**Problem**: Future doesn't run in parallel
- Start futures BEFORE the for-comprehension:
```scala
// Good (parallel)
val f1 = future1()
val f2 = future2()
for { a <- f1; b <- f2 } yield (a, b)

// Bad (sequential)
for { 
  a <- future1()  // This creates the future
  b <- future2()  // Only starts after first completes
} yield (a, b)
```

---

**Time Estimate**: 1.5-2 hours (including exercises)

**Difficulty**: Intermediate

**Next**: [Chapter 04: Referential Transparency](../chapter-04-referential-transparency/README.md)
