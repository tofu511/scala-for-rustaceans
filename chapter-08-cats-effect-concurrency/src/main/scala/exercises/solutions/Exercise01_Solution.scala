package catseffectconcurrency.exercises.solutions

import cats.effect.{IO, IOApp, Ref}
import cats.syntax.parallel._
import cats.syntax.traverse._
import cats.instances.list._
import scala.concurrent.duration._

object Exercise01Solution extends IOApp.Simple {
  
  def fetchData(id: Int): IO[String] = {
    IO.sleep(200.millis) *> IO.pure(s"Data-$id")
  }
  
  def processParallel(ids: List[Int]): IO[List[String]] = {
    ids.parTraverse(fetchData)
  }
  
  case class TaskResult(completed: Int, failed: Int)
  
  def runTask(id: Int, counter: Ref[IO, TaskResult], shouldFail: Boolean): IO[Unit] = {
    IO.sleep(100.millis) *> {
      if (shouldFail) counter.update(r => r.copy(failed = r.failed + 1))
      else counter.update(r => r.copy(completed = r.completed + 1))
    }
  }
  
  def runAllTasks(count: Int): IO[TaskResult] = {
    for {
      counter <- Ref.of[IO, TaskResult](TaskResult(0, 0))
      tasks = List.tabulate(count)(i => runTask(i + 1, counter, shouldFail = (i + 1) % 3 == 0))
      _ <- tasks.parSequence
      result <- counter.get
    } yield result
  }
  
  def run: IO[Unit] = {
    for {
      _ <- IO.println("=== Exercise 01 Solution ===\n")
      
      _ <- IO.println("--- Parallel Execution ---")
      start <- IO.realTime
      results <- processParallel(List(1, 2, 3, 4, 5))
      end <- IO.realTime
      _ <- IO.println(s"Results: $results")
      _ <- IO.println(s"Time: ${(end - start).toMillis}ms (parallel!)")
      
      _ <- IO.println("\n--- Ref Counter ---")
      result <- runAllTasks(10)
      _ <- IO.println(s"Completed: ${result.completed}")
      _ <- IO.println(s"Failed: ${result.failed}")
      
      _ <- IO.println("\n✓ All tests passed!")
    } yield ()
  }
}
