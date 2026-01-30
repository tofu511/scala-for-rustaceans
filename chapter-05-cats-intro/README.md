# Chapter 05: Introduction to Cats

## Overview

Welcome to Cats! This chapter introduces the **Cats** library, which brings functional programming abstractions to Scala. For Rust developers, think of Cats as a collection of powerful, reusable traits (type classes) that make functional programming more ergonomic.

**Learning Objectives:**
- Understand the type class pattern and how it differs from Rust traits
- Master Semigroup and Monoid for combining values
- Use Functor to transform data in contexts
- Apply Monad to chain sequential operations
- Prepare for Cats-Effect (IO monad) in Chapter 7

**Time Estimate:** 2-3 hours

## Why Cats?

### The Problem
Scala's standard library has some functional tools (Option, Either, Future), but they're not unified. You can't write generic code that works across all of them easily.

### The Solution: Type Classes
Cats provides a unified set of abstractions using the **type class pattern**:
- Define behavior separately from the type
- Add behavior to types retroactively (even sealed types)
- Write generic code that works with any type that has the behavior

### Rust Comparison

| Concept | Rust | Scala/Cats |
|---------|------|------------|
| **Traits** | `trait Add` | Type class `Semigroup` |
| **Implementation** | `impl Add for T` | `implicit val: Semigroup[T]` |
| **Constraints** | `T: Add` | `T: Semigroup` (context bound) |
| **Usage** | `a + b` (if Add trait) | `a |+| b` (with Semigroup) |
| **Generic code** | `fn combine<T: Add>(a: T, b: T)` | `def combine[T: Semigroup](a: T, b: T)` |

**Key Difference:** 
- Rust traits: Implemented once per type, part of the type
- Scala type classes: Can have multiple instances, resolved implicitly

## Core Type Classes

### 1. Semigroup - Combining Values

**Definition:** A type with an associative `combine` operation.

```scala
trait Semigroup[A] {
  def combine(x: A, y: A): A
}
```

**Rust Equivalent:**
```rust
trait Semigroup {
    fn combine(self, other: Self) -> Self;
}
```

**Examples:**
- `Semigroup[Int]`: addition (1 |+| 2 = 3)
- `Semigroup[String]`: concatenation ("hello" |+| "world" = "helloworld")
- `Semigroup[List[A]]`: concatenation

**When to use:** When you need to combine two values of the same type.

### 2. Monoid - Semigroup with Identity

**Definition:** A Semigroup with an "empty" (identity) element.

```scala
trait Monoid[A] extends Semigroup[A] {
  def empty: A
}
```

**Rust Equivalent:**
```rust
trait Monoid: Semigroup {
    fn empty() -> Self;
}
```

**Laws:**
- `combine(x, empty) == x` (right identity)
- `combine(empty, x) == x` (left identity)
- `combine(x, combine(y, z)) == combine(combine(x, y), z)` (associativity)

**Examples:**
- `Monoid[Int]`: empty = 0
- `Monoid[String]`: empty = ""
- `Monoid[List[A]]`: empty = List.empty

**When to use:** 
- Folding collections: `list.foldLeft(Monoid[A].empty)(_ |+| _)`
- Parallel aggregation (MapReduce-style)
- Building accumulator types

### 3. Functor - Mapping Over Contexts

**Definition:** A type constructor `F[_]` with a `map` operation.

```scala
trait Functor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}
```

**Rust Equivalent:**
```rust
// Option, Result, Iterator all have map()
option.map(|x| x * 2)
result.map(|x| x * 2)
iter.map(|x| x * 2)
```

**Examples:**
- `Functor[Option]`: `Some(1).map(_ * 2) == Some(2)`
- `Functor[List]`: `List(1,2,3).map(_ * 2) == List(2,4,6)`
- `Functor[Either[E, *]]`: `Right(1).map(_ * 2) == Right(2)`

**Key Insight:** Functor lets you transform the value inside a context without changing the context itself.

### 4. Monad - Chaining Dependent Operations

**Definition:** A type constructor with `flatMap` (chain effects) and `pure` (lift values).

```scala
trait Monad[F[_]] extends Functor[F] {
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  def pure[A](a: A): F[A]
}
```

**Rust Equivalent:**
```rust
// Option::and_then, Result::and_then
option.and_then(|x| some_function(x))
result.and_then(|x| another_function(x))

// ? operator for short-circuiting
let x = opt1?;
let y = opt2?;
Some(x + y)
```

