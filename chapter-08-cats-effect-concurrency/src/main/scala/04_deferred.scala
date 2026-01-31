package catseffectconcurrency

import cats.effect.{IO, IOApp, Deferred}
import cats.syntax.traverse._
import cats.instances.list._
import scala.concurrent.duration._

/*
 * DEFERRED - ONE-TIME SYNCHRONIZATION
 *
 * Deferred[IO, A] is a one-shot promise/future:
 * - Starts empty
 * - complete.set(a): Sets value once
 * - get: Waits for value (blocks fiber)
 *
 * RUST COMPARISON:
 * - Deferred ~ tokio::sync::oneshot channel
 * - get ~ receiver.await
 * - complete ~ sender.send(value)
 * - Used for coordination between fibers
 */

object DeferredDemo extends IOApp.Simple {
  
  def demonstrateBasics(): IO[Unit] = {
    println("\n=== Deferred Basics ===\n")
    
    for {
      deferred <- Deferred[IO, String]
      
      // Start fiber that waits for value
      waiter <- (for {
        _ <- IO.println("  Waiter: Waiting for value...")
        value <- deferred.get  // Blocks until set
        _ <- IO.println(s"  Waiter: Got value: $value")
      } yield value).start
      
      _ <- IO.println("Main: Doing work...")
      _ <- IO.sleep(1.second)
      
      _ <- IO.println("Main: Setting value...")
      _ <- deferred.complete("Hello!")
      
      result <- waiter.join
      _ <- IO.println(s"Main: Result = $result")
      
      _ <- IO.println("\nRust equivalent:")
      _ <- IO.println("  let (tx, rx) = oneshot::channel();")
      _ <- IO.println("  tokio::spawn(async move {")
      _ <- IO.println("      let value = rx.await.unwrap();")
      _ <- IO.println("  });")
      _ <- IO.println("  tx.send('Hello!').unwrap();")
    } yield ()
  }
  
  def demonstratePractical(): IO[Unit] = {
    println("\n=== Practical: Worker Coordination ===\n")
    
    def worker(id: Int, signal: Deferred[IO, Unit]): IO[Unit] = {
      for {
        _ <- IO.println(s"  Worker $id: Waiting for signal...")
        _ <- signal.get  // Wait for start signal
        _ <- IO.println(s"  Worker $id: Started!")
        _ <- IO.sleep(500.millis)
        _ <- IO.println(s"  Worker $id: Done!")
      } yield ()
    }
    
    for {
      signal <- Deferred[IO, Unit]
      
      // Start 3 workers
      workers <- List(1, 2, 3).traverse(id => worker(id, signal).start)
      
      _ <- IO.println("Main: Workers ready, waiting 1s...")
      _ <- IO.sleep(1.second)
      
      _ <- IO.println("Main: Sending start signal!")
      _ <- signal.complete(())
      
      // Wait for all
      _ <- workers.traverse(_.join)
      
      _ <- IO.println("\nAll workers completed")
    } yield ()
  }
  
  def run: IO[Unit] = {
    for {
      _ <- IO.println("=" * 60)
      _ <- IO.println("DEFERRED - ONE-TIME SYNCHRONIZATION")
      _ <- IO.println("=" * 60)
      
      _ <- demonstrateBasics()
      _ <- demonstratePractical()
      
      _ <- IO.println("\n" + "=" * 60)
      _ <- IO.println("KEY TAKEAWAYS")
      _ <- IO.println("=" * 60)
      _ <- IO.println("""
1. Deferred = one-shot promise
2. get waits for value (blocks fiber)
3. complete sets value once
4. Rust: tokio::sync::oneshot
5. Use for fiber coordination
6. Can't be reset (one-time only)
      """.trim)
    } yield ()
  }
}
