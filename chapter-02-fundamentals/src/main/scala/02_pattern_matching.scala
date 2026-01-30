package fundamentals

// Pattern Matching and Case Classes
// Scala's match is like Rust's match but more powerful!

object PatternMatching {
  
  // ============================================================================
  // SEALED TRAITS AND CASE CLASSES (like Rust enums)
  // ============================================================================
  
  // Sealed trait = sum type (can only be extended in same file)
  sealed trait Shape
  case class Circle(radius: Double) extends Shape
  case class Rectangle(width: Double, height: Double) extends Shape
  case class Triangle(base: Double, height: Double) extends Shape
  
  // Rust equivalent:
  // enum Shape {
  //     Circle { radius: f64 },
  //     Rectangle { width: f64, height: f64 },
  //     Triangle { base: f64, height: f64 },
  // }
  
  // ============================================================================
  // PATTERN MATCHING ON CASE CLASSES
  // ============================================================================
  
  def area(shape: Shape): Double = shape match {
    case Circle(r) => math.Pi * r * r
    case Rectangle(w, h) => w * h
    case Triangle(b, h) => 0.5 * b * h
  }
  
  // With guards (additional conditions)
  def describe(shape: Shape): String = shape match {
    case Circle(r) if r > 10 => "Large circle"
    case Circle(r) if r > 5 => "Medium circle"
    case Circle(_) => "Small circle"
    case Rectangle(w, h) if w == h => "Square"
    case Rectangle(_, _) => "Rectangle"
    case Triangle(_, _) => "Triangle"
  }
  
  // ============================================================================
  // CASE CLASSES (like Rust structs with derived traits)
  // ============================================================================
  
  case class Person(name: String, age: Int, email: String)
  
  // Case classes automatically provide:
  // 1. Constructor parameters become fields
  // 2. equals/hashCode implementation
  // 3. toString implementation
  // 4. copy method
  // 5. Pattern matching support
  
  // Rust equivalent:
  // #[derive(Debug, Clone, PartialEq, Eq)]
  // struct Person {
  //     name: String,
  //     age: i32,
  //     email: String,
  // }
  
  val alice = Person("Alice", 30, "alice@example.com")
  
  // Copy with changes (immutable update)
  val olderAlice = alice.copy(age = 31)
  
  // Rust equivalent:
  // let older_alice = Person { age: 31, ..alice };
  
  // ============================================================================
  // PATTERN MATCHING ON VALUES
  // ============================================================================
  
  def matchNumber(n: Int): String = n match {
    case 0 => "zero"
    case 1 => "one"
    case 2 => "two"
    case n if n > 2 && n < 10 => "between 3 and 9"
    case _ => "something else"  // _ is like Rust's _
  }
  
  def matchString(s: String): String = s match {
    case "hello" => "Hi there!"
    case "goodbye" => "See you!"
    case s if s.startsWith("Hello") => s"$s to you too!"
    case _ => "I don't understand"
  }
  
  // ============================================================================
  // PATTERN MATCHING ON TUPLES
  // ============================================================================
  
  def matchTuple(t: (Int, String)): String = t match {
    case (0, _) => "First element is zero"
    case (_, "") => "Second element is empty"
    case (n, s) if n > 0 => s"Positive $n with '$s'"
    case (n, s) => s"Negative $n with '$s'"
  }
  
  // ============================================================================
  // PATTERN MATCHING ON LISTS
  // ============================================================================
  
  def matchList(list: List[Int]): String = list match {
    case Nil => "Empty list"
    case List(1) => "List with just 1"
    case List(1, 2, 3) => "List [1, 2, 3]"
    case head :: Nil => s"List with one element: $head"
    case head :: tail => s"Head: $head, Tail: $tail"
  }
  
  // Rust equivalent:
  // match list.as_slice() {
  //     [] => "Empty list",
  //     [1] => "List with just 1",
  //     [1, 2, 3] => "List [1, 2, 3]",
  //     [head] => format!("List with one element: {}", head),
  //     [head, tail @ ..] => format!("Head: {}, Tail: {:?}", head, tail),
  // }
  
  // ============================================================================
  // EXTRACTING VALUES
  // ============================================================================
  
  // Pattern matching in val declarations
  val (x, y) = (10, 20)  // Destructuring
  val List(a, b, c) = List(1, 2, 3)
  
  val person = Person("Bob", 25, "bob@example.com")
  val Person(name, age, _) = person  // Extract name and age, ignore email
  
  // ============================================================================
  // OPTION WITH PATTERN MATCHING
  // ============================================================================
  
  def divideOption(a: Int, b: Int): Option[Int] = {
    if (b == 0) None else Some(a / b)
  }
  
  def describeDivision(a: Int, b: Int): String = {
    divideOption(a, b) match {
      case Some(result) => s"$a / $b = $result"
      case None => "Division by zero!"
    }
  }
  
  // ============================================================================
  // NESTED PATTERNS
  // ============================================================================
  
  sealed trait Tree
  case class Leaf(value: Int) extends Tree
  case class Branch(left: Tree, right: Tree) extends Tree
  
  def sumTree(tree: Tree): Int = tree match {
    case Leaf(value) => value
    case Branch(left, right) => sumTree(left) + sumTree(right)
  }
  
  // ============================================================================
  // TYPE PATTERNS
  // ============================================================================
  
  def matchType(x: Any): String = x match {
    case i: Int => s"Integer: $i"
    case s: String => s"String: $s"
    case list: List[_] => s"List with ${list.length} elements"
    case _ => "Unknown type"
  }
  
  // ============================================================================
  // DEMO FUNCTION
  // ============================================================================
  
  def main(args: Array[String]): Unit = {
    println("=== Pattern Matching Demo ===\n")
    
    // Shapes
    println("--- Shapes ---")
    val circle = Circle(5.0)
    val rectangle = Rectangle(4.0, 6.0)
    val square = Rectangle(5.0, 5.0)
    
    println(s"Circle area: ${area(circle)}")
    println(s"Rectangle area: ${area(rectangle)}")
    println(s"Circle description: ${describe(circle)}")
    println(s"Square description: ${describe(square)}")
    
    // Case classes
    println("\n--- Case Classes ---")
    println(s"alice = $alice")
    println(s"olderAlice = $olderAlice")
    println(s"alice == olderAlice: ${alice == olderAlice}")
    
    // Number matching
    println("\n--- Number Matching ---")
    println(s"matchNumber(1) = ${matchNumber(1)}")
    println(s"matchNumber(5) = ${matchNumber(5)}")
    println(s"matchNumber(100) = ${matchNumber(100)}")
    
    // List matching
    println("\n--- List Matching ---")
    println(s"matchList(Nil) = ${matchList(Nil)}")
    println(s"matchList(List(1)) = ${matchList(List(1))}")
    println(s"matchList(List(1, 2, 3, 4)) = ${matchList(List(1, 2, 3, 4))}")
    
    // Option matching
    println("\n--- Option Matching ---")
    println(describeDivision(10, 2))
    println(describeDivision(10, 0))
    
    // Tree matching
    println("\n--- Tree Matching ---")
    val tree = Branch(
      Branch(Leaf(1), Leaf(2)),
      Leaf(3)
    )
    println(s"Sum of tree: ${sumTree(tree)}")
    
    // Type matching
    println("\n--- Type Matching ---")
    println(matchType(42))
    println(matchType("hello"))
    println(matchType(List(1, 2, 3)))
  }
}
