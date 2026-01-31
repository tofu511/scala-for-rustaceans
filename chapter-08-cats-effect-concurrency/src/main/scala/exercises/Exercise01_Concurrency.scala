package catseffectconcurrency.exercises

import cats.effect.{IO, IOApp, Ref}
import cats.syntax.parallel._
import scala.concurrent.duration._

/*
 * EXERCISE 01: Concurrency with Fibers and Ref
 *
 * OBJECTIVES:
 * - Use fibers for concurrent execution
 * - Apply parMapN for parallelism
 * - Use Ref for shared state
 * - Build a parallel task processor
 *
 * HOW TO RUN:
 *   cd chapter-08-cats-effect-concurrency
 *   sbt "runMain catseffectconcurrency.exercises.Exercise01Concurrency"
 */

object Exercise01Concurrency extends IOApp.Simple {
  
  // Part 1: Parallel execution
  def fetchData(id: Int): IO[String] = ???
  // TODO: Sleep 200ms, return "Data-{id}"
  
  def processParallel(ids: List[Int]): IO[List[String]] = ???
  // TODO: Use parTraverse to fetch all IDs in parallel
  
  // Part 2: Ref for counting
  case class TaskResult(completed: Int, failed: Int)
  
  def runTask(id: Int, counter: Ref[IO, TaskResult], shouldFail: Boolean): IO[Unit] = ???
  // TODO: Sleep 100ms, update counter (completed or failed)
  
  def runAllTasks(count: Int): IO[TaskResult] = ???
  // TODO: Create Ref, run tasks in parallel (every 3rd fails), return final count
  
  def run: IO[Unit] = {
    for {
      _ <- IO.println("=== Exercise 01 ===\n")
      
      // TODO: Uncomment tests
      /*
      _ <- IO.println("--- Part 1: Parallel ---")
      start <- IO.realTime
      results <- processParallel(List(1, 2, 3, 4, 5))
      end <- IO.realTime
      assert(results.length == 5)
      assert((end - start).toMillis < 400) // Must be parallel!
      _ <- IO.println("✓ Parallel passed")
      
      _ <- IO.println("\n--- Part 2: Ref ---")
      finalResult <- runAllTasks(10)
      assert(finalResult.completed == 7)
      assert(finalResult.failed == 3)
      _ <- IO.println("✓ Ref passed")
      */
      
      _ <- IO.println("\n=== Uncomment tests to verify ===")
    } yield ()
  }
}

/* SOLUTION - See solutions/Exercise01_Solution.scala */
