package catseffect

import cats.effect.{IO, IOApp}
import cats.effect.unsafe.implicits.global
import cats.syntax.traverse._
import cats.instances.list._
import scala.concurrent.duration._

/*
 * IO MONAD BASICS
 *
 * IO[A] is a lazy, referentially transparent description of a side effect
 * that will eventually produce a value of type A (or fail).
 *
 * KEY INSIGHT: IO doesn't DO anything until you run it!
 *
 * RUST COMPARISON:
 * - IO[A] is like Future<Output = A> but lazy (like async fn that isn't .awaited)
 * - async fn in Rust is lazy: it doesn't execute until you .await it
 * - IO in Scala is lazy: it doesn't execute until you unsafeRunSync() or use IOApp
 *
 * Scala Future is eager (starts immediately) - this is the problem IO solves!
 */

object IOBasicsDemo extends IOApp.Simple {
  
  /*
   * CONSTRUCTION: Creating IO values
   */
  
  def demonstrateConstruction(): IO[Unit] = {
    println("\n=== IO Construction ===\n")
    
    // 1. IO.pure - lift a pure value (no side effects)
    val pureValue: IO[Int] = IO.pure(42)
    println("IO.pure(42) created (but not executed yet!)")
    
    // 2. IO.delay / IO { } - suspend a side effect
    val delayedEffect: IO[Unit] = IO.delay {
      println("  This is a side effect!")
    }
    println("IO.delay { println(...) } created")
    
    // Shorthand syntax
    val shorthand: IO[Unit] = IO {
      println("  IO { } is shorthand for IO.delay")
    }
    
    // 3. IO.defer - defer evaluation of an IO
    val deferredIO: IO[Int] = IO.defer(IO.pure(42))
    
    println("\nNow let's execute them:")
    
    for {
      _ <- IO.println("Executing pureValue:")
      v <- pureValue
      _ <- IO.println(s"  Got: $v")
      
      _ <- IO.println("\nExecuting delayedEffect:")
      _ <- delayedEffect
      
      _ <- IO.println("\nExecuting shorthand:")
      _ <- shorthand
      
      _ <- IO.println("\nKey insight: IO is lazy!")
      _ <- IO.println("  Created IOs don't run until explicitly executed")
    } yield ()
  }
  
  /*
   * COMPOSITION: Chaining IO operations
   */
  
  def demonstrateComposition(): IO[Unit] = {
    println("\n=== IO Composition ===\n")
    
    // map - transform the result
    val io1: IO[Int] = IO.pure(10)
    val io2: IO[Int] = io1.map(_ * 2)
    
    // flatMap - chain dependent operations
    val io3: IO[Int] = io1.flatMap { n =>
      IO.pure(n + 5)
    }
    
    // for-comprehension (syntactic sugar for flatMap + map)
    val io4: IO[String] = for {
      a <- IO.pure(10)
      b <- IO.pure(20)
      sum = a + b  // pure computation
      result <- IO.delay {
        s"Sum is $sum"
      }
    } yield result
    
    for {
      _ <- IO.println("map example:")
      r2 <- io2
      _ <- IO.println(s"  IO.pure(10).map(_ * 2) = $r2")
      
      _ <- IO.println("\nflatMap example:")
      r3 <- io3
      _ <- IO.println(s"  Result = $r3")
      
      _ <- IO.println("\nfor-comprehension example:")
      r4 <- io4
      _ <- IO.println(s"  $r4")
      
      _ <- IO.println("\nRust comparison:")
      _ <- IO.println("  Scala: for { a <- io1; b <- io2 } yield (a, b)")
      _ <- IO.println("  Rust:  async { let a = io1.await; let b = io2.await; (a, b) }")
    } yield ()
  }
  
  /*
   * REFERENTIAL TRANSPARENCY
   */
  
  def demonstrateRT(): IO[Unit] = {
    println("\n=== Referential Transparency ===\n")
    
    // IO is referentially transparent!
    val io = IO.println("Hello!")
    
    for {
      _ <- IO.println("Call 1:")
      _ <- io
      _ <- IO.println("\nCall 2:")
      _ <- io
      _ <- IO.println("\nCall 3:")
      _ <- io
      
      _ <- IO.println("\nKey insight:")
      _ <- IO.println("  Same IO value can be reused safely")
      _ <- IO.println("  Each execution is independent")
      _ <- IO.println("  Unlike Future, which runs immediately!")
    } yield ()
  }
  
  /*
   * PRACTICAL EXAMPLE: Simulated Database
   */
  
