# Chapter 03 Exercises: Error Handling

Quick reference for hands-on exercises.

## Exercise Files

All exercise files are in `src/main/scala/exercises/`:
- `Exercise01_Either.scala` - Validation and error handling
- `Exercise02_Try.scala` - Exception handling
- `Exercise03_Future.scala` - Async computation

## How to Work on Exercises

1. **Open the exercise file** and read the objectives
2. **Implement functions** marked with `???`
3. **Uncomment test assertions** in `main()`
4. **Run the exercise** with: `sbt "runMain errorhandling.exercises.ExerciseXX"`
5. **Check solutions** in `exercises/solutions/` if stuck

## Exercise 01: Either

**Focus**: Validation and type-safe error handling

**Functions to implement**:
- `parsePositiveInt` - Parse string to positive integer
- `validateEmail` - Validate email format
- `validateUsername` - Check username constraints
- `validateAge` - Validate age range
- `createAccount` - Combine all validations

**Run**: `sbt "runMain errorhandling.exercises.Exercise01Either"`

**Key concepts**: Either[L, R], flatMap chaining, for-comprehensions, left.map

## Exercise 02: Try

**Focus**: Exception handling in functional style

**Functions to implement**:
- `safeDivide` - Division with exception handling
- `parseConfigLine` - Parse key=value format
- `computeAverage` - Parse numbers and calculate average
- `safeParseInt` - Parse with recovery
- `chainOperations` - Chain multiple Try operations

**Run**: `sbt "runMain errorhandling.exercises.Exercise02Try"`

**Key concepts**: Try[A], Success/Failure, recover, getOrElse, chaining

## Exercise 03: Future

**Focus**: Async operations and parallel execution

**Functions to implement**:
- `fetchUserName` - Async DB lookup simulation
- `fetchUserAge` - Async DB lookup simulation
- `fetchUserProfile` - Parallel fetch and combine
- `fetchMultipleProfiles` - Parallel batch operations
- `firstCompleted` - Race multiple futures

**Run**: `sbt "runMain errorhandling.exercises.Exercise03Future"`

**Key concepts**: Future[A], parallel vs sequential, Future.sequence, eager evaluation

## Solutions

Complete solutions are in `src/main/scala/exercises/solutions/`:
- `Exercise01_Either_Solution.scala`
- `Exercise02_Try_Solution.scala`
- `Exercise03_Future_Solution.scala`

Run solutions with:
```bash
sbt "runMain errorhandling.exercises.solutions.Exercise01EitherSolution"
sbt "runMain errorhandling.exercises.solutions.Exercise02TrySolution"
sbt "runMain errorhandling.exercises.solutions.Exercise03FutureSolution"
```

## Tips

- **Either**: Think about what error type makes sense for each function
- **Try**: Use Try when working with exception-throwing code
- **Future**: Start futures before for-comprehension for parallel execution
- **Testing**: Run often, uncomment tests one at a time
- **Rust comparison**: Either ≈ Result, Try ≈ catching panics, Future ≈ async (but eager!)

## Next Steps

After completing these exercises:
1. Review the example files (01_either.scala, 02_try.scala, 03_future.scala)
2. Move on to Chapter 04 to understand referential transparency problems with Future
3. In Chapter 07, you'll learn about IO[A] which solves these problems
