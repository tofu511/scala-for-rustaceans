package fundamentals

// Scala Collections - Immutable by Default
// Similar to Rust but with richer API

object Collections {
  
  // ============================================================================
  // LIST - Immutable Linked List
  // ============================================================================
  
  // Creating lists
  val numbers = List(1, 2, 3, 4, 5)
  val empty = List.empty[Int]
  val range = List.range(1, 6)  // 1, 2, 3, 4, 5 (6 is excluded)
  val repeated = List.fill(3)("hello")  // List("hello", "hello", "hello")
  
  // Cons operator :: (prepend)
  val list1 = 1 :: 2 :: 3 :: Nil  // Nil is the empty list
  val prepended = 0 :: numbers     // List(0, 1, 2, 3, 4, 5)
  
  // Append (less efficient than prepend)
  val appended = numbers :+ 6      // List(1, 2, 3, 4, 5, 6)
  
  // Concatenation
  val combined = List(1, 2) ++ List(3, 4)  // List(1, 2, 3, 4)
  
  // Rust comparison:
  // Rust: let numbers = vec![1, 2, 3, 4, 5];
  // Scala: val numbers = List(1, 2, 3, 4, 5)
  // Note: Scala's List is immutable, Rust's Vec is mutable
  
  // ============================================================================
  // COMMON OPERATIONS
  // ============================================================================
  
  // Accessing elements
  val first = numbers.head         // 1 (throws if empty!)
  val rest = numbers.tail          // List(2, 3, 4, 5)
  val last = numbers.last          // 5
  val secondopt = numbers.lift(1)  // Some(2) - safe access
  
  // Safe alternatives
  val headOpt = numbers.headOption  // Some(1)
  val emptyHead = empty.headOption  // None
  
  // Size and checks
  val size = numbers.length         // 5
  val isEmpty = numbers.isEmpty     // false
  val nonEmpty = numbers.nonEmpty   // true
  
  // ============================================================================
  // MAP, FILTER, FOREACH
  // ============================================================================
  
  // map - transform each element
  val doubled = numbers.map(_ * 2)          // List(2, 4, 6, 8, 10)
  val strings = numbers.map(_.toString)     // List("1", "2", "3", "4", "5")
  
  // filter - keep elements matching predicate
  val evens = numbers.filter(_ % 2 == 0)    // List(2, 4)
  val odds = numbers.filterNot(_ % 2 == 0)  // List(1, 3, 5)
  
  // foreach - side effects (returns Unit)
  numbers.foreach(n => println(n))
  
  // Rust comparison:
  // Rust: numbers.iter().map(|x| x * 2).collect::<Vec<_>>()
  // Scala: numbers.map(_ * 2)
  
  // ============================================================================
  // REDUCE AND FOLD
  // ============================================================================
  
  // sum, product, min, max
  val sum = numbers.sum              // 15
  val product = numbers.product      // 120
  val min = numbers.min              // 1
  val max = numbers.max              // 5
  
  // reduce - combine elements (requires non-empty)
  val sumReduced = numbers.reduce(_ + _)  // 15
  val maxReduced = numbers.reduce((a, b) => if (a > b) a else b)  // 5
  
  // fold - like reduce but with initial value
  val sumFolded = numbers.fold(0)(_ + _)  // 15
  val productFolded = numbers.fold(1)(_ * _)  // 120
  
  // foldLeft and foldRight (more explicit)
  val sumLeft = numbers.foldLeft(0)(_ + _)
  val sumRight = numbers.foldRight(0)(_ + _)
  
  // Rust comparison:
  // Rust: numbers.iter().fold(0, |acc, x| acc + x)
  // Scala: numbers.fold(0)(_ + _)
  
  // ============================================================================
  // FLATMAP
  // ============================================================================
  
  // flatMap - map then flatten
  val nested = List(List(1, 2), List(3, 4))
  val flattened = nested.flatten  // List(1, 2, 3, 4)
  
  val duplicated = numbers.flatMap(n => List(n, n))  
  // List(1, 1, 2, 2, 3, 3, 4, 4, 5, 5)
  
  // Practical example: parse strings, filter out failures
  val strings2 = List("1", "2", "abc", "3")
  val parsed = strings2.flatMap(s => 
    try Some(s.toInt) catch { case _: NumberFormatException => None }
  )  // List(1, 2, 3)
  
  // ============================================================================
  // FINDING ELEMENTS
  // ============================================================================
  
  val found = numbers.find(_ > 3)     // Some(4) - first match
  val exists = numbers.exists(_ > 3)  // true - any match
  val forall = numbers.forall(_ > 0)  // true - all match
  
  val index = numbers.indexOf(3)      // 2
  val contains = numbers.contains(3)  // true
  