  def demonstratePractical(): IO[Unit] = {
    println("\n=== Practical Example: Database Operations ===\n")
    
    // Simulate database operations
    def fetchUser(id: Int): IO[String] = IO.delay {
      Thread.sleep(100) // Simulate latency
      s"User-$id"
    }
    
    def fetchOrders(user: String): IO[List[String]] = IO.delay {
      Thread.sleep(100)
      List(s"Order-1-$user", s"Order-2-$user")
    }
    
    def processOrder(order: String): IO[String] = IO.delay {
      Thread.sleep(50)
      s"Processed-$order"
    }
    
    // Sequential pipeline
    val pipeline: IO[List[String]] = for {
      user <- fetchUser(123)
      orders <- fetchOrders(user)
      processed <- orders.traverse(processOrder) // traverse = map + sequence
    } yield processed
    
    for {
      _ <- IO.println("Fetching user, orders, and processing...")
      start <- IO.realTime
      results <- pipeline
      end <- IO.realTime
      duration = end - start
      
      _ <- IO.println(s"Results: $results")
      _ <- IO.println(s"Time: ${duration.toMillis}ms")
      
      _ <- IO.println("\nRust comparison:")
      _ <- IO.println("  async fn fetch_user(id: u32) -> Result<User, Error>")
      _ <- IO.println("  async fn fetch_orders(user: &User) -> Result<Vec<Order>, Error>")
      _ <- IO.println("  // Use .await to chain them")
    } yield ()
  }
  
  /*
   * COMPARING IO vs Future
   */
  
  def demonstrateDifference(): IO[Unit] = {
    import scala.concurrent.Future
    import scala.concurrent.ExecutionContext.Implicits.global
    
    println("\n=== IO vs Future ===\n")
    
    var ioCount = 0
    var futureCount = 0
    
    // IO - lazy
    val io = IO.delay {
      ioCount += 1
      println(s"  IO executed! Count: $ioCount")
    }
    
    // Future - eager
    val future = Future {
      futureCount += 1
      println(s"  Future executed! Count: $futureCount")
    }
    
    for {
      _ <- IO.println("Created IO and Future...")
      _ <- IO.sleep(100.millis) // Give Future time to run
      
      _ <- IO.println("\nNow executing IO:")
      _ <- io
      _ <- io
      
      _ <- IO.println("\nKey differences:")
      _ <- IO.println("  IO: lazy, RT, executed twice (count = 2)")
      _ <- IO.println("  Future: eager, NOT RT, executed once (count = 1)")
      _ <- IO.println(s"  Final counts - IO: $ioCount, Future: $futureCount")
      
      _ <- IO.println("\nRust:")
      _ <- IO.println("  Rust Future is lazy like IO, not eager like Scala Future")
      _ <- IO.println("  async fn doesn't run until .await")
    } yield ()
  }
  
  def run: IO[Unit] = {
    for {
      _ <- IO.println("=" * 60)
      _ <- IO.println("IO MONAD BASICS - REFERENTIALLY TRANSPARENT EFFECTS")
      _ <- IO.println("=" * 60)
      
      _ <- demonstrateConstruction()
      _ <- demonstrateComposition()
      _ <- demonstrateRT()
      _ <- demonstratePractical()
      _ <- demonstrateDifference()
      
      _ <- IO.println("\n" + "=" * 60)
      _ <- IO.println("KEY TAKEAWAYS")
      _ <- IO.println("=" * 60)
      _ <- IO.println("""
1. IO[A] is a lazy description of a side effect
2. Nothing happens until you run it (IOApp or unsafeRunSync)
3. IO is referentially transparent (unlike Future)
4. Compose with map, flatMap, for-comprehensions
5. Rust Future is also lazy (like IO, not Scala Future)
6. IO.delay suspends side effects safely
7. Use IOApp.Simple for main entry point
      """.trim)
    } yield ()
  }
}

/*
 * UNSAFE RUNNING (for demonstration only!)
 */
object UnsafeExample {
  def main(args: Array[String]): Unit = {
    println("\n=== Using unsafeRunSync (NOT recommended for production!) ===\n")
    
    val io: IO[Int] = IO.delay {
      println("Executing side effect...")
      42
    }
    
    println("IO created, but not executed yet")
    
    // This actually runs the IO (blocks the thread)
    val result: Int = io.unsafeRunSync()
    println(s"Result: $result")
    
    println("\nPrefer IOApp.Simple in production!")
    println("  - Proper resource management")
    println("  - Graceful shutdown")
    println("  - Better error handling")
  }
}
