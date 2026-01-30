package fundamentals.exercises

/**
 * Exercise 02: Pattern Matching
 * 
 * OBJECTIVES:
 * - Define sealed traits and case classes
 * - Practice pattern matching
 * - Build a simple expression evaluator
 * 
 * TASKS:
 * 1. Complete the Expr sealed trait (already started)
 * 2. Implement eval function using pattern matching
 * 3. Implement toString function for pretty printing
 * 
 * HOW TO RUN:
 * 1. Fill in the ??? parts with your implementation
 * 2. Run: sbt "runMain fundamentals.exercises.Exercise02"
 * 3. Verify the output matches expected results
 * 
 * EXPECTED OUTPUT:
 * Expression: ((2 + 3) * 4)
 * Result: 20
 * 
 * Expression: (10 / (5 - 3))
 * Result: 5
 * 
 * Expression: ((2 + (3 * 4)) - 5)
 * Result: 9
 */
object Exercise02 extends App {
  
  // TODO: Complete the sealed trait and case classes
  // This represents a simple arithmetic expression
  sealed trait Expr
  
  case class Num(value: Int) extends Expr
  
  // TODO: Add case class for addition
  // case class Add(left: Expr, right: Expr) extends Expr
  
  // TODO: Add case class for multiplication  
  // case class Multiply(left: Expr, right: Expr) extends Expr
  
  // TODO: Add case class for subtraction
  // case class Subtract(left: Expr, right: Expr) extends Expr
  
  // TODO: Add case class for division
  // case class Divide(left: Expr, right: Expr) extends Expr
  
  // TODO: Implement eval function
  // Hint: Use pattern matching on the Expr
  // For each case, recursively evaluate left and right, then apply the operation
  def eval(expr: Expr): Int = expr match {
    case Num(value) => value
    // Add cases for Add, Multiply, Subtract, Divide
    case _ => ???
  }
  
  // TODO: Implement toString function for pretty printing
  // Hint: Use pattern matching to convert expression to string with parentheses
  def exprToString(expr: Expr): String = expr match {
    case Num(value) => value.toString
    // Add cases for operations, wrap in parentheses: "(left + right)"
    case _ => ???
  }
  
  // Test cases - uncomment after implementing
  println("=== Exercise 02: Pattern Matching ===\n")
  
  // Test case 1: (2 + 3) * 4 = 20
  // val expr1 = Multiply(Add(Num(2), Num(3)), Num(4))
  // println(s"Expression: ${exprToString(expr1)}")
  // println(s"Result: ${eval(expr1)}\n")
  
  // Test case 2: 10 / (5 - 3) = 5
  // val expr2 = Divide(Num(10), Subtract(Num(5), Num(3)))
  // println(s"Expression: ${exprToString(expr2)}")
  // println(s"Result: ${eval(expr2)}\n")
  
  // Test case 3: (2 + 3 * 4) - 5 = 9
  // val expr3 = Subtract(Add(Num(2), Multiply(Num(3), Num(4))), Num(5))
  // println(s"Expression: ${exprToString(expr3)}")
  // println(s"Result: ${eval(expr3)}\n")
  
  println("If you see this message without errors, uncomment the test cases above!")
}

/**
 * SOLUTION (Don't peek until you've tried!)
 * 
 * sealed trait Expr
 * case class Num(value: Int) extends Expr
 * case class Add(left: Expr, right: Expr) extends Expr
 * case class Multiply(left: Expr, right: Expr) extends Expr
 * case class Subtract(left: Expr, right: Expr) extends Expr
 * case class Divide(left: Expr, right: Expr) extends Expr
 * 
 * def eval(expr: Expr): Int = expr match {
 *   case Num(value) => value
 *   case Add(left, right) => eval(left) + eval(right)
 *   case Multiply(left, right) => eval(left) * eval(right)
 *   case Subtract(left, right) => eval(left) - eval(right)
 *   case Divide(left, right) => eval(left) / eval(right)
 * }
 * 
 * def exprToString(expr: Expr): String = expr match {
 *   case Num(value) => value.toString
 *   case Add(left, right) => s"(${exprToString(left)} + ${exprToString(right)})"
 *   case Multiply(left, right) => s"(${exprToString(left)} * ${exprToString(right)})"
 *   case Subtract(left, right) => s"(${exprToString(left)} - ${exprToString(right)})"
 *   case Divide(left, right) => s"(${exprToString(left)} / ${exprToString(right)})"
 * }
 * 
 * RUST COMPARISON:
 * 
 * enum Expr {
 *     Num(i32),
 *     Add(Box<Expr>, Box<Expr>),
 *     Multiply(Box<Expr>, Box<Expr>),
 *     Subtract(Box<Expr>, Box<Expr>),
 *     Divide(Box<Expr>, Box<Expr>),
 * }
 * 
 * fn eval(expr: &Expr) -> i32 {
 *     match expr {
 *         Expr::Num(value) => *value,
 *         Expr::Add(left, right) => eval(left) + eval(right),
 *         Expr::Multiply(left, right) => eval(left) * eval(right),
 *         Expr::Subtract(left, right) => eval(left) - eval(right),
 *         Expr::Divide(left, right) => eval(left) / eval(right),
 *     }
 * }
 */