  // ============================================================================
  // GROUPING AND PARTITIONING
  // ============================================================================
  
  // partition - split by predicate
  val (evens2, odds2) = numbers.partition(_ % 2 == 0)
  // evens2 = List(2, 4), odds2 = List(1, 3, 5)
  
  // groupBy - group by key
  val grouped = numbers.groupBy(_ % 3)
  // Map(0 -> List(3), 1 -> List(1, 4), 2 -> List(2, 5))
  
  // ============================================================================
  // SORTING
  // ============================================================================
  
  val unsorted = List(3, 1, 4, 1, 5, 9)
  val sorted = unsorted.sorted                    // List(1, 1, 3, 4, 5, 9)
  val reversed = unsorted.sorted.reverse          // List(9, 5, 4, 3, 1, 1)
  val sortedBy = List("banana", "apple", "cherry").sortBy(_.length)
  // List("apple", "banana", "cherry")
  
  // ============================================================================
  // MAP (Dictionary/HashMap)
  // ============================================================================
  
  val ages = Map("Alice" -> 30, "Bob" -> 25, "Carol" -> 35)
  
  // Creating maps
  val map1 = Map(1 -> "one", 2 -> "two", 3 -> "three")
  val empty2 = Map.empty[String, Int]
  
  // Accessing values
  val aliceAge = ages.get("Alice")      // Some(30)
  val danAge = ages.get("Dan")          // None
  val aliceAge2 = ages.getOrElse("Alice", 0)  // 30
  val danAge2 = ages.getOrElse("Dan", 0)      // 0
  
  // Adding/updating (returns new map)
  val updated = ages + ("Dan" -> 28)
  val removed = ages - "Bob"
  
  // Keys and values
  val keys = ages.keys.toList        // List("Alice", "Bob", "Carol")
  val values = ages.values.toList    // List(30, 25, 35)
  
  // Rust comparison:
  // Rust: use std::collections::HashMap;
  //       let mut map = HashMap::new();
  //       map.insert("Alice", 30);
  // Scala: val map = Map("Alice" -> 30)
  
  // ============================================================================
  // SET
  // ============================================================================
  
  val set1 = Set(1, 2, 3, 4, 5)
  val set2 = Set(3, 4, 5, 6, 7)
  
  // Set operations
  val union = set1.union(set2)         // or set1 | set2
  val intersection = set1.intersect(set2)  // or set1 & set2
  val difference = set1.diff(set2)     // or set1 &~ set2
  
  // Membership
  val hasFour = set1.contains(4)       // true
  val hasNine = set1.contains(9)       // false
  
  // ============================================================================
  // FOR-COMPREHENSIONS WITH COLLECTIONS
  // ============================================================================
  
  // Cartesian product
  val pairs = for {
    x <- List(1, 2, 3)
    y <- List('a', 'b')
  } yield (x, y)
  // List((1,a), (1,b), (2,a), (2,b), (3,a), (3,b))
  
  // With filter
  val evenPairs = for {
    x <- List(1, 2, 3, 4)
    if x % 2 == 0
    y <- List(10, 20)
  } yield x * y
  // List(20, 40, 40, 80)
  
  // ============================================================================
  // DEMO FUNCTION
  // ============================================================================
  
  def main(args: Array[String]): Unit = {
    println("=== Collections Demo ===\n")
    
    // List basics
    println("--- List Basics ---")
    println(s"numbers = $numbers")
    println(s"prepended = $prepended")
    println(s"appended = $appended")
    
    // Transformations
    println("\n--- Transformations ---")
    println(s"doubled = $doubled")
    println(s"evens = $evens")
    println(s"sum = $sum")
    
    // FlatMap
    println("\n--- FlatMap ---")
    println(s"duplicated = $duplicated")
    println(s"parsed = $parsed")
    
    // Finding
    println("\n--- Finding ---")
    println(s"find(_ > 3) = $found")
    println(s"exists(_ > 3) = $exists")
    
    // Grouping
    println("\n--- Grouping ---")
    println(s"partition (evens, odds) = ($evens2, $odds2)")
    println(s"groupBy(_ % 3) = $grouped")
    
    // Map
    println("\n--- Map ---")
    println(s"ages = $ages")
    println(s"ages.get('Alice') = $aliceAge")
    println(s"updated = $updated")
    
    // Set
    println("\n--- Set ---")
    println(s"set1 = $set1")
    println(s"set2 = $set2")
    println(s"union = $union")
    println(s"intersection = $intersection")
    
    // For-comprehensions
    println("\n--- For-Comprehensions ---")
    println(s"pairs = $pairs")
    println(s"evenPairs = $evenPairs")
  }
}
