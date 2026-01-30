package errorhandling.exercises

import scala.util.{Try, Success, Failure}
import scala.io.Source

/*
 * EXERCISE 02: Try - Exception Handling
 *
 * OBJECTIVES:
 * - Practice using Try for exception handling
 * - Chain Try operations with map and flatMap
 * - Convert between Try, Option, and Either
 * - Handle file I/O and parsing errors gracefully
 *
 * RUST COMPARISON:
 * Rust doesn't have exceptions, so this pattern is less common.
 * Try is useful when interoperating with Java libraries that throw exceptions.
 * Similar to catching panics with std::panic::catch_unwind(), but Try is for
 * regular control flow, not panic recovery.
 *
 * TASKS:
 * 1. Implement safeDivide: division with Try
 * 2. Implement parseConfig: parse key=value format
 * 3. Implement computeAverage: parse numbers and compute average
 * 4. Uncomment tests in main() to verify your implementation
 *
 * HOW TO RUN:
 *   cd chapter-03-error-handling
 *   sbt "runMain errorhandling.exercises.Exercise02Try"
 *
 * EXPECTED OUTPUT:
 * All test assertions should pass without errors.
 * You should see success for valid inputs and failures for invalid inputs.
 */

case class ConfigValue(key: String, value: String)

object Exercise02Try {
  
  // TODO: Implement safeDivide
  // Divide two numbers safely, returning Try[Double]
  // Should handle division by zero
  def safeDivide(a: Double, b: Double): Try[Double] = {
    ???
  }
  
  // TODO: Implement parseConfigLine
  // Parse a line in format "key=value"
  // Return Try[ConfigValue]
  // Should fail if line doesn't contain '='
  def parseConfigLine(line: String): Try[ConfigValue] = {
    ???
  }
  
  // TODO: Implement computeAverage
  // Given a list of number strings, parse and compute average
  // Return Try[Double]
  // Should fail if any string is not a valid number
  // Should fail if list is empty (division by zero)
  def computeAverage(numbers: List[String]): Try[Double] = {
    ???
  }
  
  // TODO: Implement safeParseInt
  // Parse string to Int, with recovery
  // If parsing fails, return the default value
  def safeParseInt(s: String, default: Int): Int = {
    ???
  }
  
  // TODO: Implement chainOperations
  // Parse string to Int, multiply by 2, divide by divisor
  // Use for-comprehension to chain operations
  // Return Try[Double]
  def chainOperations(numStr: String, divisor: Int): Try[Double] = {
    ???
  }
  
  def main(args: Array[String]): Unit = {
    println("=== Exercise 02: Try ===\n")
    
    // Test safeDivide
    println("--- Testing safeDivide ---")
    // TODO: Uncomment these tests
    /*
    assert(safeDivide(10, 2) == Success(5.0))
    assert(safeDivide(10, 0).isFailure)
    assert(safeDivide(7, 2).map(_.round) == Success(4L))
    println("✓ safeDivide tests passed")
    */
    
    // Test parseConfigLine
    println("\n--- Testing parseConfigLine ---")
    // TODO: Uncomment these tests
    /*
    assert(parseConfigLine("host=localhost") == Success(ConfigValue("host", "localhost")))
    assert(parseConfigLine("port=8080") == Success(ConfigValue("port", "8080")))
    assert(parseConfigLine("invalid").isFailure)
    assert(parseConfigLine("").isFailure)
    println("✓ parseConfigLine tests passed")
    */
    
    // Test computeAverage
    println("\n--- Testing computeAverage ---")
    // TODO: Uncomment these tests
    /*
    assert(computeAverage(List("1", "2", "3")) == Success(2.0))
    assert(computeAverage(List("10", "20", "30")).map(_.toInt) == Success(20))
    assert(computeAverage(List()).isFailure)
    assert(computeAverage(List("1", "abc", "3")).isFailure)
    println("✓ computeAverage tests passed")
    */
    
    // Test safeParseInt
    println("\n--- Testing safeParseInt ---")
    // TODO: Uncomment these tests
    /*
    assert(safeParseInt("42", 0) == 42)
    assert(safeParseInt("abc", 0) == 0)
    assert(safeParseInt("100", 50) == 100)
    println("✓ safeParseInt tests passed")
    */
    
    // Test chainOperations
    println("\n--- Testing chainOperations ---")
    // TODO: Uncomment these tests
    /*
    assert(chainOperations("10", 2) == Success(10.0))  // 10 * 2 / 2 = 10
    assert(chainOperations("5", 1) == Success(10.0))   // 5 * 2 / 1 = 10
    assert(chainOperations("abc", 2).isFailure)
    assert(chainOperations("10", 0).isFailure)
    println("✓ chainOperations tests passed")
    */
    
    println("\n=== All tests passed! ===")
  }
}

/*
 * SOLUTION (Don't peek until you've tried!)
 * 
 * def safeDivide(a: Double, b: Double): Try[Double] = {
 *   Try(a / b)
 * }
 * 
 * def parseConfigLine(line: String): Try[ConfigValue] = Try {
 *   val parts = line.split("=", 2)
 *   if (parts.length != 2) throw new Exception("Invalid config line")
 *   ConfigValue(parts(0), parts(1))
 * }
 * 
 * def computeAverage(numbers: List[String]): Try[Double] = Try {
 *   val nums = numbers.map(_.toDouble)
 *   nums.sum / nums.length
 * }
 * 
 * def safeParseInt(s: String, default: Int): Int = {
 *   Try(s.toInt).getOrElse(default)
 * }
 * 
 * def chainOperations(numStr: String, divisor: Int): Try[Double] = {
 *   for {
 *     num <- Try(numStr.toInt)
 *     doubled = num * 2
 *     result <- safeDivide(doubled, divisor)
 *   } yield result
 * }
 */
