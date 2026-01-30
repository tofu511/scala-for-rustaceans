package errorhandling

// Either - Scala's equivalent to Rust's Result<T, E>
// Either[Left, Right] where Right is success, Left is error

object EitherExamples {
  
  // ============================================================================
  // EITHER BASICS
  // ============================================================================
  
  // Either[L, R] is right-biased (Right = success, Left = error)
  val success: Either[String, Int] = Right(42)
  val failure: Either[String, Int] = Left("Something went wrong")
  
  // Rust comparison:
  // Rust: Result<i32, String> = Ok(42)  or  Err("Something went wrong")
  // Scala: Either[String, Int] = Right(42)  or  Left("Something went wrong")
  
  // ============================================================================
  // CREATING EITHERS
  // ============================================================================
  
  // Safe division returning Either
  def divide(a: Int, b: Int): Either[String, Int] = {
    if (b == 0) {
      Left("Division by zero")
    } else {
      Right(a / b)
    }
  }
  
  // Parsing with Either
  def parseInt(s: String): Either[String, Int] = {
    try {
      Right(s.toInt)
    } catch {
      case e: NumberFormatException => Left(s"Invalid number: $s")
    }
  }
  
  // Validation example
  case class User(name: String, age: Int)
  
  def validateName(name: String): Either[String, String] = {
    if (name.trim.isEmpty) Left("Name cannot be empty")
    else if (name.length < 2) Left("Name too short")
    else Right(name)
  }
  
  def validateAge(age: Int): Either[String, Int] = {
    if (age < 0) Left("Age cannot be negative")
    else if (age > 150) Left("Age too high")
    else Right(age)
  }
  
  // ============================================================================
  // PATTERN MATCHING
  // ============================================================================
  
  def describeDivision(a: Int, b: Int): String = {
    divide(a, b) match {
      case Right(result) => s"$a / $b = $result"
      case Left(error) => s"Error: $error"
    }
  }
  
  // Rust comparison:
  // match divide(a, b) {
  //     Ok(result) => format!("{} / {} = {}", a, b, result),
  //     Err(error) => format!("Error: {}", error),
  // }
  
  // ============================================================================
  // MAP - TRANSFORMING SUCCESS VALUES
  // ============================================================================
  
  val doubled = success.map(_ * 2)  // Right(84)
  val failedDouble = failure.map(_ * 2)  // Left("Something went wrong")
  
  // map only applies to Right (success) values
  val divisionResult = divide(10, 2).map(_ + 5)  // Right(10)
  
  // Rust comparison:
  // result.map(|x| x * 2)
  
  // ============================================================================
  // FLATMAP - CHAINING OPERATIONS
  // ============================================================================
  
  def parseAndDivide(numeratorStr: String, denominatorStr: String): Either[String, Int] = {
    parseInt(numeratorStr).flatMap { num =>
      parseInt(denominatorStr).flatMap { denom =>
        divide(num, denom)
      }
    }
  }
  
  // With for-comprehension (cleaner!)
  def parseAndDivideFor(numeratorStr: String, denominatorStr: String): Either[String, Int] = {
    for {
      num <- parseInt(numeratorStr)
      denom <- parseInt(denominatorStr)
      result <- divide(num, denom)
    } yield result
  }
  
  // Rust comparison:
  // fn parse_and_divide(num_str: &str, denom_str: &str) -> Result<i32, String> {
  //     let num = parse_int(num_str)?;
  //     let denom = parse_int(denom_str)?;
  //     divide(num, denom)
  // }
  
  // ============================================================================
  // FOLD - HANDLING BOTH CASES
  // ============================================================================
  
  val result1 = success.fold(
    error => s"Failed with: $error",
    value => s"Succeeded with: $value"
  )  // "Succeeded with: 42"
  
  val result2 = failure.fold(
    error => s"Failed with: $error",
    value => s"Succeeded with: $value"
  )  // "Failed with: Something went wrong"
  
