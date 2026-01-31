# Chapter 08: Cats-Effect Concurrency

## Overview

This chapter covers concurrent programming with Cats-Effect: **Fibers** (lightweight threads), **parallel execution**, **Ref** (atomic state), and **Deferred** (coordination).

**Learning Objectives:**
- Use Fibers for lightweight concurrency
- Apply parMapN/parTraverse for parallel execution
- Manage shared state safely with Ref
- Coordinate fibers with Deferred
- Understand when sequential vs parallel

**Time Estimate:** 2-3 hours

## Core Concepts

### 1. Fibers - Lightweight Threads

Fiber is like a lightweight thread managed by Cats-Effect runtime:

```scala
val fiber: IO[Fiber[IO, Throwable, Int]] = task.start  // Launch
val result: IO[Int] = fiber.flatMap(_.join)  // Wait
val cancel: IO[Unit] = fiber.flatMap(_.cancel)  // Stop
```

**Rust Equivalent:**
```rust
let handle = tokio::spawn(async move { task().await });
let result = handle.await.unwrap();
handle.abort(); // cancel
```

**Key Operations:**
- `.start` - Launch fiber in background
- `.join` - Wait for result
- `.cancel` - Stop the fiber
- `IO.race` - First wins, others canceled
- `IO.both` - Wait for both
- `.background` - Auto-cancels on scope exit

### 2. Parallel Execution

By default, for-comprehensions are sequential. Use parallel combinators for concurrency:

```scala
// Sequential (default)
for {
  a <- task1  // Wait
  b <- task2  // Wait
} yield (a, b)

// Parallel
(task1, task2).parMapN((a, b) => (a, b))
```

**Combinators:**
- `parMapN` - Run N tasks in parallel, combine results
- `parTraverse` - Parallel map over collection
- `parSequence` - Run list of IOs in parallel

**Rust Equivalent:**
```rust
// Sequential
let a = task1().await;
let b = task2().await;

// Parallel
let (a, b) = tokio::join!(task1(), task2());
```

### 3. Ref - Atomic State

Ref[IO, A] provides thread-safe mutable state:

```scala
for {
  counter <- Ref.of[IO, Int](0)
  _ <- counter.update(_ + 1)  // Atomic increment
  value <- counter.get
} yield value
```

**Operations:**
- `get` - Read current value
- `set` - Write new value
- `update(f: A => A)` - Atomic update
- `modify(f: A => (A, B))` - Atomic modify + return

**Rust Equivalent:**
```rust
let counter = Arc::new(Mutex::new(0));
*counter.lock().unwrap() += 1;  // Update
let value = *counter.lock().unwrap();  // Read
```

### 4. Deferred - One-Time Synchronization

Deferred[IO, A] is a one-shot promise:

```scala
for {
  deferred <- Deferred[IO, String]
  fiber <- deferred.get.start  // Waits for value
  _ <- deferred.complete("Hello")  // Set once
  result <- fiber.join
} yield result
```

**Rust Equivalent:**
```rust
let (tx, rx) = tokio::sync::oneshot::channel();
tokio::spawn(async move {
    let value = rx.await.unwrap();
});
tx.send("Hello").unwrap();
```

## Running Examples

```bash
cd chapter-08-cats-effect-concurrency

# Fibers
sbt "runMain catseffectconcurrency.FiberDemo"

# Parallel execution
sbt "runMain catseffectconcurrency.ParallelDemo"

# Ref (atomic state)
sbt "runMain catseffectconcurrency.RefDemo"

# Deferred (coordination)
sbt "runMain catseffectconcurrency.DeferredDemo"
```

## Exercises

### Exercise 01: Concurrency with Fibers and Ref
**File:** `src/main/scala/exercises/Exercise01_Concurrency.scala`

Practice:
1. Parallel execution with parTraverse
2. Shared state with Ref
3. Concurrent task processing

```bash
sbt "runMain catseffectconcurrency.exercises.Exercise01Concurrency"
sbt "runMain catseffectconcurrency.exercises.solutions.Exercise01Solution"
```

## Key Patterns

### 1. Independent Parallel Tasks
```scala
(fetchUser(id), fetchOrders(id)).parMapN((user, orders) => ...)
```

### 2. Parallel Collection Processing
```scala
ids.parTraverse(id => processItem(id))
```

### 3. Atomic Counter
```scala
counter <- Ref.of[IO, Int](0)
_ <- counter.update(_ + 1)
```

### 4. Worker Coordination
```scala
signal <- Deferred[IO, Unit]
workers <- List.fill(10)(worker(signal).start)
_ <- signal.complete(())  // Start all
```

## Rust vs Scala Comparison

| Concept | Rust | Scala Cats-Effect |
|---------|------|-------------------|
| **Lightweight thread** | tokio::spawn | Fiber (.start) |
| **Wait for result** | handle.await | fiber.join |
| **Cancel** | handle.abort() | fiber.cancel |
| **Parallel join** | tokio::join! | parMapN |
| **Parallel map** | join_all | parTraverse |
| **Atomic state** | Arc<Mutex<T>> | Ref[IO, A] |
| **One-shot** | oneshot::channel | Deferred[IO, A] |

## Key Takeaways

✅ **Fibers** are cheap - create thousands  
✅ **Sequential by default** - explicit parMapN for parallel  
✅ **Ref** for thread-safe state (no explicit locking)  
✅ **parTraverse** for parallel collection processing  
✅ **Deferred** for one-time coordination  
✅ **Error in parallel** = cancels others  
✅ **Rust patterns** map cleanly to Cats-Effect  

## Common Pitfalls

1. **Forgetting parallel** → Use parMapN/parTraverse
2. **Using var** → Use Ref instead
3. **Race conditions** → Ref guarantees atomicity
4. **Blocking threads** → Use IO.sleep, not Thread.sleep

## Next Steps

- **Chapter 09:** HTTP with http4s
- **Chapter 10:** Database with Doobie & Flyway
- **Chapter 11:** Complete API server

---

**Production Tip:** Use Ref for metrics, caches, counters. Use Deferred for startup coordination. Use parMapN for independent API calls!
