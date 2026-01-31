package catseffectconcurrency

import cats.effect.{IO, IOApp}
import cats.syntax.parallel._
import cats.syntax.traverse._
import cats.instances.list._
import scala.concurrent.duration._

/*
 * PARALLEL EXECUTION
 *
 * Cats provides high-level parallel combinators:
 * - parMapN: Run N operations in parallel, combine results
 * - parTraverse: Map+sequence in parallel
 * - parSequence: Run list of IOs in parallel
 *
 * RUST COMPARISON:
 * - parMapN ~ tokio::join!(future1, future2, ...)
 * - parTraverse ~ futures::future::join_all with map
 * - Sequential by default, explicit parallel
 */

object ParallelDemo extends IOApp.Simple {
  
  def demonstrateSequential(): IO[Unit] = {
    println("\n=== Sequential Execution (Default) ===\n")
    
    def task(id: Int): IO[String] = {
      IO.sleep(500.millis) *> IO.delay {
        println(s"  Task $id completed")
        s"Result-$id"
      }
    }
    
    for {
      _ <- IO.println("Running 3 tasks sequentially...")
      start <- IO.realTime
      
      // Regular for-comprehension = sequential
      result <- for {
        r1 <- task(1)
        r2 <- task(2)
        r3 <- task(3)
      } yield (r1, r2, r3)
      
      end <- IO.realTime
      duration = end - start
      
      _ <- IO.println(s"Results: $result")
      _ <- IO.println(s"Time: ${duration.toMillis}ms")
      _ <- IO.println("Expected: ~1500ms (500ms * 3)")
      
      _ <- IO.println("\nRust equivalent:")
      _ <- IO.println("  let r1 = task(1).await;  // Wait")
      _ <- IO.println("  let r2 = task(2).await;  // Wait")
      _ <- IO.println("  let r3 = task(3).await;  // Wait")
    } yield ()
  }
  
  def demonstrateParMapN(): IO[Unit] = {
    println("\n=== parMapN - Parallel Execution ===\n")
    
    def task(id: Int): IO[String] = {
      IO.sleep(500.millis) *> IO.delay {
        println(s"  Task $id completed")
        s"Result-$id"
      }
    }
    
    for {
      _ <- IO.println("Running 3 tasks in parallel with parMapN...")
      start <- IO.realTime
      
      // parMapN - runs in parallel
      result <- (task(1), task(2), task(3)).parMapN { (r1, r2, r3) =>
        (r1, r2, r3)
      }
      
      end <- IO.realTime
      duration = end - start
      
      _ <- IO.println(s"Results: $result")
      _ <- IO.println(s"Time: ${duration.toMillis}ms")
      _ <- IO.println("Expected: ~500ms (all run together!)")
      
      _ <- IO.println("\nRust equivalent:")
      _ <- IO.println("  let (r1, r2, r3) = tokio::join!(")
      _ <- IO.println("      task(1),")
      _ <- IO.println("      task(2),")
      _ <- IO.println("      task(3)")
      _ <- IO.println("  );")
    } yield ()
  }
  
  def demonstrateParTraverse(): IO[Unit] = {
    println("\n=== parTraverse - Parallel Map ===\n")
    
    def processItem(id: Int): IO[String] = {
      IO.sleep(300.millis) *> IO.delay {
        println(s"  Processed item $id")
        s"Item-$id-processed"
      }
    }
    
    val items = List(1, 2, 3, 4, 5)
    
    for {
      _ <- IO.println(s"Processing ${items.length} items...")
      
      _ <- IO.println("\nSequential traverse:")
      start1 <- IO.realTime
      result1 <- items.traverse(processItem)
      end1 <- IO.realTime
      _ <- IO.println(s"  Time: ${(end1 - start1).toMillis}ms")
      
      _ <- IO.println("\nParallel traverse:")
      start2 <- IO.realTime
      result2 <- items.parTraverse(processItem)
      end2 <- IO.realTime
      _ <- IO.println(s"  Time: ${(end2 - start2).toMillis}ms")
      
      _ <- IO.println(s"\nSpeedup: ${(end1 - start1).toMillis / (end2 - start2).toMillis}x")
      
      _ <- IO.println("\nRust equivalent:")
      _ <- IO.println("  // Sequential")
      _ <- IO.println("  for item in items {")
      _ <- IO.println("      process(item).await;")
      _ <- IO.println("  }")
      _ <- IO.println("  // Parallel")
      _ <- IO.println("  let futures: Vec<_> = items.iter().map(process).collect();")
      _ <- IO.println("  let results = join_all(futures).await;")
    } yield ()
  }
  
