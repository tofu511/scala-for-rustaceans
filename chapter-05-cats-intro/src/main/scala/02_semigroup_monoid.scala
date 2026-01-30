package catsintro

import cats.Semigroup
import cats.Monoid
import cats.instances.all._
import cats.syntax.semigroup._
import cats.syntax.monoid._

// Semigroup and Monoid: Combining values
// These are fundamental type classes in Cats

object SemigroupMonoidExamples {
  
  // ============================================================================
  // SEMIGROUP: COMBINING VALUES
  // ============================================================================
  
  // Semigroup[A] provides a way to combine two values of type A
  
  // Definition:
  // trait Semigroup[A] {
  //   def combine(x: A, y: A): A
  // }
  
  // Law: Associativity
  // combine(x, combine(y, z)) == combine(combine(x, y), z)
  
  // Rust comparison:
  // Similar to implementing Add trait, but more general
  // impl Add for MyType {
  //     fn add(self, other: Self) -> Self { ... }
  // }
  
  def demonstrateSemigroup(): Unit = {
    import cats.syntax.semigroup._
    
    println("=== Semigroup ===\n")
    
    // Int has a Semigroup instance (addition)
    println("--- Int Semigroup ---")
    println(s"1 |+| 2 = ${1 |+| 2}")  // |+| is combine operator
    println(s"Semigroup[Int].combine(3, 4) = ${Semigroup[Int].combine(3, 4)}")
    
    // String has a Semigroup instance (concatenation)
    println("\n--- String Semigroup ---")
    println(s"'Hello' |+| ' World' = ${"Hello" |+| " World"}")
    
    // List has a Semigroup instance (concatenation)
    println("\n--- List Semigroup ---")
    println(s"List(1,2) |+| List(3,4) = ${List(1, 2) |+| List(3, 4)}")
    
    // Option has a Semigroup instance (if inner type has Semigroup)
    println("\n--- Option Semigroup ---")
    val opt1: Option[Int] = Some(1)
    val opt2: Option[Int] = Some(2)
    val optNone: Option[Int] = None
    println(s"Some(1) |+| Some(2) = ${opt1 |+| opt2}")
    println(s"Some(1) |+| None = ${opt1 |+| optNone}")
    println(s"None |+| Some(2) = ${optNone |+| opt2}")
    
    // Map has a Semigroup instance
    println("\n--- Map Semigroup ---")
    val map1 = Map("a" -> 1, "b" -> 2)
    val map2 = Map("b" -> 3, "c" -> 4)
    println(s"$map1 |+| $map2 = ${map1 |+| map2}")
  }
  
  // ============================================================================
  // CUSTOM SEMIGROUP INSTANCES
  // ============================================================================
  
  // Example: Combining sets with union
  case class MySet[A](values: Set[A])
  
  implicit def mySetSemigroup[A]: Semigroup[MySet[A]] = new Semigroup[MySet[A]] {
    def combine(x: MySet[A], y: MySet[A]): MySet[A] = {
      MySet(x.values ++ y.values)
    }
  }
  
  // Example: Combining orders (first one wins)
  sealed trait Order
  case object Ascending extends Order
  case object Descending extends Order
  
  implicit val firstWinsOrderSemigroup: Semigroup[Order] = new Semigroup[Order] {
    def combine(x: Order, y: Order): Order = x  // First wins
  }
  
  // Example: Max semigroup for Int
  val maxIntSemigroup: Semigroup[Int] = new Semigroup[Int] {
    def combine(x: Int, y: Int): Int = math.max(x, y)
  }
  
  // ============================================================================
  // MONOID: SEMIGROUP + IDENTITY
  // ============================================================================
  
  // Monoid[A] extends Semigroup[A] with an identity element
  
  // Definition:
  // trait Monoid[A] extends Semigroup[A] {
  //   def empty: A  // Identity element
  // }
  
  // Laws:
  // 1. Associativity: combine(x, combine(y, z)) == combine(combine(x, y), z)
  // 2. Left identity: combine(empty, x) == x
  // 3. Right identity: combine(x, empty) == x
  
