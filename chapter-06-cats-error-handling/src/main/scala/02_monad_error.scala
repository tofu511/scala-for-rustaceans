package catserrorhandling

import cats.{ApplicativeError, MonadError}
import cats.syntax.applicativeError._
import cats.syntax.monadError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.instances.either._
import cats.instances.option._
import cats.instances.try_._
import scala.util.{Try, Success, Failure}

/*
 * MONAD ERROR - TYPE CLASS FOR ERROR HANDLING
 *
 * MonadError and ApplicativeError are type classes that abstract over
 * error handling. They work with any F[_] that can represent failure,
 * like Either, Try, Option, or IO.
 *
 * RUST COMPARISON:
 * This is like having a generic trait that works with Result<T, E>,
 * Option<T>, and custom error types. Rust doesn't have higher-kinded
 * types, so you'd implement separate traits for each type.
 *
 * ApplicativeError: raise errors, handle errors
 * MonadError: extends ApplicativeError + Monad (adds flatMap)
 */

object MonadErrorBasics {
  
  def demonstrateApplicativeError(): Unit = {
    println("\n=== ApplicativeError ===\n")
    
    // ApplicativeError[F[_], E] lets you raise and handle errors
    
    // Example with Either[String, *]
    type ErrorOr[A] = Either[String, A]
    val AE = ApplicativeError[ErrorOr, String]
    
    // raiseError - create a failed F
    val error: ErrorOr[Int] = AE.raiseError("Something went wrong")
    println(s"raiseError('Something went wrong') = $error")
    
    // handleError - recover from failure
    val recovered = error.handleError(err => -1)
    println(s"error.handleError(err => -1) = $recovered")
    
    // handleErrorWith - recover with another F
    val recoveredWith = error.handleErrorWith(err => Right(42))
    println(s"error.handleErrorWith(err => Right(42)) = $recoveredWith")
    
    // attempt - convert F[A] to F[Either[E, A]]
    val success: ErrorOr[Int] = Right(100)
    println(s"Right(100).attempt = ${success.attempt}")
    println(s"error.attempt = ${error.attempt}")
    
    println("\nRust comparison:")
    println("  raiseError ~ Err(e)")
    println("  handleError ~ result.unwrap_or_else(|e| default)")
    println("  attempt ~ wrapping Result in another Result")
  }
  
  def demonstrateMonadError(): Unit = {
    println("\n=== MonadError ===\n")
    
    // MonadError adds flatMap-based operations
    type ErrorOr[A] = Either[String, A]
    val ME = MonadError[ErrorOr, String]
    
    def divide(a: Int, b: Int): ErrorOr[Int] = {
      if (b == 0) ME.raiseError("Division by zero")
      else ME.pure(a / b)
    }
    
    println("Using MonadError operations:")
    
    // ensureOr - validate with custom error
    val positive: ErrorOr[Int] = Right(5)
    val validated = positive.ensureOr(n => s"$n is not positive")(_ > 0)
    println(s"Right(5).ensureOr(n => ..., _ > 0) = $validated")
    
    val negative: ErrorOr[Int] = Right(-5)
    val invalidated = negative.ensureOr(n => s"$n is not positive")(_ > 0)
    println(s"Right(-5).ensureOr(n => ..., _ > 0) = $invalidated")
    
    // ensure - validate with provided error
    val validated2 = positive.ensure("Not positive")(_ > 0)
    println(s"Right(5).ensure('Not positive', _ > 0) = $validated2")
    
    // adaptError - transform error type
    val adapted = divide(10, 0).adaptError {
      case "Division by zero" => "Math error: cannot divide by zero"
    }
    println(s"divide(10, 0).adaptError = $adapted")
    
    // Rust comparison:
    println("\nRust comparison:")
    println("  ensureOr ~ result.and_then(|x| if pred(x) { Ok(x) } else { Err(e) })")
    println("  adaptError ~ result.map_err(|e| transform(e))")
  }
  
  def demonstrateWithTry(): Unit = {
    println("\n=== With Try ===\n")
    
    // MonadError works with Try[A] where error type is Throwable
    val ME = MonadError[Try, Throwable]
    
    def parseInt(s: String): Try[Int] = {
      Try(s.toInt)
    }
    
    val success = parseInt("42")
    val failure = parseInt("not a number")
    
    println(s"parseInt('42') = $success")
    println(s"parseInt('not a number') = $failure")
    
    // Handle errors
    val recovered = failure.handleError(_ => 0)
    println(s"failure.handleError(_ => 0) = $recovered")
    
    // Transform errors
    val betterError = failure.adaptError {
      case ex: NumberFormatException => 
        new IllegalArgumentException(s"Invalid number format: ${ex.getMessage}")
    }
    println(s"failure.adaptError = $betterError")
    
    // Rust comparison:
    println("\nRust comparison:")
    println("  Try ~ std::result::Result<T, Box<dyn Error>>")
    println("  Throwable ~ Box<dyn Error> or custom error enum")
  }
  
