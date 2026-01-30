package referentialtransparency.exercises

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/*
 * EXERCISE 02: Understanding Future's RT Problems
 *
 * OBJECTIVES:
 * - Experience firsthand why Future breaks RT
 * - Understand eager vs lazy evaluation
 * - See the impact on testing and composition
 *
 * RUST COMPARISON:
 * Rust's Future is lazy (like IO will be), not eager like Scala's Future.
 * async { ... } in Rust creates a lazy future that doesn't execute until awaited.
 * Scala's Future { ... } starts executing immediately!
 *
 * TASKS:
 * 1. Predict the output of programs using Future
 * 2. Demonstrate the non-RT behavior
 * 3. Compare eager vs lazy evaluation
 * 4. Uncomment tests to verify your understanding
 *
 * HOW TO RUN:
 *   cd chapter-04-referential-transparency
 *   sbt "runMain referentialtransparency.exercises.Exercise02Future"
 *
 * EXPECTED OUTPUT:
 * You'll see that Future behaves differently based on how you use it.
 */

object Exercise02Future {
  
  // ============================================================================
  // PART 1: PREDICT THE BEHAVIOR
  // ============================================================================
  
  var counter1 = 0
  def increment1(): Future[Int] = Future {
    counter1 += 1
    counter1
  }
  
  // TODO: Before running, predict what these will print:
  def scenario1(): Unit = {
    println("\n--- Scenario 1: Reusing Same Future ---")
    counter1 = 0
    
    val future = increment1()
    Thread.sleep(100)  // Wait for completion
    
    val program = for {
      a <- future
      b <- future
    } yield (a, b)
    
    val result = Await.result(program, 1.second)
    println(s"Result: $result")
    println(s"Counter: $counter1")
    
    // TODO: What do you expect?
    // Result: (?, ?)
    // Counter: ?
  }
  
  def scenario2(): Unit = {
    println("\n--- Scenario 2: Creating New Futures ---")
    counter1 = 0
    
    val program = for {
      a <- increment1()
      b <- increment1()
    } yield (a, b)
    
    val result = Await.result(program, 1.second)
    println(s"Result: $result")
    println(s"Counter: $counter1")
    
    // TODO: What do you expect?
    // Result: (?, ?)
    // Counter: ?
  }
  
  // ============================================================================
  // PART 2: DEMONSTRATE EAGER EVALUATION
  // ============================================================================
  
  var sideEffectCounter = 0
  
  // TODO: Implement a function that creates a Future
  // It should increment sideEffectCounter and return it
  def createFuture(): Future[Int] = {
    ???
  }
  
  def testEagerEvaluation(): Unit = {
    println("\n--- Part 2: Eager Evaluation ---")
    sideEffectCounter = 0
    
    println("Before creating future:")
    println(s"Counter: $sideEffectCounter")
    
    // TODO: Create a future (don't run it yet!)
    val future = createFuture()
    
    println("After creating future (before waiting):")
    Thread.sleep(100)
    println(s"Counter: $sideEffectCounter")
    
    println("\nNotice: Side effect happened just by creating the Future!")
  }
  
  // ============================================================================
  // PART 3: TESTING CHALLENGES
  // ============================================================================
  
  case class User(id: Int, name: String)
  var database = Map(1 -> User(1, "Alice"), 2 -> User(2, "Bob"))
  var dbCallCount = 0
  
  def fetchFromDB(id: Int): Future[Option[User]] = Future {
    dbCallCount += 1
    println(s"DB call #$dbCallCount for user $id")
    database.get(id)
  }
  
  // TODO: Implement a function that fetches a user and transforms the result
  // It should use fetchFromDB and map the name to uppercase
  def getUserUppercase(id: Int): Future[Option[String]] = {
    ???
  }
  
