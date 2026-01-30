package errorhandling

import scala.util.{Try, Success, Failure}

// Try - For handling exceptions in a functional way
// Try[A] is similar to Either[Throwable, A]

object TryExamples {
  
  // ============================================================================
  // TRY BASICS
  // ============================================================================
  
  // Try[A] has two cases:
  // - Success(value): Operation succeeded
  // - Failure(exception): Operation threw an exception
  
  val successTry: Try[Int] = Success(42)
  val failureTry: Try[Int] = Failure(new Exception("Something went wrong"))
  
  // Creating Try from potentially failing code
  val parseTry = Try("42".toInt)  // Success(42)
  val parseFailTry = Try("abc".toInt)  // Failure(NumberFormatException)
  
  // Rust comparison:
  // Rust doesn't have exceptions, but Try is like:
  // Result<T, Box<dyn Error>>
  
  // ============================================================================
  // CREATING TRYS
  // ============================================================================
  
  // Wrapping exception-throwing code
  def parseInt(s: String): Try[Int] = Try(s.toInt)
  
  def divide(a: Int, b: Int): Try[Int] = Try(a / b)
  
  // Reading file (can throw IOException)
  def readFile(path: String): Try[String] = Try {
    scala.io.Source.fromFile(path).mkString
  }
  
  // ============================================================================
  // PATTERN MATCHING
  // ============================================================================
  
  def describeParse(s: String): String = {
    parseInt(s) match {
      case Success(num) => s"Parsed: $num"
      case Failure(ex) => s"Failed: ${ex.getMessage}"
    }
  }
  
  // ============================================================================
  // MAP - TRANSFORMING SUCCESS VALUES
  // ============================================================================
  
  val doubled = successTry.map(_ * 2)  // Success(84)
  val failedDouble = failureTry.map(_ * 2)  // Failure(...)
  
  // Chaining transformations
  val result = parseInt("42")
    .map(_ * 2)      // Success(84)
    .map(_ + 10)     // Success(94)
    .map(_.toString)  // Success("94")
  
  // ============================================================================
  // FLATMAP - CHAINING OPERATIONS THAT RETURN TRY
  // ============================================================================
  
  def parseAndDivide(numStr: String, denomStr: String): Try[Int] = {
    parseInt(numStr).flatMap { num =>
      parseInt(denomStr).flatMap { denom =>
        divide(num, denom)
      }
    }
  }
  
  // With for-comprehension
  def parseAndDivideFor(numStr: String, denomStr: String): Try[Int] = {
    for {
      num <- parseInt(numStr)
      denom <- parseInt(denomStr)
      result <- divide(num, denom)
    } yield result
  }
  
  // ============================================================================
  // RECOVER - HANDLING FAILURES
  // ============================================================================
  
  // recover: provide alternative value on specific exceptions
  val recovered = parseInt("abc").recover {
    case _: NumberFormatException => 0
  }  // Success(0)
  
  // recoverWith: provide alternative Try on specific exceptions
  val recoveredWith = parseInt("abc").recoverWith {
    case _: NumberFormatException => Try("0".toInt)
  }  // Success(0)
  
  // ============================================================================
  // GETORELSE AND ORELSE
  // ============================================================================
  
  val value1 = successTry.getOrElse(0)  // 42
  val value2 = failureTry.getOrElse(0)  // 0
  
  // orElse: try alternative if first fails
  val alternative = parseInt("abc").orElse(parseInt("42"))  // Success(42)
  
  // ============================================================================
  // FOLD - HANDLING BOTH CASES
  // ============================================================================
  
  val result1 = successTry.fold(
    ex => s"Failed: ${ex.getMessage}",
    value => s"Success: $value"
  )
  
  // ============================================================================
  // FILTER - CONDITIONAL SUCCESS
  // ============================================================================
  
  val evenOnly = parseInt("42").filter(_ % 2 == 0)  // Success(42)
  val oddFiltered = parseInt("43").filter(_ % 2 == 0)  
  // Failure(NoSuchElementException)
  
  // ============================================================================
  // CONVERTING TO/FROM OPTION AND EITHER
  // ============================================================================
  
  val tryToOption: Option[Int] = successTry.toOption  // Some(42)
  val failureToOption: Option[Int] = failureTry.toOption  // None
  
  val tryToEither: Either[Throwable, Int] = successTry.toEither  // Right(42)
  val failureToEither: Either[Throwable, Int] = failureTry.toEither  
  // Left(Exception(...))
  
