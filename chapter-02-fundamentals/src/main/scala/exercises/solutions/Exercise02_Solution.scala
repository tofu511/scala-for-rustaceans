package fundamentals.exercises.solutions

/**
 * SOLUTION for Exercise 02: Pattern Matching
 * 
 * HOW TO RUN:
 * sbt "runMain fundamentals.exercises.solutions.Exercise02Solution"
 */
object Exercise02Solution extends App {
  
  sealed trait Expr
  case class Num(value: Int) extends Expr
  case class Add(left: Expr, right: Expr) extends Expr
  case class Multiply(left: Expr, right: Expr) extends Expr
  case class Subtract(left: Expr, right: Expr) extends Expr
  case class Divide(left: Expr, right: Expr) extends Expr
  
  def eval(expr: Expr): Int = expr match {
    case Num(value) => value
    case Add(left, right) => eval(left) + eval(right)
    case Multiply(left, right) => eval(left) * eval(right)
    case Subtract(left, right) => eval(left) - eval(right)
    case Divide(left, right) => eval(left) / eval(right)
  }
  
  def exprToString(expr: Expr): String = expr match {
    case Num(value) => value.toString
    case Add(left, right) => s"(${exprToString(left)} + ${exprToString(right)})"
    case Multiply(left, right) => s"(${exprToString(left)} * ${exprToString(right)})"
    case Subtract(left, right) => s"(${exprToString(left)} - ${exprToString(right)})"
    case Divide(left, right) => s"(${exprToString(left)} / ${exprToString(right)})"
  }
  
  println("=== Exercise 02: Pattern Matching (SOLUTION) ===\n")
  
  val expr1 = Multiply(Add(Num(2), Num(3)), Num(4))
  println(s"Expression: ${exprToString(expr1)}")
  println(s"Result: ${eval(expr1)}\n")
  
  val expr2 = Divide(Num(10), Subtract(Num(5), Num(3)))
  println(s"Expression: ${exprToString(expr2)}")
  println(s"Result: ${eval(expr2)}\n")
  
  val expr3 = Subtract(Add(Num(2), Multiply(Num(3), Num(4))), Num(5))
  println(s"Expression: ${exprToString(expr3)}")
  println(s"Result: ${eval(expr3)}")
}
