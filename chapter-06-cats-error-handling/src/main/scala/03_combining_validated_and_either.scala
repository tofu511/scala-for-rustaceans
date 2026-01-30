package catserrorhandling

import cats.data.{Validated, ValidatedNel}
import cats.data.Validated.{Valid, Invalid}
import cats.syntax.apply._
import cats.syntax.validated._
import cats.instances.list._

/*
 * COMBINING VALIDATED AND EITHER
 *
 * In real applications, you often need both:
 * - Validated for parallel validations (collect ALL errors)
 * - Either/MonadError for sequential operations (short-circuit)
 *
 * This example shows when to use each and how to convert between them.
 *
 * RUST COMPARISON:
 * This pattern doesn't exist directly in Rust. You'd either:
 * 1. Use ? operator (short-circuit like Either)
 * 2. Manually collect errors into Vec (like Validated)
 * 3. Use external crates for validation (garde, validator)
 */

object CombiningApproaches {
  
  // Domain model
  case class Email(value: String) extends AnyVal
  case class Age(value: Int) extends AnyVal
  case class Username(value: String) extends AnyVal
  case class User(username: Username, email: Email, age: Age)
  
  // Error types
  sealed trait ValidationError {
    def message: String
  }
  case class InvalidUsername(message: String) extends ValidationError
  case class InvalidEmail(message: String) extends ValidationError
  case class InvalidAge(message: String) extends ValidationError
  case class DatabaseError(message: String) extends ValidationError
  
  // Type alias for ValidatedNel (Validated with NonEmptyList of errors)
  type ValidationResult[A] = ValidatedNel[ValidationError, A]
  
  def demonstrateValidationPhase(): Unit = {
    println("\n=== Phase 1: Input Validation (Use Validated) ===\n")
    
    // Validate individual fields - these are INDEPENDENT
    def validateUsername(s: String): ValidationResult[Username] = {
      if (s.length >= 3 && s.length <= 20)
        Username(s).validNel
      else
        InvalidUsername(s"Username must be 3-20 chars, got ${s.length}").invalidNel
    }
    
    def validateEmail(s: String): ValidationResult[Email] = {
      if (s.contains("@") && s.contains("."))
        Email(s).validNel
      else
        InvalidEmail(s"Invalid email format: $s").invalidNel
    }
    
    def validateAge(n: Int): ValidationResult[Age] = {
      if (n >= 18 && n <= 120)
        Age(n).validNel
      else
        InvalidAge(s"Age must be 18-120, got $n").invalidNel
    }
    
    // Combine validations with mapN - runs ALL validations
    def validateUserInput(
      username: String,
      email: String,
      age: Int
    ): ValidationResult[User] = {
      (validateUsername(username), validateEmail(email), validateAge(age))
        .mapN(User)
    }
    
    println("Valid input:")
    val result1 = validateUserInput("alice", "alice@example.com", 25)
    println(s"  $result1\n")
    
    println("Multiple validation errors (all reported):")
    val result2 = validateUserInput("ab", "invalid", 15)
    println(s"  $result2")
    result2 match {
      case Invalid(errors) =>
        println("  All errors:")
        errors.toList.foreach(err => println(s"    - ${err.message}"))
      case _ =>
    }
    
    // Rust comparison:
    println("\nRust equivalent:")
    println("""
  let mut errors = Vec::new();
  if let Err(e) = validate_username(username) { errors.push(e); }
  if let Err(e) = validate_email(email) { errors.push(e); }
  if let Err(e) = validate_age(age) { errors.push(e); }
  
  if errors.is_empty() {
      Ok(User { ... })
  } else {
      Err(errors)
  }
    """.trim)
  }
  
