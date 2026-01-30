package errorhandling.exercises.solutions

import scala.util.{Try, Success, Failure}

case class ConfigValue(key: String, value: String)

object Exercise02TrySolution {
  
  def safeDivide(a: Double, b: Double): Try[Double] = {
    Try(a / b)
  }
  
  def parseConfigLine(line: String): Try[ConfigValue] = Try {
    val parts = line.split("=", 2)
    if (parts.length != 2) throw new Exception("Invalid config line")
    ConfigValue(parts(0), parts(1))
  }
  
  def computeAverage(numbers: List[String]): Try[Double] = Try {
    val nums = numbers.map(_.toDouble)
    nums.sum / nums.length
  }
  
  def safeParseInt(s: String, default: Int): Int = {
    Try(s.toInt).getOrElse(default)
  }
  
  def chainOperations(numStr: String, divisor: Int): Try[Double] = {
    for {
      num <- Try(numStr.toInt)
      doubled = num * 2
      result <- safeDivide(doubled, divisor)
    } yield result
  }
  
  def main(args: Array[String]): Unit = {
    println("=== Exercise 02 Solution ===\n")
    
    println("--- safeDivide ---")
    println(s"safeDivide(10, 2) = ${safeDivide(10, 2)}")
    println(s"safeDivide(10, 0) = ${safeDivide(10, 0)}")
    
    println("\n--- parseConfigLine ---")
    println(s"parseConfigLine('host=localhost') = ${parseConfigLine("host=localhost")}")
    println(s"parseConfigLine('invalid') = ${parseConfigLine("invalid")}")
    
    println("\n--- computeAverage ---")
    println(s"computeAverage(List('1', '2', '3')) = ${computeAverage(List("1", "2", "3"))}")
    println(s"computeAverage(List()) = ${computeAverage(List())}")
    
    println("\n--- safeParseInt ---")
    println(s"safeParseInt('42', 0) = ${safeParseInt("42", 0)}")
    println(s"safeParseInt('abc', 0) = ${safeParseInt("abc", 0)}")
    
    println("\n--- chainOperations ---")
    println(s"chainOperations('10', 2) = ${chainOperations("10", 2)}")
    println(s"chainOperations('abc', 2) = ${chainOperations("abc", 2)}")
  }
}
