package catseffect.exercises

import cats.effect.{IO, IOApp}
import cats.data.EitherT

/*
 * EXERCISE 01: IO Basics and EitherT
 *
 * OBJECTIVES:
 * - Create and compose IO operations
 * - Handle errors with attempt, handleError
 * - Use EitherT for typed errors
 * - Build a practical pipeline
 *
 * HOW TO RUN:
 *   cd chapter-07-cats-effect-basics
 *   sbt "runMain catseffect.exercises.Exercise01IOBasics"
 */

object Exercise01IOBasics extends IOApp.Simple {
  
  // Part 1: IO Basics
  def fetchUserName(id: Int): IO[String] = ???
  // TODO: Return IO with "User-{id}" after 100ms delay
  
  def fetchUserAge(id: Int): IO[Int] = ???
  // TODO: Return random age 18-80
  
  // Part 2: Error Handling  
  def divide(a: Int, b: Int): IO[Int] = ???
  // TODO: Implement safe division, raise error if b == 0
  
  def divideWithDefault(a: Int, b: Int, default: Int): IO[Int] = ???
  // TODO: Use handleError to provide default on division by zero
  
  // Part 3: EitherT
  sealed trait ValidationError
  case class InvalidInput(msg: String) extends ValidationError
  case class OutOfRange(msg: String) extends ValidationError
  
  type Result[A] = EitherT[IO, ValidationError, A]
  
  def validatePositive(n: Int): Result[Int] = ???
  // TODO: Return error if n <= 0
  
  def validateRange(n: Int, min: Int, max: Int): Result[Int] = ???
  // TODO: Return error if n not in [min, max]
  
  def processNumber(n: Int): Result[String] = ???
  // TODO: Chain validatePositive and validateRange(1, 100), return "Valid: {n}"
  
  def run: IO[Unit] = {
    for {
      _ <- IO.println("=== Exercise 01 ===\n")
      
      // TODO: Uncomment tests
      /*
      _ <- IO.println("--- Part 1: IO Basics ---")
      name <- fetchUserName(42)
      assert(name == "User-42")
      _ <- IO.println("✓ IO basics passed")
      
      _ <- IO.println("\n--- Part 2: Error Handling ---")
      r1 <- divide(10, 2)
      assert(r1 == 5)
      
      r2 <- divide(10, 0).attempt
      assert(r2.isLeft)
      
      r3 <- divideWithDefault(10, 0, -1)
      assert(r3 == -1)
      _ <- IO.println("✓ Error handling passed")
      
      _ <- IO.println("\n--- Part 3: EitherT ---")
      r4 <- processNumber(50).value
      assert(r4.isRight)
      
      r5 <- processNumber(-5).value
      assert(r5.isLeft)
      
      r6 <- processNumber(150).value
      assert(r6.isLeft)
      _ <- IO.println("✓ EitherT passed")
      */
      
      _ <- IO.println("\n=== Uncomment tests to verify ===")
    } yield ()
  }
}

/* SOLUTION - See solutions/Exercise01_Solution.scala */
