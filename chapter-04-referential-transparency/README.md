# Chapter 04: Referential Transparency

Understand referential transparency (RT), why it matters, and why Scala's Future breaks it.

## Overview

This chapter explains one of the most important concepts in functional programming: **referential transparency**. You'll learn what it means, why it's valuable, and most importantly, why Scala's `Future` breaks it (motivating the need for `IO[A]` in Chapter 07).

## Learning Objectives

- Understand what referential transparency means
- Identify pure vs impure functions
- Apply the substitution test
- Recognize how Future breaks RT
- Understand the problems this creates
- Preview the solution (IO[A])

## Prerequisites

- Completed Chapter 03 (Error Handling with Future)
- Familiar with side effects and state
- Basic understanding of functional programming concepts

## Quick Start

```bash
# Compile the project
sbt compile

# Run examples
sbt "runMain referentialtransparency.ReferentialTransparencyExamples"
sbt "runMain referentialtransparency.FutureProblemsExamples"
sbt "runMain referentialtransparency.TowardsIOExamples"

# Work on exercises
sbt "runMain referentialtransparency.exercises.Exercise01RT"
sbt "runMain referentialtransparency.exercises.Exercise02Future"
```

## Content Structure

### Example Files

Located in `src/main/scala/`:

1. **01_referential_transparency.scala** - RT fundamentals
   - What is referential transparency?
   - Pure vs impure functions
   - The substitution test
   - Why RT matters (reasoning, testing, refactoring, parallelization)
   - Practical guidelines

2. **02_future_problems.scala** - Future's RT violations
   - Problem 1: Eager evaluation
   - Problem 2: Not RT (the classic example)
   - Problem 3: Immediate side effects
   - Problem 4: Composition difficulties
   - Problem 5: Testing challenges
   - Problem 6: Race conditions
   - Problem 7: Retry/repeat issues
   - Preview of IO[A] solution

