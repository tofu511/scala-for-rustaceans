package fundamentals

// Scala Basics: Types, Values, Variables, and Functions
// Compare with Rust as you learn!

object Basics {
  
  // ============================================================================
  // VALUES AND VARIABLES
  // ============================================================================
  
  // val = immutable (like Rust's `let`)
  val immutableValue: Int = 42
  // immutableValue = 43  // Compile error: reassignment to val
  
  // var = mutable (like Rust's `let mut`)
  var mutableValue: Int = 42
  // mutableValue = 43  // OK!
  
  // Type inference (just like Rust)
  val inferredInt = 42          // Int
  val inferredString = "hello"  // String
  val inferredDouble = 3.14     // Double
  
  // ============================================================================
  // BASIC TYPES
  // ============================================================================
  
  // Numeric types
  val byte: Byte = 127           // 8-bit signed (-128 to 127)
  val short: Short = 32767       // 16-bit signed
  val int: Int = 2147483647      // 32-bit signed (most common)
  val long: Long = 9223372036854775807L  // 64-bit signed
  
  val float: Float = 3.14f       // 32-bit floating point
  val double: Double = 3.14159   // 64-bit floating point (default)
  
  // Boolean
  val bool: Boolean = true       // like Rust's bool
  
  // Char and String
  val char: Char = 'A'
  val string: String = "Hello, Rustacean!"
  
  // Unit (like Rust's () unit type)
  val unit: Unit = ()            // Represents "no meaningful value"
  
  // ============================================================================
  // STRING INTERPOLATION
  // ============================================================================
  
  // s-interpolation (most common)
  val name = "Alice"
  val age = 30
  val greeting = s"Hello, $name! You are $age years old."
  val math = s"2 + 2 = ${2 + 2}"
  
  // f-interpolation (formatted strings)
  val pi = 3.14159
  val formatted = f"Pi is approximately $pi%.2f"  // "Pi is approximately 3.14"
  
  // raw interpolation (no escape sequences)
  val path = raw"C:\Users\Alice\Documents"  // Backslashes not escaped
  
  // Rust comparison:
  // Rust: format!("Hello, {}! You are {} years old.", name, age)
  // Scala: s"Hello, $name! You are $age years old."
  
  // ============================================================================
  // FUNCTIONS
  // ============================================================================
  
  // Basic function definition
  def add(x: Int, y: Int): Int = {
    x + y
  }
  
  // Single expression (no braces needed)
  def multiply(x: Int, y: Int): Int = x * y
  
  // Return type inference (not recommended for public APIs)
  def subtract(x: Int, y: Int) = x - y
  
  // Function with no parameters
  def getCurrentTime(): Long = System.currentTimeMillis()
  
  // Unit return type (like Rust's () or no return)
  def printMessage(msg: String): Unit = {
    println(msg)
  }
  
  // Default parameters (Rust doesn't have this)
  def greet(name: String, greeting: String = "Hello"): String = {
    s"$greeting, $name!"
  }
  // Usage: greet("Alice") -> "Hello, Alice!"
  //        greet("Bob", "Hi") -> "Hi, Bob!"
  
  // Named parameters
  def createUser(name: String, age: Int, email: String): String = {
    s"User($name, $age, $email)"
  }
  // Usage: createUser(name = "Alice", email = "alice@example.com", age = 30)
  
  // ============================================================================
  // ANONYMOUS FUNCTIONS (LAMBDAS)
  // ============================================================================
  
  // Full syntax
  val addLambda: (Int, Int) => Int = (x: Int, y: Int) => x + y
  
  // Type inference
  val addLambda2 = (x: Int, y: Int) => x + y
  
  // Single parameter with underscore syntax
  val doubleFn = (x: Int) => x * 2
  val doubleShort: Int => Int = _ * 2  // Underscore for single parameter
  
  // Rust comparison:
  // Rust: |x, y| x + y
  // Scala: (x, y) => x + y  or  _ + _  for point-free style
  
  // ============================================================================
  // HIGHER-ORDER FUNCTIONS
  // ============================================================================
  
  // Function that takes a function as parameter
  def applyTwice(f: Int => Int, x: Int): Int = {
    f(f(x))
  }
  
  // Usage:
  // applyTwice(_ * 2, 5)  // 20 (5 * 2 * 2)
  
  // Function that returns a function
  def makeAdder(x: Int): Int => Int = {
    (y: Int) => x + y
  }
  
  // Usage:
  // val add5 = makeAdder(5)
  // add5(3)  // 8
  
  // ============================================================================
  // BLOCKS AND EXPRESSIONS
  // ============================================================================
  
  // Everything is an expression (returns a value)
  val result = {
    val x = 10
    val y = 20
    x + y  // Last expression is the return value
  }
  // result = 30
  
  // if is an expression (like Rust)
  val max = if (10 > 5) 10 else 5
  
  // Rust comparison:
  // Rust: let max = if 10 > 5 { 10 } else { 5 };
  // Scala: val max = if (10 > 5) 10 else 5
  
  // ============================================================================
  // TUPLES
  // ============================================================================
  
  val tuple2: (Int, String) = (42, "answer")
  val tuple3: (Int, String, Double) = (1, "one", 1.0)
  
  // Accessing tuple elements (1-indexed!)
  val first = tuple2._1   // 42
  val second = tuple2._2  // "answer"
  
  // Pattern matching with tuples
  val (number, text) = tuple2
  
  // Rust comparison:
  // Rust: let tuple = (42, "answer");
  //       let (number, text) = tuple;
  // Scala: val tuple = (42, "answer")
  //        val (number, text) = tuple
  
  // ============================================================================
  // DEMO FUNCTION
  // ============================================================================
  
  def main(args: Array[String]): Unit = {
    println("=== Scala Basics Demo ===\n")
    
    // Values and variables
    println("--- Values and Variables ---")
    println(s"Immutable value: $immutableValue")
    var mutable = 10
    println(s"Mutable before: $mutable")
    mutable = 20
    println(s"Mutable after: $mutable")
    
    // String interpolation
    println("\n--- String Interpolation ---")
    println(greeting)
    println(math)
    println(formatted)
    
    // Functions
    println("\n--- Functions ---")
    println(s"add(5, 3) = ${add(5, 3)}")
    println(s"multiply(4, 7) = ${multiply(4, 7)}")
    println(s"greet('Alice') = ${greet("Alice")}")
    println(s"greet('Bob', 'Hi') = ${greet("Bob", "Hi")}")
    
    // Lambdas
    println("\n--- Lambdas ---")
    println(s"addLambda(10, 20) = ${addLambda(10, 20)}")
    println(s"doubleFn(5) = ${doubleFn(5)}")
    
    // Higher-order functions
    println("\n--- Higher-order Functions ---")
    println(s"applyTwice(_ * 2, 5) = ${applyTwice(_ * 2, 5)}")
    val add10 = makeAdder(10)
    println(s"add10(5) = ${add10(5)}")
    
    // Tuples
    println("\n--- Tuples ---")
    println(s"tuple2 = $tuple2")
    println(s"tuple2._1 = ${tuple2._1}, tuple2._2 = ${tuple2._2}")
    val (num, str) = tuple2
    println(s"Destructured: num = $num, str = $str")
  }
}
