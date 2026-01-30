package errorhandling

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

// Future - Async computation that will complete in the future
// Similar to Rust's Future but with important differences!

object FutureExamples {
  
  // ============================================================================
  // FUTURE BASICS
  // ============================================================================
  
  // Future[A] represents an async computation that will produce A
  val simpleFuture: Future[Int] = Future {
    Thread.sleep(100)
    42
  }
  
  // WARNING: Future starts executing immediately (eager evaluation)
  // This is different from Rust's Future which is lazy!
  
  // Rust comparison:
  // Rust: let future = async { ... };  // Doesn't start until awaited
  // Scala: val future = Future { ... }  // Starts immediately!
  
  // ============================================================================
  // CREATING FUTURES
  // ============================================================================
  
  def expensiveComputation(): Future[Int] = Future {
    Thread.sleep(1000)
    println("Expensive computation done!")
    42
  }
  
  def fetchUser(id: Int): Future[String] = Future {
    Thread.sleep(500)
    s"User$id"
  }
  
  def fetchOrders(userId: String): Future[List[String]] = Future {
    Thread.sleep(500)
    List(s"Order1-$userId", s"Order2-$userId")
  }
  
  // Successful and failed futures
  val successFuture: Future[Int] = Future.successful(42)
  val failedFuture: Future[Int] = Future.failed(new Exception("Failed"))
  
  // ============================================================================
  // WAITING FOR RESULTS (BLOCKING - AVOID IN PRODUCTION!)
  // ============================================================================
  
  def blockingExample(): Unit = {
    val future = Future { 42 }
    val result = Await.result(future, 5.seconds)  // Blocks current thread
    println(s"Result: $result")
  }
  
  // Rust comparison:
  // Rust: let result = future.await;  // Suspends, doesn't block
  // Scala: val result = Await.result(future, duration)  // BLOCKS thread
  
  // ============================================================================
  // MAP - TRANSFORMING RESULTS
  // ============================================================================
  
  val doubled: Future[Int] = simpleFuture.map(_ * 2)
  val asString: Future[String] = simpleFuture.map(_.toString)
  
  // Chaining transformations
  val transformed: Future[String] = Future(42)
    .map(_ * 2)      // Future(84)
    .map(_ + 10)     // Future(94)
    .map(_.toString)  // Future("94")
  
  // ============================================================================
  // FLATMAP - CHAINING ASYNC OPERATIONS
  // ============================================================================
  
  def getUserOrders(userId: Int): Future[List[String]] = {
    fetchUser(userId).flatMap { user =>
      fetchOrders(user)
    }
  }
  
  // With for-comprehension
  def getUserOrdersFor(userId: Int): Future[List[String]] = {
    for {
      user <- fetchUser(userId)
      orders <- fetchOrders(user)
    } yield orders
  }
  
  // Rust comparison:
  // async fn get_user_orders(user_id: i32) -> Vec<String> {
  //     let user = fetch_user(user_id).await;
  //     fetch_orders(&user).await
  // }
  
  // ============================================================================
  // ERROR HANDLING
  // ============================================================================
  
  def riskyOperation(): Future[Int] = Future {
    if (scala.util.Random.nextBoolean()) 42
    else throw new Exception("Random failure!")
  }
  
  // recover: handle failure
  val recovered: Future[Int] = riskyOperation().recover {
    case _: Exception => 0
  }
  
  // recoverWith: handle failure with another Future
  val recoveredWith: Future[Int] = riskyOperation().recoverWith {
    case _: Exception => Future.successful(0)
  }
  
  // fallbackTo: try alternative if first fails
  val withFallback: Future[Int] = riskyOperation().fallbackTo(Future.successful(0))
  
  // ============================================================================
  // CALLBACKS
  // ============================================================================
  
  def callbackExample(): Unit = {
    val future = Future { 42 }
    
    // onComplete: called when future completes (success or failure)
    future.onComplete {
      case Success(value) => println(s"Success: $value")
      case Failure(ex) => println(s"Failure: ${ex.getMessage}")
    }
    
    // andThen: side effects (doesn't change the future)
    future.andThen {
      case Success(value) => println(s"Logging: $value")
    }
  }
  
  // ============================================================================
  // COMBINING FUTURES
  // ============================================================================
  
  // Sequential execution (one after another)
  def sequential(): Future[(String, List[String])] = {
    for {
      user <- fetchUser(1)
      orders <- fetchOrders(user)
    } yield (user, orders)
  }
  