  def demonstrateBusinessLogicPhase(): Unit = {
    println("\n\n=== Phase 2: Business Logic (Use Either) ===\n")
    
    // Business logic operations - these are SEQUENTIAL/DEPENDENT
    type Result[A] = Either[ValidationError, A]
    
    def checkUsernameAvailable(username: Username): Result[Username] = {
      // Simulate database check
      if (username.value == "admin") {
        Left(InvalidUsername("Username 'admin' is reserved"))
      } else {
        Right(username)
      }
    }
    
    def checkEmailNotRegistered(email: Email): Result[Email] = {
      // Simulate database check
      if (email.value == "taken@example.com") {
        Left(InvalidEmail("Email already registered"))
      } else {
        Right(email)
      }
    }
    
    def saveToDatabase(user: User): Result[User] = {
      // Simulate database save
      // In real app, this could fail with DatabaseError
      Right(user)
    }
    
    // Chain business logic with Either (short-circuits on first error)
    def registerUser(user: User): Result[User] = {
      for {
        username <- checkUsernameAvailable(user.username)
        email <- checkEmailNotRegistered(user.email)
        saved <- saveToDatabase(user.copy(username = username, email = email))
      } yield saved
    }
    
    val validUser = User(Username("alice"), Email("alice@example.com"), Age(25))
    val adminUser = User(Username("admin"), Email("admin@example.com"), Age(30))
    val takenEmail = User(Username("bob"), Email("taken@example.com"), Age(28))
    
    println("Successful registration:")
    println(s"  ${registerUser(validUser)}\n")
    
    println("Username taken (short-circuits, doesn't check email):")
    println(s"  ${registerUser(adminUser)}\n")
    
    println("Email taken:")
    println(s"  ${registerUser(takenEmail)}")
    
    // Rust comparison:
    println("\nRust equivalent:")
    println("""
  fn register_user(user: User) -> Result<User, ValidationError> {
      let username = check_username_available(user.username)?;
      let email = check_email_not_registered(user.email)?;
      let saved = save_to_database(user)?;
      Ok(saved)
  }
    """.trim)
  }
  
  def demonstrateCombinedWorkflow(): Unit = {
    println("\n\n=== Complete Workflow ===\n")
    
    // Step 1: Validate input (Validated)
    type ValidationResult[A] = ValidatedNel[ValidationError, A]
    type Result[A] = Either[ValidationError, A]
    
    def validateUsername(s: String): ValidationResult[Username] = {
      if (s.length >= 3 && s.length <= 20)
        Username(s).validNel
      else
        InvalidUsername(s"Username must be 3-20 chars").invalidNel
    }
    
    def validateEmail(s: String): ValidationResult[Email] = {
      if (s.contains("@") && s.contains("."))
        Email(s).validNel
      else
        InvalidEmail(s"Invalid email format").invalidNel
    }
    
    def validateAge(n: Int): ValidationResult[Age] = {
      if (n >= 18 && n <= 120)
        Age(n).validNel
      else
        InvalidAge(s"Age must be 18-120").invalidNel
    }
    
    def validateInput(username: String, email: String, age: Int): ValidationResult[User] = {
      (validateUsername(username), validateEmail(email), validateAge(age)).mapN(User)
    }
    
    // Step 2: Business logic (Either)
    def checkUsernameAvailable(username: Username): Result[Username] = {
      if (username.value == "admin") Left(InvalidUsername("Reserved"))
      else Right(username)
    }
    
    def saveUser(user: User): Result[User] = Right(user)
    
    def processUser(user: User): Result[User] = {
      for {
        username <- checkUsernameAvailable(user.username)
        saved <- saveUser(user.copy(username = username))
      } yield saved
    }
    
    // Combined: Validate then process
    def createUser(username: String, email: String, age: Int): Either[List[ValidationError], User] = {
      val validated = validateInput(username, email, age)
        .toEither  // Convert Validated to Either
        .left.map(_.toList)  // Convert NonEmptyList to List
      
      validated.flatMap { user =>
        processUser(user).left.map(List(_))  // Wrap single error in List
      }
    }
    
    println("Success case:")
    println(s"  ${createUser("alice", "alice@example.com", 25)}\n")
    
    println("Input validation failures (all errors):")
    println(s"  ${createUser("ab", "invalid", 15)}\n")
    
    println("Business logic failure:")
    println(s"  ${createUser("admin", "admin@example.com", 25)}")
    
    println("\nPattern:")
    println("  1. Validated for input validation (parallel, all errors)")
    println("  2. Convert to Either")
    println("  3. Either for business logic (sequential, short-circuit)")
  }
  
  def main(args: Array[String]): Unit = {
    println("=" * 70)
    println("COMBINING VALIDATED AND EITHER")
    println("=" * 70)
    
    demonstrateValidationPhase()
    demonstrateBusinessLogicPhase()
    demonstrateCombinedWorkflow()
    
    println("\n" + "=" * 70)
    println("KEY TAKEAWAYS")
    println("=" * 70)
    println("""
1. Validated: Use for parallel, independent validations
   - Collects ALL errors
   - Perfect for form validation

2. Either/MonadError: Use for sequential business logic
   - Short-circuits on first error
   - Operations depend on previous results

3. Common pattern:
   - Phase 1: Validated for input validation
   - Convert to Either (.toEither)
   - Phase 2: Either for business logic

4. Rust comparison:
   - Validated ~ manual error collection (Vec<Error>)
   - Either ~ Result<T, E> with ? operator

5. Choose based on semantics:
   - Independent checks? Use Validated
   - Dependent operations? Use Either
    """.trim)
  }
}
