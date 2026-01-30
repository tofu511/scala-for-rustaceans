package catsintro.exercises.solutions

import cats.{Semigroup, Monoid, Functor, Monad}
import cats.instances.all._
import cats.syntax.semigroup._

case class Score(value: Int)

object Score {
  implicit val scoreSemigroup: Semigroup[Score] = new Semigroup[Score] {
    def combine(x: Score, y: Score): Score = Score(x.value + y.value)
  }
  
  implicit val scoreMonoid: Monoid[Score] = new Monoid[Score] {
    def combine(x: Score, y: Score): Score = scoreSemigroup.combine(x, y)
    def empty: Score = Score(0)
  }
}

object FunctorExercise {
  def doubleScores(scores: List[Score]): List[Score] = {
    Functor[List].map(scores)(s => Score(s.value * 2))
  }
  
  def transformAll[F[_]: Functor, A, B](fa: F[A])(f: A => B): F[B] = {
    Functor[F].map(fa)(f)
  }
}

object MonadExercise {
  def safeDivide(a: Int, b: Int): Option[Int] = {
    if (b == 0) None else Some(a / b)
  }
  
  def parseAndDivide(numStr: String, divisor: Int): Option[Int] = {
    for {
      num <- scala.util.Try(numStr.toInt).toOption
      result <- safeDivide(num, divisor)
    } yield result
  }
  
  def sequenceOptions[A](list: List[Option[A]]): Option[List[A]] = {
    list.foldRight(Option(List.empty[A])) { (optA, acc) =>
      for {
        a <- optA
        as <- acc
      } yield a :: as
    }
  }
}

object Exercise01Solution {
  def main(args: Array[String]): Unit = {
    println("=== Exercise 01 Solution ===\n")
    
    println("--- Semigroup/Monoid ---")
    val score1 = Score(100)
    val score2 = Score(50)
    val combined = score1 |+| score2
    println(s"Score(100) |+| Score(50) = $combined")
    println(s"Monoid[Score].empty = ${Monoid[Score].empty}")
    
    println("\n--- Functor ---")
    val scores = List(Score(10), Score(20), Score(30))
    val doubled = FunctorExercise.doubleScores(scores)
    println(s"Doubled scores: $doubled")
    
    println("\n--- Monad ---")
    println(s"safeDivide(10, 2) = ${MonadExercise.safeDivide(10, 2)}")
    println(s"safeDivide(10, 0) = ${MonadExercise.safeDivide(10, 0)}")
    println(s"parseAndDivide('20', 4) = ${MonadExercise.parseAndDivide("20", 4)}")
    
    val opts = List(Some(1), Some(2), Some(3))
    println(s"sequenceOptions(List(Some(1), Some(2), Some(3))) = ${MonadExercise.sequenceOptions(opts)}")
    
    println("\n✓ All tests passed!")
  }
}
