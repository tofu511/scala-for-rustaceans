package fundamentals.exercises.solutions

/**
 * SOLUTION for Exercise 03: Working with Options
 * 
 * HOW TO RUN:
 * sbt "runMain fundamentals.exercises.solutions.Exercise03Solution"
 */
object Exercise03Solution extends App {
  
  def safeDivide(a: Int, b: Int): Option[Int] = {
    if (b == 0) None
    else Some(a / b)
  }
  
  def divideEven(a: Int, b: Int): Option[Int] = {
    safeDivide(a, b).filter(_ % 2 == 0)
  }
  
  def parseInt(s: String): Option[Int] = {
    try {
      Some(s.toInt)
    } catch {
      case _: NumberFormatException => None
    }
  }
  
  def parseAndDivide(s: String, divisor: Int): Option[Int] = {
    for {
      n <- parseInt(s)
      result <- safeDivide(n, divisor)
    } yield result
  }
  
  case class User(id: Int, name: String, email: Option[String])
  
  val users = List(
    User(1, "Alice", Some("alice@example.com")),
    User(2, "Bob", None),
    User(3, "Carol", Some("carol@example.com"))
  )
  
  def findUserById(id: Int): Option[User] = {
    users.find(_.id == id)
  }
  
  def getUserEmail(id: Int): String = {
    findUserById(id)
      .flatMap(_.email)
      .getOrElse("no-email@example.com")
  }
  
  println("=== Exercise 03: Working with Options (SOLUTION) ===\n")
  
  println("--- safeDivide ---")
  println(s"safeDivide(10, 2) = ${safeDivide(10, 2)}")
  println(s"safeDivide(10, 0) = ${safeDivide(10, 0)}")
  
  println("\n--- divideEven ---")
  println(s"divideEven(20, 5) = ${divideEven(20, 5)}")
  println(s"divideEven(20, 6) = ${divideEven(20, 6)}")
  println(s"divideEven(20, 0) = ${divideEven(20, 0)}")
  
  println("\n--- parseAndDivide ---")
  println(s"parseAndDivide(\"20\", 4) = ${parseAndDivide("20", 4)}")
  println(s"parseAndDivide(\"20\", 0) = ${parseAndDivide("20", 0)}")
  println(s"parseAndDivide(\"abc\", 4) = ${parseAndDivide("abc", 4)}")
  
  println("\n--- getUserEmail ---")
  println(s"User 1 email: ${getUserEmail(1)}")
  println(s"User 2 email: ${getUserEmail(2)}")
  println(s"User 999 email: ${getUserEmail(999)}")
}
