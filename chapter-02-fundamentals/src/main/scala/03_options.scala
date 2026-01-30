package fundamentals

// Option Type - Scala's way to handle nullable values
// Like Rust's Option<T>, but with some differences

object Options {
  
  // ============================================================================
  // OPTION BASICS
  // ============================================================================
  
  // Option[A] is a sealed trait with two cases:
  // - Some(value): contains a value
  // - None: no value (like null, but type-safe)
  
  val someValue: Option[Int] = Some(42)
  val noValue: Option[Int] = None
  
  // Rust comparison:
  // Rust: let some_value: Option<i32> = Some(42);
  //       let no_value: Option<i32> = None;
  // Scala: val someValue: Option[Int] = Some(42)
  //        val noValue: Option[Int] = None
  
  // ============================================================================
  // CREATING OPTIONS
  // ============================================================================
  
  // From nullable value
  def fromNullable(s: String): Option[String] = Option(s)
  // If s is null, returns None; otherwise Some(s)
  
  // Example usage
  val maybeStr1 = Option("hello")      // Some("hello")
  val maybeStr2 = Option(null: String) // None
  
  // ============================================================================
  // EXTRACTING VALUES
  // ============================================================================
  
  // 1. Pattern matching (safest)
  def getValueSafe(opt: Option[Int]): Int = opt match {
    case Some(value) => value
    case None => 0  // default value
  }
  
  // 2. getOrElse (common and safe)
  val value1 = someValue.getOrElse(0)  // 42
  val value2 = noValue.getOrElse(0)    // 0
  
  // 3. get (DANGEROUS - can throw exception!)
  // val value3 = noValue.get  // NoSuchElementException!
  // Avoid this! Use pattern matching or getOrElse instead
  
  // Rust comparison:
  // Rust: option.unwrap_or(0)  // like getOrElse
  //       option.unwrap()      // like get (dangerous!)
  
  // ============================================================================
  // CHECKING FOR VALUES
  // ============================================================================
  
  val hasValue = someValue.isDefined  // true
  val isEmpty = noValue.isEmpty       // true
  
  // if-else with isDefined
  if (someValue.isDefined) {
    println(s"Value is ${someValue.get}")
  } else {
    println("No value")
  }
  
  // Rust comparison:
  // Rust: option.is_some()  // like isDefined
  //       option.is_none()  // like isEmpty
  
  // ============================================================================
  // MAP - TRANSFORMING VALUES
  // ============================================================================
  
  val doubled = someValue.map(_ * 2)  // Some(84)
  val doubledNone = noValue.map(_ * 2)  // None
  
  // map only applies function if value exists
  val lengths = List(
    Some("hello").map(_.length),  // Some(5)
    None.map((s: String) => s.length)  // None
  )
  
  // Rust comparison:
  // Rust: option.map(|x| x * 2)
  // Scala: option.map(_ * 2)
  
  // ============================================================================
  // FLATMAP - CHAINING OPTIONAL OPERATIONS
  // ============================================================================
  
  def parseInt(s: String): Option[Int] = {
    try {
      Some(s.toInt)
    } catch {
      case _: NumberFormatException => None
    }
  }
  
  def divide(a: Int, b: Int): Option[Int] = {
    if (b == 0) None else Some(a / b)
  }
  
  // Chaining with flatMap
  def parseAndDivide(s: String, divisor: Int): Option[Int] = {
    parseInt(s).flatMap(n => divide(n, divisor))
  }
  
  // Without flatMap, we'd get Option[Option[Int]]
  // val nested: Option[Option[Int]] = parseInt("10").map(n => divide(n, 2))
  
  // Rust comparison:
  // Rust: option.and_then(|x| some_operation(x))
  // Scala: option.flatMap(x => someOperation(x))
  
  // ============================================================================
  // FOR-COMPREHENSIONS (syntactic sugar for flatMap/map)
  // ============================================================================
  
  def parseAndDivideForComp(s: String, divisor: Int): Option[Int] = {
    for {
      n <- parseInt(s)      // flatMap
      result <- divide(n, divisor)  // flatMap
    } yield result           // map
  }
  
  // Multiple Options
  def addParsedNumbers(s1: String, s2: String): Option[Int] = {
    for {
      n1 <- parseInt(s1)
      n2 <- parseInt(s2)
    } yield n1 + n2
  }
  
  // Rust comparison:
  // Rust doesn't have for-comprehensions, but you can use ? operator:
  // fn add_parsed(s1: &str, s2: &str) -> Option<i32> {
  //     let n1 = parse_int(s1)?;
  //     let n2 = parse_int(s2)?;
  //     Some(n1 + n2)
  // }
  
