# Chapter 07: Exercises Quick Reference

## Exercise 01: IO Basics and EitherT

**File:** `src/main/scala/exercises/Exercise01_IOBasics.scala`  
**Solution:** `src/main/scala/exercises/solutions/Exercise01_Solution.scala`  
**Estimated Time:** 45-60 minutes

### Objectives
- Create IO operations with delays
- Handle errors with attempt, handleError
- Use EitherT for typed error handling
- Build validation pipeline

### Tasks
**Part 1:** IO Basics
- `fetchUserName`: Return IO with user name after delay
- `fetchUserAge`: Return IO with random age

**Part 2:** Error Handling
- `divide`: Safe division that raises error on zero
- `divideWithDefault`: Use handleError for fallback

**Part 3:** EitherT (Important!)
- `validatePositive`: Return error if not positive
- `validateRange`: Return error if out of range
- `processNumber`: Chain validations with for-comprehension

### How to Run
```bash
cd chapter-07-cats-effect-basics
sbt "runMain catseffect.exercises.Exercise01IOBasics"
sbt "runMain catseffect.exercises.solutions.Exercise01Solution"
```

### Expected Output
```
=== Exercise 01 Solution ===

--- IO Basics ---
Name: User-42

--- Error Handling ---
10 / 2 = 5
10 / 0 with default = -1

--- EitherT ---
processNumber(50) = Right(Valid: 50)
processNumber(-5) = Left(InvalidInput(...))
processNumber(150) = Left(OutOfRange(...))

✓ All tests passed!
```

### Key Concepts
- **IO.delay** - Suspend side effects
- **IO.raiseError** - Create failed IO
- **handleError** - Recover from errors  
- **EitherT.pure** - Success value
- **EitherT.leftT** - Error value
- **for-comprehension** - Short-circuiting composition

### Tips
1. **IO.delay**: Use for any side-effecting code
2. **IO.sleep**: Use for delays (not Thread.sleep!)
3. **EitherT**: Think of it like Rust's Result with ? operator
4. **Type alias**: `type Result[A] = EitherT[IO, Error, A]` makes code cleaner
5. **.value**: Extracts `IO[Either[E, A]]` from EitherT

### Rust Comparison
```rust
// IO equivalent
async fn fetch_user(id: u32) -> String {
    sleep(Duration::from_millis(100)).await;
    format!("User-{}", id)
}

// Error handling
fn divide(a: i32, b: i32) -> Result<i32, Error> {
    if b == 0 { Err(Error::DivisionByZero) }
    else { Ok(a / b) }
}

// EitherT ~ chaining with ?
async fn process(n: i32) -> Result<String, ValidationError> {
    let positive = validate_positive(n)?;  // Short-circuit
    let in_range = validate_range(positive, 1, 100)?;
    Ok(format!("Valid: {}", in_range))
}
```

### Common Mistakes
1. Using `Thread.sleep` instead of `IO.sleep`
2. Forgetting to call `.value` on EitherT
3. Not importing `cats.data.EitherT`
4. Using `Right`/`Left` directly instead of EitherT constructors

---

## Quick Reference

### IO Construction
```scala
IO.pure(value)           // Lift pure value
IO.delay { sideEffect }  // Suspend side effect
IO { block }             // Shorthand
IO.raiseError(ex)        // Failed IO
```

### Error Handling
```scala
io.attempt               // IO[Either[Throwable, A]]
io.handleError(f)        // Recover with value
io.handleErrorWith(f)    // Recover with IO
```

### EitherT
```scala
EitherT.pure[IO, E](value)        // Right
EitherT.leftT[IO, A](error)       // Left
EitherT.liftF(io)                 // Lift IO
eitherT.value                     // Extract IO[Either]
```

### Composition
```scala
for {
  a <- io1
  b <- io2(a)
} yield b
```

---

## Key Takeaways

✅ IO is lazy - nothing happens until run  
✅ Use handleError for recovery, not try/catch  
✅ EitherT[IO, E, A] is the production pattern  
✅ for-comprehension = clean sequential composition  
✅ EitherT short-circuits on Left (like Rust ?)  

**Next:** Chapter 08 covers parallel execution with Fibers!