  // Rust comparison:
  // Similar to Default trait + Add trait
  // impl Default for MyType { fn default() -> Self { ... } }
  // impl Add for MyType { fn add(self, other: Self) -> Self { ... } }
  
  def demonstrateMonoid(): Unit = {
    println("\n=== Monoid ===\n")
    
    // Int Monoid (addition, 0 is identity)
    println("--- Int Monoid ---")
    println(s"Monoid[Int].empty = ${Monoid[Int].empty}")
    println(s"Monoid[Int].combine(0, 5) = ${Monoid[Int].combine(0, 5)}")
    println(s"Monoid[Int].combine(5, 0) = ${Monoid[Int].combine(5, 0)}")
    
    // String Monoid (concatenation, "" is identity)
    println("\n--- String Monoid ---")
    println(s"Monoid[String].empty = '${Monoid[String].empty}'")
    println(s"Monoid[String].combine('', 'hello') = ${Monoid[String].combine("", "hello")}")
    
    // List Monoid (concatenation, Nil is identity)
    println("\n--- List Monoid ---")
    println(s"Monoid[List[Int]].empty = ${Monoid[List[Int]].empty}")
    println(s"Monoid[List[Int]].combine(Nil, List(1,2)) = ${Monoid[List[Int]].combine(Nil, List(1, 2))}")
    
    // Option Monoid
    println("\n--- Option Monoid ---")
    println(s"Monoid[Option[Int]].empty = ${Monoid[Option[Int]].empty}")
    println(s"Monoid[Option[Int]].combine(None, Some(5)) = ${Monoid[Option[Int]].combine(None, Some(5))}")
  }
  
  // ============================================================================
  // CUSTOM MONOID INSTANCES
  // ============================================================================
  
  // Example: Boolean with OR
  val booleanOrMonoid: Monoid[Boolean] = new Monoid[Boolean] {
    def combine(x: Boolean, y: Boolean): Boolean = x || y
    def empty: Boolean = false
  }
  
  // Example: Boolean with AND
  val booleanAndMonoid: Monoid[Boolean] = new Monoid[Boolean] {
    def combine(x: Boolean, y: Boolean): Boolean = x && y
    def empty: Boolean = true
  }
  
  // ============================================================================
  // PRACTICAL APPLICATIONS
  // ============================================================================
  
  // 1. COMBINING MULTIPLE VALUES
  
  def combineAll[A: Monoid](values: List[A]): A = {
    import cats.syntax.semigroup._
    values.foldLeft(Monoid[A].empty)(_ |+| _)
  }
  
  // Cats provides combineAll on List
  def demonstrateCombineAll(): Unit = {
    println("\n=== Combining Multiple Values ===\n")
    
    println(s"combineAll(List(1, 2, 3, 4)) = ${combineAll(List(1, 2, 3, 4))}")
    println(s"combineAll(List('a', 'b', 'c')) = ${combineAll(List("a", "b", "c"))}")
    println(s"combineAll(List()) = ${combineAll(List.empty[Int])}")
    
    // Using Cats combineAll
    import cats.syntax.foldable._
    println(s"List(1,2,3,4).combineAll = ${List(1, 2, 3, 4).combineAll}")
  }
  
  // 2. MERGING CONFIGURATIONS
  
  case class Config(
    host: Option[String],
    port: Option[Int],
    timeout: Option[Int]
  )
  
  object Config {
    import cats.syntax.semigroup._
    
    implicit val configMonoid: Monoid[Config] = new Monoid[Config] {
      def combine(x: Config, y: Config): Config = Config(
        host = x.host |+| y.host,      // First Some wins
        port = x.port |+| y.port,
        timeout = x.timeout |+| y.timeout
      )
      
      def empty: Config = Config(None, None, None)
    }
  }
  
  def demonstrateConfigMerging(): Unit = {
    import cats.syntax.semigroup._
    
    println("\n=== Config Merging ===\n")
    
    val defaultConfig = Config(Some("localhost"), Some(8080), Some(30))
    val userConfig = Config(Some("example.com"), None, Some(60))
    val envConfig = Config(None, Some(9000), None)
    
    val merged = defaultConfig |+| userConfig |+| envConfig
    println(s"Merged config: $merged")
  }
  
  // 3. ACCUMULATING ANALYTICS
  