  // ============================================================================
  // GETORSLSE - PROVIDING DEFAULT
  // ============================================================================
  
  val value1 = success.getOrElse(0)  // 42
  val value2 = failure.getOrElse(0)  // 0
  
  // Rust comparison:
  // result.unwrap_or(0)
  
  // ============================================================================
  // ACCUMULATING ERRORS
  // ============================================================================
  
  // With for-comprehension, first error stops execution
  def createUser(name: String, age: Int): Either[String, User] = {
    for {
      validName <- validateName(name)
      validAge <- validateAge(age)
    } yield User(validName, validAge)
  }
  
  // Better approach with Validated (shown in Cats chapter)
  
  // ============================================================================
  // CONVERTING BETWEEN OPTION AND EITHER
  // ============================================================================
  
  val opt: Option[Int] = Some(42)
  val either1: Either[String, Int] = opt.toRight("No value")  // Right(42)
  
  val noneOpt: Option[Int] = None
  val either2: Either[String, Int] = noneOpt.toRight("No value")  // Left("No value")
  
  val eitherToOpt: Option[Int] = success.toOption  // Some(42)
  val failureToOpt: Option[Int] = failure.toOption  // None
  
  // ============================================================================
  // LEFT AND RIGHT PROJECTIONS
  // ============================================================================
  
  // Working with Left values (less common)
  val leftMapped = failure.left.map(_.toUpperCase)  
  // Left("SOMETHING WENT WRONG")
  
  // ============================================================================
  // PRACTICAL EXAMPLE: USER REGISTRATION
  // ============================================================================
  
  case class Email(value: String)
  case class Password(value: String)
  case class RegisteredUser(name: String, email: Email, age: Int)
  
  def validateEmail(email: String): Either[String, Email] = {
    if (email.contains("@")) Right(Email(email))
    else Left("Invalid email format")
  }
  
  def validatePassword(password: String): Either[String, Password] = {
    if (password.length >= 8) Right(Password(password))
    else Left("Password must be at least 8 characters")
  }
  
  def registerUser(
    name: String, 
    email: String, 
    password: String, 
    age: Int
  ): Either[String, RegisteredUser] = {
    for {
      validName <- validateName(name)
      validEmail <- validateEmail(email)
      validPassword <- validatePassword(password)
      validAge <- validateAge(age)
    } yield RegisteredUser(validName, validEmail, validAge)
  }
  
  // ============================================================================
  // DEMO FUNCTION
  // ============================================================================
  
  def main(args: Array[String]): Unit = {
    println("=== Either Examples ===\n")
    
    // Basic Either
    println("--- Basic Either ---")
    println(s"success = $success")
    println(s"failure = $failure")
    
    // Division
    println("\n--- Division ---")
    println(describeDivision(10, 2))
    println(describeDivision(10, 0))
    
    // Parsing and chaining
    println("\n--- Parse and Divide ---")
    println(s"parseAndDivide('20', '4') = ${parseAndDivide("20", "4")}")
    println(s"parseAndDivide('20', '0') = ${parseAndDivide("20", "0")}")
    println(s"parseAndDivide('abc', '4') = ${parseAndDivide("abc", "4")}")
    
    // Map
    println("\n--- Map ---")
    println(s"success.map(_ * 2) = $doubled")
    println(s"failure.map(_ * 2) = $failedDouble")
    
    // Fold
    println("\n--- Fold ---")
    println(result1)
    println(result2)
    
    // User creation
    println("\n--- User Creation ---")
    println(s"createUser('Alice', 30) = ${createUser("Alice", 30)}")
    println(s"createUser('A', 30) = ${createUser("A", 30)}")
    println(s"createUser('Alice', -5) = ${createUser("Alice", -5)}")
    
    // User registration
    println("\n--- User Registration ---")
    println(registerUser("Alice", "alice@example.com", "password123", 30))
    println(registerUser("Bob", "invalid-email", "password123", 25))
    println(registerUser("Carol", "carol@example.com", "short", 28))
  }
}
