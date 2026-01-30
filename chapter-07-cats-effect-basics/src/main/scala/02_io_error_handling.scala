package catseffect

import cats.effect.{IO, IOApp}
import cats.syntax.applicativeError._
import cats.syntax.monadError._
import scala.concurrent.duration._

/*
 * ERROR HANDLING IN IO
 *
 * IO[A] can fail with Throwable. Cats-Effect provides powerful error handling:
 * - raiseError: create failed IO
 * - attempt: convert to Either
 * - handleError/handleErrorWith: recover from errors
 * - redeem/redeemWith: handle both success and failure
 *
 * RUST COMPARISON:
 * - IO error handling ~ Result<T, E> with ? operator
 * - attempt ~ converting to explicit Result
 * - handleError ~ .unwrap_or_else(|e| default)
 * - Short-circuiting like ? operator
 */

object IOErrorHandlingDemo extends IOApp.Simple {
  
  /*
   * CREATING ERRORS
   */
  
  def demonstrateRaisingErrors(): IO[Unit] = {
    println("\n=== Raising Errors ===\n")
    
    // 1. IO.raiseError - explicitly create a failed IO
    val explicitError: IO[Int] = IO.raiseError(new Exception("Explicit error"))
    
    // 2. Throwing in IO.delay
    val implicitError: IO[Int] = IO.delay {
      throw new Exception("Thrown in delay")
    }
    
    // 3. Failed computation
    def divide(a: Int, b: Int): IO[Int] = IO.delay {
      if (b == 0) throw new ArithmeticException("Division by zero")
      else a / b
    }
    
    for {
      _ <- IO.println("Error creation methods:")
      _ <- IO.println("  1. IO.raiseError(exception)")
      _ <- IO.println("  2. throw in IO.delay { ... }")
      _ <- IO.println("  3. Regular exceptions in computations")
      
      _ <- IO.println("\nRust comparison:")
      _ <- IO.println("  Err(error)  // explicit")
      _ <- IO.println("  func()?     // propagate with ?")
    } yield ()
  }
  
  /*
   * ATTEMPT - Convert to Either
   */
  
  def demonstrateAttempt(): IO[Unit] = {
    println("\n=== Attempt - IO[A] to IO[Either[Throwable, A]] ===\n")
    
    def riskyOperation(n: Int): IO[Int] = IO.delay {
      if (n < 0) throw new IllegalArgumentException(s"Negative: $n")
      n * 2
    }
    
    for {
      _ <- IO.println("Success case:")
      result1 <- riskyOperation(5).attempt
      _ <- IO.println(s"  riskyOperation(5).attempt = $result1")
      
      _ <- IO.println("\nFailure case:")
      result2 <- riskyOperation(-5).attempt
      _ <- IO.println(s"  riskyOperation(-5).attempt = $result2")
      
      _ <- IO.println("\nPattern match on result:")
      _ <- riskyOperation(10).attempt.flatMap {
        case Right(value) => IO.println(s"  Success: $value")
        case Left(error) => IO.println(s"  Error: ${error.getMessage}")
      }
      
      _ <- IO.println("\nRust comparison:")
      _ <- IO.println("  // Rust Result is already explicit")
      _ <- IO.println("  match risky_operation(5) {")
      _ <- IO.println("      Ok(val) => ...,")
      _ <- IO.println("      Err(e) => ...,")
      _ <- IO.println("  }")
    } yield ()
  }
  
  /*
   * HANDLE ERROR - Recovery
   */
  