  case class Analytics(
    visits: Int,
    uniqueUsers: Set[String],
    errors: List[String]
  )
  
  object Analytics {
    implicit val analyticsMonoid: Monoid[Analytics] = new Monoid[Analytics] {
      def combine(x: Analytics, y: Analytics): Analytics = Analytics(
        visits = x.visits + y.visits,
        uniqueUsers = x.uniqueUsers ++ y.uniqueUsers,
        errors = x.errors ++ y.errors
      )
      
      def empty: Analytics = Analytics(0, Set.empty, List.empty)
    }
  }
  
  def demonstrateAnalytics(): Unit = {
    println("\n=== Analytics Accumulation ===\n")
    
    val day1 = Analytics(100, Set("user1", "user2"), List("error1"))
    val day2 = Analytics(150, Set("user2", "user3"), List())
    val day3 = Analytics(120, Set("user1", "user4"), List("error2"))
    
    val total = combineAll(List(day1, day2, day3))
    println(s"Total analytics: $total")
  }
  
  // 4. PARALLEL AGGREGATION
  
  def parallelSum(numbers: List[Int]): Int = {
    import cats.syntax.foldable._
    // In a real app, this would use parallel collections or Cats-Effect
    // For demonstration, we just combine
    numbers.combineAll
  }
  
  // ============================================================================
  // LAWS AND TESTING
  // ============================================================================
  
  def checkSemigroupLaws[A: Semigroup](x: A, y: A, z: A): Boolean = {
    val sg = Semigroup[A]
    
    // Associativity
    sg.combine(x, sg.combine(y, z)) == sg.combine(sg.combine(x, y), z)
  }
  
  def checkMonoidLaws[A: Monoid](x: A, y: A, z: A): Boolean = {
    val m = Monoid[A]
    
    // Semigroup associativity
    val associative = m.combine(x, m.combine(y, z)) == m.combine(m.combine(x, y), z)
    
    // Left identity
    val leftIdentity = m.combine(m.empty, x) == x
    
    // Right identity
    val rightIdentity = m.combine(x, m.empty) == x
    
    associative && leftIdentity && rightIdentity
  }
  
  def demonstrateLaws(): Unit = {
    println("\n=== Monoid Laws ===\n")
    
    println(s"Int monoid laws hold: ${checkMonoidLaws(1, 2, 3)}")
    println(s"String monoid laws hold: ${checkMonoidLaws("a", "b", "c")}")
    println(s"List monoid laws hold: ${checkMonoidLaws(List(1), List(2), List(3))}")
  }
  
  // ============================================================================
  // RUST COMPARISON
  // ============================================================================
  
  // Rust doesn't have built-in Semigroup/Monoid traits,
  // but you can implement similar patterns:
  
  // trait Semigroup {
  //     fn combine(self, other: Self) -> Self;
  // }
  //
  // trait Monoid: Semigroup {
  //     fn empty() -> Self;
  // }
  //
  // impl Semigroup for i32 {
  //     fn combine(self, other: i32) -> i32 {
  //         self + other
  //     }
  // }
  //
  // impl Monoid for i32 {
  //     fn empty() -> i32 { 0 }
  // }
  //
  // fn combine_all<M: Monoid>(values: Vec<M>) -> M {
  //     values.into_iter().fold(M::empty(), |acc, x| acc.combine(x))
  // }
  
  // Rust's Add trait is similar to Semigroup
  // Rust's Default trait is similar to Monoid's empty
  
  // ============================================================================
  // DEMO FUNCTION
  // ============================================================================
  
  def main(args: Array[String]): Unit = {
    println("=== Semigroup and Monoid Examples ===\n")
    
    demonstrateSemigroup()
    demonstrateMonoid()
    demonstrateCombineAll()
    demonstrateConfigMerging()
    demonstrateAnalytics()
    demonstrateLaws()
    
    println("\n--- Summary ---")
    println("Semigroup: Combine values associatively")
    println("Monoid: Semigroup + identity element")
    println("Use cases:")
    println("- Merging configurations")
    println("- Accumulating analytics")
    println("- Parallel aggregation")
    println("- Reducing collections")
  }
}
