package referentialtransparency.exercises.solutions

object Exercise01RTSolution {
  
  // Part 1 Answers:
  // A: Pure
  // B: Impure (modifies global state)
  // C: Impure (println side effect)
  // D: Pure
  // E: Impure (non-deterministic)
  // F: Pure
  
  // Part 2: Refactored functions
  def withdrawPure(currentBalance: Int, amount: Int): (Int, Boolean) = {
    if (currentBalance >= amount) {
      (currentBalance - amount, true)
    } else {
      (currentBalance, false)
    }
  }
  
  def processPure(x: Int): (Int, String) = {
    val result = x * 2
    val log = s"Processing $x"
    (result, log)
  }
  
  def getConfigValuePure(config: String): String = {
    config
  }
  
  // Part 3: Purity test
  def testPurity[A](operation: () => A): Boolean = {
    val result1 = operation()
    val result2 = operation()
    result1 == result2
  }
  
  // Part 4: Pure programs
  def factorial(n: Int): Int = {
    if (n <= 1) 1
    else n * factorial(n - 1)
  }
  
  def isValidEmail(email: String): Boolean = {
    email.contains("@") && email.contains(".")
  }
  
  def sumEvens(numbers: List[Int]): Int = {
    numbers.filter(_ % 2 == 0).sum
  }
  
  def main(args: Array[String]): Unit = {
    println("=== Exercise 01 Solution ===\n")
    
    println("--- Part 2 Tests ---")
    val (newBalance1, success1) = withdrawPure(100, 30)
    println(s"withdrawPure(100, 30) = ($newBalance1, $success1)")
    assert(newBalance1 == 70 && success1 == true)
    
    val (result1, log1) = processPure(5)
    println(s"processPure(5) = ($result1, '$log1')")
    assert(result1 == 10 && log1 == "Processing 5")
    
    println("\n--- Part 4 Tests ---")
    println(s"factorial(5) = ${factorial(5)}")
    assert(factorial(5) == 120)
    
    println(s"isValidEmail('user@example.com') = ${isValidEmail("user@example.com")}")
    assert(isValidEmail("user@example.com") == true)
    
    println(s"sumEvens(List(1,2,3,4,5,6)) = ${sumEvens(List(1,2,3,4,5,6))}")
    assert(sumEvens(List(1,2,3,4,5,6)) == 12)
    
    println("\n✓ All tests passed!")
  }
}
