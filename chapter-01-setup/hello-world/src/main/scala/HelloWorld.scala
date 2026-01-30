object HelloWorld {
  def main(args: Array[String]): Unit = {
    println("Hello, Rustacean! Welcome to Scala!")
    
    // Variables (similar to Rust's let and let mut)
    val immutable = "Cannot change"      // like: let x = "..."
    var mutable = "Can change"           // like: let mut x = "..."
    
    println(s"Immutable: $immutable")
    println(s"Mutable: $mutable")
    
    // Trying to reassign immutable would cause compile error:
    // immutable = "New value"  // ERROR: reassignment to val
    
    // But mutable can be changed:
    mutable = "Changed!"
    println(s"Mutable after change: $mutable")
    
    // Collections (List is immutable by default, like Rust's Vec but immutable)
    val numbers = List(1, 2, 3, 4, 5)
    val doubled = numbers.map(_ * 2)      // _ * 2 is like |x| x * 2 in Rust
    println(s"Doubled: $doubled")
    
    // String interpolation with s"" prefix
    val name = "Rustacean"
    val greeting = s"Welcome, $name! You know ${numbers.length} numbers."
    println(greeting)
    
    // Pattern matching (like Rust's match)
    val language = "Scala"
    val message = language match {
      case "Rust"  => "Systems programming!"
      case "Scala" => "Functional programming!"
      case _       => "Some other language"
    }
    println(message)
    
    // Option type (like Rust's Option<T>)
    val maybeValue: Option[Int] = Some(42)
    val result = maybeValue match {
      case Some(value) => s"Got value: $value"
      case None        => "No value"
    }
    println(result)
    
    // Using map on Option (like Rust)
    val doubled2 = maybeValue.map(_ * 2)
    println(s"Doubled option: $doubled2")  // Some(84)
  }
}
