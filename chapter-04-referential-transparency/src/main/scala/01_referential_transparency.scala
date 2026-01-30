package referentialtransparency

// Referential Transparency (RT): The foundation of functional programming
// A expression is referentially transparent if it can be replaced with its value
// without changing the program's behavior.

object ReferentialTransparencyExamples {
  
  // ============================================================================
  // WHAT IS REFERENTIAL TRANSPARENCY?
  // ============================================================================
  
  // An expression is RT if you can replace it with its result value
  // without changing the program's meaning.
  
  // Example: Pure arithmetic
  val x = 2 + 3  // Can replace (2 + 3) with 5 anywhere
  val y = x * 2  // Same as: val y = 5 * 2
  
  // This is the "substitution model" of evaluation
  
  // ============================================================================
  // PURE FUNCTIONS (Referentially Transparent)
  // ============================================================================
  
  // A pure function:
  // 1. Always returns the same output for the same input
  // 2. Has no side effects (doesn't modify external state)
  
  def add(a: Int, b: Int): Int = a + b
  
  // This is pure - we can substitute calls with their results:
  val result1 = add(2, 3)  // 5
  val result2 = add(2, 3)  // 5 - always the same
  val combined = result1 + result1  // Same as: 5 + 5
  
  // Rust comparison:
  // fn add(a: i32, b: i32) -> i32 { a + b }
  // Rust also values purity, though it's not enforced
  
  def multiply(a: Int, b: Int): Int = {
    val temp = a * b
    temp  // No side effects, just computation
  }
  
  def square(x: Int): Int = x * x
  
  // Chaining pure functions is safe
  def compute(x: Int): Int = {
    val doubled = multiply(x, 2)
    square(doubled)
  }
  
  // ============================================================================
  // IMPURE FUNCTIONS (NOT Referentially Transparent)
  // ============================================================================
  
  // Example 1: Reading/Writing mutable state
  var counter = 0
  
  def incrementCounter(): Int = {
    counter += 1  // Side effect: modifies external state
    counter
  }
  
  // NOT referentially transparent:
  val a = incrementCounter()  // Returns 1
  val b = incrementCounter()  // Returns 2 - different result!
  // We cannot replace incrementCounter() with a fixed value
  
  // Example 2: I/O operations
  def readUserInput(): String = {
    scala.io.StdIn.readLine()  // Side effect: reads from stdin
  }
  
  // NOT RT: result depends on external input
  // Each call can return different values
  
  // Example 3: Random numbers
  def rollDice(): Int = {
    scala.util.Random.nextInt(6) + 1  // Non-deterministic
  }
  
  // NOT RT: same input (no args), different outputs
  
  // Example 4: Current time
  def getCurrentTime(): Long = {
    System.currentTimeMillis()  // Depends on external state
  }
  
  // NOT RT: returns different value each call
  
  // Example 5: Printing (side effect)
  def printAndReturn(x: Int): Int = {
    println(s"Value: $x")  // Side effect: I/O
    x
  }
  
  // NOT RT: has observable side effect even though it returns the same value
  
  // ============================================================================
  // TESTING REFERENTIAL TRANSPARENCY
  // ============================================================================
  
  // The substitution test:
  // If we can replace a function call with its result value,
  // and the program behaves identically, it's RT
  
  def pureFunction(x: Int): Int = x * 2
  
  // Test 1: Original code
  def test1(): Int = {
    val a = pureFunction(5)  // 10
    val b = pureFunction(5)  // 10
    a + b  // 20
  }
  
  // Test 2: Substituted code
  def test2(): Int = {
    val a = 10  // Substituted pureFunction(5) with 10
    val b = 10  // Substituted pureFunction(5) with 10
    a + b  // 20 - same result!
  }
  
  // Both tests return 20 → pureFunction is RT
  
  // Now test with impure function:
  def test3(): Int = {
    val a = incrementCounter()  // Returns 1, counter = 1
    val b = incrementCounter()  // Returns 2, counter = 2
    a + b  // 3
  }
  
  def test4(): Int = {
    val a = 1  // Try to substitute first call
    val b = 1  // Try to substitute second call
    a + b  // 2 - DIFFERENT result!
  }
  
  // Different results → incrementCounter is NOT RT
  
  // ============================================================================
  // WHY DOES REFERENTIAL TRANSPARENCY MATTER?
  // ============================================================================
  
  // 1. REASONING: Easy to understand and predict
  def example1Pure(x: Int): Int = {
    val step1 = x * 2      // Can trace: 5 * 2 = 10
    val step2 = step1 + 1  // Can trace: 10 + 1 = 11
    step2 * 3              // Can trace: 11 * 3 = 33
  }
  