  def demonstrateHandleError(): IO[Unit] = {
    println("\n=== HandleError - Recovery ===\n")
    
    def fetchData(id: Int): IO[String] = IO.delay {
      if (id < 0) throw new Exception("Invalid ID")
      s"Data-$id"
    }
    
    // handleError - provide default value
    val recovered1: IO[String] = fetchData(-1).handleError { error =>
      s"Error: ${error.getMessage}, using default"
    }
    
    // handleErrorWith - provide alternative IO
    val recovered2: IO[String] = fetchData(-1).handleErrorWith { error =>
      IO.println(s"  Error occurred: ${error.getMessage}") *>
        IO.pure("Fallback data")
    }
    
    for {
      _ <- IO.println("handleError (synchronous recovery):")
      r1 <- recovered1
      _ <- IO.println(s"  Result: $r1")
      
      _ <- IO.println("\nhandleErrorWith (effectful recovery):")
      r2 <- recovered2
      _ <- IO.println(s"  Result: $r2")
      
      _ <- IO.println("\nRust comparison:")
      _ <- IO.println("  result.unwrap_or_else(|e| default)")
      _ <- IO.println("  result.or_else(|e| fallback_operation())")
    } yield ()
  }
  
  /*
   * REDEEM - Handle both cases
   */
  
  def demonstrateRedeem(): IO[Unit] = {
    println("\n=== Redeem - Handle Success and Failure ===\n")
    
    def operation(n: Int): IO[Int] = IO.delay {
      if (n < 0) throw new Exception("Negative")
      n * 2
    }
    
    // redeem - map both error and success to same type
    val redeemed: IO[String] = operation(-5).redeem(
      error => s"Failed: ${error.getMessage}",
      success => s"Success: $success"
    )
    
    // redeemWith - effectful version
    val redeemedWith: IO[Unit] = operation(5).redeemWith(
      error => IO.println(s"  Error path: ${error.getMessage}"),
      success => IO.println(s"  Success path: $success")
    )
    
    for {
      _ <- IO.println("redeem example:")
      r <- redeemed
      _ <- IO.println(s"  $r")
      
      _ <- IO.println("\nredeemWith example:")
      _ <- redeemedWith
      
      _ <- IO.println("\nRust comparison:")
      _ <- IO.println("  match result {")
      _ <- IO.println("      Ok(val) => handle_success(val),")
      _ <- IO.println("      Err(e) => handle_error(e),")
      _ <- IO.println("  }")
    } yield ()
  }
  
  /*
   * PRACTICAL: Retry Logic
   */
  
  def demonstrateRetry(): IO[Unit] = {
    println("\n=== Practical: Retry Logic ===\n")
    
    var attempts = 0
    
    def unreliableService: IO[String] = IO.delay {
      attempts += 1
      if (attempts < 3) throw new Exception(s"Attempt $attempts failed")
      "Success!"
    }
    
    def retryIO[A](io: IO[A], maxRetries: Int): IO[A] = {
      io.handleErrorWith { error =>
        if (maxRetries > 0) {
          IO.println(s"  Retry... (${maxRetries} left)") *>
            retryIO(io, maxRetries - 1)
        } else {
          IO.raiseError(error)
        }
      }
    }
    
    for {
      _ <- IO.println("Calling unreliable service with retry:")
      result <- retryIO(unreliableService, maxRetries = 3)
      _ <- IO.println(s"  Final result: $result")
      _ <- IO.println(s"  Total attempts: $attempts")
      
      _ <- IO.println("\nRust comparison:")
      _ <- IO.println("  for attempt in 0..max_retries {")
      _ <- IO.println("      match operation().await {")
      _ <- IO.println("          Ok(val) => return Ok(val),")
      _ <- IO.println("          Err(e) if attempt < max_retries => continue,")
      _ <- IO.println("          Err(e) => return Err(e),")
      _ <- IO.println("      }")
      _ <- IO.println("  }")
    } yield ()
  }
  
  /*
   * ADAPT ERROR - Transform Error Type
   */
  
  def demonstrateAdaptError(): IO[Unit] = {
    println("\n=== AdaptError - Transform Errors ===\n")
    
    def parseNumber(s: String): IO[Int] = IO.delay {
      s.toInt // Can throw NumberFormatException
    }
    
    // Transform generic exception to domain error
    sealed trait AppError extends Exception
    case class ParseError(input: String, cause: Throwable) extends AppError {
      override def getMessage: String = s"Failed to parse '$input': ${cause.getMessage}"
    }
    
    val adapted: IO[Int] = parseNumber("not-a-number").adaptError {
      case nfe: NumberFormatException => ParseError("not-a-number", nfe)
    }
    
    for {
      _ <- IO.println("Original error:")
      result1 <- parseNumber("abc").attempt
      _ <- IO.println(s"  $result1")
      
      _ <- IO.println("\nAdapted error:")
      result2 <- adapted.attempt
      _ <- IO.println(s"  $result2")
      
      _ <- IO.println("\nRust comparison:")
      _ <- IO.println("  result.map_err(|e| AppError::Parse(e))")
    } yield ()
  }
  
