package errorhandling.exercises.solutions

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

case class UserProfile(id: Int, name: String, age: Int)

object Exercise03FutureSolution {
  
  def simulateDelay(ms: Int): Unit = Thread.sleep(ms)
  
  def fetchUserName(id: Int): Future[String] = Future {
    simulateDelay(200)
    if (id >= 1 && id <= 5) s"User$id"
    else throw new Exception(s"User not found: $id")
  }
  
  def fetchUserAge(id: Int): Future[Int] = Future {
    simulateDelay(200)
    if (id >= 1 && id <= 5) id * 10
    else throw new Exception(s"User not found: $id")
  }
  
  def fetchUserProfile(id: Int): Future[UserProfile] = {
    // Start both futures BEFORE the for-comprehension (parallel)
    val nameFuture = fetchUserName(id)
    val ageFuture = fetchUserAge(id)
    
    for {
      name <- nameFuture
      age <- ageFuture
    } yield UserProfile(id, name, age)
  }
  
  def fetchMultipleProfiles(ids: List[Int]): Future[List[UserProfile]] = {
    val futures = ids.map(fetchUserProfile)
    Future.sequence(futures)
  }
  
  def firstCompleted(f1: Future[Int], f2: Future[Int]): Future[Int] = {
    Future.firstCompletedOf(Seq(f1, f2))
  }
  
  def main(args: Array[String]): Unit = {
    println("=== Exercise 03 Solution ===\n")
    
    println("--- fetchUserName ---")
    val name = Await.result(fetchUserName(1), 1.second)
    println(s"fetchUserName(1) = $name")
    
    println("\n--- fetchUserAge ---")
    val age = Await.result(fetchUserAge(1), 1.second)
    println(s"fetchUserAge(1) = $age")
    
    println("\n--- fetchUserProfile (Parallel) ---")
    val start = System.currentTimeMillis()
    val profile = Await.result(fetchUserProfile(1), 1.second)
    val duration = System.currentTimeMillis() - start
    println(s"fetchUserProfile(1) = $profile")
    println(s"Time taken: ${duration}ms (should be ~200ms, not 400ms)")
    
    println("\n--- fetchMultipleProfiles ---")
    val start2 = System.currentTimeMillis()
    val profiles = Await.result(fetchMultipleProfiles(List(1, 2, 3)), 2.seconds)
    val duration2 = System.currentTimeMillis() - start2
    println(s"Fetched ${profiles.length} profiles")
    println(s"Time taken: ${duration2}ms (parallel)")
    
    println("\n--- firstCompleted ---")
    val fast = Future { simulateDelay(100); 1 }
    val slow = Future { simulateDelay(500); 2 }
    val result = Await.result(firstCompleted(fast, slow), 1.second)
    println(s"firstCompleted = $result (should be 1)")
    
    // Give async operations time to complete
    Thread.sleep(100)
  }
}
