package catseffect

import cats.effect.{IO, IOApp}
import cats.data.EitherT
import cats.syntax.applicative._
import cats.syntax.either._

/*
 * EITHERT - MONAD TRANSFORMER FOR TYPED ERRORS
 *
 * EitherT[IO, E, A] = IO[Either[E, A]]
 *
 * PROBLEM: IO[Either[E, A]] composition is awkward:
 *   for {
 *     either1 <- io1  // IO[Either[E, A]]
 *     a <- either1 match {  // Ugly!
 *       case Right(a) => ...
 *       case Left(e) => ...
 *     }
 *   } yield ...
 *
 * SOLUTION: EitherT wraps IO[Either[E, A]] and provides clean composition
 *
 * RUST COMPARISON:
 * - EitherT[IO, E, A] ~ async fn -> Result<A, E>
 * - Composes like Rust's ? operator
 * - Type-safe error handling (not just Throwable)
 * - Short-circuits on error (Left)
 *
 * This is THE pattern for production Scala + Cats-Effect code!
 */

object EitherTDemo extends IOApp.Simple {
  
  // Domain model
  sealed trait AppError {
    def message: String
  }
  case class ValidationError(message: String) extends AppError
  case class DatabaseError(message: String) extends AppError
  case class NotFoundError(message: String) extends AppError
  case class AuthError(message: String) extends AppError
  
  case class User(id: Int, name: String, email: String)
  case class Order(id: Int, userId: Int, amount: Double)
  
  /*
   * THE PROBLEM: IO[Either[E, A]] is awkward
   */
  
  def demonstrateProblem(): IO[Unit] = {
    println("\n=== The Problem: IO[Either[E, A]] ===\n")
    
    def validate(email: String): IO[Either[ValidationError, String]] = IO.pure {
      if (email.contains("@")) Right(email)
      else Left(ValidationError("Invalid email"))
    }
    
    def saveUser(email: String): IO[Either[DatabaseError, User]] = IO.pure {
      Right(User(1, "Alice", email))
    }
    
    // Composing is ugly!
    val awkward: IO[Either[AppError, User]] = for {
      validationResult <- validate("alice@example.com")
      user <- validationResult match {
        case Right(email) => saveUser(email).map(_.leftMap(identity))
        case Left(err) => IO.pure(Left(err))
      }
    } yield user
    
    for {
      _ <- IO.println("Without EitherT (awkward):")
      result <- awkward
      _ <- IO.println(s"  Result: $result")
      _ <- IO.println("  Problem: Manual pattern matching, error type widening")
    } yield ()
  }
  
  /*
   * THE SOLUTION: EitherT
   */
  
  def demonstrateSolution(): IO[Unit] = {
    println("\n=== The Solution: EitherT ===\n")
    
    type Result[A] = EitherT[IO, AppError, A]
    
    def validate(email: String): Result[String] = {
      if (email.contains("@"))
        EitherT.pure[IO, AppError](email)
      else
        EitherT.leftT[IO, String](ValidationError("Invalid email"))
    }
    
    def saveUser(email: String): Result[User] = {
      EitherT.pure[IO, AppError](User(1, "Alice", email))
    }
    
    // Clean composition!
    val clean: Result[User] = for {
      email <- validate("alice@example.com")
      user <- saveUser(email)
    } yield user
    
    for {
      _ <- IO.println("With EitherT (clean):")
      result <- clean.value  // Extract IO[Either[AppError, User]]
      _ <- IO.println(s"  Result: $result")
      _ <- IO.println("  ✓ Clean for-comprehension")
      _ <- IO.println("  ✓ Automatic short-circuiting")
      _ <- IO.println("  ✓ Type-safe errors")
      
      _ <- IO.println("\nRust comparison:")
      _ <- IO.println("  async fn process() -> Result<User, AppError> {")
      _ <- IO.println("      let email = validate_email(input)?;  // Short-circuit")
      _ <- IO.println("      let user = save_user(email).await?;")
      _ <- IO.println("      Ok(user)")
      _ <- IO.println("  }")
    } yield ()
  }
  
  /*
   * CONSTRUCTION: Creating EitherT values
   */
  
