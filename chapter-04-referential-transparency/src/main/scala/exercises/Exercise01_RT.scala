package referentialtransparency.exercises

/*
 * EXERCISE 01: Identifying Referential Transparency
 *
 * OBJECTIVES:
 * - Learn to identify pure vs impure functions
 * - Practice the substitution test
 * - Understand why RT matters
 *
 * RUST COMPARISON:
 * Rust also values pure functions, though the language doesn't enforce purity.
 * Safe Rust prevents many side effects (like data races) at compile time.
 * Scala uses FP patterns to achieve similar safety at the design level.
 *
 * TASKS:
 * 1. Implement isPure function to test if a computation is RT
 * 2. Identify which functions are pure and which are impure
 * 3. Refactor impure functions to be pure
 * 4. Uncomment tests in main() to verify your understanding
 *
 * HOW TO RUN:
 *   cd chapter-04-referential-transparency
 *   sbt "runMain referentialtransparency.exercises.Exercise01RT"
 *
 * EXPECTED OUTPUT:
 * All assertions should pass, demonstrating understanding of RT.
 */

object Exercise01RT {
  
  // ============================================================================
  // PART 1: IDENTIFY PURE FUNCTIONS
  // ============================================================================
  
  // TODO: For each function below, determine if it's pure or impure
  // Mark your answer in the comments
  
  // Function A
  def functionA(x: Int): Int = {
    x * 2 + 10
  }
  // Is this pure? YES / NO (choose one)
  
  // Function B
  var globalState = 0
  def functionB(x: Int): Int = {
    globalState += x
    globalState
  }
  // Is this pure? YES / NO (choose one)
  
  // Function C
  def functionC(x: Int): Int = {
    println(s"Computing: $x")
    x * 2
  }
  // Is this pure? YES / NO (choose one)
  
  // Function D
  def functionD(x: List[Int]): Int = {
    x.sum
  }
  // Is this pure? YES / NO (choose one)
  
  // Function E
  def functionE(): Int = {
    scala.util.Random.nextInt(100)
  }
  // Is this pure? YES / NO (choose one)
  
  // Function F
  def functionF(x: Int, y: Int): Option[Int] = {
    if (y == 0) None
    else Some(x / y)
  }
  // Is this pure? YES / NO (choose one)
  
  // ============================================================================
  // PART 2: REFACTOR TO PURE
  // ============================================================================
  
  // TODO: Refactor this impure function to be pure
  // Hint: Instead of modifying state, return the new state
  var balance = 0
  def withdraw(amount: Int): Boolean = {
    if (balance >= amount) {
      balance -= amount
      true
    } else {
      false
    }
  }
  
  // Pure version:
  def withdrawPure(currentBalance: Int, amount: Int): (Int, Boolean) = {
    ???
  }
  
  // TODO: Refactor this impure function to be pure
  // Hint: Return the log message along with the result
  def processImpure(x: Int): Int = {
    println(s"Processing $x")
    x * 2
  }
  
  // Pure version:
  def processPure(x: Int): (Int, String) = {
    ???
  }
  
  // TODO: Refactor this impure function to be pure
  // Hint: Take configuration as a parameter
  var config = "default"
  def getConfigValue(): String = config
  
  // Pure version:
  def getConfigValuePure(config: String): String = {
    ???
  }
  
  // ============================================================================
  // PART 3: SUBSTITUTION TEST
  // ============================================================================
  
  // TODO: Implement a function to test if an operation is pure
  // It should run the operation twice and check if results are the same
  def testPurity[A](operation: () => A): Boolean = {
    ???
    // Hint: Run operation twice and compare results
    // Warning: This only tests determinism, not full purity!
  }
  
  // ============================================================================
  // PART 4: BUILD PURE PROGRAM
  // ============================================================================
  
  // TODO: Implement a pure function that computes factorial
  def factorial(n: Int): Int = {
    ???
  }
  
  // TODO: Implement a pure function that validates an email
  // Return true if it contains @ and .
  def isValidEmail(email: String): Boolean = {
    ???
  }
  
  // TODO: Implement a pure function that computes the sum of even numbers
  def sumEvens(numbers: List[Int]): Int = {
    ???
  }
  
  def main(args: Array[String]): Unit = {
    println("=== Exercise 01: Referential Transparency ===\n")
    
    // Part 1: Identify
    println("--- Part 1: Identify Pure Functions ---")
    println("Check your answers in the code comments")
    // Answers:
    // A: Pure (no side effects, deterministic)
    // B: Impure (modifies global state)
    // C: Impure (println is a side effect)
    // D: Pure (no side effects, deterministic)
    // E: Impure (non-deterministic)
    // F: Pure (deterministic, no side effects)
    
    // Part 2: Test refactored functions
    println("\n--- Part 2: Refactored Pure Functions ---")
    // TODO: Uncomment these tests
    /*
    val (newBalance1, success1) = withdrawPure(100, 30)
    assert(newBalance1 == 70 && success1 == true)
    
    val (newBalance2, success2) = withdrawPure(100, 150)
    assert(newBalance2 == 100 && success2 == false)
    
    val (result1, log1) = processPure(5)
    assert(result1 == 10 && log1 == "Processing 5")
    
    val configValue = getConfigValuePure("production")
    assert(configValue == "production")
    
    println("✓ All refactored functions are pure!")
    */
    
    // Part 3: Purity test
    println("\n--- Part 3: Purity Test ---")
    // TODO: Uncomment these tests
    /*
    val pureOp = () => 2 + 3
    val impureOp = () => scala.util.Random.nextInt(100)
    
    assert(testPurity(pureOp) == true, "Pure operation should pass")
    // Note: impureOp might occasionally pass by chance!
    println("✓ Purity test implemented!")
    */
    
    // Part 4: Pure programs
    println("\n--- Part 4: Pure Programs ---")
    // TODO: Uncomment these tests
    /*
    assert(factorial(0) == 1)
    assert(factorial(5) == 120)
    assert(factorial(3) == 6)
    
    assert(isValidEmail("user@example.com") == true)
    assert(isValidEmail("invalid") == false)
    assert(isValidEmail("no@domain") == false)
    
    assert(sumEvens(List(1, 2, 3, 4, 5, 6)) == 12)
    assert(sumEvens(List(1, 3, 5)) == 0)
    assert(sumEvens(List()) == 0)
    
    println("✓ All pure programs work correctly!")
    */
    
    println("\n=== Complete the exercises by uncommenting tests ===")
  }
}

/*
 * SOLUTION (Don't peek until you've tried!)
 *
 * def withdrawPure(currentBalance: Int, amount: Int): (Int, Boolean) = {
 *   if (currentBalance >= amount) {
 *     (currentBalance - amount, true)
 *   } else {
 *     (currentBalance, false)
 *   }
 * }
 *
 * def processPure(x: Int): (Int, String) = {
 *   val result = x * 2
 *   val log = s"Processing $x"
 *   (result, log)
 * }
 *
 * def getConfigValuePure(config: String): String = {
 *   config
 * }
 *
 * def testPurity[A](operation: () => A): Boolean = {
 *   val result1 = operation()
 *   val result2 = operation()
 *   result1 == result2
 * }
 *
 * def factorial(n: Int): Int = {
 *   if (n <= 1) 1
 *   else n * factorial(n - 1)
 * }
 *
 * def isValidEmail(email: String): Boolean = {
 *   email.contains("@") && email.contains(".")
 * }
 *
 * def sumEvens(numbers: List[Int]): Int = {
 *   numbers.filter(_ % 2 == 0).sum
 * }
 */
