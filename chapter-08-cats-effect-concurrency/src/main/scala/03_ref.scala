package catseffectconcurrency

import cats.effect.{IO, IOApp, Ref}
import cats.syntax.parallel._
import scala.concurrent.duration._

/*
 * REF - ATOMIC CONCURRENT STATE
 *
 * Ref[IO, A] provides atomic, thread-safe mutable state.
 * Operations are:
 * - get: Read current value
 * - set: Write new value
 * - update: Atomic update (A => A)
 * - modify: Atomic modify and return (A => (A, B))
 *
 * RUST COMPARISON:
 * - Ref[IO, A] ~ Arc<Mutex<A>> or Arc<RwLock<A>>
 * - Atomic updates without explicit locking
 * - get ~ lock().unwrap().clone()
 * - update ~ lock().unwrap() then modify
 * - modify ~ lock, read, write, return in one operation
 */

object RefDemo extends IOApp.Simple {
  
  def demonstrateBasics(): IO[Unit] = {
    println("\n=== Ref Basics ===\n")
    
    for {
      // Create Ref with initial value
      counter <- Ref.of[IO, Int](0)
      
      _ <- IO.println("Initial value:")
      initial <- counter.get
      _ <- IO.println(s"  counter = $initial")
      
      _ <- IO.println("\nSet to 42:")
      _ <- counter.set(42)
      value1 <- counter.get
      _ <- IO.println(s"  counter = $value1")
      
      _ <- IO.println("\nUpdate (increment):")
      _ <- counter.update(_ + 1)
      value2 <- counter.get
      _ <- IO.println(s"  counter = $value2")
      
      _ <- IO.println("\nRust comparison:")
      _ <- IO.println("  let counter = Arc::new(Mutex::new(0));")
      _ <- IO.println("  *counter.lock().unwrap() = 42;")
      _ <- IO.println("  *counter.lock().unwrap() += 1;")
    } yield ()
  }
  
  def demonstrateConcurrentUpdates(): IO[Unit] = {
    println("\n=== Concurrent Updates ===\n")
    
    def increment(ref: Ref[IO, Int], n: Int): IO[Unit] = {
      List.fill(n)(ref.update(_ + 1)).parSequence.void
    }
    
    for {
      counter <- Ref.of[IO, Int](0)
      
      _ <- IO.println("Running 1000 increments from 3 fibers...")
      start <- IO.realTime
      
      // 3 fibers, each incrementing 1000 times
      _ <- (
        increment(counter, 1000),
        increment(counter, 1000),
        increment(counter, 1000)
      ).parMapN((_, _, _) => ())
      
      end <- IO.realTime
      
      finalValue <- counter.get
      _ <- IO.println(s"Final value: $finalValue")
      _ <- IO.println(s"Expected: 3000")
      _ <- IO.println(s"Time: ${(end - start).toMillis}ms")
      
      _ <- IO.println("\nKey: Ref guarantees atomicity!")
      _ <- IO.println("No lost updates, no race conditions")
      
      _ <- IO.println("\nRust comparison:")
      _ <- IO.println("  let counter = Arc::new(Mutex::new(0));")
      _ <- IO.println("  // Spawn tasks")
      _ <- IO.println("  for _ in 0..3 {")
      _ <- IO.println("      let c = counter.clone();")
      _ <- IO.println("      tokio::spawn(async move {")
      _ <- IO.println("          for _ in 0..1000 {")
      _ <- IO.println("              *c.lock().unwrap() += 1;")
      _ <- IO.println("          }")
      _ <- IO.println("      });")
      _ <- IO.println("  }")
    } yield ()
  }
  
  def demonstrateModify(): IO[Unit] = {
    println("\n=== Modify - Atomic Read-Modify-Write ===\n")
    
    case class Stats(total: Int, count: Int) {
      def average: Double = if (count == 0) 0.0 else total.toDouble / count
    }
    
    for {
      stats <- Ref.of[IO, Stats](Stats(0, 0))
      
      _ <- IO.println("Adding values: 10, 20, 30")
      
      // modify returns both new state and a result
      _ <- stats.modify { s =>
        val newStats = Stats(s.total + 10, s.count + 1)
        (newStats, s"Added 10, avg now: ${newStats.average}")
      }.flatMap(IO.println)
      
      _ <- stats.modify { s =>
        val newStats = Stats(s.total + 20, s.count + 1)
        (newStats, s"Added 20, avg now: ${newStats.average}")
      }.flatMap(IO.println)
      
      _ <- stats.modify { s =>
        val newStats = Stats(s.total + 30, s.count + 1)
        (newStats, s"Added 30, avg now: ${newStats.average}")
      }.flatMap(IO.println)
      
      finalStats <- stats.get
      _ <- IO.println(s"\nFinal stats: $finalStats")
      _ <- IO.println(s"Average: ${finalStats.average}")
      
      _ <- IO.println("\nRust comparison:")
      _ <- IO.println("  let stats = Arc::new(Mutex::new(Stats { ... }));")
      _ <- IO.println("  let mut guard = stats.lock().unwrap();")
      _ <- IO.println("  guard.total += 10;")
      _ <- IO.println("  guard.count += 1;")
      _ <- IO.println("  let avg = guard.average();  // Read in same lock")
    } yield ()
  }
  
