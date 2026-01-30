package fundamentals.exercises.solutions

/**
 * SOLUTION for Exercise 04: Collections and Transformations
 * 
 * HOW TO RUN:
 * sbt "runMain fundamentals.exercises.solutions.Exercise04Solution"
 */
object Exercise04Solution extends App {
  
  def processStrings(strings: List[String]): List[Int] = {
    strings
      .filter(_.length >= 3)
      .map(_.toUpperCase)
      .map(_.length)
  }
  
  def wordFrequency(words: List[String]): Map[String, Int] = {
    words.groupBy(identity).view.mapValues(_.length).toMap
  }
  
  def flattenAndSum(nested: List[List[Int]]): Int = {
    nested.flatten.sum
  }
  
  def cartesianProduct[A, B](list1: List[A], list2: List[B]): List[(A, B)] = {
    for {
      a <- list1
      b <- list2
    } yield (a, b)
  }
  
  println("=== Exercise 04: Collections (SOLUTION) ===\n")
  
  println("--- processStrings ---")
  val strings = List("hi", "hello", "world", "a", "scala")
  val result1 = processStrings(strings)
  println(s"processStrings: $result1")
  
  println("\n--- wordFrequency ---")
  val words = List("hello", "world", "hello", "scala")
  val freq = wordFrequency(words)
  freq.foreach { case (word, count) =>
    println(s"$word -> $count")
  }
  
  println("\n--- flattenAndSum ---")
  val nested = List(List(1, 2), List(3, 4, 5), List(6, 7, 8, 9))
  val sum = flattenAndSum(nested)
  println(s"flattenAndSum: $sum")
  
  println("\n--- cartesianProduct ---")
  val product = cartesianProduct(List(1, 2, 3), List('A', 'B'))
  println(s"cartesianProduct: $product")
}
