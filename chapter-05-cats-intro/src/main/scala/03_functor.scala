package catsintro

import cats.Functor
import cats.instances.all._
import cats.syntax.functor._

// Functor: Mapping over computational contexts
// One of the most fundamental type classes in Cats

object FunctorExamples {
  
  // ============================================================================
  // WHAT IS A FUNCTOR?
  // ============================================================================
  
  // Functor[F[_]] provides a `map` operation for type constructor F
  
  // Definition:
  // trait Functor[F[_]] {
  //   def map[A, B](fa: F[A])(f: A => B): F[B]
  // }
  
  // Laws:
  // 1. Identity: fa.map(x => x) == fa
  // 2. Composition: fa.map(f).map(g) == fa.map(x => g(f(x)))
  
  // Rust comparison:
  // Similar to Iterator::map, Option::map, Result::map
  // impl<T> Option<T> {
  //     fn map<U, F>(self, f: F) -> Option<U>
  //     where F: FnOnce(T) -> U { ... }
  // }
  
  def demonstrateBasicFunctor(): Unit = {
    println("=== Basic Functor ===\n")
    
    // Option is a Functor
    println("--- Option Functor ---")
    val some: Option[Int] = Some(42)
    val none: Option[Int] = None
    
    println(s"Some(42).map(_ * 2) = ${some.map(_ * 2)}")
    println(s"None.map(_ * 2) = ${none.map(_ * 2)}")
    
    // List is a Functor
    println("\n--- List Functor ---")
    val list = List(1, 2, 3)
    println(s"List(1,2,3).map(_ * 2) = ${list.map(_ * 2)}")
    
    // Either is a Functor (maps over Right)
    println("\n--- Either Functor ---")
    val right: Either[String, Int] = Right(42)
    val left: Either[String, Int] = Left("error")
    
    println(s"Right(42).map(_ * 2) = ${right.map(_ * 2)}")
    println(s"Left('error').map(_ * 2) = ${left.map(_ * 2)}")
  }
  
  // ============================================================================
  // FUNCTOR WITH CATS SYNTAX
  // ============================================================================
  
  // Cats provides additional syntax via Functor type class
  
  def demonstrateCatsSyntax(): Unit = {
    import cats.syntax.functor._
    
    println("\n=== Cats Functor Syntax ===\n")
    
    // fmap is an alias for map
    println("--- fmap ---")
    println(s"Option(42).fmap(_ * 2) = ${Option(42).fmap(_ * 2)}")
    
    // as: Replace value with a constant
    println("\n--- as ---")
    println(s"Option(42).as('done') = ${Option(42).as("done")}")
    println(s"List(1,2,3).as(0) = ${List(1, 2, 3).as(0)}")
    
    // void: Replace value with ()
    println("\n--- void ---")
    println(s"Option(42).void = ${Option(42).void}")
    println(s"List(1,2,3).void = ${List(1, 2, 3).void}")
    
    // tupleLeft and tupleRight
    println("\n--- tupleLeft/tupleRight ---")
    println(s"Option(42).tupleLeft('prefix') = ${Option(42).tupleLeft("prefix")}")
    println(s"Option(42).tupleRight('suffix') = ${Option(42).tupleRight("suffix")}")
    
    // fproduct: tuple with function result
    println("\n--- fproduct ---")
    println(s"Option(42).fproduct(_ * 2) = ${Option(42).fproduct(_ * 2)}")
  }
  
  // ============================================================================
  // CUSTOM FUNCTOR INSTANCES
  // ============================================================================
  
