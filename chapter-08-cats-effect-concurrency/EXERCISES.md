# Chapter 08: Exercises Quick Reference

## Exercise 01: Concurrency with Fibers and Ref

**File:** `src/main/scala/exercises/Exercise01_Concurrency.scala`  
**Solution:** `src/main/scala/exercises/solutions/Exercise01_Solution.scala`  
**Estimated Time:** 30-45 minutes

### Objectives
- Use parTraverse for parallel execution
- Apply Ref for shared atomic state
- Build concurrent task processor
- Understand parallel vs sequential timing

### Tasks
**Part 1:** Parallel Execution
- `fetchData`: Return data after delay
- `processParallel`: Use parTraverse on list

**Part 2:** Ref Counter
- `runTask`: Update Ref atomically
- `runAllTasks`: Run tasks in parallel, track results

### How to Run
```bash
cd chapter-08-cats-effect-concurrency
sbt "runMain catseffectconcurrency.exercises.Exercise01Concurrency"
sbt "runMain catseffectconcurrency.exercises.solutions.Exercise01Solution"
```

### Expected Output
```
=== Exercise 01 Solution ===

--- Parallel Execution ---
Results: List(Data-1, Data-2, Data-3, Data-4, Data-5)
Time: 200ms (parallel!)

--- Ref Counter ---
Completed: 7
Failed: 3

✓ All tests passed!
```

### Key Concepts
- **parTraverse** - Parallel map over collection
- **Ref.update** - Atomic state modification
- **parSequence** - Run list of IOs in parallel
- **Timing** - Parallel should be ~200ms, not 1000ms

### Tips
1. Import `cats.syntax.parallel._` for parTraverse
2. Use `Ref.of[IO, A](initial)` to create
3. `update(f)` is atomic - no race conditions
4. `parSequence` runs all in parallel

### Rust Comparison
```rust
// parTraverse equivalent
let futures: Vec<_> = ids.iter().map(fetch_data).collect();
let results = futures::future::join_all(futures).await;

// Ref equivalent  
let counter = Arc::new(Mutex::new(TaskResult { ... }));
let c = counter.clone();
tokio::spawn(async move {
    let mut guard = c.lock().unwrap();
    guard.completed += 1;
});
```

---

## Quick Reference

### Parallel Combinators
```scala
(io1, io2).parMapN((a, b) => ...)        // 2 tasks
(io1, io2, io3).parMapN((a, b, c) => ...) // 3 tasks
list.parTraverse(f)                       // Map in parallel
list.parSequence                          // Run all in parallel
```

### Ref Operations
```scala
ref <- Ref.of[IO, A](initial)
value <- ref.get
_ <- ref.set(newValue)
_ <- ref.update(a => transform(a))
result <- ref.modify(a => (newA, result))
```

### Timing Checks
```scala
start <- IO.realTime
_ <- operation
end <- IO.realTime
duration = (end - start).toMillis
```

---

## Key Takeaways

✅ **parTraverse** = parallel map (like Rust join_all)  
✅ **Ref** = Arc<Mutex<T>> without explicit locking  
✅ **update** is atomic - thread-safe  
✅ **Parallel timing** = max(tasks), not sum(tasks)  
✅ **parSequence** runs list concurrently  

**Next:** Chapter 09 covers http4s for building web servers!