  def demonstrateGenericCode(): Unit = {
    println("\n=== Generic Error Handling ===\n")
    
    // Write code that works with ANY error type!
    def safeDivide[F[_]](a: Int, b: Int)(implicit ME: MonadError[F, Throwable]): F[Int] = {
      if (b == 0) ME.raiseError(new ArithmeticException("Division by zero"))
      else ME.pure(a / b)
    }
    
    def validatePositive[F[_]](n: Int)(implicit ME: MonadError[F, Throwable]): F[Int] = {
      ME.pure(n).ensure(new IllegalArgumentException(s"$n is not positive"))(_ > 0)
    }
    
    // Chain operations
    def divideAndValidate[F[_]](a: Int, b: Int)(implicit ME: MonadError[F, Throwable]): F[Int] = {
      for {
        result <- safeDivide[F](a, b)
        validated <- validatePositive[F](result)
      } yield validated
    }
    
    println("With Try:")
    println(s"  divideAndValidate[Try](10, 2) = ${divideAndValidate[Try](10, 2)}")
    println(s"  divideAndValidate[Try](10, 0) = ${divideAndValidate[Try](10, 0)}")
    println(s"  divideAndValidate[Try](-10, 1) = ${divideAndValidate[Try](-10, 1)}")
    
    // This is powerful: same code works with Try, Either, IO, etc!
    
    println("\nRust comparison:")
    println("  In Rust, you'd need separate implementations for")
    println("  Result, Option, custom types (no higher-kinded types)")
    println("  Or use trait objects: Box<dyn Future<Output = Result<T, E>>>")
  }
  
  def demonstratePracticalExample(): Unit = {
    println("\n=== Practical Example ===\n")
    
    // User registration with validation
    case class User(name: String, email: String, age: Int)
    
    sealed trait ValidationError extends Throwable
    case class InvalidName(msg: String) extends ValidationError {
      override def getMessage: String = msg
    }
    case class InvalidEmail(msg: String) extends ValidationError {
      override def getMessage: String = msg
    }
    case class InvalidAge(msg: String) extends ValidationError {
      override def getMessage: String = msg
    }
    
    def validateName[F[_]](name: String)(implicit ME: MonadError[F, Throwable]): F[String] = {
      ME.pure(name).ensure(InvalidName("Name must be non-empty"))(_.nonEmpty)
    }
    
    def validateEmail[F[_]](email: String)(implicit ME: MonadError[F, Throwable]): F[String] = {
      ME.pure(email).ensure(InvalidEmail("Email must contain @"))(_.contains("@"))
    }
    
    def validateAge[F[_]](age: Int)(implicit ME: MonadError[F, Throwable]): F[Int] = {
      ME.pure(age).ensure(InvalidAge("Age must be 18-120"))(a => a >= 18 && a <= 120)
    }
    
    def createUser[F[_]](
      name: String, 
      email: String, 
      age: Int
    )(implicit ME: MonadError[F, Throwable]): F[User] = {
      for {
        validName <- validateName[F](name)
        validEmail <- validateEmail[F](email)
        validAge <- validateAge[F](age)
      } yield User(validName, validEmail, validAge)
    }
    
    println("Valid user:")
    val user1 = createUser[Try]("Alice", "alice@example.com", 25)
    println(s"  $user1")
    
    println("\nInvalid name:")
    val user2 = createUser[Try]("", "alice@example.com", 25)
    println(s"  $user2")
    
    println("\nInvalid email:")
    val user3 = createUser[Try]("Alice", "invalid", 25)
    println(s"  $user3")
    
    println("\nNote: This short-circuits on first error (use Validated for accumulation)")
    
    // Rust comparison:
    println("\nRust equivalent:")
    println("""
  fn create_user(name: &str, email: &str, age: i32) 
    -> Result<User, ValidationError> 
  {
      let name = validate_name(name)?;
      let email = validate_email(email)?;
      let age = validate_age(age)?;
      Ok(User { name, email, age })
  }
    """.trim)
  }
  
  def main(args: Array[String]): Unit = {
    println("=" * 60)
    println("MONAD ERROR - GENERIC ERROR HANDLING")
    println("=" * 60)
    
    demonstrateApplicativeError()
    demonstrateMonadError()
    demonstrateWithTry()
    demonstrateGenericCode()
    demonstratePracticalExample()
    
    println("\n" + "=" * 60)
    println("KEY TAKEAWAYS")
    println("=" * 60)
    println("""
1. ApplicativeError: raise, handle, recover from errors
2. MonadError: adds flatMap-based operations (ensure, adapt)
3. Works with Either, Try, Option, IO, any F[_] with errors
4. Write generic code that works with any error-capable type
5. Short-circuits on first error (unlike Validated)
6. Rust: Similar to Result<T, E> but more composable
    """.trim)
  }
}