  def demonstratePractical(): IO[Unit] = {
    println("\n=== Practical: Request Counter ===\n")
    
    case class Metrics(
      requests: Int,
      errors: Int,
      totalLatencyMs: Long
    ) {
      def successRate: Double = 
        if (requests == 0) 100.0 
        else ((requests - errors).toDouble / requests) * 100.0
      
      def avgLatency: Double =
        if (requests == 0) 0.0
        else totalLatencyMs.toDouble / requests
    }
    
    def handleRequest(
      id: Int, 
      metrics: Ref[IO, Metrics],
      shouldFail: Boolean
    ): IO[Unit] = {
      for {
        start <- IO.realTime
        
        // Simulate request processing
        _ <- IO.sleep((50 + scala.util.Random.nextInt(100)).millis)
        
        end <- IO.realTime
        latency = (end - start).toMillis
        
        // Update metrics atomically
        _ <- if (shouldFail) {
          metrics.update(m => m.copy(
            requests = m.requests + 1,
            errors = m.errors + 1,
            totalLatencyMs = m.totalLatencyMs + latency
          )) *> IO.println(s"  Request $id: FAILED (${latency}ms)")
        } else {
          metrics.update(m => m.copy(
            requests = m.requests + 1,
            totalLatencyMs = m.totalLatencyMs + latency
          )) *> IO.println(s"  Request $id: OK (${latency}ms)")
        }
      } yield ()
    }
    
    for {
      metrics <- Ref.of[IO, Metrics](Metrics(0, 0, 0))
      
      _ <- IO.println("Processing 10 requests in parallel...")
      
      // Process requests (some fail)
      requests = List.tabulate(10) { i =>
        handleRequest(i + 1, metrics, shouldFail = i % 4 == 0)
      }
      _ <- requests.parSequence
      
      _ <- IO.println("\nFinal Metrics:")
      finalMetrics <- metrics.get
      _ <- IO.println(s"  Total requests: ${finalMetrics.requests}")
      _ <- IO.println(s"  Errors: ${finalMetrics.errors}")
      _ <- IO.println(s"  Success rate: ${finalMetrics.successRate}%")
      _ <- IO.println(s"  Avg latency: ${finalMetrics.avgLatency}ms")
    } yield ()
  }
  
  def demonstrateCompareAndSet(): IO[Unit] = {
    println("\n=== Access - Low-level CAS ===\n")
    
    for {
      ref <- Ref.of[IO, Int](0)
      
      _ <- IO.println("Using access for compare-and-set:")
      
      // access provides snapshot + setter
      _ <- ref.access.flatMap { case (current, setter) =>
        IO.println(s"  Current value: $current") *>
        IO.println(s"  Attempting to set to ${current + 10}...") *>
        setter(current + 10).flatMap { success =>
          if (success) IO.println("  ✓ Success!")
          else IO.println("  ✗ Failed (value changed)")
        }
      }
      
      newValue <- ref.get
      _ <- IO.println(s"  New value: $newValue")
      
      _ <- IO.println("\nRust comparison:")
      _ <- IO.println("  // AtomicI32::compare_exchange")
      _ <- IO.println("  counter.compare_exchange(")
      _ <- IO.println("      current,")
      _ <- IO.println("      current + 10,")
      _ <- IO.println("      Ordering::SeqCst,")
      _ <- IO.println("      Ordering::SeqCst")
      _ <- IO.println("  );")
    } yield ()
  }
  
  def run: IO[Unit] = {
    for {
      _ <- IO.println("=" * 60)
      _ <- IO.println("REF - ATOMIC CONCURRENT STATE")
      _ <- IO.println("=" * 60)
      
      _ <- demonstrateBasics()
      _ <- demonstrateConcurrentUpdates()
      _ <- demonstrateModify()
      _ <- demonstratePractical()
      _ <- demonstrateCompareAndSet()
      
      _ <- IO.println("\n" + "=" * 60)
      _ <- IO.println("KEY TAKEAWAYS")
      _ <- IO.println("=" * 60)
      _ <- IO.println("""
1. Ref[IO, A] = atomic, thread-safe state
2. get/set for simple read/write
3. update for atomic modifications
4. modify for atomic read-modify-write
5. No race conditions (unlike var)
6. Rust: Arc<Mutex<T>> or Arc<RwLock<T>>
7. No explicit locking needed!
8. Perfect for counters, metrics, caches
      """.trim)
    } yield ()
  }
}
