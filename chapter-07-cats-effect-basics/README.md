# Chapter 07: IO Monad Basics

## Overview

This chapter introduces **Cats-Effect's IO monad** - the solution to Future's referential transparency problems. IO[A] is a lazy, referentially transparent description of a side effect that produces a value of type A.

**Learning Objectives:**
- Understand what IO[A] is and why it's better than Future
- Create and compose IO operations
- Handle errors in IO effectively
- Use **EitherT[IO, E, A]** for typed error handling (production pattern!)
- Manage resources safely with bracket and Resource

**Time Estimate:** 3-4 hours (this is a foundational chapter!)

## Why IO?

Recall from Chapter 4: Scala's Future has referential transparency problems:
- ❌ Eager evaluation (runs immediately)
- ❌ Not referentially transparent
- ❌ Race conditions possible
- ❌ Hard to test

**IO solves all of these:**
- ✅ Lazy evaluation (doesn't run until explicitly told)
- ✅ Referentially transparent (can substitute freely)
- ✅ Deterministic composition
- ✅ Easy to test

## Core Concepts

### 1. IO[A] - Lazy Effect Description

```scala
val io: IO[Int] = IO.delay {
  println("Side effect!")
  42
}
// Nothing printed yet! IO is just a description.

// Actually run it:
io.unsafeRunSync()  // NOW it prints and returns 42
```

**Rust Comparison:**
```rust
// Rust Future is also lazy (like IO, not Scala Future)
async fn example() -> i32 {
    println!("Side effect!");
    42
}

let future = example();  // Nothing happens yet
future.await;  // NOW it runs
```

### 2. Construction

| Method | Purpose | Example |
|--------|---------|---------|
| `IO.pure(a)` | Lift pure value | `IO.pure(42)` |
| `IO.delay { }` | Suspend side effect | `IO.delay(println("hi"))` |
| `IO { }` | Shorthand for delay | `IO(doSomething())` |
| `IO.defer` | Defer IO creation | `IO.defer(someIO)` |
| `IO.raiseError` | Create failed IO | `IO.raiseError(new Exception)` |

### 3. Error Handling

```scala
// Convert to Either
val result: IO[Either[Throwable, Int]] = riskyOp.attempt

// Recover from errors
val recovered: IO[Int] = riskyOp.handleError(_ => 0)
val recoveredWith: IO[Int] = riskyOp.handleErrorWith(_ => fallbackIO)

// Handle both paths
val redeemed: IO[String] = riskyOp.redeem(
  error => s"Failed: $error",
  success => s"Success: $success"
)
```

**Rust Equivalent:**
```rust
result.map_err(|e| transform(e))
result.or(Ok(default))
result.or_else(|_| fallback_operation())
```

### 4. EitherT[IO, E, A] - THE Production Pattern! 🌟

**Problem:** `IO[Either[E, A]]` is awkward to compose.

**Solution:** EitherT wraps it and provides clean composition:

```scala
type Result[A] = EitherT[IO, AppError, A]

def validate(email: String): Result[String] = {
  if (email.contains("@"))
    EitherT.pure[IO, AppError](email)
  else
    EitherT.leftT[IO, String](ValidationError("Invalid email"))
}

def saveUser(email: String): Result[User] = {
  EitherT.liftF(saveToDatabase(email))
}

// Clean composition with for-comprehension!
val program: Result[User] = for {
  validEmail <- validate(input)      // Short-circuits on Left
  user <- saveUser(validEmail)
} yield user
```

**Why EitherT?**
- ✅ Type-safe errors (not just Throwable)
- ✅ Short-circuits like Rust's ? operator
- ✅ Clean for-comprehension syntax
- ✅ Standard pattern in production Scala

**Rust Equivalent:**
```rust
async fn register(input: &str) -> Result<User, AppError> {
    let email = validate(input)?;  // Short-circuit on Err
    let user = save_user(email).await?;
    Ok(user)
}
```

### 5. Resource Management

```scala
// bracket - guarantees cleanup
val program = openFile("data.txt").bracket(readFile)(closeFile)

// Resource - composable resources
val dbResource = Resource.make(openDB)(closeDB)
val cacheResource = Resource.make(openCache)(closeCache)

val combined = for {
  db <- dbResource
  cache <- cacheResource
} yield (db, cache)

combined.use { case (db, cache) =>
  // Use resources
  // Both automatically cleaned up (in reverse order)
}
```

**Rust Equivalent:** RAII with Drop trait, but Scala's is explicit.

## Running Examples

```bash
cd chapter-07-cats-effect-basics

# IO basics
sbt "runMain catseffect.IOBasicsDemo"

# Error handling
sbt "runMain catseffect.IOErrorHandlingDemo"

# EitherT (important!)
sbt "runMain catseffect.EitherTDemo"

# Resource management
sbt "runMain catseffect.ResourceDemo"
```

## Exercises

### Exercise 01: IO Basics and EitherT
**File:** `src/main/scala/exercises/Exercise01_IOBasics.scala`

Practice:
1. Creating and composing IO operations
2. Error handling with attempt, handleError
3. EitherT for typed errors with validation pipeline

```bash
sbt "runMain catseffect.exercises.Exercise01IOBasics"
sbt "runMain catseffect.exercises.solutions.Exercise01Solution"
```

## IOApp - Proper Entry Point

**Don't use main() with unsafeRunSync!** Use IOApp:

```scala
object MyApp extends IOApp.Simple {
  def run: IO[Unit] = {
    for {
      _ <- IO.println("Hello")
      _ <- myProgram
    } yield ()
  }
}
```

**Benefits:**
- Proper runtime initialization
- Graceful shutdown
- Resource cleanup
- Better error handling

## Key Patterns

### 1. Sequential Composition
```scala
for {
  a <- io1
  b <- io2(a)  // depends on a
  c <- io3(b)  // depends on b
} yield c
```

### 2. Error Recovery
```scala
riskyOp
  .handleError(e => fallback)
  .attempt  // Convert to Either
```

### 3. EitherT Pipeline (PRODUCTION!)
```scala
type Result[A] = EitherT[IO, AppError, A]

val pipeline: Result[Output] = for {
  input <- validate(rawInput)
  processed <- process(input)
  saved <- save(processed)
} yield saved

// Run it
pipeline.value: IO[Either[AppError, Output]]
```

### 4. Resource Safety
```scala
Resource.make(acquire)(release).use { resource =>
  // Use it safely
}
```

## Rust vs Scala Comparison

| Concept | Rust | Scala IO |
|---------|------|----------|
| **Lazy async** | `async fn` | `IO[A]` |
| **Execution** | `.await` | `.unsafeRunSync()` or IOApp |
| **Error type** | `Result<T, E>` | `IO[A]` (Throwable) or EitherT |
| **Error propagation** | `?` operator | `.attempt`, `flatMap` |
| **Typed errors** | `Result<T, CustomError>` | `EitherT[IO, E, A]` |
| **Resource cleanup** | RAII (Drop) | bracket, Resource |
| **Composition** | async/await | for-comprehension |

## Key Takeaways

✅ **IO[A]** is lazy and referentially transparent (unlike Future)  
✅ **Lazy execution** means nothing happens until you run it  
✅ **IOApp** is the proper entry point (not main + unsafeRunSync)  
✅ **Error handling** with attempt, handleError, redeem  
✅ **EitherT[IO, E, A]** is THE pattern for typed errors in production  
✅ **Resource management** with bracket and Resource  
✅ **Rust comparison:** IO ~ Future (lazy), EitherT ~ Result with ?  

## Common Pitfalls

1. **Using unsafeRunSync in production** → Use IOApp
2. **Forgetting IO is lazy** → IO creation doesn't execute
3. **Not using EitherT** → Stick with IO[Either[E, A]] (awkward!)
4. **Forgetting resource cleanup** → Use bracket/Resource

## Next Steps

- **Chapter 08:** Concurrency with Fibers, parallelism, Ref, Deferred
- Learn how to run IOs in parallel, manage concurrent state

---

**Production Tip:** Most production Scala + Cats-Effect code uses `EitherT[IO, DomainError, A]` for the main application type. Master EitherT - it's everywhere!
