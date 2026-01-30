package referentialtransparency

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

// Future breaks referential transparency
// This file demonstrates the problems in detail

object FutureProblemsExamples {
  
  // ============================================================================
  // PROBLEM 1: EAGER EVALUATION
  // ============================================================================
  
  // Future evaluates immediately when created
  var executionCount = 0
  
  def expensiveComputation(): Future[Int] = Future {
    executionCount += 1
    println(s"Executing expensive computation (count: $executionCount)")
    Thread.sleep(100)
    42
  }
  
  def demonstrateEagerEvaluation(): Unit = {
    println("=== Problem 1: Eager Evaluation ===")
    executionCount = 0
    
    // Just creating the Future starts execution!
    val future = expensiveComputation()
    println("Future created, but computation already started!")
    
    Thread.sleep(200)
    println(s"Total executions: $executionCount")
    
    // Rust comparison:
    // let future = async { expensive_computation().await };
    // // In Rust, nothing executes until you .await it
  }
  
  // ============================================================================
  // PROBLEM 2: NOT REFERENTIALLY TRANSPARENT
  // ============================================================================
  
  // The classic example from Chapter 03, expanded
  var counter = 0
  
  def increment(): Future[Int] = Future {
    counter += 1
    println(s"Incremented counter to: $counter")
    counter
  }
  
  def demonstrateNonRT(): Unit = {
    println("\n=== Problem 2: Non-RT ===")
    counter = 0
    
    // Example A: Reusing the same future
    println("Example A: Reusing same future")
    val future = increment()
    val resultA = for {
      a <- future
      b <- future  // Reusing same future
    } yield (a, b)
    
    println(s"Result A: ${Await.result(resultA, 1.second)}")
    println(s"Counter after A: $counter")
    
    // Example B: Creating new futures
    println("\nExample B: Creating new futures")
    counter = 0
    val resultB = for {
      a <- increment()  // Creates new future
      b <- increment()  // Creates another new future
    } yield (a, b)
    
    println(s"Result B: ${Await.result(resultB, 1.second)}")
    println(s"Counter after B: $counter")
    
    println("\nExample A and B are NOT equivalent!")
    println("We cannot substitute increment() with a value.")
    println("This breaks referential transparency!")
  }
  
  // ============================================================================
  // PROBLEM 3: SIDE EFFECTS HAPPEN IMMEDIATELY
  // ============================================================================
  
  def writeToDB(id: Int): Future[Unit] = Future {
    println(s"!!! WRITING TO DATABASE: User $id !!!")
    Thread.sleep(50)
  }
  
  def demonstrateSideEffects(): Unit = {
    println("\n=== Problem 3: Immediate Side Effects ===")
    
    // Just defining this causes side effects!
    println("Defining operation...")
    val operation = writeToDB(1)
    
    println("Operation defined, but side effect already happened!")
    println("We wanted to describe what to do, not do it immediately!")
    
    Thread.sleep(100)
    
    // Contrast with what we want:
    // val operation = IO { writeToDB(1) }  // Just a description
    // operation.unsafeRunSync()  // Explicit execution
  }
  
  // ============================================================================
  // PROBLEM 4: DIFFICULT TO COMPOSE
  // ============================================================================
  
  var requestCount = 0
  
  def fetchUser(id: Int): Future[String] = Future {
    requestCount += 1
    println(s"Fetching user $id (request #$requestCount)")
    Thread.sleep(100)
    s"User$id"
  }
  
  def demonstrateCompositionIssues(): Unit = {
    println("\n=== Problem 4: Composition Issues ===")
    requestCount = 0
    
    // Trying to build a reusable operation
    def getUserOperation(id: Int): Future[String] = {
      println(s"Building operation for user $id")
      fetchUser(id)  // Oops, already executing!
    }
    
    println("Creating operations...")
    val op1 = getUserOperation(1)
    val op2 = getUserOperation(2)
    
    println("Operations created - but requests already sent!")
    
    Thread.sleep(200)
    println(s"Total requests: $requestCount")
    
    println("\nProblem: We can't build reusable operation descriptions.")
    println("Each time we call getUserOperation, side effects happen.")
  }
  
  // ============================================================================
  // PROBLEM 5: TESTING IS DIFFICULT
  // ============================================================================
  
  var testCounter = 0
  
  def operationWithSideEffect(): Future[Int] = Future {
    testCounter += 1
    println(s"Side effect in test! Counter: $testCounter")
    42
  }
  
  def demonstrateTestingIssues(): Unit = {
    println("\n=== Problem 5: Testing Issues ===")
    
    println("Setting up test...")
    testCounter = 0
    
    // In a test, just referencing the operation causes side effects
    val operation = operationWithSideEffect()
    
    println("Test setup complete, but side effect already happened!")
    println("This makes tests fragile and order-dependent.")
    
    Thread.sleep(100)
    
    // What we want:
    // val operation = IO { ... }  // No side effects yet
    // operation.unsafeRunSync()   // Explicit execution in test
  }
  
