package catsintro

// Type Classes: A different approach to polymorphism
// Understanding type classes is key to using Cats effectively

object TypeClassesIntro {
  
  // ============================================================================
  // WHAT IS A TYPE CLASS?
  // ============================================================================
  
  // A type class is a pattern for ad-hoc polymorphism
  // It allows you to add functionality to existing types without modifying them
  
  // Rust comparison:
  // Type classes are similar to Rust traits, but used differently:
  // - Rust: trait implementation is defined alongside type or in same crate
  // - Scala: type class instance can be defined anywhere (more flexible)
  
  // ============================================================================
  // EXAMPLE: SHOW TYPE CLASS
  // ============================================================================
  
  // Step 1: Define the type class (the interface/contract)
  trait Show[A] {
    def show(value: A): String
  }
  
  // Step 2: Define instances for specific types
  object Show {
    // Instance for Int
    implicit val intShow: Show[Int] = new Show[Int] {
      def show(value: Int): String = s"Int($value)"
    }
    
    // Instance for String
    implicit val stringShow: Show[String] = new Show[String] {
      def show(value: String): String = s"String($value)"
    }
    
    // Instance for List[A] (if A has Show)
    implicit def listShow[A](implicit sa: Show[A]): Show[List[A]] = 
      new Show[List[A]] {
        def show(value: List[A]): String = {
          val elements = value.map(sa.show).mkString(", ")
          s"List($elements)"
        }
      }
    
    // Helper method to get instance
    def apply[A](implicit instance: Show[A]): Show[A] = instance
  }
  
  // Step 3: Use the type class
  def printShow[A](value: A)(implicit showInstance: Show[A]): Unit = {
    println(showInstance.show(value))
  }
  
  // Or with context bound (syntactic sugar)
  def printShowShort[A: Show](value: A): Unit = {
    println(Show[A].show(value))
  }
  
  // Rust comparison:
  // trait Show {
  //     fn show(&self) -> String;
  // }
  //
  // impl Show for i32 {
  //     fn show(&self) -> String {
  //         format!("Int({})", self)
  //     }
  // }
  //
  // fn print_show<T: Show>(value: T) {
  //     println!("{}", value.show());
  // }
  
  // ============================================================================
  // WHY TYPE CLASSES?
  // ============================================================================
  
  // 1. ADD FUNCTIONALITY TO EXISTING TYPES
  
  // You can add Show to types you don't control:
  case class Person(name: String, age: Int)
  
  implicit val personShow: Show[Person] = new Show[Person] {
    def show(value: Person): String = 
      s"Person(name=${value.name}, age=${value.age})"
  }
  
  // 2. MULTIPLE IMPLEMENTATIONS
  
  // You can have different implementations in different contexts
  object VerboseShow {
    implicit val intVerboseShow: Show[Int] = new Show[Int] {
      def show(value: Int): String = s"Integer value: $value"
    }
  }
  
  // 3. COMPOSITION
  
  // Type class instances can compose automatically
  // If we have Show[Int], we automatically get Show[List[Int]]
  
  // ============================================================================
  // TYPE CLASS LAWS
  // ============================================================================
  
  // Good type classes follow laws (properties that must hold)
  // Laws help with:
  // - Reasoning about code
  // - Safe refactoring
  // - Composition
  
  // Example: Show should be deterministic
  // For any value x: show(x) == show(x)
  
  // ============================================================================
  // EXAMPLE: EQUALITY TYPE CLASS
  // ============================================================================
  
  trait Equal[A] {
    def equal(a: A, b: A): Boolean
  }
  
  object Equal {
    implicit val intEqual: Equal[Int] = new Equal[Int] {
      def equal(a: Int, b: Int): Boolean = a == b
    }
    
    implicit val stringEqual: Equal[String] = new Equal[String] {
      def equal(a: String, b: String): Boolean = a == b
    }
    
    // Generic instance for case classes using ==
    def apply[A](implicit instance: Equal[A]): Equal[A] = instance
    
    // Helper syntax
    implicit class EqualOps[A](val a: A) extends AnyVal {
      def ===(b: A)(implicit eq: Equal[A]): Boolean = eq.equal(a, b)
    }
  }
  
  // Laws for Equal:
  // 1. Reflexive: a === a must be true
  // 2. Symmetric: a === b implies b === a
  // 3. Transitive: a === b and b === c implies a === c
  
  // ============================================================================
  // EXAMPLE: JSON SERIALIZATION TYPE CLASS
  // ============================================================================
  
  trait JsonWriter[A] {
    def write(value: A): String
  }
  
  object JsonWriter {
    implicit val intWriter: JsonWriter[Int] = new JsonWriter[Int] {
      def write(value: Int): String = value.toString
    }
    
    implicit val stringWriter: JsonWriter[String] = new JsonWriter[String] {
      def write(value: String): String = s""""$value""""
    }
    
    implicit val boolWriter: JsonWriter[Boolean] = new JsonWriter[Boolean] {
      def write(value: Boolean): String = value.toString
    }
    
    implicit def listWriter[A](implicit wa: JsonWriter[A]): JsonWriter[List[A]] = 
      new JsonWriter[List[A]] {
        def write(values: List[A]): String = {
          val elements = values.map(wa.write).mkString(",")
          s"[$elements]"
        }
      }
    
