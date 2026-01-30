package catseffect.exercises.solutions

import cats.effect.{IO, IOApp}
import cats.data.EitherT
import cats.syntax.applicativeError._
import scala.concurrent.duration._

object Exercise01Solution extends IOApp.Simple {
  
  def fetchUserName(id: Int): IO[String] = {
    IO.sleep(100.millis) *> IO.pure(s"User-$id")
  }
  
  def fetchUserAge(id: Int): IO[Int] = {
    IO.delay(scala.util.Random.between(18, 81))
  }
  
  def divide(a: Int, b: Int): IO[Int] = {
    if (b == 0) IO.raiseError(new ArithmeticException("Division by zero"))
    else IO.pure(a / b)
  }
  
  def divideWithDefault(a: Int, b: Int, default: Int): IO[Int] = {
    divide(a, b).handleError(_ => default)
  }
  
  sealed trait ValidationError
  case class InvalidInput(msg: String) extends ValidationError
  case class OutOfRange(msg: String) extends ValidationError
  
  type Result[A] = EitherT[IO, ValidationError, A]
  
  def validatePositive(n: Int): Result[Int] = {
    if (n > 0) EitherT.pure[IO, ValidationError](n)
    else EitherT.leftT[IO, Int](InvalidInput(s"$n is not positive"))
  }
  
  def validateRange(n: Int, min: Int, max: Int): Result[Int] = {
    if (n >= min && n <= max) EitherT.pure[IO, ValidationError](n)
    else EitherT.leftT[IO, Int](OutOfRange(s"$n not in [$min, $max]"))
  }
  
  def processNumber(n: Int): Result[String] = {
    for {
      positive <- validatePositive(n)
      inRange <- validateRange(positive, 1, 100)
    } yield s"Valid: $inRange"
  }
  
  def run: IO[Unit] = {
    for {
      _ <- IO.println("=== Exercise 01 Solution ===\n")
      
      _ <- IO.println("--- IO Basics ---")
      name <- fetchUserName(42)
      _ <- IO.println(s"Name: $name")
      
      _ <- IO.println("\n--- Error Handling ---")
      r1 <- divide(10, 2)
      _ <- IO.println(s"10 / 2 = $r1")
      r2 <- divideWithDefault(10, 0, -1)
      _ <- IO.println(s"10 / 0 with default = $r2")
      
      _ <- IO.println("\n--- EitherT ---")
      r3 <- processNumber(50).value
      _ <- IO.println(s"processNumber(50) = $r3")
      r4 <- processNumber(-5).value
      _ <- IO.println(s"processNumber(-5) = $r4")
      r5 <- processNumber(150).value
      _ <- IO.println(s"processNumber(150) = $r5")
      
      _ <- IO.println("\n✓ All tests passed!")
    } yield ()
  }
}
