package fundamentals.exercises

/**
 * Exercise 04: Collections and Transformations
 * 
 * OBJECTIVES:
 * - Practice map, filter, fold operations
 * - Work with List transformations
 * - Use for-comprehensions
 * 
 * TASKS:
 * 1. Implement processStrings with multiple transformations
 * 2. Implement wordFrequency to count word occurrences
 * 3. Implement flattenAndSum for nested lists
 * 4. Implement cartesianProduct
 * 
 * HOW TO RUN:
 * 1. Fill in the ??? parts with your implementation
 * 2. Run: sbt "runMain fundamentals.exercises.Exercise04"
 * 3. Verify the output matches expected results
 * 
 * EXPECTED OUTPUT:
 * processStrings: List(5, 5, 5)
 * 
 * wordFrequency:
 * hello -> 2
 * world -> 1
 * scala -> 1
 * 
 * flattenAndSum: 21
 * 
 * cartesianProduct: List((1,A), (1,B), (2,A), (2,B), (3,A), (3,B))
 */
object Exercise04 extends App {
  
  // TODO: Implement processStrings
  // Given a list of strings:
  // 1. Filter strings with length >= 3
  // 2. Convert to uppercase
  // 3. Return list of their lengths
  def processStrings(strings: List[String]): List[Int] = {
    ???  // Replace ??? with your implementation
  }
  
  // TODO: Implement wordFrequency
  // Count how many times each word appears
  // Return a Map[String, Int] with word counts
  // Hint: Use groupBy and mapValues or map
  def wordFrequency(words: List[String]): Map[String, Int] = {
    ???  // Replace ??? with your implementation
  }
  
  // TODO: Implement flattenAndSum
  // Given nested list [[1,2], [3,4,5], [6,7,8,9]]
  // Flatten it and return the sum of all numbers
  // Hint: Use flatten and sum, or flatMap
  def flattenAndSum(nested: List[List[Int]]): Int = {
    ???  // Replace ??? with your implementation
  }
  
  // TODO: Implement cartesianProduct
  // Given two lists, return all pairs (Cartesian product)
  // Example: cartesianProduct(List(1,2), List('A','B'))
  // Result: List((1,'A'), (1,'B'), (2,'A'), (2,'B'))
  // Hint: Use for-comprehension or flatMap
  def cartesianProduct[A, B](list1: List[A], list2: List[B]): List[(A, B)] = {
    ???  // Replace ??? with your implementation
  }
  
  // Test cases - uncomment after implementing
  println("=== Exercise 04: Collections ===\n")
  
  // Test processStrings
  println("--- processStrings ---")
  // val strings = List("hi", "hello", "world", "a", "scala")
  // val result1 = processStrings(strings)
  // println(s"processStrings: $result1")  // Should be List(5, 5, 5)
  
  // Test wordFrequency
  // println("\n--- wordFrequency ---")
  // val words = List("hello", "world", "hello", "scala")
  // val freq = wordFrequency(words)
  // freq.foreach { case (word, count) =>
  //   println(s"$word -> $count")
  // }
  // // Should print:
  // // hello -> 2
  // // world -> 1
  // // scala -> 1
  
  // Test flattenAndSum
  // println("\n--- flattenAndSum ---")
  // val nested = List(List(1, 2), List(3, 4, 5), List(6, 7, 8, 9))
  // val sum = flattenAndSum(nested)
  // println(s"flattenAndSum: $sum")  // Should be 45... wait, let me recalculate
  // // 1+2+3+4+5+6+7+8+9 = 45? No wait: (1+2) + (3+4+5) + (6+7+8+9) = 3 + 12 + 30 = 45. Yes!
  
  // Test cartesianProduct
  // println("\n--- cartesianProduct ---")
  // val product = cartesianProduct(List(1, 2, 3), List('A', 'B'))
  // println(s"cartesianProduct: $product")
  // // Should be List((1,A), (1,B), (2,A), (2,B), (3,A), (3,B))
  
  println("\nIf you see this message without errors, uncomment the test cases above!")
}

/**
 * SOLUTION (Don't peek until you've tried!)
 * 
 * def processStrings(strings: List[String]): List[Int] = {
 *   strings
 *     .filter(_.length >= 3)
 *     .map(_.toUpperCase)
 *     .map(_.length)
 *   
 *   // Or with for-comprehension:
 *   // for {
 *   //   s <- strings
 *   //   if s.length >= 3
 *   //   upper = s.toUpperCase
 *   // } yield upper.length
 * }
 * 
 * def wordFrequency(words: List[String]): Map[String, Int] = {
 *   words.groupBy(identity).view.mapValues(_.length).toMap
 *   // Or simpler:
 *   // words.groupBy(identity).map { case (word, list) => (word, list.length) }
 * }
 * 
 * def flattenAndSum(nested: List[List[Int]]): Int = {
 *   nested.flatten.sum
 *   // Or:
 *   // nested.flatMap(identity).sum
 * }
 * 
 * def cartesianProduct[A, B](list1: List[A], list2: List[B]): List[(A, B)] = {
 *   for {
 *     a <- list1
 *     b <- list2
 *   } yield (a, b)
 *   
 *   // Or with flatMap:
 *   // list1.flatMap(a => list2.map(b => (a, b)))
 * }
 * 
 * RUST COMPARISON:
 * 
 * fn process_strings(strings: Vec<String>) -> Vec<usize> {
 *     strings.iter()
 *         .filter(|s| s.len() >= 3)
 *         .map(|s| s.to_uppercase())
 *         .map(|s| s.len())
 *         .collect()
 * }
 * 
 * use std::collections::HashMap;
 * fn word_frequency(words: Vec<String>) -> HashMap<String, usize> {
 *     let mut map = HashMap::new();
 *     for word in words {
 *         *map.entry(word).or_insert(0) += 1;
 *     }
 *     map
 * }
 * 
 * fn flatten_and_sum(nested: Vec<Vec<i32>>) -> i32 {
 *     nested.into_iter().flatten().sum()
 * }
 * 
 * fn cartesian_product<A, B>(list1: Vec<A>, list2: Vec<B>) -> Vec<(A, B)> 
 * where A: Clone, B: Clone {
 *     list1.iter()
 *         .flat_map(|a| list2.iter().map(move |b| (a.clone(), b.clone())))
 *         .collect()
 * }
 */