  /*
   * ENSURE - Validation
   */
  
  def demonstrateEnsure(): IO[Unit] = {
    println("\n=== Ensure - Validation ===\n")
    
    def getAge(input: String): IO[Int] = {
      IO.delay(input.toInt).ensure(
        new IllegalArgumentException("Age must be positive")
      )(_ > 0)
    }
    
    for {
      _ <- IO.println("Valid age:")
      result1 <- getAge("25").attempt
      _ <- IO.println(s"  $result1")
      
      _ <- IO.println("\nInvalid age:")
      result2 <- getAge("-5").attempt
      _ <- IO.println(s"  $result2")
      
      _ <- IO.println("\nRust comparison:")
      _ <- IO.println("  let age = parse_age(input)?;")
      _ <- IO.println("  if age > 0 { Ok(age) }")
      _ <- IO.println("  else { Err(ValidationError) }")
    } yield ()
  }
  
  /*
   * REAL-WORLD EXAMPLE
   */
  
  def demonstrateRealWorld(): IO[Unit] = {
    println("\n=== Real-World: HTTP Client with Error Handling ===\n")
    
    // Simulate HTTP client
    def httpGet(url: String): IO[String] = IO.delay {
      url match {
        case s if s.contains("error") => throw new Exception("HTTP 500")
        case s if s.contains("timeout") => throw new Exception("Timeout")
        case _ => s"""{"data": "response from $url"}"""
      }
    }
    
    def fetchWithRetry(url: String, retries: Int = 2): IO[String] = {
      httpGet(url)
        .handleErrorWith { error =>
          if (retries > 0 && error.getMessage.contains("Timeout")) {
            IO.println(s"  Timeout, retrying... ($retries left)") *>
              fetchWithRetry(url, retries - 1)
          } else {
            IO.raiseError(error)
          }
        }
    }
    
    def fetchWithFallback(url: String): IO[String] = {
      fetchWithRetry(url).handleError { _ =>
        """{"data": "cached fallback"}"""
      }
    }
    
    for {
      _ <- IO.println("Success case:")
      r1 <- fetchWithFallback("https://api.example.com/users")
      _ <- IO.println(s"  $r1")
      
      _ <- IO.println("\nTimeout with retry:")
      r2 <- fetchWithFallback("https://api.example.com/timeout")
      _ <- IO.println(s"  $r2")
      
      _ <- IO.println("\nError with fallback:")
      r3 <- fetchWithFallback("https://api.example.com/error")
      _ <- IO.println(s"  $r3")
    } yield ()
  }
  
  def run: IO[Unit] = {
    for {
      _ <- IO.println("=" * 60)
      _ <- IO.println("IO ERROR HANDLING")
      _ <- IO.println("=" * 60)
      
      _ <- demonstrateRaisingErrors()
      _ <- demonstrateAttempt()
      _ <- demonstrateHandleError()
      _ <- demonstrateRedeem()
      _ <- demonstrateRetry()
      _ <- demonstrateAdaptError()
      _ <- demonstrateEnsure()
      _ <- demonstrateRealWorld()
      
      _ <- IO.println("\n" + "=" * 60)
      _ <- IO.println("KEY TAKEAWAYS")
      _ <- IO.println("=" * 60)
      _ <- IO.println("""
1. IO.raiseError creates failed IO
2. attempt converts IO[A] to IO[Either[Throwable, A]]
3. handleError/handleErrorWith for recovery
4. redeem/redeemWith handle both paths
5. adaptError transforms error types
6. ensure validates results
7. Compose error handling with flatMap
8. Similar to Rust Result with ? operator
      """.trim)
    } yield ()
  }
}