  // ============================================================================
  // PROBLEM 6: RACE CONDITIONS
  // ============================================================================
  
  var sharedState = 0
  
  def modifyState(delta: Int): Future[Int] = Future {
    val current = sharedState
    Thread.sleep(10)  // Simulate some work
    sharedState = current + delta
    sharedState
  }
  
  def demonstrateRaceConditions(): Unit = {
    println("\n=== Problem 6: Race Conditions ===")
    sharedState = 0
    
    // Start multiple futures that modify shared state
    val futures = (1 to 5).map(i => modifyState(1))
    
    // Wait for all to complete
    val results = Await.result(Future.sequence(futures), 1.second)
    
    println(s"Results: $results")
    println(s"Expected sharedState to be 5")
    println(s"Actual sharedState: $sharedState")
    println("Race condition: lost updates!")
    
    // With IO and proper state management (Ref), this is prevented
  }
  
  // ============================================================================
  // PROBLEM 7: CAN'T RETRY OR REPEAT SAFELY
  // ============================================================================
  
  var attemptNumber = 0
  
  def unreliableOperation(): Future[String] = Future {
    attemptNumber += 1
    println(s"Attempt #$attemptNumber")
    if (attemptNumber < 3) {
      throw new Exception("Simulated failure")
    }
    "Success"
  }
  
  def demonstrateRetryIssues(): Unit = {
    println("\n=== Problem 7: Retry Issues ===")
    attemptNumber = 0
    
    println("Trying to retry operation...")
    val operation = unreliableOperation()
    
    // Can't retry! Operation already happened
    println("Operation already executed on creation!")
    println("Can't retry the same future.")
    
    Thread.sleep(100)
    
    // You'd need to create a function that returns Future:
    def retryable(): Future[String] = unreliableOperation()
    
    println("\nNeed to create new futures for each retry:")
    // But then each call has side effects immediately
    attemptNumber = 0
    val attempt1 = retryable()  // Side effect!
    Thread.sleep(100)
    val attempt2 = retryable()  // Another side effect!
    
    println("With IO, retry is built-in and safe.")
  }
  
  // ============================================================================
  // WHAT WE WANT: IO[A]
  // ============================================================================
  
  // Preview of what we'll learn in Chapter 07:
  
  def describeIOOperation(): String = {
    """
    |// With IO[A]:
    |var counter = 0
    |def increment(): IO[Int] = IO {
    |  counter += 1
    |  counter
    |}
    |
    |// This is referentially transparent:
    |val operation = increment()  // Just a description, no execution
    |val program1 = for {
    |  a <- operation
    |  b <- operation
    |} yield (a, b)  // Running this gives (1, 1)
    |
    |val program2 = for {
    |  a <- increment()
    |  b <- increment()
    |} yield (a, b)  // Running this gives (1, 2)
    |
    |// But substitute increment() with operation:
    |val program3 = for {
    |  a <- operation
    |  b <- operation
    |} yield (a, b)  // Same as program1: (1, 1)
    |
    |// program1 and program3 are equivalent!
    |// Referentially transparent! ✅
    """.stripMargin
  }
  
  // ============================================================================
  // SUMMARY
  // ============================================================================
  
  def summary(): Unit = {
    println("\n=== Summary: Why Future Breaks RT ===")
    println("""
    |1. Eager evaluation: Executes immediately on creation
    |2. Not RT: Can't substitute future with its value
    |3. Immediate side effects: Can't separate description from execution
    |4. Composition issues: Can't build reusable operation descriptions
    |5. Testing difficulties: Side effects happen during test setup
    |6. Race conditions: Shared mutable state problems
    |7. Retry/repeat issues: Can't safely retry the same future
    |
    |Solution: IO[A] from Cats-Effect (Chapter 07)
    |- Lazy evaluation: Nothing happens until explicitly run
    |- Referentially transparent: Can substitute with values
    |- Separates description from execution
    |- Composable and testable
    |- Built-in retry, timeout, racing, etc.
    |
    |Rust comparison:
    |Rust's Future is lazy (like IO), but Scala's Future is eager.
    |IO[A] is more similar to Rust's Future trait.
    """.stripMargin)
  }
  
  // ============================================================================
  // DEMO FUNCTION
  // ============================================================================
  
  def main(args: Array[String]): Unit = {
    println("=== Future's Referential Transparency Problems ===\n")
    
    demonstrateEagerEvaluation()
    Thread.sleep(200)
    
    demonstrateNonRT()
    Thread.sleep(200)
    
    demonstrateSideEffects()
    Thread.sleep(200)
    
    demonstrateCompositionIssues()
    Thread.sleep(200)
    
    demonstrateTestingIssues()
    Thread.sleep(200)
    
    demonstrateRaceConditions()
    Thread.sleep(200)
    
    demonstrateRetryIssues()
    Thread.sleep(200)
    
    println(describeIOOperation())
    
    summary()
    
    // Give async operations time to complete
    Thread.sleep(500)
  }
}
