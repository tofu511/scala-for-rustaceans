package errorhandling.exercises

/*
 * EXERCISE 01: Either - Validation and Error Handling
 *
 * OBJECTIVES:
 * - Practice using Either for error handling
 * - Chain Either operations with flatMap and for-comprehensions
 * - Handle multiple error types
 * - Build a small validation system
 *
 * RUST COMPARISON:
 * This is like working with Result<T, E> in Rust, but with more expressive
 * error types and better composition.
 *
 * TASKS:
 * 1. Implement parsePositiveInt: parse string to positive Int
 * 2. Implement validateEmail: validate email format
 * 3. Implement createAccount: validate username, email, and age
 * 4. Uncomment tests in main() to verify your implementation
 *
 * HOW TO RUN:
 *   cd chapter-03-error-handling
 *   sbt "runMain errorhandling.exercises.Exercise01Either"
 *
 * EXPECTED OUTPUT:
 * All test assertions should pass without errors.
 * You should see success messages for valid inputs and
 * appropriate error messages for invalid inputs.
 */

// Error types for account creation
sealed trait ValidationError
case class InvalidUsername(reason: String) extends ValidationError
case class InvalidEmail(reason: String) extends ValidationError
case class InvalidAge(reason: String) extends ValidationError

case class Account(username: String, email: String, age: Int)

object Exercise01Either {
  
  // TODO: Implement parsePositiveInt
  // Parse a string to a positive integer
  // Return Left(error message) if:
  // - String is not a valid integer
  // - Integer is not positive (> 0)
  def parsePositiveInt(s: String): Either[String, Int] = {
    ???
  }
  
  // TODO: Implement validateEmail
  // Simple email validation (must contain @ and .)
  // Return Left(InvalidEmail) if invalid
  def validateEmail(email: String): Either[InvalidEmail, String] = {
    ???
  }
  
  // TODO: Implement validateUsername
  // Username must be 3-20 characters, alphanumeric only
  // Return Left(InvalidUsername) if invalid
  def validateUsername(username: String): Either[InvalidUsername, String] = {
    ???
  }
  
  // TODO: Implement validateAge
  // Age must be between 13 and 120
  // Return Left(InvalidAge) if invalid
  def validateAge(age: Int): Either[InvalidAge, Int] = {
    ???
  }
  
  // TODO: Implement createAccount
  // Use for-comprehension to validate all fields and create Account
  // Should fail fast: stop at first validation error
  def createAccount(
    username: String,
    email: String,
    ageStr: String
  ): Either[ValidationError, Account] = {
    ???
  }
  
  def main(args: Array[String]): Unit = {
    println("=== Exercise 01: Either ===\n")
    
    // Test parsePositiveInt
    println("--- Testing parsePositiveInt ---")
    // TODO: Uncomment these tests
    /*
    assert(parsePositiveInt("42") == Right(42), "Should parse valid positive int")
    assert(parsePositiveInt("0").isLeft, "Should reject 0")
    assert(parsePositiveInt("-5").isLeft, "Should reject negative")
    assert(parsePositiveInt("abc").isLeft, "Should reject non-numeric")
    println("✓ parsePositiveInt tests passed")
    */
    
    // Test validateEmail
    println("\n--- Testing validateEmail ---")
    // TODO: Uncomment these tests
    /*
    assert(validateEmail("user@example.com") == Right("user@example.com"))
    assert(validateEmail("invalid").isLeft)
    assert(validateEmail("no@domain").isLeft)
    println("✓ validateEmail tests passed")
    */
    
    // Test validateUsername
    println("\n--- Testing validateUsername ---")
    // TODO: Uncomment these tests
    /*
    assert(validateUsername("alice") == Right("alice"))
    assert(validateUsername("ab").isLeft, "Too short")
    assert(validateUsername("a" * 21).isLeft, "Too long")
    assert(validateUsername("user@123").isLeft, "Special chars")
    println("✓ validateUsername tests passed")
    */
    
    // Test validateAge
    println("\n--- Testing validateAge ---")
    // TODO: Uncomment these tests
    /*
    assert(validateAge(25) == Right(25))
    assert(validateAge(12).isLeft, "Too young")
    assert(validateAge(121).isLeft, "Too old")
    println("✓ validateAge tests passed")
    */
    
    // Test createAccount
    println("\n--- Testing createAccount ---")
    // TODO: Uncomment these tests
    /*
    val valid = createAccount("alice", "alice@example.com", "25")
    assert(valid == Right(Account("alice", "alice@example.com", 25)))
    
    val invalidUsername = createAccount("ab", "alice@example.com", "25")
    assert(invalidUsername.isLeft)
    
    val invalidEmail = createAccount("alice", "invalid", "25")
    assert(invalidEmail.isLeft)
    
    val invalidAge = createAccount("alice", "alice@example.com", "12")
    assert(invalidAge.isLeft)
    
    val invalidAgeFormat = createAccount("alice", "alice@example.com", "abc")
    assert(invalidAgeFormat.isLeft)
    
    println("✓ createAccount tests passed")
    */
    
    println("\n=== All tests passed! ===")
  }
}

/*
 * SOLUTION (Don't peek until you've tried!)
 * 
 * def parsePositiveInt(s: String): Either[String, Int] = {
 *   try {
 *     val num = s.toInt
 *     if (num > 0) Right(num)
 *     else Left(s"Number must be positive: $num")
 *   } catch {
 *     case _: NumberFormatException => Left(s"Invalid number: $s")
 *   }
 * }
 * 
 * def validateEmail(email: String): Either[InvalidEmail, String] = {
 *   if (email.contains("@") && email.contains(".")) Right(email)
 *   else Left(InvalidEmail(s"Invalid email format: $email"))
 * }
 * 
 * def validateUsername(username: String): Either[InvalidUsername, String] = {
 *   if (username.length < 3) Left(InvalidUsername("Username too short"))
 *   else if (username.length > 20) Left(InvalidUsername("Username too long"))
 *   else if (!username.forall(_.isLetterOrDigit)) 
 *     Left(InvalidUsername("Username must be alphanumeric"))
 *   else Right(username)
 * }
 * 
 * def validateAge(age: Int): Either[InvalidAge, Int] = {
 *   if (age < 13) Left(InvalidAge("Must be at least 13 years old"))
 *   else if (age > 120) Left(InvalidAge("Invalid age"))
 *   else Right(age)
 * }
 * 
 * def createAccount(
 *   username: String,
 *   email: String,
 *   ageStr: String
 * ): Either[ValidationError, Account] = {
 *   for {
 *     validUsername <- validateUsername(username)
 *     validEmail <- validateEmail(email)
 *     age <- parsePositiveInt(ageStr).left.map(msg => InvalidAge(msg))
 *     validAge <- validateAge(age)
 *   } yield Account(validUsername, validEmail, validAge)
 * }
 */