    def apply[A](implicit instance: JsonWriter[A]): JsonWriter[A] = instance
  }
  
  def toJson[A: JsonWriter](value: A): String = {
    JsonWriter[A].write(value)
  }
  
  // Custom type with JsonWriter instance
  case class User(name: String, age: Int, active: Boolean)
  
  implicit val userWriter: JsonWriter[User] = new JsonWriter[User] {
    def write(user: User): String = {
      import JsonWriter._
      s"""{
         |  "name": ${toJson(user.name)},
         |  "age": ${toJson(user.age)},
         |  "active": ${toJson(user.active)}
         |}""".stripMargin
    }
  }
  
  // ============================================================================
  // INTERFACE SYNTAX (Extension Methods)
  // ============================================================================
  
  // Type classes often provide syntax for convenient usage
  
  object ShowSyntax {
    implicit class ShowOps[A](val value: A) extends AnyVal {
      def show(implicit showInstance: Show[A]): String = {
        showInstance.show(value)
      }
    }
  }
  
  // Usage:
  // import ShowSyntax._
  // 42.show  // "Int(42)"
  
  // ============================================================================
  // CATS TYPE CLASS PATTERN
  // ============================================================================
  
  // Cats follows a standard pattern for all type classes:
  
  // 1. Trait defining the interface
  // trait TypeClass[F[_]] {
  //   def operation[A](fa: F[A]): F[B]
  // }
  
  // 2. Companion object with:
  //    - apply method to summon instances
  //    - Instances for common types
  
  // 3. Syntax package with extension methods
  
  // Example structure:
  // package cats
  // trait Functor[F[_]] {
  //   def map[A, B](fa: F[A])(f: A => B): F[B]
  // }
  //
  // object Functor {
  //   def apply[F[_]](implicit instance: Functor[F]): Functor[F] = instance
  //   
  //   implicit val optionFunctor: Functor[Option] = ...
  //   implicit val listFunctor: Functor[List] = ...
  // }
  //
  // package cats.syntax
  // implicit class FunctorOps[F[_], A](val fa: F[A]) {
  //   def map[B](f: A => B)(implicit F: Functor[F]): F[B] = F.map(fa)(f)
  // }
  
  // ============================================================================
  // BENEFITS OF TYPE CLASSES
  // ============================================================================
  
  // 1. RETROACTIVE EXTENSION
  //    Add functionality to types you don't control
  
  // 2. AD-HOC POLYMORPHISM
  //    Different behavior for different types
  
  // 3. MULTIPLE IMPLEMENTATIONS
  //    Different instances in different scopes
  
  // 4. COMPOSITION
  //    Instances compose automatically
  
  // 5. LAWS
  //    Formal properties that enable reasoning
  
  // 6. SEPARATION OF CONCERNS
  //    Data types separate from operations
  
  // ============================================================================
  // RUST VS SCALA TYPE CLASSES
  // ============================================================================
  
  // Similarities:
  // - Both define shared behavior across types
  // - Both allow generic functions with constraints
  // - Both enable composition
  
  // Differences:
  
  // Rust traits:
  // - Implemented in same crate as type or trait
  // - Coherence rules prevent conflicting implementations
  // - Can have default methods
  // - Can have associated types and constants
  
  // Scala type classes:
  // - Can be defined anywhere (more flexible)
  // - Uses implicit resolution
  // - Can have multiple instances in different scopes
  // - Higher-kinded types (F[_]) for abstracting over type constructors
  
  // Example comparison:
  
  // Rust:
  // trait Show {
  //     fn show(&self) -> String;
  // }
  // 
  // impl Show for i32 {
  //     fn show(&self) -> String { format!("{}", self) }
  // }
  //
  // fn print<T: Show>(value: T) {
  //     println!("{}", value.show());
  // }
  
  // Scala:
  // trait Show[A] {
  //   def show(value: A): String
  // }
  //
  // implicit val intShow: Show[Int] = ...
  //
  // def print[A: Show](value: A): Unit = {
  //   println(Show[A].show(value))
  // }
  
  // ============================================================================
  // DEMO FUNCTION
  // ============================================================================
  
  def main(args: Array[String]): Unit = {
    println("=== Type Classes Introduction ===\n")
    
    // Show type class
    println("--- Show Type Class ---")
    printShow(42)
    printShow("hello")
    printShow(List(1, 2, 3))
    
    val person = Person("Alice", 30)
    printShow(person)
    
    // Equal type class
    println("\n--- Equal Type Class ---")
    import Equal._
    println(s"42 === 42: ${42 === 42}")
    println(s"42 === 43: ${42 === 43}")
    println(s"'hello' === 'hello': ${"hello" === "hello"}")
    
    // JsonWriter type class
    println("\n--- JsonWriter Type Class ---")
    println(toJson(42))
    println(toJson("hello"))
    println(toJson(true))
    println(toJson(List(1, 2, 3)))
    
    val user = User("Alice", 30, active = true)
    println(toJson(user))
    
    // ShowSyntax
    println("\n--- Show Syntax ---")
    import ShowSyntax._
    println(42.show)
    println("hello".show)
    println(List(1, 2, 3).show)
    
    println("\n--- Summary ---")
    println("Type classes enable:")
    println("- Retroactive extension")
    println("- Ad-hoc polymorphism")
    println("- Multiple implementations")
    println("- Composition")
    println("- Lawful abstractions")
  }
}
