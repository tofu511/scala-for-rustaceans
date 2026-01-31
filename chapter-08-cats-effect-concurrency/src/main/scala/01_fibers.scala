package catseffectconcurrency

import cats.effect.{IO, IOApp, Fiber}
import cats.syntax.parallel._
import scala.concurrent.duration._

/*
 * FIBERS - LIGHTWEIGHT CONCURRENCY
 *
 * Fiber is like a lightweight thread managed by Cats-Effect.
 * Fibers are:
 * - Cheap to create (thousands of them)
 * - Cooperative (not preemptive like OS threads)
 * - Cancelable
 * - Can be joined (wait for result)
 *
 * RUST COMPARISON:
 * - Fiber ~ tokio::spawn (async task)
 * - start ~ tokio::spawn(async move { ... })
 * - join ~ task_handle.await
 * - cancel ~ task_handle.abort()
 * - Both provide structured concurrency
 */

object FiberDemo extends IOApp.Simple {
  
  def demonstrateBasicFiber(): IO[Unit] = {
    println("\n=== Basic Fiber ===\n")
    
    def task(n: Int): IO[Int] = IO.sleep(1.second) *> IO.delay {
      println(s"  Task $n completed")
      n * 2
    }
    
    for {
      _ <- IO.println("Starting fiber...")
      
      // start - launches fiber in background
      fiber <- task(5).start
      
      _ <- IO.println("Fiber started (running in background)")
      _ <- IO.println("Doing other work...")
      _ <- IO.sleep(500.millis)
      
      // join - waits for fiber to complete
      result <- fiber.join
      _ <- IO.println(s"Fiber result: $result")
      
      _ <- IO.println("\nRust comparison:")
      _ <- IO.println("  let handle = tokio::spawn(async move {")
      _ <- IO.println("      task(5).await")
      _ <- IO.println("  });")
      _ <- IO.println("  let result = handle.await.unwrap();")
    } yield ()
  }
  
  def demonstrateMultipleFibers(): IO[Unit] = {
    println("\n=== Multiple Fibers ===\n")
    
    def task(id: Int, duration: FiniteDuration): IO[String] = {
      IO.sleep(duration) *> IO.delay {
        println(s"  Task $id done (${duration.toMillis}ms)")
        s"Result-$id"
      }
    }
    
    for {
      _ <- IO.println("Starting 3 fibers...")
      start <- IO.realTime
      
      // Start multiple fibers
      fiber1 <- task(1, 1.second).start
      fiber2 <- task(2, 500.millis).start
      fiber3 <- task(3, 800.millis).start
      
      _ <- IO.println("All fibers started, now waiting...")
      
      // Join all (wait for all to complete)
      result1 <- fiber1.join
      result2 <- fiber2.join
      result3 <- fiber3.join
      
      end <- IO.realTime
      duration = end - start
      
      _ <- IO.println(s"\nResults: $result1, $result2, $result3")
      _ <- IO.println(s"Total time: ${duration.toMillis}ms (ran concurrently!)")
      
      _ <- IO.println("\nRust comparison:")
      _ <- IO.println("  let h1 = tokio::spawn(task(1, 1000));")
      _ <- IO.println("  let h2 = tokio::spawn(task(2, 500));")
      _ <- IO.println("  let h3 = tokio::spawn(task(3, 800));")
      _ <- IO.println("  let (r1, r2, r3) = tokio::join!(h1, h2, h3);")
    } yield ()
  }
  
  def demonstrateCancellation(): IO[Unit] = {
    println("\n=== Fiber Cancellation ===\n")
    
    def longRunning: IO[Unit] = {
      IO.sleep(2.seconds) *> IO.println("  Long task completed")
    }
    
    for {
      _ <- IO.println("Starting long-running fiber...")
      fiber <- longRunning.start
      
      _ <- IO.println("Waiting 500ms then canceling...")
      _ <- IO.sleep(500.millis)
      
      // cancel - stops the fiber
      _ <- fiber.cancel
      _ <- IO.println("Fiber canceled!")
      
      _ <- IO.println("\nRust comparison:")
      _ <- IO.println("  let handle = tokio::spawn(long_task());")
      _ <- IO.println("  sleep(Duration::from_millis(500)).await;")
      _ <- IO.println("  handle.abort();  // Cancel the task")
    } yield ()
  }
  