  // Option to Try
  def optionToTry[A](opt: Option[A]): Try[A] = opt match {
    case Some(v) => Success(v)
    case None => Failure(new Exception("No value"))
  }
  
  // ============================================================================
  // PRACTICAL EXAMPLE: CONFIGURATION LOADING
  // ============================================================================
  
  case class Config(host: String, port: Int, timeout: Int)
  
  def readConfigValue(key: String): Try[String] = Try {
    // Simulate reading from config file or environment
    key match {
      case "host" => "localhost"
      case "port" => "8080"
      case "timeout" => "30"
      case _ => throw new Exception(s"Key not found: $key")
    }
  }
  
  def loadConfig(): Try[Config] = {
    for {
      host <- readConfigValue("host")
      portStr <- readConfigValue("port")
      port <- Try(portStr.toInt)
      timeoutStr <- readConfigValue("timeout")
      timeout <- Try(timeoutStr.toInt)
    } yield Config(host, port, timeout)
  }
  
  // ============================================================================
  // PRACTICAL EXAMPLE: DATABASE OPERATIONS
  // ============================================================================
  
  case class User(id: Int, name: String)
  
  // Simulated database operations
  def findUser(id: Int): Try[User] = Try {
    if (id > 0 && id <= 3) User(id, s"User$id")
    else throw new Exception(s"User not found: $id")
  }
  
  def updateUser(user: User): Try[User] = Try {
    // Simulate database update that might fail
    if (user.name.nonEmpty) user
    else throw new Exception("Invalid user data")
  }
  
  def getUserAndUpdate(id: Int, newName: String): Try[User] = {
    for {
      user <- findUser(id)
      updated = user.copy(name = newName)
      result <- updateUser(updated)
    } yield result
  }
  
  // ============================================================================
  // WHEN TO USE TRY VS EITHER
  // ============================================================================
  
  // Use Try when:
  // - Working with Java libraries that throw exceptions
  // - Exception type doesn't matter (just success/failure)
  // - Quick prototyping
  
  // Use Either when:
  // - Need specific error types (not just Throwable)
  // - Building APIs with clear error cases
  // - Better type safety for errors
  
  // Example: Try for Java interop
  def parseIntJava(s: String): Try[Int] = Try {
    Integer.parseInt(s)  // Java method that throws
  }
  
  // Example: Either for typed errors
  sealed trait ParseError
  case class InvalidFormat(input: String) extends ParseError
  case class OutOfRange(input: String) extends ParseError
  
  def parseIntTyped(s: String): Either[ParseError, Int] = {
    try {
      val num = s.toInt
      if (num >= Int.MinValue && num <= Int.MaxValue) Right(num)
      else Left(OutOfRange(s))
    } catch {
      case _: NumberFormatException => Left(InvalidFormat(s))
    }
  }
  
  // ============================================================================
  // DEMO FUNCTION
  // ============================================================================
  
  def main(args: Array[String]): Unit = {
    println("=== Try Examples ===\n")
    
    // Basic Try
    println("--- Basic Try ---")
    println(s"successTry = $successTry")
    println(s"failureTry = $failureTry")
    
    // Parsing
    println("\n--- Parsing ---")
    println(describeParse("42"))
    println(describeParse("abc"))
    
    // Map
    println("\n--- Map ---")
    println(s"result = $result")
    
    // Parse and divide
    println("\n--- Parse and Divide ---")
    println(s"parseAndDivide('20', '4') = ${parseAndDivide("20", "4")}")
    println(s"parseAndDivide('20', '0') = ${parseAndDivide("20", "0")}")
    println(s"parseAndDivide('abc', '4') = ${parseAndDivide("abc", "4")}")
    
    // Recover
    println("\n--- Recover ---")
    println(s"recovered = $recovered")
    
    // Config loading
    println("\n--- Config Loading ---")
    println(s"loadConfig() = ${loadConfig()}")
    
    // Database operations
    println("\n--- Database Operations ---")
    println(s"findUser(1) = ${findUser(1)}")
    println(s"findUser(999) = ${findUser(999)}")
    println(s"getUserAndUpdate(1, 'Alice') = ${getUserAndUpdate(1, "Alice")}")
    
    // Conversions
    println("\n--- Conversions ---")
    println(s"successTry.toOption = $tryToOption")
    println(s"successTry.toEither = $tryToEither")
  }
}
