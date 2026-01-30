# Chapter 02 Exercises - Quick Reference

## Exercise Files Structure

```
src/main/scala/exercises/
├── Exercise01_Functions.scala          # Recursive functions, lambdas
├── Exercise02_PatternMatching.scala    # Expression evaluator
├── Exercise03_Options.scala            # Safe operations with Option
├── Exercise04_Collections.scala        # List transformations
└── solutions/
    ├── Exercise01_Solution.scala
    ├── Exercise02_Solution.scala
    ├── Exercise03_Solution.scala
    └── Exercise04_Solution.scala
```

## Quick Start Guide

### Step 1: Open Exercise File
```bash
# Open in your editor
vim src/main/scala/exercises/Exercise01_Functions.scala
```

### Step 2: Read Instructions
Each file contains:
- **OBJECTIVES**: What you'll learn
- **TASKS**: What to implement
- **HOW TO RUN**: Command to test
- **EXPECTED OUTPUT**: What you should see

### Step 3: Implement
Replace `???` with your code:
```scala
def factorial(n: Int): Int = {
  ???  // Replace this with your implementation
}
```

### Step 4: Uncomment Tests
Remove `//` from test cases:
```scala
// Before:
// println(s"factorial(5) = ${factorial(5)}")

// After:
println(s"factorial(5) = ${factorial(5)}")
```

### Step 5: Run
```bash
sbt "runMain fundamentals.exercises.Exercise01"
```

### Step 6: Check Solution (if stuck)
```bash
sbt "runMain fundamentals.exercises.solutions.Exercise01Solution"
```

## All Exercise Commands

### Run Exercises (Your Code)
```bash
sbt "runMain fundamentals.exercises.Exercise01"
sbt "runMain fundamentals.exercises.Exercise02"
sbt "runMain fundamentals.exercises.Exercise03"
sbt "runMain fundamentals.exercises.Exercise04"
```

### Run Solutions (Reference)
```bash
sbt "runMain fundamentals.exercises.solutions.Exercise01Solution"
sbt "runMain fundamentals.exercises.solutions.Exercise02Solution"
sbt "runMain fundamentals.exercises.solutions.Exercise03Solution"
sbt "runMain fundamentals.exercises.solutions.Exercise04Solution"
```

## Exercise Difficulty

| Exercise | Topic | Difficulty | Time |
|----------|-------|------------|------|
| 01 | Functions & Lambdas | ⭐⭐ Easy | 15-20 min |
| 02 | Pattern Matching | ⭐⭐⭐ Medium | 20-30 min |
| 03 | Options | ⭐⭐⭐ Medium | 20-30 min |
| 04 | Collections | ⭐⭐⭐⭐ Medium-Hard | 30-40 min |

## Learning Flow

```
Exercise 01: Functions & Lambdas
     ↓
     Learn recursion and higher-order functions
     ↓
Exercise 02: Pattern Matching
     ↓
     Build expression evaluator with sealed traits
     ↓
Exercise 03: Options
     ↓
     Handle nullable values safely
     ↓
Exercise 04: Collections
     ↓
     Master List transformations
```

## Tips

1. **Use sbt console for quick tests**:
   ```bash
   sbt console
   scala> def factorial(n: Int): Int = if (n <= 1) 1 else n * factorial(n - 1)
   scala> factorial(5)
   ```

2. **Check compilation without running**:
   ```bash
   sbt compile
   ```

3. **Work incrementally**:
   - Implement one function at a time
   - Uncomment one test at a time
   - Verify output matches expected

4. **Stuck? Check the hints**:
   - Each exercise file has hints in comments
   - Solutions include Rust comparisons
   - README has detailed explanations

5. **Try both approaches**:
   - Many exercises can use for-comprehensions OR flatMap
   - Try both to understand the difference

## Common Errors and Fixes

### Error: "not found: value ???"
**Problem**: You forgot to replace `???`  
**Fix**: Implement the function

### Error: "type mismatch"
**Problem**: Return type doesn't match expected type  
**Fix**: Check function signature and return value

### Error: "diverging implicit expansion"
**Problem**: Compiler can't infer types  
**Fix**: Add explicit type annotations

### Error: "value X is not a member of Y"
**Problem**: Trying to use method that doesn't exist  
**Fix**: Check Scala API docs or examples

## Resources

- [Scala Collections API](https://www.scala-lang.org/api/current/scala/collection/)
- [Option API](https://www.scala-lang.org/api/current/scala/Option.html)
- [Pattern Matching Guide](https://docs.scala-lang.org/tour/pattern-matching.html)

## Next Steps

After completing all exercises:
1. ✅ You understand Scala fundamentals
2. ✅ You can write recursive functions
3. ✅ You can use pattern matching effectively
4. ✅ You handle nullable values with Option
5. ✅ You can transform collections

Ready for **Chapter 03: Error Handling**! 🚀
