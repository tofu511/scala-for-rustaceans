package errorhandling.exercises

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

/*
 * EXERCISE 03: Future - Async Computation
 *
 * OBJECTIVES:
 * - Practice using Future for async operations
 * - Chain async operations with map and flatMap
 * - Run operations in parallel
 * - Handle timeouts and errors
 *
 * RUST COMPARISON:
 * Similar to Rust's async/await, but with key differences:
 * - Scala Future is EAGER (starts immediately)
 * - Rust Future is LAZY (doesn't start until .await)
 * - Scala blocks with Await.result (avoid in production!)
 * - Rust suspends with .await (doesn't block thread)
 *
 * IMPORTANT: Future is NOT referentially transparent!
 * We'll learn about IO[A] in Chapter 07 which solves this.
 *
 * TASKS:
 * 1. Implement fetchUserName: simulate async DB lookup
 * 2. Implement fetchUserAge: simulate async DB lookup
 * 3. Implement fetchUserProfile: combine both lookups
 * 4. Implement fetchMultipleProfiles: parallel execution
 * 5. Uncomment tests in main() to verify your implementation
 *
 * HOW TO RUN:
 *   cd chapter-03-error-handling
 *   sbt "runMain errorhandling.exercises.Exercise03Future"
 *
 * EXPECTED OUTPUT:
 * All test assertions should pass.
 * Parallel operations should complete faster than sequential.
 */

case class UserProfile(id: Int, name: String, age: Int)

object Exercise03Future {
  
  // Simulate database delay
  def simulateDelay(ms: Int): Unit = Thread.sleep(ms)
  
  // TODO: Implement fetchUserName
  // Simulate fetching user name from database (200ms delay)
  // Return Future[String]
  // Should return "User{id}" for ids 1-5, fail for others
  def fetchUserName(id: Int): Future[String] = {
    ???
  }
  
  // TODO: Implement fetchUserAge
  // Simulate fetching user age from database (200ms delay)
  // Return Future[Int]
  // Should return id * 10 for ids 1-5, fail for others
  def fetchUserAge(id: Int): Future[Int] = {
    ???
  }
  
  // TODO: Implement fetchUserProfile
  // Fetch name and age in PARALLEL, combine into UserProfile
  // Hint: Start both futures before the for-comprehension
  def fetchUserProfile(id: Int): Future[UserProfile] = {
    ???
  }
  
  // TODO: Implement fetchMultipleProfiles
  // Fetch multiple profiles in PARALLEL
  // Use Future.sequence to combine List[Future[UserProfile]]
  def fetchMultipleProfiles(ids: List[Int]): Future[List[UserProfile]] = {
    ???
  }
  
  // TODO: Implement firstCompleted
  // Return the result of whichever future completes first
  // Hint: Use Future.firstCompletedOf
  def firstCompleted(f1: Future[Int], f2: Future[Int]): Future[Int] = {
    ???
  }
  
  def main(args: Array[String]): Unit = {
    println("=== Exercise 03: Future ===\n")
    
    // Test fetchUserName
    println("--- Testing fetchUserName ---")
    // TODO: Uncomment these tests
    /*
    val name1 = Await.result(fetchUserName(1), 1.second)
    assert(name1 == "User1")
    
    val name2 = Await.result(fetchUserName(2), 1.second)
    assert(name2 == "User2")
    
    val failedName = fetchUserName(999)
    assert(Await.result(failedName.map(_ => false).recover { case _ => true }, 1.second))
    println("✓ fetchUserName tests passed")
    */
    
    // Test fetchUserAge
    println("\n--- Testing fetchUserAge ---")
    // TODO: Uncomment these tests
    /*
    val age1 = Await.result(fetchUserAge(1), 1.second)
    assert(age1 == 10)
    
    val age2 = Await.result(fetchUserAge(2), 1.second)
    assert(age2 == 20)
    println("✓ fetchUserAge tests passed")
    */
    
    // Test fetchUserProfile (parallel execution)
    println("\n--- Testing fetchUserProfile (Parallel) ---")
    // TODO: Uncomment these tests
    /*
    val start = System.currentTimeMillis()
    val profile = Await.result(fetchUserProfile(1), 1.second)
    val duration = System.currentTimeMillis() - start
    
    assert(profile == UserProfile(1, "User1", 10))
    assert(duration < 300, s"Should complete in ~200ms (parallel), took ${duration}ms")
    println(s"✓ fetchUserProfile tests passed (took ${duration}ms)")
    */
    
    // Test fetchMultipleProfiles
    println("\n--- Testing fetchMultipleProfiles (Parallel) ---")
    // TODO: Uncomment these tests
    /*
    val start2 = System.currentTimeMillis()
    val profiles = Await.result(fetchMultipleProfiles(List(1, 2, 3)), 2.seconds)
    val duration2 = System.currentTimeMillis() - start2
    
    assert(profiles.length == 3)
    assert(profiles.head == UserProfile(1, "User1", 10))
    assert(duration2 < 500, s"Should complete in ~200ms (parallel), took ${duration2}ms")
    println(s"✓ fetchMultipleProfiles tests passed (took ${duration2}ms)")
    */
    
    // Test firstCompleted
    println("\n--- Testing firstCompleted ---")
    // TODO: Uncomment these tests
    /*
    val fast = Future { simulateDelay(100); 1 }
    val slow = Future { simulateDelay(500); 2 }
    val result = Await.result(firstCompleted(fast, slow), 1.second)
    assert(result == 1, "Should return result of faster future")
    println("✓ firstCompleted tests passed")
    */
    
    println("\n=== All tests passed! ===")
    
    // Give async operations time to complete
    Thread.sleep(100)
  }
}

/*
 * SOLUTION (Don't peek until you've tried!)
 * 
 * def fetchUserName(id: Int): Future[String] = Future {
 *   simulateDelay(200)
 *   if (id >= 1 && id <= 5) s"User$id"
 *   else throw new Exception(s"User not found: $id")
 * }
 * 
 * def fetchUserAge(id: Int): Future[Int] = Future {
 *   simulateDelay(200)
 *   if (id >= 1 && id <= 5) id * 10
 *   else throw new Exception(s"User not found: $id")
 * }
 * 
 * def fetchUserProfile(id: Int): Future[UserProfile] = {
 *   val nameFuture = fetchUserName(id)
 *   val ageFuture = fetchUserAge(id)
 *   
 *   for {
 *     name <- nameFuture
 *     age <- ageFuture
 *   } yield UserProfile(id, name, age)
 * }
 * 
 * def fetchMultipleProfiles(ids: List[Int]): Future[List[UserProfile]] = {
 *   val futures = ids.map(fetchUserProfile)
 *   Future.sequence(futures)
 * }
 * 
 * def firstCompleted(f1: Future[Int], f2: Future[Int]): Future[Int] = {
 *   Future.firstCompletedOf(Seq(f1, f2))
 * }
 */
