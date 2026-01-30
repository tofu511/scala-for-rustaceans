package catserrorhandling.exercises

import cats.data.{Validated, ValidatedNel}
import cats.data.Validated.{Valid, Invalid}
import cats.syntax.apply._
import cats.syntax.validated._
import cats.{MonadError}
import scala.util.Try

/*
 * EXERCISE 01: Error Handling with Validated and MonadError
 *
 * OBJECTIVES:
 * - Use Validated to accumulate validation errors
 * - Use MonadError for sequential business logic
 * - Combine both approaches in a real workflow
 *
 * HOW TO RUN:
 *   cd chapter-06-cats-error-handling
 *   sbt "runMain catserrorhandling.exercises.Exercise01Validation"
 */

object Exercise01Validation {
  
  // Part 1: Product validation with Validated
  case class Product(name: String, price: Double, quantity: Int)
  
  sealed trait ProductError {
    def message: String
  }
  case class InvalidName(message: String) extends ProductError
  case class InvalidPrice(message: String) extends ProductError
  case class InvalidQuantity(message: String) extends ProductError
  
  type ValidationResult[A] = ValidatedNel[ProductError, A]
  
  // TODO: Implement validations that accumulate errors
  def validateName(name: String): ValidationResult[String] = ???
  // Name must be non-empty and <= 50 chars
  
  def validatePrice(price: Double): ValidationResult[Double] = ???
  // Price must be > 0 and < 10000
  
  def validateQuantity(qty: Int): ValidationResult[Int] = ???
  // Quantity must be > 0 and <= 1000
  
  def validateProduct(name: String, price: Double, qty: Int): ValidationResult[Product] = ???
  // Combine all validations with mapN
  
  // Part 2: Generic error handling with MonadError
  def parseDouble[F[_]](s: String)(implicit ME: MonadError[F, Throwable]): F[Double] = ???
  // Parse string to Double, raise error if fails
  
  def parseInt[F[_]](s: String)(implicit ME: MonadError[F, Throwable]): F[Int] = ???
  // Parse string to Int, raise error if fails
  
  def calculateTotal[F[_]](price: Double, quantity: Int)(implicit ME: MonadError[F, Throwable]): F[Double] = ???
  // Multiply price * quantity, ensure result is positive
  
  def main(args: Array[String]): Unit = {
    println("=== Exercise 01: Validation ===\n")
    
    // TODO: Uncomment tests
    /*
    println("--- Part 1: Validated ---")
    val valid = validateProduct("Widget", 29.99, 10)
    assert(valid.isValid)
    
    val invalid = validateProduct("", -5.0, 2000)
    assert(invalid.isInvalid)
    invalid match {
      case Invalid(errors) => 
        assert(errors.size == 3) // All 3 errors collected!
      case _ =>
    }
    println("✓ Validated tests passed")
    
    println("\n--- Part 2: MonadError ---")
    import cats.syntax.flatMap._
    import cats.syntax.functor._
    
    val result: Try[Double] = for {
      p <- parseDouble[Try]("29.99")
      q <- parseInt[Try]("10")
      total <- calculateTotal[Try](p, q)
    } yield total
    
    assert(result == scala.util.Success(299.9))
    
    val failParse = parseDouble[Try]("not-a-number")
    assert(failParse.isFailure)
    println("✓ MonadError tests passed")
    */
    
    println("\n=== Uncomment tests to verify ===")
  }
}

/* SOLUTION - See exercises/solutions/Exercise01_Solution.scala */
