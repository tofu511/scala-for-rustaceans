package fundamentals.exercises

/**
 * Exercise 01: Functions and Lambdas
 * 
 * OBJECTIVES:
 * - Practice writing recursive functions
 * - Work with higher-order functions
 * - Use lambda expressions
 * 
 * TASKS:
 * 1. Implement factorial function (recursively)
 * 2. Implement applyNTimes higher-order function
 * 3. Use it to compute powers of 2
 * 
 * HOW TO RUN:
 * 1. Fill in the ??? parts with your implementation
 * 2. Run: sbt "runMain fundamentals.exercises.Exercise01"
 * 3. Verify the output matches expected results
 * 
 * EXPECTED OUTPUT:
 * factorial(5) = 120
 * factorial(10) = 3628800
 * applyNTimes(double, 3, 1) = 8
 * 2^8 = 256
 * 2^10 = 1024
 */
object Exercise01 extends App {
  
  // TODO: Implement factorial function
  // Hint: Use recursion
  // Base case: factorial(0) = 1, factorial(1) = 1
  // Recursive case: factorial(n) = n * factorial(n - 1)
  def factorial(n: Int): Int = {
    ???  // Replace ??? with your implementation
  }
  
  // TODO: Implement applyNTimes
  // This function should apply function f to x, n times
  // Example: applyNTimes(double, 3, 1) = double(double(double(1))) = 8
  // Hint: Use recursion
  // Base case: when n = 0, return x
  // Recursive case: applyNTimes(f, n-1, f(x))
  def applyNTimes(f: Int => Int, n: Int, x: Int): Int = {
    ???  // Replace ??? with your implementation
  }
  
  // Helper function for testing
  val double: Int => Int = _ * 2
  
  // Test cases - uncomment after implementing the functions
  println("=== Exercise 01: Functions and Lambdas ===\n")
  
  // Test factorial
  // println(s"factorial(5) = ${factorial(5)}")      // Should be 120
  // println(s"factorial(10) = ${factorial(10)}")    // Should be 3628800
  
  // Test applyNTimes
  // println(s"\napplyNTimes(double, 3, 1) = ${applyNTimes(double, 3, 1)}")  // Should be 8
  
  // Calculate powers of 2
  // println(s"\n2^8 = ${applyNTimes(double, 8, 1)}")   // Should be 256
  // println(s"2^10 = ${applyNTimes(double, 10, 1)}")  // Should be 1024
  
  println("\nIf you see this message without errors, uncomment the test cases above!")
}

/**
 * SOLUTION (Don't peek until you've tried!)
 * 
 * def factorial(n: Int): Int = {
 *   if (n <= 1) 1
 *   else n * factorial(n - 1)
 * }
 * 
 * def applyNTimes(f: Int => Int, n: Int, x: Int): Int = {
 *   if (n == 0) x
 *   else applyNTimes(f, n - 1, f(x))
 * }
 * 
 * RUST COMPARISON:
 * 
 * fn factorial(n: i32) -> i32 {
 *     if n <= 1 { 1 }
 *     else { n * factorial(n - 1) }
 * }
 * 
 * fn apply_n_times<F>(f: F, n: i32, x: i32) -> i32 
 * where F: Fn(i32) -> i32 {
 *     if n == 0 { x }
 *     else { apply_n_times(f, n - 1, f(x)) }
 * }
 */