**Examples:**
- `Monad[Option]`: chain operations that might fail
- `Monad[Either[E, *]]`: chain operations with typed errors
- `Monad[List]`: non-deterministic computation
- `Monad[IO]`: sequence side effects (Chapter 7!)

**For-comprehensions:**
```scala
// Scala
for {
  x <- opt1
  y <- opt2
} yield x + y

// Rust equivalent
opt1.and_then(|x| opt2.map(|y| x + y))
```

**When to use:** When you have a sequence of operations where each step depends on the previous result.

## Running the Examples

```bash
cd chapter-05-cats-intro

# Type classes
sbt "runMain catsintro.TypeClassesIntro"

# Semigroup and Monoid
sbt "runMain catsintro.SemigroupMonoidExamples"

# Functor
sbt "runMain catsintro.FunctorExamples"

# Monad
sbt "runMain catsintro.MonadExamples"
```

## Exercises

See `EXERCISES.md` for a quick reference of all exercises.

### Exercise 01: Type Classes and Composition
**File:** `src/main/scala/exercises/Exercise01_TypeClasses.scala`

Practice implementing and using type classes:
1. Create Semigroup and Monoid instances
2. Transform data with Functor
3. Chain operations with Monad

```bash
sbt "runMain catsintro.exercises.Exercise01TypeClasses"
```

## Type Class Pattern in Detail

### How It Works

1. **Define the trait (type class):**
```scala
trait Show[A] {
  def show(a: A): String
}
```

2. **Create instances (implementations):**
```scala
implicit val intShow: Show[Int] = new Show[Int] {
  def show(a: Int): String = a.toString
}
```

3. **Use implicitly:**
```scala
def print[A](a: A)(implicit s: Show[A]): Unit = {
  println(s.show(a))
}

print(42) // implicitly finds intShow
```

### Rust vs Scala Type Classes

**Rust traits:**
```rust
trait Show {
    fn show(&self) -> String;
}

impl Show for i32 {
    fn show(&self) -> String {
        self.to_string()
    }
}

fn print<T: Show>(value: &T) {
    println!("{}", value.show());
}
```

**Key Differences:**
1. **Uniqueness:** Rust has one impl per type; Scala can have multiple instances
2. **Retroactive:** Both allow implementing for external types
3. **Resolution:** Rust at compile-time via trait bounds; Scala via implicit search
4. **Syntax:** Rust uses method call syntax; Scala uses implicit parameters

## Common Pitfalls for Rust Developers

1. **Imports matter!**
   ```scala
   import cats.instances.all._  // Get instances for Option, List, etc.
   import cats.syntax.semigroup._ // Get |+| operator
   ```

2. **Type inference can be tricky**
   ```scala
   // Sometimes need explicit type
   val x: Option[Int] = Functor[Option].map(Some(1))(_ * 2)
   ```

3. **Context bounds vs implicit parameters**
   ```scala
   def foo[F[_]: Monad] = ...              // Context bound
   def foo[F[_]](implicit M: Monad[F]) = ... // Explicit
   ```

## Why This Matters for Cats-Effect

Everything in this chapter builds toward understanding `IO[A]` in Chapter 7:
- **Functor**: Transform IO results with `map`
- **Monad**: Chain IO operations with `flatMap` and for-comprehensions
- **Semigroup/Monoid**: Combine results from parallel IOs
- **Type classes**: Write generic code that works with IO

## Next Steps

- **Chapter 06:** Cats Error Handling (Validated, MonadError)
- **Chapter 07:** IO Monad - The foundation of Cats-Effect

## Resources

- [Cats Documentation](https://typelevel.org/cats/)
- [Type Classes](https://typelevel.org/cats/typeclasses.html)
- [Herding Cats (Tutorial)](http://eed3si9n.com/herding-cats/)
- [Scala with Cats (Book)](https://www.scalawithcats.com/) - Free online

## Key Takeaways

✅ **Type classes** = traits + implicit instances (more flexible than Rust traits)  
✅ **Semigroup** = combine two values (like Rust's Add/Mul traits)  
✅ **Monoid** = Semigroup + identity element (useful for folds)  
✅ **Functor** = map over contexts (like Rust's map on Option/Result/Iterator)  
✅ **Monad** = flatMap + pure (like Rust's and_then + ? operator)  
✅ **For-comprehensions** = Syntactic sugar for flatMap chains  
✅ **Preparation** = Everything leads to IO[A] in Chapter 7  

---

**Rust Developers:** Think of Cats as a "standard library of abstractions" that makes functional patterns first-class citizens in Scala, similar to how Rust's Iterator trait unifies iteration.