  // Parallel execution (both at same time)
  def parallel(): Future[(String, String)] = {
    val user1Future = fetchUser(1)  // Starts immediately
    val user2Future = fetchUser(2)  // Starts immediately
    
    for {
      user1 <- user1Future  // Wait for both
      user2 <- user2Future
    } yield (user1, user2)
  }
  
  // Using Future.sequence
  def fetchMultipleUsers(ids: List[Int]): Future[List[String]] = {
    val futures = ids.map(fetchUser)  // List[Future[String]]
    Future.sequence(futures)  // Future[List[String]]
  }
  
  // Rust comparison:
  // Rust: tokio::join!(future1, future2)  // Parallel
  // Scala: Future.sequence(List(future1, future2))
  
  // ============================================================================
  // TIMEOUT
  // ============================================================================
  
  def withTimeout[A](future: Future[A], timeout: Duration): Future[A] = {
    val timeoutFuture = Future {
      Thread.sleep(timeout.toMillis)
      throw new Exception("Timeout!")
    }
    Future.firstCompletedOf(Seq(future, timeoutFuture))
  }
  
  // ============================================================================
  // PRACTICAL EXAMPLE: API CLIENT
  // ============================================================================
  
  case class UserProfile(id: Int, name: String, email: String)
  case class UserStats(userId: Int, posts: Int, followers: Int)
  
  def fetchProfile(id: Int): Future[UserProfile] = Future {
    Thread.sleep(300)
    UserProfile(id, s"User$id", s"user$id@example.com")
  }
  
  def fetchStats(id: Int): Future[UserStats] = Future {
    Thread.sleep(300)
    UserStats(id, 42, 100)
  }
  
  def getUserData(id: Int): Future[(UserProfile, UserStats)] = {
    // These run in parallel!
    val profileFuture = fetchProfile(id)
    val statsFuture = fetchStats(id)
    
    for {
      profile <- profileFuture
      stats <- statsFuture
    } yield (profile, stats)
  }
  
  // ============================================================================
  // THE PROBLEM WITH FUTURE (teaser for Chapter 04)
  // ============================================================================
  
  // Future is NOT referentially transparent!
  var counter = 0
  
  def incrementCounter(): Future[Int] = Future {
    counter += 1
    println(s"Counter incremented to: $counter")
    counter
  }
  
  // Problem: Each call creates a new Future that executes immediately
  def demonstrateProblem(): Unit = {
    println("\n--- The Problem with Future ---")
    
    // These are NOT the same!
    val future1 = incrementCounter()
    val result1 = for {
      a <- future1
      b <- future1
    } yield (a, b)
    
    val result2 = for {
      a <- incrementCounter()
      b <- incrementCounter()
    } yield (a, b)
    
    Thread.sleep(100)
    // result1: Future((1, 1))  - same future used twice
    // result2: Future((2, 3))  - new future created each time
    
    println("This breaks referential transparency!")
    println("We'll learn how to fix this in Chapter 04 with IO[A]")
  }
  
  // ============================================================================
  // DEMO FUNCTION
  // ============================================================================
  
  def main(args: Array[String]): Unit = {
    println("=== Future Examples ===\n")
    
    // Basic future
    println("--- Basic Future ---")
    val basicResult = Await.result(simpleFuture, 1.second)
    println(s"simpleFuture result: $basicResult")
    
    // Map
    println("\n--- Map ---")
    val doubledResult = Await.result(doubled, 1.second)
    println(s"doubled result: $doubledResult")
    
    // FlatMap
    println("\n--- FlatMap (Sequential) ---")
    val ordersResult = Await.result(getUserOrders(1), 2.seconds)
    println(s"User orders: $ordersResult")
    
    // Parallel execution
    println("\n--- Parallel Execution ---")
    val start = System.currentTimeMillis()
    val parallelResult = Await.result(parallel(), 2.seconds)
    val duration = System.currentTimeMillis() - start
    println(s"Parallel result: $parallelResult")
    println(s"Time taken: ${duration}ms (should be ~500ms, not 1000ms)")
    
    // Multiple users
    println("\n--- Multiple Users ---")
    val multipleUsers = Await.result(fetchMultipleUsers(List(1, 2, 3)), 2.seconds)
    println(s"Multiple users: $multipleUsers")
    
    // User data (parallel)
    println("\n--- User Data (Parallel) ---")
    val start2 = System.currentTimeMillis()
    val userData = Await.result(getUserData(1), 2.seconds)
    val duration2 = System.currentTimeMillis() - start2
    println(s"User data: $userData")
    println(s"Time taken: ${duration2}ms (parallel, should be ~300ms)")
    
    // The problem with Future
    demonstrateProblem()
    
    // Give async operations time to complete
    Thread.sleep(500)
  }
}
