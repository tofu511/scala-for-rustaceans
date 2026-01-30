# Chapter 06: Exercises Quick Reference

## Exercise 01: Product Validation

**File:** `src/main/scala/exercises/Exercise01_Validation.scala`  
**Solution:** `src/main/scala/exercises/solutions/Exercise01_Solution.scala`  
**Estimated Time:** 30-40 minutes

### Objectives
- Implement validators using Validated (accumulate all errors)
- Use MonadError for generic parsing/calculation
- Understand when to use each approach

### Tasks
**Part 1:** Validated accumulation
- Validate product name (1-50 chars)
- Validate price (0-10000)
- Validate quantity (1-1000)
- Combine with mapN

**Part 2:** MonadError generics
- Parse strings to Double/Int
- Calculate total with validation
- Works with any F[_]: Try, Either, etc.

### How to Run
```bash
cd chapter-06-cats-error-handling
sbt "runMain catserrorhandling.exercises.Exercise01Validation"
sbt "runMain catserrorhandling.exercises.solutions.Exercise01Solution"
```

### Expected Output (Solution)
```
=== Exercise 01 Solution ===

--- Validated (accumulates all errors) ---
Valid: Valid(Product(Widget,29.99,10))
Invalid: Invalid(NonEmptyList(...3 errors...))

--- MonadError (sequential operations) ---
Success: Success(299.9)
Parse failure: Failure(NumberFormatException)

✓ All tests passed!
```

### Key Concepts
- **Validated** = Collect all errors (form validation)
- **MonadError** = Generic error handling (works with Try, Either, IO)
- **mapN** = Applicative combination (parallel validations)
- **for-comprehension** = Sequential operations (short-circuit)

### Tips
1. Use `.validNel` to create Valid with NonEmptyList error type
2. Use `.invalidNel` to create Invalid
3. MonadError needs `ME.raiseError` and `ME.pure`
4. Import `cats.syntax.monadError._` for `.ensure`

### Rust Comparison
```rust
// Validated equivalent (manual)
let mut errors = Vec::new();
// ... collect all errors ...
if errors.is_empty() { Ok(data) } else { Err(errors) }

// MonadError equivalent
fn parse<T: FromStr>(s: &str) -> Result<T, Error> {
    s.parse().map_err(|e| Error::Parse(e))
}
```

---

## Quick Tips

### Common Patterns

**1. Accumulate errors (Validated):**
```scala
(v1, v2, v3).mapN((a, b, c) => Result(a, b, c))
```

**2. Short-circuit (Either/MonadError):**
```scala
for {
  a <- operation1
  b <- operation2(a)  // depends on a
  c <- operation3(b)  // depends on b
} yield c
```

**3. Convert between:**
```scala
validated.toEither  // Validated → Either
either.toValidated  // Either → Validated
```

### When Things Go Wrong

**"could not find implicit Semigroup"**
→ Add: `import cats.instances.list._` (for List error type)

**"value mapN is not a member"**
→ Add: `import cats.syntax.apply._`

**"value ensure is not a member"**
→ Add: `import cats.syntax.monadError._`

### Learning Strategy
1. Run example files first to see patterns
2. Implement exercise without peeking at solution
3. Uncomment tests one at a time
4. Check solution if stuck

---

## Next Chapter

After mastering error handling:
- **Chapter 07:** IO Monad (finally, referentially transparent effects!)
- Learn why all this error handling matters for building reliable systems

## Key Takeaways

✅ Validated = parallel, independent validations (all errors)  
✅ MonadError = sequential, dependent operations (first error)  
✅ Real apps use both: Validated for input, Either for logic  
✅ Rust: Manual error collection vs Cats: type-safe abstractions  

You're ready for IO[A] and pure functional effects!