  def demonstrateConstruction(): IO[Unit] = {
    println("\n=== EitherT Construction ===\n")
    
    type Result[A] = EitherT[IO, AppError, A]
    
    // 1. EitherT.pure - lift pure value to Right
    val pure: Result[Int] = EitherT.pure[IO, AppError](42)
    
    // 2. EitherT.leftT - create Left (error)
    val error: Result[Int] = EitherT.leftT[IO, Int](ValidationError("Error"))
    
    // 3. EitherT.fromEither - lift Either
    val either: Either[AppError, Int] = Right(42)
    val fromEither: Result[Int] = EitherT.fromEither[IO](either)
    
    // 4. EitherT.liftF - lift IO[A] to EitherT (always Right)
    val io: IO[Int] = IO.pure(42)
    val lifted: Result[Int] = EitherT.liftF(io)
    
    // 5. EitherT(...) - wrap IO[Either[E, A]]
    val ioEither: IO[Either[AppError, Int]] = IO.pure(Right(42))
    val wrapped: Result[Int] = EitherT(ioEither)
    
    for {
      _ <- IO.println("Construction methods:")
      _ <- IO.println("  1. EitherT.pure[IO, E](value)           // Right")
      _ <- IO.println("  2. EitherT.leftT[IO, A](error)          // Left")
      _ <- IO.println("  3. EitherT.fromEither[IO](either)       // From Either")
      _ <- IO.println("  4. EitherT.liftF(io)                    // From IO")
      _ <- IO.println("  5. EitherT(ioEither)                    // Wrap IO[Either]")
      
      r1 <- pure.value
      _ <- IO.println(s"\n  pure result: $r1")
      r2 <- error.value
      _ <- IO.println(s"  error result: $r2")
    } yield ()
  }
  
  /*
   * COMPOSITION: Chaining operations
   */
  
  def demonstrateComposition(): IO[Unit] = {
    println("\n=== EitherT Composition ===\n")
    
    type Result[A] = EitherT[IO, AppError, A]
    
    def step1(input: String): Result[Int] = {
      if (input.nonEmpty)
        EitherT.pure[IO, AppError](input.length)
      else
        EitherT.leftT[IO, Int](ValidationError("Empty input"))
    }
    
    def step2(n: Int): Result[Int] = {
      if (n > 0)
        EitherT.pure[IO, AppError](n * 2)
      else
        EitherT.leftT[IO, Int](ValidationError("Non-positive"))
    }
    
    def step3(n: Int): Result[String] = {
      EitherT.liftF(IO.delay(s"Result: $n"))
    }
    
    // Chain with for-comprehension
    val pipeline: Result[String] = for {
      len <- step1("hello")
      doubled <- step2(len)
      result <- step3(doubled)
    } yield result
    
    for {
      _ <- IO.println("Success case:")
      r1 <- pipeline.value
      _ <- IO.println(s"  $r1")
      
      _ <- IO.println("\nFailure case (short-circuits):")
      failPipeline <- step1("").flatMap(step2).flatMap(step3).value
      _ <- IO.println(s"  $failPipeline")
      _ <- IO.println("  Note: step2 and step3 never executed!")
      
      _ <- IO.println("\nRust equivalent:")
      _ <- IO.println("  let len = step1(input)?;      // Short-circuit on error")
      _ <- IO.println("  let doubled = step2(len)?;")
      _ <- IO.println("  let result = step3(doubled)?;")
      _ <- IO.println("  Ok(result)")
    } yield ()
  }
  
  /*
   * ERROR HANDLING: Transform and recover
   */
  
  def demonstrateErrorHandling(): IO[Unit] = {
    println("\n=== EitherT Error Handling ===\n")
    
    type Result[A] = EitherT[IO, AppError, A]
    
    def riskyOp: Result[Int] = {
      EitherT.leftT[IO, Int](DatabaseError("Connection lost"))
    }
    
    // leftMap - transform error
    val transformed: Result[Int] = riskyOp.leftMap { error =>
      ValidationError(s"Wrapped: ${error.message}")
    }
    
    // recover - provide default on error
    val recovered: Result[Int] = riskyOp.recover {
      case DatabaseError(_) => 42
    }
    
    // recoverWith - provide alternative EitherT
    val recoveredWith: Result[Int] = riskyOp.recoverWith {
      case DatabaseError(_) => EitherT.pure[IO, AppError](99)
    }
    
    for {
      _ <- IO.println("leftMap (transform error):")
      r1 <- transformed.value
      _ <- IO.println(s"  $r1")
      
      _ <- IO.println("\nrecover (default value):")
      r2 <- recovered.value
      _ <- IO.println(s"  $r2")
      
      _ <- IO.println("\nrecoverWith (alternative):")
      r3 <- recoveredWith.value
      _ <- IO.println(s"  $r3")
      
      _ <- IO.println("\nRust comparison:")
      _ <- IO.println("  result.map_err(|e| transform(e))")
      _ <- IO.println("  result.or(Ok(default))")
      _ <- IO.println("  result.or_else(|_| fallback_operation())")
    } yield ()
  }
  
  /*
   * REAL-WORLD EXAMPLE: User Registration
   */
  
