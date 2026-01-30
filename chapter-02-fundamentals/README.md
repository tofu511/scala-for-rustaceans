# Chapter 02: Scala Fundamentals for Rustaceans

Welcome to Scala fundamentals! This chapter introduces core Scala concepts with direct comparisons to Rust, helping you leverage your existing knowledge.

## Learning Objectives

By the end of this chapter, you will understand:
- Scala's type system and how it compares to Rust
- Values (`val`) vs variables (`var`) - like `let` vs `let mut`
- Functions, lambdas, and higher-order functions
- Pattern matching (similar to Rust's `match`)
- Case classes (like Rust enums/structs)
- Option type (Scala's `Option<T>`)
- Collections (List, Map, Set) and their immutability
- For-comprehensions (like Rust's `?` operator but more general)

## Project Structure

```
chapter-02-fundamentals/
├── build.sbt                    # Build configuration with scalatest
├── src/
│   ├── main/scala/
│   │   ├── 01_basics.scala           # Types, functions, lambdas
│   │   ├── 02_pattern_matching.scala # Match, case classes
│   │   ├── 03_options.scala          # Option type
│   │   └── 04_collections.scala      # List, Map, Set operations
│   └── test/scala/              # Test specs (to be added)
```

## Running the Examples

Each file has a `main` method you can run:

```bash
cd chapter-02-fundamentals

# Run individual examples
sbt "runMain fundamentals.Basics"
sbt "runMain fundamentals.PatternMatching"
sbt "runMain fundamentals.Options"
sbt "runMain fundamentals.Collections"
```

## Quick Rust ⟷ Scala Reference

### Values and Variables
```rust
// Rust
let x = 42;           // immutable
let mut y = 10;       // mutable
y = 20;               // OK
```

```scala
// Scala
val x = 42            // immutable
var y = 10            // mutable
y = 20                // OK
```

### Functions
```rust
// Rust
fn add(x: i32, y: i32) -> i32 {
    x + y
}
```

```scala
// Scala
def add(x: Int, y: Int): Int = {
  x + y
}
```

### Lambdas
```rust
// Rust
let double = |x| x * 2;
let add = |x, y| x + y;
```

```scala
// Scala
val double = (x: Int) => x * 2
val add = (x: Int, y: Int) => x + y

// Or with underscore syntax
val double: Int => Int = _ * 2
```

### Pattern Matching
```rust
// Rust
match value {
    Some(x) => println!("Got {}", x),
    None => println!("Nothing"),
}
```

```scala
// Scala
value match {
  case Some(x) => println(s"Got $x")
  case None => println("Nothing")
}
```

### Option Type
```rust
// Rust
let some: Option<i32> = Some(42);
let none: Option<i32> = None;

some.map(|x| x * 2)
some.and_then(|x| Some(x / 2))
some.unwrap_or(0)
```

```scala
// Scala
val some: Option[Int] = Some(42)
val none: Option[Int] = None

some.map(_ * 2)
some.flatMap(x => Some(x / 2))
some.getOrElse(0)
```

### Collections
```rust
// Rust
let numbers = vec![1, 2, 3, 4, 5];
let doubled: Vec<i32> = numbers.iter()
    .map(|x| x * 2)
    .collect();
let evens: Vec<i32> = numbers.iter()
    .filter(|x| x % 2 == 0)
    .copied()
    .collect();
```

```scala
// Scala
val numbers = List(1, 2, 3, 4, 5)
val doubled = numbers.map(_ * 2)
val evens = numbers.filter(_ % 2 == 0)
```

## Key Concepts

### 1. Everything is an Expression

In Scala, almost everything returns a value:

```scala
// if is an expression
val max = if (a > b) a else b

// blocks are expressions
val result = {
  val x = 10
  val y = 20
  x + y  // last expression is the value
}

// match is an expression
val description = value match {
  case 0 => "zero"
  case _ => "not zero"
}
```

Compare with Rust:
```rust
// Very similar!
let max = if a > b { a } else { b };

let result = {
    let x = 10;
    let y = 20;
    x + y
};
```

### 2. Immutability by Default (for Collections)

```scala
val list = List(1, 2, 3)
val newList = list :+ 4  // Creates new list, doesn't mutate

// Mutable collections available but discouraged
import scala.collection.mutable
val buffer = mutable.ListBuffer(1, 2, 3)
buffer += 4  // Mutates in place
```

Rust comparison:
```rust
// Rust requires explicit mut
let list = vec![1, 2, 3];         // immutable
let mut list = vec![1, 2, 3];     // mutable
list.push(4);
```

### 3. Type Inference

Both Rust and Scala have strong type inference:

```scala
val x = 42              // Int inferred
val s = "hello"         // String inferred
val list = List(1, 2)   // List[Int] inferred

// But you can (and should for APIs) annotate:
val x: Int = 42
def add(x: Int, y: Int): Int = x + y
```

### 4. No Null (use Option instead)

```scala
// AVOID THIS (Java legacy)
val s: String = null  // Compiles but bad practice

// USE THIS
val s: Option[String] = None
val s2: Option[String] = Some("hello")
```

This is just like Rust's approach!

### 5. For-Comprehensions

For-comprehensions are syntactic sugar for flatMap/map chains:

```scala
// Without for-comprehension
parseInt("10").flatMap(x =>
  parseInt("20").map(y =>
    x + y
  )
)

// With for-comprehension
for {
  x <- parseInt("10")
  y <- parseInt("20")
} yield x + y
```

Similar to Rust's `?` operator but more general:
```rust
fn add_parsed(s1: &str, s2: &str) -> Option<i32> {
    let x = parse_int(s1)?;
    let y = parse_int(s2)?;
    Some(x + y)
}
```

## Code Examples

### Example 1: Basics

See [01_basics.scala](src/main/scala/01_basics.scala) for:
- Type system (Int, String, Double, Boolean, etc.)
- String interpolation
- Functions and lambdas
- Higher-order functions
- Tuples

Run with: `sbt "runMain fundamentals.Basics"`

### Example 2: Pattern Matching

See [02_pattern_matching.scala](src/main/scala/02_pattern_matching.scala) for:
- Sealed traits (sum types like Rust enums)
- Case classes (product types like Rust structs)
- Pattern matching with guards
- Extracting values from patterns

Run with: `sbt "runMain fundamentals.PatternMatching"`

### Example 3: Options

See [03_options.scala](src/main/scala/03_options.scala) for:
- Creating and using Options
- map, flatMap, filter operations
- For-comprehensions with Options
- Practical examples with user data

Run with: `sbt "runMain fundamentals.Options"`

### Example 4: Collections

See [04_collections.scala](src/main/scala/04_collections.scala) for:
- List operations (map, filter, fold, etc.)
- Map (dictionary) usage
- Set operations
- For-comprehensions with collections

Run with: `sbt "runMain fundamentals.Collections"`

## Exercises

### How to Complete Exercises

1. **Start with Exercise Files**: Open `src/main/scala/exercises/ExerciseXX_*.scala`
2. **Read Instructions**: Each file has detailed objectives and tasks
3. **Replace `???`**: Implement the functions marked with `???`
4. **Uncomment Tests**: Remove `//` from test cases
5. **Run**: `sbt "runMain fundamentals.exercises.ExerciseXX"`
6. **Verify Output**: Compare with expected output in comments
7. **Check Solution**: Solutions are in `src/main/scala/exercises/solutions/`

### Exercise 1: Functions and Lambdas

**File**: `exercises/Exercise01_Functions.scala`

Implement:
- `factorial(n: Int): Int` - recursive factorial
- `applyNTimes(f, n, x)` - apply function n times
- Use it to compute powers of 2

**Run**:
```bash
sbt "runMain fundamentals.exercises.Exercise01"
```

**Solution**:
```bash
sbt "runMain fundamentals.exercises.solutions.Exercise01Solution"
```

### Exercise 2: Pattern Matching

**File**: `exercises/Exercise02_PatternMatching.scala`

Build an expression evaluator:
- Define `Add`, `Multiply`, `Subtract`, `Divide` case classes
- Implement `eval(expr: Expr): Int`
- Implement `exprToString(expr: Expr): String`

**Run**:
```bash
sbt "runMain fundamentals.exercises.Exercise02"
```

**Solution**:
```bash
sbt "runMain fundamentals.exercises.solutions.Exercise02Solution"
```

### Exercise 3: Options

**File**: `exercises/Exercise03_Options.scala`

Implement safe operations:
- `safeDivide(a, b)` - returns `Option[Int]`
- `divideEven(a, b)` - returns even results only
- `parseAndDivide(s, divisor)` - parse string and divide
- `getUserEmail(id)` - handle nested Options

**Run**:
```bash
sbt "runMain fundamentals.exercises.Exercise03"
```

**Solution**:
```bash
sbt "runMain fundamentals.exercises.solutions.Exercise03Solution"
```

### Exercise 4: Collections

**File**: `exercises/Exercise04_Collections.scala`

Work with collections:
- `processStrings` - filter, map, transform
- `wordFrequency` - count word occurrences
- `flattenAndSum` - flatten nested lists and sum
- `cartesianProduct` - create all pairs

**Run**:
```bash
sbt "runMain fundamentals.exercises.Exercise04"
```

**Solution**:
```bash
sbt "runMain fundamentals.exercises.solutions.Exercise04Solution"
```

### Tips for Exercises

1. **Start Simple**: Get basic cases working first
2. **Use REPL**: Test small pieces with `sbt console`
3. **Read Error Messages**: Scala's compiler is helpful
4. **Compare with Examples**: Look at the main example files
5. **Try Multiple Approaches**: For-comprehension vs flatMap
6. **Don't Peek Too Soon**: Try for 15-20 minutes before checking solution

---

## Old Exercises Section (for reference)

### Exercise 1: Functions and Lambdas

Create a file `exercises/Exercise01.scala`:

1. Write a function `factorial(n: Int): Int` that calculates factorial recursively
2. Write a higher-order function `applyNTimes(f: Int => Int, n: Int, x: Int): Int` that applies function `f` to `x` `n` times
3. Use it to compute 2^8 (apply doubling 8 times to 1)

<details>
<summary>💡 Solution Hint</summary>

```scala
def factorial(n: Int): Int = {
  if (n <= 1) 1
  else n * factorial(n - 1)
}

def applyNTimes(f: Int => Int, n: Int, x: Int): Int = {
  if (n == 0) x
  else applyNTimes(f, n - 1, f(x))
}

// 2^8
applyNTimes(_ * 2, 8, 1)  // 256
```
</details>

### Exercise 2: Pattern Matching

Create sealed trait for a simple expression evaluator:

```scala
sealed trait Expr
case class Num(value: Int) extends Expr
case class Add(left: Expr, right: Expr) extends Expr
case class Multiply(left: Expr, right: Expr) extends Expr
```

Write function `eval(expr: Expr): Int` that evaluates the expression.

Test with: `eval(Add(Num(2), Multiply(Num(3), Num(4))))  // 14`

<details>
<summary>💡 Solution Hint</summary>

```scala
def eval(expr: Expr): Int = expr match {
  case Num(value) => value
  case Add(left, right) => eval(left) + eval(right)
  case Multiply(left, right) => eval(left) * eval(right)
}
```
</details>

### Exercise 3: Options

Write a function that safely divides and returns even results only:

```scala
def divideEven(a: Int, b: Int): Option[Int] = {
  // Return Some(result) if division succeeds AND result is even
  // Return None if division by zero OR result is odd
}
```

Test cases:
- `divideEven(20, 5)` → `Some(4)`
- `divideEven(20, 6)` → `None` (result is odd)
- `divideEven(20, 0)` → `None` (division by zero)

<details>
<summary>💡 Solution Hint</summary>

```scala
def divideEven(a: Int, b: Int): Option[Int] = {
  if (b == 0) None
  else Some(a / b).filter(_ % 2 == 0)
}

// Or with for-comprehension:
def divideEven(a: Int, b: Int): Option[Int] = {
  for {
    result <- if (b == 0) None else Some(a / b)
    if result % 2 == 0
  } yield result
}
```
</details>

### Exercise 4: Collections

Given a list of strings, write a function that:
1. Filters out strings shorter than 3 characters
2. Converts each to uppercase
3. Returns a list of their lengths

```scala
def processStrings(strings: List[String]): List[Int] = {
  // Your code here
}

// Test
processStrings(List("hi", "hello", "world", "a", "scala"))
// Should return: List(5, 5, 5)
```

<details>
<summary>💡 Solution Hint</summary>

```scala
def processStrings(strings: List[String]): List[Int] = {
  strings
    .filter(_.length >= 3)
    .map(_.toUpperCase)
    .map(_.length)
}

// Or with for-comprehension:
def processStrings(strings: List[String]): List[Int] = {
  for {
    s <- strings
    if s.length >= 3
    upper = s.toUpperCase
  } yield upper.length
}
```
</details>

## Common Pitfalls for Rust Developers

### 1. Null exists (but avoid it!)

Unlike Rust, Scala runs on the JVM which has `null`. However, idiomatic Scala uses `Option` instead.

```scala
// BAD (but compiles)
val s: String = null

// GOOD
val s: Option[String] = None
```

### 2. No Ownership System

Scala uses garbage collection, not ownership. This means:
- No borrowing or lifetimes
- Can have multiple references to mutable data (but be careful!)
- Less control over memory, but simpler to write

### 3. JVM Performance Characteristics

- Startup time is slower than Rust
- Peak performance is good (JIT compilation)
- Memory usage is higher than Rust
- Garbage collection pauses can occur

### 4. Type Inference Limitations

Scala's type inference is good but has limitations:
- Can't infer return types for recursive functions
- Sometimes need type annotations in complex scenarios

```scala
// ERROR: recursive without return type
def factorial(n: Int) = {
  if (n <= 1) 1
  else n * factorial(n - 1)  // Can't infer
}

// OK: with return type
def factorial(n: Int): Int = {
  if (n <= 1) 1
  else n * factorial(n - 1)
}
```

## Testing

Tests are coming in the next phase. For now, run the examples to verify your understanding:

```bash
sbt "runMain fundamentals.Basics"
sbt "runMain fundamentals.PatternMatching"
sbt "runMain fundamentals.Options"
sbt "runMain fundamentals.Collections"
```

## Next Steps

Now that you understand Scala fundamentals, you're ready to learn about error handling patterns in [Chapter 03](../chapter-03-error-handling/).

You'll learn about:
- `Either[L, R]` (like Rust's `Result<T, E>`)
- `Try[A]` for exception handling
- `Future[A]` for async computation
- Why `Future` has problems (leading to Cats-Effect)

## Additional Resources

- [Scala Documentation - Basics](https://docs.scala-lang.org/tour/basics.html)
- [Scala Collections](https://docs.scala-lang.org/overviews/collections-2.13/introduction.html)
- [Pattern Matching](https://docs.scala-lang.org/tour/pattern-matching.html)

---

**Time to complete**: ~2-3 hours  
**Next chapter**: [Chapter 03: Error Handling](../chapter-03-error-handling/)