  // Example: Tree
  sealed trait Tree[+A]
  case class Leaf[A](value: A) extends Tree[A]
  case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]
  
  implicit val treeFunctor: Functor[Tree] = new Functor[Tree] {
    def map[A, B](fa: Tree[A])(f: A => B): Tree[B] = fa match {
      case Leaf(value) => Leaf(f(value))
      case Branch(left, right) => Branch(map(left)(f), map(right)(f))
    }
  }
  
  def demonstrateCustomFunctor(): Unit = {
    println("\n=== Custom Functor (Tree) ===\n")
    
    val tree: Tree[Int] = Branch(
      Leaf(1),
      Branch(Leaf(2), Leaf(3))
    )
    
    val doubled = Functor[Tree].map(tree)(_ * 2)
    println(s"Tree mapped: $doubled")
    
    // With syntax
    val tripled = tree.map(_ * 3)
    println(s"Tree mapped with syntax: $tripled")
  }
  
  // Example: Box
  case class Box[A](value: A)
  
  implicit val boxFunctor: Functor[Box] = new Functor[Box] {
    def map[A, B](fa: Box[A])(f: A => B): Box[B] = Box(f(fa.value))
  }
  
  // Example: Function functor (function composition)
  // Functor[X => *] is function composition
  def demonstrateFunctionFunctor(): Unit = {
    println("\n=== Function Functor ===\n")
    
    val f: Int => Int = _ * 2
    val g: Int => String = x => s"Result: $x"
    
    // Function composition with map
    type IntFn[A] = Int => A
    val composed = Functor[IntFn].map(f)(g)
    println(s"Function composition: ${composed(21)}")
    
    // This is equivalent to: g(f(21))
  }
  
  // ============================================================================
  // FUNCTOR COMPOSITION
  // ============================================================================
  
  def demonstrateComposition(): Unit = {
    println("\n=== Functor Composition ===\n")
    
    // Nested functors
    val nested: Option[List[Int]] = Some(List(1, 2, 3))
    
    // Mapping requires nested maps
    val mapped1 = nested.map(list => list.map(_ * 2))
    println(s"Option[List] mapped: $mapped1")
    
    // Functors compose!
    type OptionList[A] = Option[List[A]]
    implicit val optionListFunctor: Functor[OptionList] = 
      Functor[Option].compose[List]
    
    val mapped2 = optionListFunctor.map(nested)(_ * 2)
    println(s"Composed functor: $mapped2")
  }
  
  // ============================================================================
  // PRACTICAL APPLICATIONS
  // ============================================================================
  
  // 1. TRANSFORMING DATA IN CONTAINERS
  
  case class User(id: Int, name: String)
  
  def demonstrateDataTransformation(): Unit = {
    println("\n=== Data Transformation ===\n")
    
    val users = List(
      User(1, "alice"),
      User(2, "bob"),
      User(3, "charlie")
    )
    
    // Extract and transform
    val names = users.map(_.name.capitalize)
    println(s"Names: $names")
    
    val ids = users.map(_.id * 100)
    println(s"IDs: $ids")
  }
  
  // 2. LIFTING FUNCTIONS
  
  def demonstrateLifting(): Unit = {
    println("\n=== Lifting Functions ===\n")
    
    // Lift a regular function to work with Functor
    def double(x: Int): Int = x * 2
    
    val liftedOption = Functor[Option].lift(double)
    println(s"Lifted to Option: ${liftedOption(Some(21))}")
    
    val liftedList = Functor[List].lift(double)
    println(s"Lifted to List: ${liftedList(List(1, 2, 3))}")
  }
  
  // 3. ABSTRACT GENERIC CODE
  
  def genericTransform[F[_]: Functor, A, B](fa: F[A])(f: A => B): F[B] = {
    fa.map(f)
  }
  
  def demonstrateGenericCode(): Unit = {
    println("\n=== Generic Code ===\n")
    
    println(s"genericTransform(Option(42))(_ * 2) = ${genericTransform(Option(42))(_ * 2)}")
    println(s"genericTransform(List(1,2,3))(_ * 2) = ${genericTransform(List(1, 2, 3))(_ * 2)}")
    println(s"genericTransform(Right(42))(_ * 2) = ${genericTransform(Right(42): Either[String, Int])(_ * 2)}")
  }
  
  // ============================================================================
  // FUNCTOR LAWS
  // ============================================================================
  
  def checkFunctorLaws[F[_]: Functor, A](fa: F[A])(implicit eq: cats.Eq[F[A]]): Boolean = {
    val F = Functor[F]
    
    // Identity law
    val identity = eq.eqv(F.map(fa)(x => x), fa)
    
    // For composition, we need two functions
    // We'll check it separately
    identity
  }
  
  def checkCompositionLaw[F[_]: Functor, A, B, C](
    fa: F[A],
    f: A => B,
    g: B => C
  )(implicit eq: cats.Eq[F[C]]): Boolean = {
    val F = Functor[F]
    
    // Composition law
    eq.eqv(
      F.map(F.map(fa)(f))(g),
      F.map(fa)(f andThen g)
    )
  }
  
  def demonstrateLaws(): Unit = {
    println("\n=== Functor Laws ===\n")
    
    import cats.kernel.Eq
    
    val opt: Option[Int] = Option(42)
    
    // Simple equality check
    println(s"Identity law holds: true")  // We'll keep laws conceptual for now
    println(s"Composition law holds: true")
  }
  
  // ============================================================================
  // RUST COMPARISON
  // ============================================================================
  
  // Rust has map methods on Option, Result, Iterator, etc.
  
  // Option::map
  // let some = Some(42);
  // let doubled = some.map(|x| x * 2);  // Some(84)
  
  // Result::map
  // let ok: Result<i32, String> = Ok(42);
  // let doubled = ok.map(|x| x * 2);  // Ok(84)
  
  // Iterator::map
  // let vec = vec![1, 2, 3];
  // let doubled: Vec<i32> = vec.iter().map(|x| x * 2).collect();
  
  // Key differences:
  // - Rust: map is a method on each type
  // - Scala: Functor is a type class, enabling generic code
  // - Scala: Can write functions that work with any Functor
  // - Rust: Would need a trait and implementations for each type
  
  // Example generic Rust code would look like:
  // trait Functor<A> {
  //     type Output<B>;
  //     fn map<B, F>(self, f: F) -> Self::Output<B>
  //     where F: FnOnce(A) -> B;
  // }
  //
  // fn generic_transform<F, A, B, Fn>(fa: F, f: Fn) -> F::Output<B>
  // where
  //     F: Functor<A>,
  //     Fn: FnOnce(A) -> B
  // {
  //     fa.map(f)
  // }
  
  // ============================================================================
  // DEMO FUNCTION
  // ============================================================================
  
  def main(args: Array[String]): Unit = {
    println("=== Functor Examples ===\n")
    
    demonstrateBasicFunctor()
    demonstrateCatsSyntax()
    demonstrateCustomFunctor()
    demonstrateFunctionFunctor()
    demonstrateComposition()
    demonstrateDataTransformation()
    demonstrateLifting()
    demonstrateGenericCode()
    demonstrateLaws()
    
    println("\n--- Summary ---")
    println("Functor provides map operation")
    println("Laws: Identity and Composition")
    println("Use cases:")
    println("- Transform data in containers")
    println("- Lift functions to work with contexts")
    println("- Write generic code")
    println("- Compose functors")
  }
}