  def testDatabaseOperations(): Unit = {
    println("\n--- Part 3: Testing Challenges ---")
    dbCallCount = 0
    
    // Test 1: Check that user is fetched correctly
    val result1 = Await.result(getUserUppercase(1), 1.second)
    println(s"Test 1 result: $result1")
    println(s"DB calls so far: $dbCallCount")
    
    // Test 2: Check non-existent user
    val result2 = Await.result(getUserUppercase(999), 1.second)
    println(s"Test 2 result: $result2")
    println(s"DB calls so far: $dbCallCount")
    
    println("\nProblem: Each test execution makes real DB calls!")
    println("With IO, we could test the description without executing.")
  }
  
  // ============================================================================
  // PART 4: COMPARE WITH LAZY ALTERNATIVE
  // ============================================================================
  
  // A simple lazy wrapper
  case class LazyFuture[A](create: () => Future[A]) {
    def run(): Future[A] = create()
    
    def map[B](f: A => B): LazyFuture[B] = {
      LazyFuture(() => create().map(f))
    }
    
    def flatMap[B](f: A => LazyFuture[B]): LazyFuture[B] = {
      LazyFuture(() => create().flatMap(a => f(a).run()))
    }
  }
  
  var lazyCounter = 0
  def incrementLazy(): LazyFuture[Int] = LazyFuture(() => Future {
    lazyCounter += 1
    lazyCounter
  })
  
  // TODO: Implement a program that uses LazyFuture
  // It should increment twice and return the tuple
  def lazyProgram(): LazyFuture[(Int, Int)] = {
    ???
    // Hint: Use for-comprehension
  }
  
  def compareLazyEager(): Unit = {
    println("\n--- Part 4: Lazy vs Eager ---")
    
    // Eager Future
    println("Eager Future:")
    counter1 = 0
    val eagerFuture = increment1()
    println("Created, but already executing!")
    Thread.sleep(100)
    println(s"Counter: $counter1")
    
    // Lazy Future
    println("\nLazy Future:")
    lazyCounter = 0
    val lazyFut = incrementLazy()
    println("Created (just a description)")
    println(s"Counter: $lazyCounter")  // Should still be 0
    
    println("Now explicitly running...")
    val result = Await.result(lazyFut.run(), 1.second)
    println(s"Result: $result, Counter: $lazyCounter")
    
    println("\nLazy gives us control over when effects happen!")
  }
  
  def main(args: Array[String]): Unit = {
    println("=== Exercise 02: Future's RT Problems ===")
    
    // Run scenarios
    scenario1()
    scenario2()
    
    println("\nNotice: scenario1 and scenario2 produce different results!")
    println("This is because Future is not referentially transparent.")
    
    // TODO: Uncomment to run additional parts
    /*
    testEagerEvaluation()
    testDatabaseOperations()
    compareLazyEager()
    
    println("\n--- Summary ---")
    println("Future problems:")
    println("1. Eager: Executes immediately on creation")
    println("2. Not RT: Can't substitute future with its value")
    println("3. Testing: Side effects happen during setup")
    println("4. Composition: Hard to build reusable descriptions")
    println("\nSolution: Use IO[A] (Chapter 7) which is lazy and RT!")
    */
    
    println("\n=== Uncomment tests to explore more ===")
    
    // Give async ops time to complete
    Thread.sleep(100)
  }
}

/*
 * SOLUTION (Don't peek until you've tried!)
 *
 * Scenario 1 predictions:
 *   Result: (1, 1)  - Same future reused
 *   Counter: 1      - Only executed once
 *
 * Scenario 2 predictions:
 *   Result: (1, 2)  - New futures created
 *   Counter: 2      - Executed twice
 *
 * def createFuture(): Future[Int] = Future {
 *   sideEffectCounter += 1
 *   sideEffectCounter
 * }
 *
 * def getUserUppercase(id: Int): Future[Option[String]] = {
 *   fetchFromDB(id).map(_.map(_.name.toUpperCase))
 * }
 *
 * def lazyProgram(): LazyFuture[(Int, Int)] = {
 *   for {
 *     a <- incrementLazy()
 *     b <- incrementLazy()
 *   } yield (a, b)
 * }
 */
