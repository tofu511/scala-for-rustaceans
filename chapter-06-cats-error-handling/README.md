# Chapter 06: Cats Error Handling

## Overview

This chapter covers Cats' powerful error handling abstractions: **Validated** for accumulating errors and **MonadError/ApplicativeError** for generic error handling across different effect types.

**Learning Objectives:**
- Use Validated to collect all validation errors (not just the first)
- Apply MonadError/ApplicativeError type classes for generic error handling
- Combine Validated (parallel) and Either (sequential) effectively
- Understand when to use each approach

**Time Estimate:** 1.5-2 hours

## Key Concepts

### 1. Validated - Error Accumulation

**Problem:** Either short-circuits on first error. When validating user input, you want to show ALL errors at once.

**Solution:** Validated[E, A] accumulates errors using Semigroup.

```scala
// Either - stops at first error
for {
  name <- validateName(input.name)   // ❌ Stops here if invalid
  email <- validateEmail(input.email) // Never runs
  age <- validateAge(input.age)
} yield User(name, email, age)

// Validated - collects ALL errors
(validateName(input.name), 
 validateEmail(input.email), 
 validateAge(input.age)
).mapN(User)  // ✓ All three run, errors accumulated
```

**Rust Comparison:**
```rust
// Rust: Manual error collection
let mut errors = Vec::new();
if let Err(e) = validate_name(&name) { errors.push(e); }
if let Err(e) = validate_email(&email) { errors.push(e); }
if let Err(e) = validate_age(age) { errors.push(e); }

if errors.is_empty() { Ok(User { ... }) }
else { Err(errors) }
```

### 2. MonadError / ApplicativeError

**Type classes for generic error handling.** Works with Either, Try, Option, IO, any F[_] that can represent failure.

```scala
// Generic code that works with Try, Either, IO, etc.
def safeDivide[F[_]: MonadError[*[_], Throwable]](a: Int, b: Int): F[Int] = {
  if (b == 0) MonadError[F, Throwable].raiseError(new ArithmeticException("Div by zero"))
  else MonadError[F, Throwable].pure(a / b)
}

safeDivide[Try](10, 2)     // Success(5)
safeDivide[Either[Throwable, *]](10, 0)  // Left(ArithmeticException)
```

**Key Operations:**
- `raiseError[A](e: E): F[A]` - create a failed F
- `handleError(f: E => A): F[A]` - recover from failure
- `ensure(error: E)(predicate: A => Boolean): F[A]` - validate
- `adaptError(pf: PartialFunction[E, E]): F[A]` - transform error

### 3. When to Use What

| Scenario | Use | Why |
|----------|-----|-----|
| Form validation | **Validated** | Show all errors at once |
| Independent checks | **Validated** | Can run in parallel |
| Sequential operations | **Either/MonadError** | Each depends on previous |
| Generic error handling | **MonadError** | Works with any F[_] |
| Need to convert | `.toEither` / `.toValidated` | Both directions supported |

### Real-World Pattern

```scala
// Phase 1: Input validation (Validated - accumulate)
def validateInput(...): Validated[NonEmptyList[Error], User] = {
  (validateName(...), validateEmail(...), validateAge(...)).mapN(User)
}

// Phase 2: Business logic (Either - short-circuit)
def processUser(user: User): Either[Error, User] = {
  for {
    _ <- checkUsernameAvailable(user.username)
    _ <- checkEmailNotDuplicate(user.email)
    saved <- saveToDatabase(user)
  } yield saved
}

// Combined workflow
def registerUser(...): Either[List[Error], User] = {
  validateInput(...).toEither.left.map(_.toList).flatMap(processUser)
}
```

## Running Examples

```bash
cd chapter-06-cats-error-handling

# Validated basics
sbt "runMain catserrorhandling.ValidatedBasics"

# MonadError/ApplicativeError
sbt "runMain catserrorhandling.MonadErrorBasics"

# Combining approaches
sbt "runMain catserrorhandling.CombiningApproaches"
```

## Exercises

### Exercise 01: Product Validation
**File:** `src/main/scala/exercises/Exercise01_Validation.scala`

Practice both approaches:
1. Validated for product input validation (name, price, quantity)
2. MonadError for parsing and calculations

```bash
sbt "runMain catserrorhandling.exercises.Exercise01Validation"
sbt "runMain catserrorhandling.exercises.solutions.Exercise01Solution"
```

## Rust vs Scala Comparison

| Concept | Rust | Scala/Cats |
|---------|------|------------|
| **Short-circuit** | `?` operator | Either, MonadError |
| **Accumulate errors** | Manual Vec | Validated |
| **Generic over error types** | Not possible (no HKT) | MonadError[F[_], E] |
| **Error transformation** | `.map_err()` | `.leftMap()`, `.adaptError()` |
| **Recovery** | `.unwrap_or_else()` | `.handleError()` |

## Key Takeaways

✅ **Validated** accumulates errors (unlike Either) - perfect for form validation  
✅ **MonadError** provides generic error handling across types (Try, Either, IO)  
✅ **Combine both**: Validated for input, Either for business logic  
✅ **Rust comparison**: Validated ~ manual Vec, MonadError ~ generic Result trait  
✅ **Choose based on semantics**: Independent → Validated, Sequential → Either  

## Next Steps

- **Chapter 07:** IO Monad - Referentially transparent effects
- **Chapter 08:** Concurrency with Cats-Effect

---

**Rust Developers:** Think of Validated as "collect all errors" mode and MonadError as a trait that abstracts over Result-like types. The key insight: choose your error handling strategy based on whether operations are independent (Validated) or dependent (Either).
