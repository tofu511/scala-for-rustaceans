package fundamentals.exercises.solutions

/**
 * SOLUTION for Exercise 01: Functions and Lambdas
 * 
 * HOW TO RUN:
 * sbt "runMain fundamentals.exercises.solutions.Exercise01Solution"
 */
object Exercise01Solution extends App {
  
  def factorial(n: Int): Int = {
    if (n <= 1) 1
    else n * factorial(n - 1)
  }
  
  def applyNTimes(f: Int => Int, n: Int, x: Int): Int = {
    if (n == 0) x
    else applyNTimes(f, n - 1, f(x))
  }
  
  val double: Int => Int = _ * 2
  
  println("=== Exercise 01: Functions and Lambdas (SOLUTION) ===\n")
  
  println(s"factorial(5) = ${factorial(5)}")      // 120
  println(s"factorial(10) = ${factorial(10)}")    // 3628800
  
  println(s"\napplyNTimes(double, 3, 1) = ${applyNTimes(double, 3, 1)}")  // 8
  
  println(s"\n2^8 = ${applyNTimes(double, 8, 1)}")   // 256
  println(s"2^10 = ${applyNTimes(double, 10, 1)}")  // 1024
}
