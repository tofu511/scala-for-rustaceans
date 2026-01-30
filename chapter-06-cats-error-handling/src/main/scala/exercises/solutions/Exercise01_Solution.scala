package catserrorhandling.exercises.solutions

import cats.data.{Validated, ValidatedNel}
import cats.data.Validated.{Valid, Invalid}
import cats.syntax.apply._
import cats.syntax.validated._
import cats.{MonadError}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.monadError._
import scala.util.Try

object Exercise01Solution {
  
  case class Product(name: String, price: Double, quantity: Int)
  
  sealed trait ProductError {
    def message: String
  }
  case class InvalidName(message: String) extends ProductError
  case class InvalidPrice(message: String) extends ProductError
  case class InvalidQuantity(message: String) extends ProductError
  
  type ValidationResult[A] = ValidatedNel[ProductError, A]
  
  def validateName(name: String): ValidationResult[String] = {
    if (name.nonEmpty && name.length <= 50)
      name.validNel
    else
      InvalidName(s"Name must be 1-50 chars, got ${name.length}").invalidNel
  }
  
  def validatePrice(price: Double): ValidationResult[Double] = {
    if (price > 0 && price < 10000)
      price.validNel
    else
      InvalidPrice(s"Price must be 0-10000, got $price").invalidNel
  }
  
  def validateQuantity(qty: Int): ValidationResult[Int] = {
    if (qty > 0 && qty <= 1000)
      qty.validNel
    else
      InvalidQuantity(s"Quantity must be 1-1000, got $qty").invalidNel
  }
  
  def validateProduct(name: String, price: Double, qty: Int): ValidationResult[Product] = {
    (validateName(name), validatePrice(price), validateQuantity(qty)).mapN(Product)
  }
  
  def parseDouble[F[_]](s: String)(implicit ME: MonadError[F, Throwable]): F[Double] = {
    Try(s.toDouble).fold(
      ex => ME.raiseError(new NumberFormatException(s"Cannot parse '$s' as Double")),
      d => ME.pure(d)
    )
  }
  
  def parseInt[F[_]](s: String)(implicit ME: MonadError[F, Throwable]): F[Int] = {
    Try(s.toInt).fold(
      ex => ME.raiseError(new NumberFormatException(s"Cannot parse '$s' as Int")),
      i => ME.pure(i)
    )
  }
  
  def calculateTotal[F[_]](price: Double, quantity: Int)(implicit ME: MonadError[F, Throwable]): F[Double] = {
    val total = price * quantity
    ME.pure(total).ensure(new IllegalArgumentException("Total must be positive"))(_ > 0)
  }
  
  def main(args: Array[String]): Unit = {
    println("=== Exercise 01 Solution ===\n")
    
    println("--- Validated (accumulates all errors) ---")
    println(s"Valid: ${validateProduct("Widget", 29.99, 10)}")
    println(s"Invalid: ${validateProduct("", -5.0, 2000)}")
    
    println("\n--- MonadError (sequential operations) ---")
    val result: Try[Double] = for {
      p <- parseDouble[Try]("29.99")
      q <- parseInt[Try]("10")
      total <- calculateTotal[Try](p, q)
    } yield total
    
    println(s"Success: $result")
    println(s"Parse failure: ${parseDouble[Try]("not-a-number")}")
    
    println("\n✓ All tests passed!")
  }
}