  def demonstrateParSequence(): IO[Unit] = {
    println("\n=== parSequence - Run List in Parallel ===\n")
    
    def fetchData(id: Int): IO[String] = {
      IO.sleep(400.millis) *> IO.pure(s"Data-$id")
    }
    
    val tasks: List[IO[String]] = List(
      fetchData(1),
      fetchData(2),
      fetchData(3)
    )
    
    for {
      _ <- IO.println("Running list of IOs in parallel...")
      start <- IO.realTime
      
      // parSequence - run all in parallel
      results <- tasks.parSequence
      
      end <- IO.realTime
      duration = end - start
      
      _ <- IO.println(s"Results: $results")
      _ <- IO.println(s"Time: ${duration.toMillis}ms (~400ms, not 1200ms)")
      
      _ <- IO.println("\nRust equivalent:")
      _ <- IO.println("  let tasks = vec![fetch(1), fetch(2), fetch(3)];")
      _ <- IO.println("  let results = join_all(tasks).await;")
    } yield ()
  }
  
  def demonstratePractical(): IO[Unit] = {
    println("\n=== Practical: Parallel API Calls ===\n")
    
    case class User(id: Int, name: String)
    case class Order(id: Int, total: Double)
    case class Profile(id: Int, bio: String)
    
    def fetchUser(id: Int): IO[User] = {
      IO.sleep(300.millis) *> IO.pure(User(id, s"User-$id"))
    }
    
    def fetchOrders(userId: Int): IO[List[Order]] = {
      IO.sleep(200.millis) *> IO.pure(List(Order(1, 99.99), Order(2, 49.99)))
    }
    
    def fetchProfile(userId: Int): IO[Profile] = {
      IO.sleep(250.millis) *> IO.pure(Profile(userId, "Bio text"))
    }
    
    for {
      _ <- IO.println("Fetching user data...")
      start <- IO.realTime
      
      // Fetch user first (needed for next calls)
      user <- fetchUser(123)
      
      // Then fetch orders and profile in parallel
      ordersAndProfile <- (
        fetchOrders(user.id),
        fetchProfile(user.id)
      ).parMapN((o, p) => (o, p))
      (orders, profile) = ordersAndProfile
      
      end <- IO.realTime
      duration = end - start
      
      _ <- IO.println(s"User: $user")
      _ <- IO.println(s"Orders: $orders")
      _ <- IO.println(s"Profile: $profile")
      _ <- IO.println(s"Total time: ${duration.toMillis}ms")
      _ <- IO.println("Sequential would take: ~750ms")
      _ <- IO.println("Parallel took: ~500ms (300ms + max(200ms, 250ms))")
      
      _ <- IO.println("\nKey insight: Parallelize independent operations!")
    } yield ()
  }
  
  def demonstrateErrorHandling(): IO[Unit] = {
    println("\n=== Error Handling in Parallel ===\n")
    
    def task(id: Int, shouldFail: Boolean): IO[String] = {
      IO.sleep(200.millis) *> IO.delay {
        if (shouldFail) throw new Exception(s"Task $id failed")
        s"Success-$id"
      }
    }
    
    for {
      _ <- IO.println("Case 1: All succeed")
      result1 <- (task(1, false), task(2, false)).parMapN((a, b) => (a, b)).attempt
      _ <- IO.println(s"  $result1")
      
      _ <- IO.println("\nCase 2: One fails (all are canceled)")
      result2 <- (task(1, false), task(2, true), task(3, false))
        .parMapN((a, b, c) => (a, b, c))
        .attempt
      _ <- IO.println(s"  $result2")
      _ <- IO.println("  Note: First error cancels others!")
      
      _ <- IO.println("\nRust comparison:")
      _ <- IO.println("  tokio::try_join!(future1, future2, future3)")
      _ <- IO.println("  // Fails fast on first error")
    } yield ()
  }
  
  def run: IO[Unit] = {
    for {
      _ <- IO.println("=" * 60)
      _ <- IO.println("PARALLEL EXECUTION")
      _ <- IO.println("=" * 60)
      
      _ <- demonstrateSequential()
      _ <- demonstrateParMapN()
      _ <- demonstrateParTraverse()
      _ <- demonstrateParSequence()
      _ <- demonstratePractical()
      _ <- demonstrateErrorHandling()
      
      _ <- IO.println("\n" + "=" * 60)
      _ <- IO.println("KEY TAKEAWAYS")
      _ <- IO.println("=" * 60)
      _ <- IO.println("""
1. Default: Sequential (for-comprehension)
2. parMapN: Run N operations in parallel
3. parTraverse: Parallel map over collection
4. parSequence: Run list of IOs in parallel
5. Error in parallel = cancels others
6. Use for independent operations only!
7. Rust: tokio::join!, join_all
8. Explicit parallel = predictable
      """.trim)
    } yield ()
  }
}
