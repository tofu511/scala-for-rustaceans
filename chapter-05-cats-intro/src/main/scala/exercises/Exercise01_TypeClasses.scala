package catsintro.exercises

import cats.Semigroup
import cats.Monoid
import cats.Functor
import cats.Monad
import cats.instances.all._

/*
 * EXERCISE 01: Type Classes and Composition
 *
 * OBJECTIVES:
 * - Practice using Semigroup and Monoid
 * - Work with Functor to transform data
 * - Use Monad to chain operations
 * - Understand how type classes compose
 *
 * RUST COMPARISON:
 * This is like implementing traits for your types, but more flexible.
 * Type classes let you add behavior without modifying the original type.
 *
 * TASKS:
 * 1. Implement Semigroup and Monoid instances
 * 2. Use Functor to transform collections
 * 3. Chain operations with Monad
 * 4. Uncomment tests to verify
 *
 * HOW TO RUN:
 *   cd chapter-05-cats-intro
 *   sbt "runMain catsintro.exercises.Exercise01TypeClasses"
 */

// Part 1: Custom Semigroup and Monoid
case class Score(value: Int)

object Score {
  // TODO: Implement Semigroup[Score] that adds scores
  implicit val scoreSemigroup: Semigroup[Score] = ???
  
  // TODO: Implement Monoid[Score] with zero as identity
  implicit val scoreMonoid: Monoid[Score] = ???
}

// Part 2: Functor usage
object FunctorExercise {
  // TODO: Implement a function that transforms a list of scores
  // Double each score value
  def doubleScores(scores: List[Score]): List[Score] = {
    ???
  }
  
  // TODO: Generic function that works with any Functor
  def transformAll[F[_]: Functor, A, B](fa: F[A])(f: A => B): F[B] = {
    ???
  }
}

// Part 3: Monad chaining
object MonadExercise {
  // TODO: Implement safe division returning Option
  def safeDivide(a: Int, b: Int): Option[Int] = {
    ???
  }
  
  // TODO: Chain operations: parse string, divide by divisor
  def parseAndDivide(numStr: String, divisor: Int): Option[Int] = {
    ???
    // Hint: Use for-comprehension
  }
  
  // TODO: Sequence a list of Options
  def sequenceOptions[A](list: List[Option[A]]): Option[List[A]] = {
    ???
    // Hint: Use foldRight with Monad operations
  }
}

object Exercise01TypeClasses {
  def main(args: Array[String]): Unit = {
    println("=== Exercise 01: Type Classes ===\n")
    
    // Part 1: Semigroup and Monoid
    println("--- Part 1: Semigroup/Monoid ---")
    // TODO: Uncomment these tests
    /*
    import cats.syntax.semigroup._
    
    val score1 = Score(100)
    val score2 = Score(50)
    val combined = Semigroup[Score].combine(score1, score2)
    
    assert(combined == Score(150), s"Expected Score(150), got $combined")
    
    val empty = Monoid[Score].empty
    assert(empty == Score(0), s"Expected Score(0), got $empty")
    
    val withEmpty = Semigroup[Score].combine(score1, empty)
    assert(withEmpty == score1, s"Expected $score1, got $withEmpty")
    
    println("✓ Semigroup/Monoid tests passed")
    */
    
    // Part 2: Functor
    println("\n--- Part 2: Functor ---")
    // TODO: Uncomment these tests
    /*
    val scores = List(Score(10), Score(20), Score(30))
    val doubled = FunctorExercise.doubleScores(scores)
    assert(doubled == List(Score(20), Score(40), Score(60)))
    
    val transformed = FunctorExercise.transformAll(Option(5))(_ * 2)
    assert(transformed == Some(10))
    
    println("✓ Functor tests passed")
    */
    
    // Part 3: Monad
    println("\n--- Part 3: Monad ---")
    // TODO: Uncomment these tests
    /*
    assert(MonadExercise.safeDivide(10, 2) == Some(5))
    assert(MonadExercise.safeDivide(10, 0) == None)
    
    assert(MonadExercise.parseAndDivide("20", 4) == Some(5))
    assert(MonadExercise.parseAndDivide("abc", 4) == None)
    assert(MonadExercise.parseAndDivide("20", 0) == None)
    
    val opts = List(Some(1), Some(2), Some(3))
    assert(MonadExercise.sequenceOptions(opts) == Some(List(1, 2, 3)))
    
    val optsWithNone = List(Some(1), None, Some(3))
    assert(MonadExercise.sequenceOptions(optsWithNone) == None)
    
    println("✓ Monad tests passed")
    */
    
    println("\n=== Uncomment tests to verify your solution ===")
  }
}

/*
 * SOLUTION (Don't peek!)
 *
 * object Score {
 *   implicit val scoreSemigroup: Semigroup[Score] = new Semigroup[Score] {
 *     def combine(x: Score, y: Score): Score = Score(x.value + y.value)
 *   }
 *   
 *   implicit val scoreMonoid: Monoid[Score] = new Monoid[Score] {
 *     def combine(x: Score, y: Score): Score = scoreSemigroup.combine(x, y)
 *     def empty: Score = Score(0)
 *   }
 * }
 *
 * object FunctorExercise {
 *   def doubleScores(scores: List[Score]): List[Score] = {
 *     Functor[List].map(scores)(s => Score(s.value * 2))
 *   }
 *   
 *   def transformAll[F[_]: Functor, A, B](fa: F[A])(f: A => B): F[B] = {
 *     Functor[F].map(fa)(f)
 *   }
 * }
 *
 * object MonadExercise {
 *   def safeDivide(a: Int, b: Int): Option[Int] = {
 *     if (b == 0) None else Some(a / b)
 *   }
 *   
 *   def parseAndDivide(numStr: String, divisor: Int): Option[Int] = {
 *     for {
 *       num <- scala.util.Try(numStr.toInt).toOption
 *       result <- safeDivide(num, divisor)
 *     } yield result
 *   }
 *   
 *   def sequenceOptions[A](list: List[Option[A]]): Option[List[A]] = {
 *     list.foldRight(Option(List.empty[A])) { (optA, acc) =>
 *       for {
 *         a <- optA
 *         as <- acc
 *       } yield a :: as
 *     }
 *   }
 * }
 */