  def demonstrateRace(): IO[Unit] = {
    println("\n=== Race - First Wins ===\n")
    
    def task(id: Int, duration: FiniteDuration): IO[String] = {
      IO.sleep(duration) *> IO.pure(s"Task-$id")
    }
    
    for {
      _ <- IO.println("Racing two tasks...")
      
      // race - runs both, returns first, cancels other
      winner <- IO.race(
        task(1, 1.second),
        task(2, 500.millis)
      )
      
      _ <- winner match {
        case Left(result) => IO.println(s"  Left won: $result")
        case Right(result) => IO.println(s"  Right won: $result")
      }
      
      _ <- IO.println("\nRust comparison:")
      _ <- IO.println("  tokio::select! {")
      _ <- IO.println("      result1 = task1() => println!('Task 1 won'),")
      _ <- IO.println("      result2 = task2() => println!('Task 2 won'),")
      _ <- IO.println("  }")
    } yield ()
  }
  
  def demonstrateBoth(): IO[Unit] = {
    println("\n=== Both - Wait for Both ===\n")
    
    def fetchUser(id: Int): IO[String] = {
      IO.sleep(500.millis) *> IO.pure(s"User-$id")
    }
    
    def fetchOrders(userId: Int): IO[List[String]] = {
      IO.sleep(300.millis) *> IO.pure(List(s"Order-1-$userId", s"Order-2-$userId"))
    }
    
    for {
      _ <- IO.println("Fetching user and orders in parallel...")
      start <- IO.realTime
      
      // both - runs both concurrently, waits for both
      result <- IO.both(fetchUser(123), fetchOrders(123))
      
      end <- IO.realTime
      duration = end - start
      
      _ <- IO.println(s"  Result: $result")
      _ <- IO.println(s"  Time: ${duration.toMillis}ms (parallel!)")
      
      _ <- IO.println("\nSequential would take 800ms")
      _ <- IO.println("Parallel took ~500ms")
      
      _ <- IO.println("\nRust comparison:")
      _ <- IO.println("  let (user, orders) = tokio::join!(")
      _ <- IO.println("      fetch_user(123),")
      _ <- IO.println("      fetch_orders(123)")
      _ <- IO.println("  );")
    } yield ()
  }
  
  def demonstrateBackground(): IO[Unit] = {
    println("\n=== Background Task ===\n")
    
    def backgroundTask: IO[Unit] = {
      def tick(n: Int): IO[Unit] = {
        if (n > 0)
          IO.println(s"  Background tick $n") *>
            IO.sleep(200.millis) *>
            tick(n - 1)
        else
          IO.unit
      }
      tick(5)
    }
    
    def mainTask: IO[Unit] = {
      for {
        _ <- IO.println("Main task running...")
        _ <- IO.sleep(600.millis)
        _ <- IO.println("Main task done")
      } yield ()
    }
    
    for {
      _ <- IO.println("Starting background task...")
      
      // background - runs in background, auto-canceled when scope exits
      result <- backgroundTask.background.use { _ =>
        mainTask
      }
      
      _ <- IO.println("Exited scope (background task auto-canceled)")
      
      _ <- IO.println("\nKey insight: background auto-cancels on scope exit")
    } yield ()
  }
  
  def run: IO[Unit] = {
    for {
      _ <- IO.println("=" * 60)
      _ <- IO.println("FIBERS - LIGHTWEIGHT CONCURRENCY")
      _ <- IO.println("=" * 60)
      
      _ <- demonstrateBasicFiber()
      _ <- demonstrateMultipleFibers()
      _ <- demonstrateCancellation()
      _ <- demonstrateRace()
      _ <- demonstrateBoth()
      _ <- demonstrateBackground()
      
      _ <- IO.println("\n" + "=" * 60)
      _ <- IO.println("KEY TAKEAWAYS")
      _ <- IO.println("=" * 60)
      _ <- IO.println("""
1. Fiber = lightweight thread (like tokio task)
2. .start launches fiber in background
3. .join waits for result
4. .cancel stops the fiber
5. IO.race - first wins, others canceled
6. IO.both - wait for both concurrently
7. .background - auto-cancels on scope exit
8. Fibers are cheap - create thousands!
      """.trim)
    } yield ()
  }
}