3. **03_towards_io.scala** - Building a solution
   - Attempt 1: Lazy val (doesn't work)
   - Attempt 2: Thunk (better, but limited)
   - Attempt 3: SimpleIO wrapper (good!)
   - Demonstrating RT with SimpleIO
   - What's missing vs real IO
   - Preview of Cats-Effect IO
   - Rust Future comparison

### Exercises

Located in `src/main/scala/exercises/`:

- **Exercise01_RT.scala** - Identify pure functions and refactor impure ones
- **Exercise02_Future.scala** - Experience Future's RT problems firsthand

See [EXERCISES.md](EXERCISES.md) for detailed instructions.

## Key Concepts

### Referential Transparency

**Definition**: An expression is referentially transparent if you can replace it with its value without changing the program's behavior.

```scala
// Pure function - RT
def add(a: Int, b: Int): Int = a + b

val x = add(2, 3)  // Can replace with 5
val y = x * 2      // Same as: val y = 5 * 2
```

**The Substitution Test**:
```scala
// Original
def program1 = {
  val a = add(2, 3)
  val b = add(2, 3)
  a + b
}

// Substituted
def program2 = {
  val a = 5
  val b = 5
  a + b
}

// If program1 == program2, then add is RT ✅
```

### Pure Functions

A function is pure if:
1. **Deterministic**: Same inputs → same outputs
2. **No side effects**: Doesn't modify external state or perform I/O

```scala
// ✅ Pure
def square(x: Int): Int = x * x

// ❌ Impure (side effect)
var counter = 0
def increment(): Int = {
  counter += 1  // Modifies external state
  counter
}

// ❌ Impure (non-deterministic)
def rollDice(): Int = scala.util.Random.nextInt(6) + 1
```

**Rust Comparison**:
Rust also values purity, though it's not enforced:
```rust
// Pure in Rust
fn square(x: i32) -> i32 { x * x }

// Impure (but allowed)
static mut COUNTER: i32 = 0;
fn increment() -> i32 {
    unsafe {
        COUNTER += 1;
        COUNTER
    }
}
```

### Why Future Breaks RT

The classic example from Chapter 03:

```scala
var counter = 0
def increment(): Future[Int] = Future {
  counter += 1
  counter
}

// Example A: Reuse same future
val future = increment()
val resultA = for {
  a <- future
  b <- future
} yield (a, b)  // (1, 1)

// Example B: Create new futures
val resultB = for {
  a <- increment()
  b <- increment()
} yield (a, b)  // (1, 2)

// NOT equivalent! Can't substitute increment() with future
// ❌ Breaks referential transparency
```

**Why it breaks**:
1. **Eager evaluation**: Future executes immediately on creation
2. **Side effects happen immediately**: Can't control when effects occur
3. **Can't substitute**: Replacing `increment()` with `future` changes behavior

### The Seven Problems with Future

1. **Eager evaluation** - Starts executing on creation
2. **Not RT** - Can't substitute with values
3. **Immediate side effects** - No separation of description vs execution
4. **Composition issues** - Can't build reusable operation descriptions
5. **Testing difficulties** - Side effects during test setup
6. **Race conditions** - Shared mutable state problems
7. **Retry/repeat issues** - Can't safely retry the same future

### The Solution: IO[A]

Preview of what we'll learn in Chapter 07:

```scala
import cats.effect.IO

var counter = 0
def increment(): IO[Int] = IO {
  counter += 1
  counter
}

// Just a description, no execution yet
val io = increment()

// Example A: Reuse same IO
val programA = for {
  a <- io
  b <- io
} yield (a, b)

// Example B: Create new IOs  
val programB = for {
  a <- increment()
  b <- increment()
} yield (a, b)

// Can substitute increment() with io:
val programC = for {
  a <- io
  b <- io
} yield (a, b)

// programA and programC are equivalent!
// ✅ Referentially transparent!

// Explicit execution
programA.unsafeRunSync()  // (1, 1)
```

**Why IO works**:
- **Lazy**: Nothing happens until explicitly run
- **Separates description from execution**: `IO` is just a value describing an effect
- **Referentially transparent**: Can substitute IO values freely
- **Composable**: Can build complex programs from simple ones
- **Testable**: Test descriptions without executing side effects

## Rust Comparison

### Scala Future vs IO vs Rust Future

| Type | Evaluation | RT | Rust Equivalent |
|------|-----------|-------|-----------------|
| `Future[A]` | Eager | ❌ No | `tokio::spawn()` |
| `IO[A]` | Lazy | ✅ Yes | `async { ... }` / `Future` trait |

**Key insight for Rustaceans**:
```rust
// Rust's Future (lazy)
let future = async { side_effect() };  // Nothing happens yet
future.await;  // Execution happens here

// Scala's IO (similar to Rust)
val io = IO { sideEffect() }  // Nothing happens yet
io.unsafeRunSync()  // Execution happens here

// Scala's Future (different from Rust!)
val future = Future { sideEffect() }  // Already executing!
Await.result(future, duration)  // Just waiting for result
```

## Benefits of Referential Transparency

### 1. Easy Reasoning

```scala
// Can trace execution mentally
def compute(x: Int): Int = {
  val step1 = x * 2      // 5 * 2 = 10
  val step2 = step1 + 1  // 10 + 1 = 11
  step2 * 3              // 11 * 3 = 33
}
```

### 2. Simple Testing

```scala
// No mocks, no setup, no teardown needed
assert(add(2, 3) == 5)
assert(add(2, 3) == 5)  // Same result every time
```

### 3. Safe Refactoring

```scala
// Can inline or extract without changing behavior
val temp = x * 2
temp + 1

// Same as:
x * 2 + 1
```

### 4. Parallelization

```scala
// Safe to parallelize pure functions
items.map(f)  // Can run in parallel, no race conditions
```

### 5. Caching/Memoization

```scala
// Can cache results safely
cache.getOrElseUpdate(key, expensivePureFunction(key))
```

## Practical Guidelines

### Prefer Pure Functions

```scala
// ❌ Impure
var total = 0
def addToTotal(x: Int): Unit = {
  total += x
}

// ✅ Pure
def addToValue(current: Int, x: Int): Int = {
  current + x
}
```

### Separate I/O from Logic

```scala
// ❌ Mixed
def processUser(id: Int): User = {
  val data = database.fetch(id)  // I/O
  User(data.name.toUpperCase)    // Logic
}

// ✅ Separated
def parseUser(data: UserData): User = {  // Pure logic
  User(data.name.toUpperCase)
}

def fetchAndParse(id: Int): IO[User] = {  // I/O wrapper
  IO { database.fetch(id) }.map(parseUser)
}
```

### Push Side Effects to Edges

```scala
// Keep core logic pure
def validateUser(user: User): Either[Error, User] = ???  // Pure

def saveUser(user: User): IO[Unit] = ???  // Effect at edge

// Compose
val program: IO[Either[Error, Unit]] = for {
  user <- fetchUser(id)
  validated = validateUser(user)  // Pure computation
  result <- validated.traverse(saveUser)  // Effect at end
} yield result
```

## Common Pitfalls

### Pitfall 1: Hidden Side Effects

```scala
// Looks pure, but isn't!
def getUser(id: Int): User = {
  log.debug(s"Fetching user $id")  // Hidden side effect!
  database.query(id)
}
```

### Pitfall 2: Non-local State

```scala
// Depends on mutable state
var config = loadConfig()
def process(data: String): String = {
  s"${config.prefix}:$data"  // Not pure!
}
```

### Pitfall 3: Clock/Random

```scala
// Non-deterministic
def isExpired(token: Token): Boolean = {
  token.expiresAt < System.currentTimeMillis()  // Not pure!
}

// Pure alternative: pass time as parameter
def isExpired(token: Token, now: Long): Boolean = {
  token.expiresAt < now
}
```

## Exercises

Complete the two exercises to practice:

1. **Exercise 01**: Identify pure functions and refactor impure ones
2. **Exercise 02**: Experience Future's RT problems firsthand

See [EXERCISES.md](EXERCISES.md) for instructions.

## Summary

**Referential Transparency**:
- Can replace expression with its value
- Foundation of functional programming
- Enables reasoning, testing, refactoring, parallelization

**Pure Functions**:
- Deterministic (same inputs → same outputs)
- No side effects
- Composable and testable

**Future Problems**:
- Eager evaluation
- Not referentially transparent
- Difficult to compose and test

**Solution**:
- IO[A] from Cats-Effect (Chapter 07)
- Lazy, referentially transparent
- Separates description from execution

## Next Steps

After completing this chapter:

1. **Chapter 05**: Learn about Cats and type classes
2. **Chapter 06**: Cats error handling
3. **Chapter 07**: IO[A] - the solution to Future's problems
4. **Chapter 08**: Concurrency with Cats-Effect

## Resources

- [What is Referential Transparency?](https://www.scala-exercises.org/fp_in_scala/getting_started_with_functional_programming)
- [Why Functional Programming Matters](https://www.cs.kent.ac.uk/people/staff/dat/miranda/whyfp90.pdf)
- [Cats-Effect Documentation](https://typelevel.org/cats-effect/)
- [Rust Async Book](https://rust-lang.github.io/async-book/)

---

**Time Estimate**: 1-1.5 hours (including exercises)

**Difficulty**: Intermediate (conceptual)

**Next**: [Chapter 05: Introduction to Cats](../chapter-05-cats-intro/README.md)
