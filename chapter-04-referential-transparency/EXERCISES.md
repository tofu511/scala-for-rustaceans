# Chapter 04 Exercises: Referential Transparency

Quick reference for hands-on exercises.

## Exercise Files

All exercise files are in `src/main/scala/exercises/`:
- `Exercise01_RT.scala` - Identifying and creating pure functions
- `Exercise02_Future.scala` - Understanding Future's RT problems

## How to Work on Exercises

1. **Open the exercise file** and read the objectives
2. **Implement functions** marked with `???` or answer questions in comments
3. **Uncomment test assertions** in `main()`
4. **Run the exercise** with: `sbt "runMain referentialtransparency.exercises.ExerciseXX"`
5. **Check solutions** in `exercises/solutions/` if stuck

## Exercise 01: Referential Transparency

**Focus**: Understanding pure vs impure functions

**Parts**:
- Part 1: Identify which functions are pure/impure
- Part 2: Refactor impure functions to be pure
- Part 3: Implement a purity test
- Part 4: Build pure programs (factorial, validation, etc.)

**Run**: `sbt "runMain referentialtransparency.exercises.Exercise01RT"`

**Key concepts**: Substitution principle, determinism, side effects, pure functions

## Exercise 02: Future's RT Problems

**Focus**: Experience why Future breaks RT

**Parts**:
- Part 1: Predict behavior of Future programs
- Part 2: Demonstrate eager evaluation
- Part 3: See testing challenges
- Part 4: Compare lazy vs eager with LazyFuture wrapper

**Run**: `sbt "runMain referentialtransparency.exercises.Exercise02Future"`

**Key concepts**: Eager vs lazy evaluation, non-RT behavior, composition issues

## Solutions

Complete solutions are in `src/main/scala/exercises/solutions/`:
- `Exercise01_RT_Solution.scala`
- `Exercise02_Future_Solution.scala`

Run solutions with:
```bash
sbt "runMain referentialtransparency.exercises.solutions.Exercise01RTSolution"
sbt "runMain referentialtransparency.exercises.solutions.Exercise02FutureSolution"
```

## Tips

- **RT Test**: If you can substitute an expression with its value without changing behavior, it's RT
- **Pure Functions**: Same inputs → same outputs, no side effects
- **Future**: Executes immediately (eager), not RT
- **LazyFuture/IO**: Description of effect (lazy), referentially transparent
- **Rust Comparison**: Scala Future ≈ tokio::spawn(), Scala IO ≈ Rust's Future trait

## Next Steps

After completing these exercises:
1. You understand what referential transparency means
2. You can identify pure vs impure functions
3. You see why Future is problematic
4. You're motivated to learn IO[A] in Chapter 07
5. Continue with Chapter 05 (Cats) to build foundations for Chapter 07

---

**Time Estimate**: 1-1.5 hours

**Difficulty**: Intermediate (conceptual)

**Next**: [Chapter 05: Introduction to Cats](../chapter-05-cats-intro/README.md)
