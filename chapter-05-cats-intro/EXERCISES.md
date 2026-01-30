# Chapter 05: Exercises Quick Reference

## Exercise 01: Type Classes and Composition

**File:** `src/main/scala/exercises/Exercise01_TypeClasses.scala`  
**Solution:** `src/main/scala/exercises/solutions/Exercise01_Solution.scala`  
**Estimated Time:** 30-45 minutes

### Objectives
- Implement Semigroup and Monoid instances for custom types
- Use Functor to transform data in containers
- Apply Monad to chain dependent operations
- Write generic code using type class constraints

### Tasks
1. **Part 1:** Create Semigroup[Score] and Monoid[Score]
2. **Part 2:** Implement functions using Functor
3. **Part 3:** Chain operations with Monad (safe division, sequencing)

### How to Run
```bash
cd chapter-05-cats-intro

# Run the exercise (tests commented out)
sbt "runMain catsintro.exercises.Exercise01TypeClasses"

# Run the solution
sbt "runMain catsintro.exercises.solutions.Exercise01Solution"
```

### Expected Output (Solution)
```
=== Exercise 01 Solution ===

--- Semigroup/Monoid ---
Score(100) |+| Score(50) = Score(150)
Monoid[Score].empty = Score(0)

--- Functor ---
Doubled scores: List(Score(20), Score(40), Score(60))

--- Monad ---
safeDivide(10, 2) = Some(5)
safeDivide(10, 0) = None
parseAndDivide('20', 4) = Some(5)
sequenceOptions(List(Some(1), Some(2), Some(3))) = Some(List(1, 2, 3))

✓ All tests passed!
```

### Key Concepts
- **Semigroup:** Binary operation that combines values
- **Monoid:** Semigroup + identity element (empty)
- **Functor:** Transform values inside a context
- **Monad:** Chain operations that return new contexts

### Rust Comparison
```rust
// Rust equivalent patterns

// Semigroup/Monoid
trait Combine {
    fn combine(self, other: Self) -> Self;
}

// Functor
option.map(|x| x * 2)
result.map(|x| x * 2)

// Monad
option.and_then(|x| some_func(x))
result.and_then(|x| another_func(x))

// Sequence Options
opts.into_iter()
    .collect::<Option<Vec<_>>>()
```

### Tips
1. **Semigroup:** The combine operation must be associative
2. **Monoid:** The empty value must be an identity (x |+| empty == x)
3. **Functor:** Use `Functor[F].map` to transform values in context
4. **Monad:** For-comprehensions make chaining easier to read
5. **Type inference:** Sometimes need explicit type annotations for F[_]

### Common Mistakes
1. Forgetting to import `cats.instances.all._` and `cats.syntax.semigroup._`
2. Using `Some` instead of `Option` in generic code
3. Not providing implicit instances in companion object
4. Confusing `map` (Functor) with `flatMap` (Monad)

### Verification Checklist
- [ ] Score Semigroup combines by adding values
- [ ] Score Monoid has Score(0) as identity
- [ ] Functor functions transform containers correctly
- [ ] safeDivide handles division by zero
- [ ] parseAndDivide chains parsing and division
- [ ] sequenceOptions returns None if any element is None
- [ ] All tests uncommented and passing

---

## Tips for All Exercises

### Running Tests
Uncomment the test assertions in each exercise file to verify your solution.

### Compilation Errors
If you get "could not find implicit value", you're likely missing:
- An import (`cats.instances.all._` or `cats.syntax.xxx._`)
- An implicit instance definition
- A context bound (`[F[_]: Monad]`)

### Getting Stuck?
1. Check the example files (01_type_classes.scala, etc.)
2. Look at the solution comments at the bottom of the exercise file
3. Run the complete solution in `exercises/solutions/`

### Learning Strategy
1. **Read** the objectives and understand what's being asked
2. **Try** to implement without looking at solutions
3. **Compile** frequently to catch errors early
4. **Test** by uncommenting assertions one at a time
5. **Compare** your solution with the provided one

---

## Next Steps

After completing these exercises:
1. Review the main README for conceptual understanding
2. Run all example files to see type classes in action
3. Move on to **Chapter 06: Cats Error Handling**

## Key Takeaways

✅ Type classes provide generic abstractions  
✅ Semigroup + Monoid = composable aggregation  
✅ Functor = transform values in contexts  
✅ Monad = chain dependent computations  
✅ For-comprehensions = readable flatMap chains  

You're now ready for more advanced Cats patterns!
