package referentialtransparency.exercises.solutions

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case class User(id: Int, name: String)

object Exercise02FutureSolution {
  
  var database = Map(1 -> User(1, "Alice"), 2 -> User(2, "Bob"))
  var dbCallCount = 0
  
  def fetchFromDB(id: Int): Future[Option[User]] = Future {
    dbCallCount += 1
    println(s"DB call #$dbCallCount for user $id")
    database.get(id)
  }
  
  var sideEffectCounter = 0
  
  def createFuture(): Future[Int] = Future {
    sideEffectCounter += 1
    sideEffectCounter
  }
  
  def getUserUppercase(id: Int): Future[Option[String]] = {
    fetchFromDB(id).map(_.map(_.name.toUpperCase))
  }
  
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
  
  def lazyProgram(): LazyFuture[(Int, Int)] = {
    for {
      a <- incrementLazy()
      b <- incrementLazy()
    } yield (a, b)
  }
  
  def main(args: Array[String]): Unit = {
    println("=== Exercise 02 Solution ===\n")
    
    // Eager evaluation demo
    println("--- Eager Evaluation ---")
    sideEffectCounter = 0
    println(s"Before: $sideEffectCounter")
    val future = createFuture()
    println("Created future...")
    Thread.sleep(100)
    println(s"After (no await yet): $sideEffectCounter")
    
    // Database operations
    println("\n--- Database Operations ---")
    dbCallCount = 0
    val result1 = Await.result(getUserUppercase(1), 1.second)
    println(s"Result: $result1, DB calls: $dbCallCount")
    
    // Lazy vs Eager
    println("\n--- Lazy Program ---")
    lazyCounter = 0
    val lazyProg = lazyProgram()
    println(s"Defined program, counter: $lazyCounter")
    val lazyResult = Await.result(lazyProg.run(), 1.second)
    println(s"Executed program: $lazyResult, counter: $lazyCounter")
    
    println("\n✓ Solution demonstrated!")
    Thread.sleep(100)
  }
}
