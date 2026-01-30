package fundamentals.exercises

/**
 * Exercise 03: Working with Options
 * 
 * OBJECTIVES:
 * - Practice safe operations with Option
 * - Use map, flatMap, filter
 * - Chain optional operations
 * 
 * TASKS:
 * 1. Implement safeDivide that returns Option
 * 2. Implement divideEven that returns even results only
 * 3. Implement parseAndDivide that parses string and divides
 * 4. Implement getUserEmail with nested Options
 * 
 * HOW TO RUN:
 * 1. Fill in the ??? parts with your implementation
 * 2. Run: sbt "runMain fundamentals.exercises.Exercise03"
 * 3. Verify the output matches expected results
 * 
 * EXPECTED OUTPUT:
 * safeDivide(10, 2) = Some(5)
 * safeDivide(10, 0) = None
 * 
 * divideEven(20, 5) = Some(4)
 * divideEven(20, 6) = None
 * divideEven(20, 0) = None
 * 
 * parseAndDivide("20", 4) = Some(5)
 * parseAndDivide("20", 0) = None
 * parseAndDivide("abc", 4) = None
 * 
 * User 1 email: alice@example.com
 * User 2 email: no-email@example.com
 * User 999 email: no-email@example.com
 */
object Exercise03 extends App {
  
  // TODO: Implement safeDivide
  // Return Some(result) if b != 0, None if b == 0
  def safeDivide(a: Int, b: Int): Option[Int] = {
    ???  // Replace ??? with your implementation
  }
  
  // TODO: Implement divideEven
  // Return Some(result) if:
  //   1. Division succeeds (b != 0), AND
  //   2. Result is even
  // Otherwise return None
  // Hint: Use safeDivide and filter
  def divideEven(a: Int, b: Int): Option[Int] = {
    ???  // Replace ??? with your implementation
  }
  
  // Helper function for parsing integers
  def parseInt(s: String): Option[Int] = {
    try {
      Some(s.toInt)
    } catch {
      case _: NumberFormatException => None
    }
  }
  
  // TODO: Implement parseAndDivide
  // Parse string s to Int, then divide by divisor
  // Return None if parsing fails OR division fails
  // Hint: Use for-comprehension or flatMap
  def parseAndDivide(s: String, divisor: Int): Option[Int] = {
    ???  // Replace ??? with your implementation
  }
  
  // User data for testing
  case class User(id: Int, name: String, email: Option[String])
  
  val users = List(
    User(1, "Alice", Some("alice@example.com")),
    User(2, "Bob", None),
    User(3, "Carol", Some("carol@example.com"))
  )
  
  def findUserById(id: Int): Option[User] = {
    users.find(_.id == id)
  }
  
  // TODO: Implement getUserEmail
  // Find user by ID and return their email, or default email
  // Hint: Use flatMap to handle nested Options
  // Steps:
  //   1. Find user by ID (returns Option[User])
  //   2. Get their email (returns Option[String])
  //   3. Use getOrElse to provide default
  def getUserEmail(id: Int): String = {
    ???  // Replace ??? with your implementation
  }
  
  // Test cases - uncomment after implementing
  println("=== Exercise 03: Working with Options ===\n")
  
  // Test safeDivide
  println("--- safeDivide ---")
  // println(s"safeDivide(10, 2) = ${safeDivide(10, 2)}")
  // println(s"safeDivide(10, 0) = ${safeDivide(10, 0)}")
  
  // Test divideEven
  // println("\n--- divideEven ---")
  // println(s"divideEven(20, 5) = ${divideEven(20, 5)}")    // 20/5 = 4 (even) -> Some(4)
  // println(s"divideEven(20, 6) = ${divideEven(20, 6)}")    // 20/6 = 3 (odd) -> None
  // println(s"divideEven(20, 0) = ${divideEven(20, 0)}")    // division by zero -> None
  
  // Test parseAndDivide
  // println("\n--- parseAndDivide ---")
  // println(s"parseAndDivide(\"20\", 4) = ${parseAndDivide("20", 4)}")
  // println(s"parseAndDivide(\"20\", 0) = ${parseAndDivide("20", 0)}")
  // println(s"parseAndDivide(\"abc\", 4) = ${parseAndDivide("abc", 4)}")
  
  // Test getUserEmail
  // println("\n--- getUserEmail ---")
  // println(s"User 1 email: ${getUserEmail(1)}")
  // println(s"User 2 email: ${getUserEmail(2)}")
  // println(s"User 999 email: ${getUserEmail(999)}")
  
  println("\nIf you see this message without errors, uncomment the test cases above!")
}

/**
 * SOLUTION (Don't peek until you've tried!)
 * 
 * def safeDivide(a: Int, b: Int): Option[Int] = {
 *   if (b == 0) None
 *   else Some(a / b)
 * }
 * 
 * def divideEven(a: Int, b: Int): Option[Int] = {
 *   safeDivide(a, b).filter(_ % 2 == 0)
 *   // Or with for-comprehension:
 *   // for {
 *   //   result <- safeDivide(a, b)
 *   //   if result % 2 == 0
 *   // } yield result
 * }
 * 
 * def parseAndDivide(s: String, divisor: Int): Option[Int] = {
 *   parseInt(s).flatMap(n => safeDivide(n, divisor))
 *   // Or with for-comprehension:
 *   // for {
 *   //   n <- parseInt(s)
 *   //   result <- safeDivide(n, divisor)
 *   // } yield result
 * }
 * 
 * def getUserEmail(id: Int): String = {
 *   findUserById(id)
 *     .flatMap(_.email)
 *     .getOrElse("no-email@example.com")
 * }
 * 
 * RUST COMPARISON:
 * 
 * fn safe_divide(a: i32, b: i32) -> Option<i32> {
 *     if b == 0 { None }
 *     else { Some(a / b) }
 * }
 * 
 * fn divide_even(a: i32, b: i32) -> Option<i32> {
 *     safe_divide(a, b).filter(|x| x % 2 == 0)
 * }
 * 
 * fn parse_and_divide(s: &str, divisor: i32) -> Option<i32> {
 *     s.parse::<i32>().ok()
 *         .and_then(|n| safe_divide(n, divisor))
 * }
 * 
 * fn get_user_email(id: i32) -> String {
 *     find_user_by_id(id)
 *         .and_then(|user| user.email)
 *         .unwrap_or_else(|| "no-email@example.com".to_string())
 * }
 */