  // ============================================================================
  // FILTER - CONDITIONAL CHECKS
  // ============================================================================
  
  val evenNumber = Some(42).filter(_ % 2 == 0)  // Some(42)
  val oddNumber = Some(43).filter(_ % 2 == 0)   // None
  
  def getPositive(n: Int): Option[Int] = {
    Some(n).filter(_ > 0)
  }
  
  // ============================================================================
  // FOLD - PROVIDING ALTERNATIVE COMPUTATION
  // ============================================================================
  
  val result1 = someValue.fold(0)(_ * 2)  // 84 (42 * 2)
  val result2 = noValue.fold(0)(_ * 2)    // 0 (default)
  
  // fold(default)(function)
  // If Some(x), apply function(x)
  // If None, return default
  
  // ============================================================================
  // ORELSE - CHAINING OPTIONS
  // ============================================================================
  
  val primary: Option[Int] = None
  val backup: Option[Int] = Some(99)
  
  val chosen = primary.orElse(backup)  // Some(99)
  
  // Chain multiple fallbacks
  val first: Option[String] = None
  val second: Option[String] = None
  val third: Option[String] = Some("found!")
  
  val result = first.orElse(second).orElse(third)  // Some("found!")
  
  // Rust comparison:
  // Rust: option1.or(option2)
  // Scala: option1.orElse(option2)
  
  // ============================================================================
  // CONVERTING TO OTHER TYPES
  // ============================================================================
  
  val optList = Some(42).toList       // List(42)
  val noneList = None.toList          // List()
  
  val optEither = Some(42).toRight("error")  // Right(42)
  val noneEither = None.toRight("error")     // Left("error")
  
  // ============================================================================
  // PRACTICAL EXAMPLES
  // ============================================================================
  
  case class User(id: Int, name: String, email: Option[String])
  
  val users = List(
    User(1, "Alice", Some("alice@example.com")),
    User(2, "Bob", None),
    User(3, "Carol", Some("carol@example.com"))
  )
  
  // Get all emails (filtering out None values)
  def getAllEmails(users: List[User]): List[String] = {
    users.flatMap(_.email)  // flatMap automatically filters out Nones
  }
  
  // Find user by ID
  def findUserById(id: Int): Option[User] = {
    users.find(_.id == id)
  }
  
  // Get user's email, or default
  def getUserEmail(id: Int): String = {
    findUserById(id)
      .flatMap(_.email)
      .getOrElse("no-email@example.com")
  }
  
  // ============================================================================
  // DEMO FUNCTION
  // ============================================================================
  
  def main(args: Array[String]): Unit = {
    println("=== Option Demo ===\n")
    
    // Basic operations
    println("--- Basic Operations ---")
    println(s"someValue = $someValue")
    println(s"noValue = $noValue")
    println(s"someValue.getOrElse(0) = ${someValue.getOrElse(0)}")
    println(s"noValue.getOrElse(0) = ${noValue.getOrElse(0)}")
    
    // Map
    println("\n--- Map ---")
    println(s"someValue.map(_ * 2) = $doubled")
    println(s"noValue.map(_ * 2) = $doubledNone")
    
    // FlatMap and for-comprehension
    println("\n--- FlatMap ---")
    println(s"parseAndDivide('20', 4) = ${parseAndDivide("20", 4)}")
    println(s"parseAndDivide('20', 0) = ${parseAndDivide("20", 0)}")
    println(s"parseAndDivide('abc', 4) = ${parseAndDivide("abc", 4)}")
    
    println("\n--- For-Comprehension ---")
    println(s"addParsedNumbers('10', '20') = ${addParsedNumbers("10", "20")}")
    println(s"addParsedNumbers('10', 'abc') = ${addParsedNumbers("10", "abc")}")
    
    // Filter
    println("\n--- Filter ---")
    println(s"Some(42).filter(_ % 2 == 0) = $evenNumber")
    println(s"Some(43).filter(_ % 2 == 0) = $oddNumber")
    
    // OrElse
    println("\n--- OrElse ---")
    println(s"primary.orElse(backup) = $chosen")
    
    // Practical examples
    println("\n--- Practical Examples ---")
    println(s"All emails: ${getAllEmails(users)}")
    println(s"User 1 email: ${getUserEmail(1)}")
    println(s"User 2 email: ${getUserEmail(2)}")
    println(s"User 999 email: ${getUserEmail(999)}")
  }
}