  def demonstrateRealWorld(): IO[Unit] = {
    println("\n=== Real-World: User Registration ===\n")
    
    type Result[A] = EitherT[IO, AppError, A]
    
    // Simulate database
    var users = Map(1 -> User(1, "Alice", "alice@example.com"))
    var nextId = 2
    
    def validateEmail(email: String): Result[String] = {
      if (email.contains("@") && email.contains("."))
        EitherT.pure[IO, AppError](email)
      else
        EitherT.leftT[IO, String](ValidationError(s"Invalid email: $email"))
    }
    
    def validateName(name: String): Result[String] = {
      if (name.length >= 2 && name.length <= 50)
        EitherT.pure[IO, AppError](name)
      else
        EitherT.leftT[IO, String](ValidationError(s"Name must be 2-50 chars"))
    }
    
    def checkEmailUnique(email: String): Result[String] = {
      if (users.values.exists(_.email == email))
        EitherT.leftT[IO, String](ValidationError(s"Email already registered: $email"))
      else
        EitherT.pure[IO, AppError](email)
    }
    
    def saveUser(name: String, email: String): Result[User] = {
      EitherT.liftF[IO, AppError, User] {
        IO.delay {
          val user = User(nextId, name, email)
          users = users + (nextId -> user)
          nextId += 1
          user
        }
      }
    }
    
    def sendWelcomeEmail(user: User): Result[Unit] = {
      EitherT.liftF[IO, AppError, Unit] {
        IO.println(s"    📧 Sending welcome email to ${user.email}")
      }
    }
    
    // Complete registration pipeline
    def registerUser(name: String, email: String): Result[User] = {
      for {
        validName <- validateName(name)
        validEmail <- validateEmail(email)
        uniqueEmail <- checkEmailUnique(validEmail)
        user <- saveUser(validName, uniqueEmail)
        _ <- sendWelcomeEmail(user)
      } yield user
    }
    
    for {
      _ <- IO.println("Success case:")
      r1 <- registerUser("Bob", "bob@example.com").value
      _ <- IO.println(s"  Result: $r1")
      
      _ <- IO.println("\nValidation error:")
      r2 <- registerUser("A", "invalid-email").value
      _ <- IO.println(s"  Result: $r2")
      _ <- IO.println("  Note: Short-circuited at first validation!")
      
      _ <- IO.println("\nDuplicate email:")
      r3 <- registerUser("Charlie", "alice@example.com").value
      _ <- IO.println(s"  Result: $r3")
      
      _ <- IO.println("\nKey insights:")
      _ <- IO.println("  ✓ Clean sequential validation")
      _ <- IO.println("  ✓ Short-circuits on first error")
      _ <- IO.println("  ✓ Type-safe errors (AppError)")
      _ <- IO.println("  ✓ Composable with for-comprehensions")
    } yield ()
  }
  
  /*
   * MIXING IO AND EITHERT
   */
  
  def demonstrateMixing(): IO[Unit] = {
    println("\n=== Mixing IO and EitherT ===\n")
    
    type Result[A] = EitherT[IO, AppError, A]
    
    // Pure IO operation
    def fetchFromApi: IO[String] = IO.delay {
      """{"status": "ok"}"""
    }
    
    // EitherT operation
    def validate(json: String): Result[String] = {
      if (json.contains("ok"))
        EitherT.pure[IO, AppError](json)
      else
        EitherT.leftT[IO, String](ValidationError("Invalid response"))
    }
    
    // Mix them!
    val mixed: Result[String] = for {
      response <- EitherT.liftF[IO, AppError, String](fetchFromApi)  // IO -> EitherT
      validated <- validate(response)
    } yield validated
    
    for {
      _ <- IO.println("Mixing IO and EitherT:")
      result <- mixed.value
      _ <- IO.println(s"  $result")
      
      _ <- IO.println("\nPattern:")
      _ <- IO.println("  - Use EitherT.liftF to lift IO[A] into EitherT")
      _ <- IO.println("  - Use .value to extract IO[Either[E, A]]")
      _ <- IO.println("  - Seamless composition!")
    } yield ()
  }
  
  def run: IO[Unit] = {
    for {
      _ <- IO.println("=" * 70)
      _ <- IO.println("EITHERT - MONAD TRANSFORMER FOR TYPED ERRORS")
      _ <- IO.println("=" * 70)
      
      _ <- demonstrateProblem()
      _ <- demonstrateSolution()
      _ <- demonstrateConstruction()
      _ <- demonstrateComposition()
      _ <- demonstrateErrorHandling()
      _ <- demonstrateRealWorld()
      _ <- demonstrateMixing()
      
      _ <- IO.println("\n" + "=" * 70)
      _ <- IO.println("KEY TAKEAWAYS")
      _ <- IO.println("=" * 70)
      _ <- IO.println("""
1. EitherT[IO, E, A] = IO[Either[E, A]] with clean composition
2. Type-safe errors (not just Throwable)
3. Short-circuits on Left (like Rust ? operator)
4. Use for-comprehension for sequential operations
5. EitherT.liftF to lift IO into EitherT
6. .value to extract IO[Either[E, A]]
7. Common pattern in production Scala + Cats-Effect
8. Rust equivalent: async fn -> Result<A, E>

PRODUCTION TIP: EitherT[IO, E, A] is the standard pattern!
      """.trim)
    } yield ()
  }
}