  // 2. TESTING: Same inputs → same outputs, no setup needed
  def testPure(): Unit = {
    assert(add(2, 3) == 5)
    assert(add(2, 3) == 5)  // Can call multiple times, always same
    // No mocks, no state management needed
  }
  
  // 3. REFACTORING: Safe to extract or inline
  def before(x: Int): Int = {
    val temp = x * 2
    temp + 1
  }
  
  def after(x: Int): Int = {
    x * 2 + 1  // Inlined temp - safe because it's pure
  }
  
  // 4. PARALLELIZATION: No race conditions
  def processPure(items: List[Int]): List[Int] = {
    items.map(x => x * 2)  // Can be safely parallelized (using par in Scala 2.12)
  }
  
  // 5. CACHING/MEMOIZATION: Can cache results
  val memoizedSquare = scala.collection.mutable.Map[Int, Int]()
  
  def squareWithMemo(x: Int): Int = {
    memoizedSquare.getOrElseUpdate(x, x * x)
    // Only works if square is pure!
  }
  
  // ============================================================================
  // RUST COMPARISON
  // ============================================================================
  
  // Rust also values referential transparency, though not enforced:
  
  // Pure in Rust:
  // fn add(a: i32, b: i32) -> i32 { a + b }
  
  // Impure in Rust (but allowed):
  // fn impure() -> i32 {
  //     static mut COUNTER: i32 = 0;
  //     unsafe {
  //         COUNTER += 1;
  //         COUNTER
  //     }
  // }
  
  // Key difference:
  // - Rust uses ownership to prevent many side effects at compile time
  // - Scala uses FP patterns and libraries (like Cats-Effect) to encode effects
  // - Both aim for predictable, maintainable code
  
  // ============================================================================
  // PRACTICAL GUIDELINES
  // ============================================================================
  
  // Prefer pure functions whenever possible:
  
  // ❌ Impure: modifies external state
  var total = 0
  def addToTotal(x: Int): Unit = {
    total += x
  }
  
  // ✅ Pure: returns new value
  def addToValue(current: Int, x: Int): Int = {
    current + x
  }
  
  // ❌ Impure: depends on external state
  var config = "default"
  def getConfig(): String = config
  
  // ✅ Pure: takes configuration as parameter
  def processWithConfig(config: String, data: String): String = {
    s"$config: $data"
  }
  
  // ❌ Impure: println is a side effect
  def processAndLog(x: Int): Int = {
    println(s"Processing $x")
    x * 2
  }
  
  // ✅ Pure: return value and log message
  def processWithLog(x: Int): (Int, String) = {
    val result = x * 2
    val log = s"Processing $x"
    (result, log)
  }
  
  // ============================================================================
  // THE PROBLEM: Real programs need side effects!
  // ============================================================================
  
  // Pure functions are great, but:
  // - We need to read files
  // - We need to make HTTP requests
  // - We need to write to databases
  // - We need to interact with the outside world
  
  // How can we maintain RT while performing side effects?
  // Answer: Describe effects as values (IO[A] in Chapter 07)
  
  // Preview:
  // val program: IO[String] = IO {
  //   println("Enter name: ")
  //   scala.io.StdIn.readLine()
  // }
  // // program is just a description, nothing executed yet
  // // It's RT because it's just a value
  // // Side effects only happen when we explicitly run it
  
  // ============================================================================
  // DEMO FUNCTION
  // ============================================================================
  
  def main(args: Array[String]): Unit = {
    println("=== Referential Transparency Examples ===\n")
    
    // Pure functions
    println("--- Pure Functions ---")
    println(s"add(2, 3) = ${add(2, 3)}")
    println(s"add(2, 3) = ${add(2, 3)}")  // Same result
    println(s"square(5) = ${square(5)}")
    
    // Impure function
    println("\n--- Impure Functions ---")
    counter = 0  // Reset
    println(s"incrementCounter() = ${incrementCounter()}")  // 1
    println(s"incrementCounter() = ${incrementCounter()}")  // 2 - different!
    println(s"rollDice() = ${rollDice()}")
    println(s"rollDice() = ${rollDice()}")  // Different values
    
    // Substitution test
    println("\n--- Substitution Test ---")
    println(s"test1() with function calls = ${test1()}")
    println(s"test2() with substituted values = ${test2()}")
    println("Both return 20 - pureFunction is RT!")
    
    counter = 0  // Reset
    println(s"\ntest3() with incrementCounter = ${test3()}")
    println(s"test4() with substituted values = ${test4()}")
    println("Different results - incrementCounter is NOT RT!")
    
    // Benefits
    println("\n--- Benefits of RT ---")
    println(s"example1Pure(5) = ${example1Pure(5)}")
    println("- Easy to reason about")
    println("- Simple to test")
    println("- Safe to refactor")
    println("- Can parallelize")
    println("- Can cache/memoize")
  }
}
