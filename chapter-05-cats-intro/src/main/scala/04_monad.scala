package catsintro

import cats.Monad
import cats.instances.all._
import cats.syntax.flatMap._
import cats.syntax.functor._

// Monad: Sequencing computational effects
// One of the most important type classes for understanding Cats-Effect

object MonadExamples {
  
  // ============================================================================
  // WHAT IS A MONAD?
  // ============================================================================
  
  // Monad[F[_]] provides flatMap and pure for sequencing effects
  
  // Definition:
  // trait Monad[F[_]] extends Functor[F] {
  //   def pure[A](a: A): F[A]
  //   def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  // }
  
  // Laws:
  // 1. Left identity: pure(a).flatMap(f) == f(a)
  // 2. Right identity: m.flatMap(pure) == m
  // 3. Associativity: m.flatMap(f).flatMap(g) == m.flatMap(x => f(x).flatMap(g))
  
  // Rust comparison:
  // Similar to ? operator and iterator chains
  // Option::and_then, Result::and_then are like flatMap
  
  def demonstrateBasicMonad(): Unit = {
    println("=== Basic Monad ===\n")
    
    // Option is a Monad
    println("--- Option Monad ---")
    val some: Option[Int] = Some(42)
    val none: Option[Int] = None
    
    val result1 = some.flatMap(x => Some(x * 2))
    val result2 = none.flatMap(x => Some(x * 2))
    
    println(s"Some(42).flatMap(x => Some(x * 2)) = $result1")
    println(s"None.flatMap(x => Some(x * 2)) = $result2")
    
    // List is a Monad
    println("\n--- List Monad ---")
    val list = List(1, 2, 3)
    val result3 = list.flatMap(x => List(x, x * 10))
    println(s"List(1,2,3).flatMap(x => List(x, x*10)) = $result3")
    
    // Either is a Monad
    println("\n--- Either Monad ---")
    val right: Either[String, Int] = Right(42)
    val left: Either[String, Int] = Left("error")
    
    val result4 = right.flatMap(x => Right(x * 2))
    val result5 = left.flatMap(x => Right(x * 2))
    
    println(s"Right(42).flatMap(x => Right(x * 2)) = $result4")
    println(s"Left('error').flatMap(x => Right(x * 2)) = $result5")
  }
  
  // ============================================================================
  // PURE: LIFTING VALUES INTO CONTEXT
  // ============================================================================
  
  def demonstratePure(): Unit = {
    println("\n=== Pure ===\n")
    
    // pure lifts a value into the monadic context
    println(s"Monad[Option].pure(42) = ${Monad[Option].pure(42)}")
    println(s"Monad[List].pure(42) = ${Monad[List].pure(42)}")
    
    // For Either, need to fix the right type
    type StringOr[A] = Either[String, A]
    println(s"Monad[Either[String, *]].pure(42) = ${Monad[StringOr].pure(42)}")
    
    // Rust comparison:
    // Some(42) // lift into Option
    // Ok(42)   // lift into Result
  }
  
  // ============================================================================
  // FOR-COMPREHENSIONS
  // ============================================================================
  
  def demonstrateForComprehensions(): Unit = {
    println("\n=== For-Comprehensions ===\n")
    
    // For-comprehension is syntactic sugar for flatMap + map
    println("--- Option ---")
    val result1 = for {
      x <- Some(10)
      y <- Some(20)
      z <- Some(30)
    } yield x + y + z
    
    println(s"for-comprehension with Option: $result1")
    
    // This desugars to:
    val result2 = Some(10).flatMap { x =>
      Some(20).flatMap { y =>
        Some(30).map { z =>
          x + y + z
        }
      }
    }
    
    println(s"Desugared version: $result2")
    
    // With a None, short-circuits
    println("\n--- Short-circuiting ---")
    val result3 = for {
      x <- Some(10)
      y <- None: Option[Int]
      z <- Some(30)
    } yield x + y + z
    
    println(s"With None: $result3")
    
    // Rust comparison:
    // let result = some_fn()?
    //     .and_then(|x| other_fn(x))?
    //     .and_then(|y| third_fn(y))?;
  }
  
  // ============================================================================
  // CHAINING EFFECTS
  // ============================================================================
  
  def demonstrateChaining(): Unit = {
    println("\n=== Chaining Effects ===\n")
    
    // Simulate database operations
    def findUser(id: Int): Option[String] = {
      if (id > 0 && id <= 3) Some(s"User$id") else None
    }
    
    def findEmail(name: String): Option[String] = {
      if (name.nonEmpty) Some(s"$name@example.com") else None
    }
    
    def sendEmail(email: String): Option[String] = {
      Some(s"Email sent to $email")
    }
    
    // Chain operations
    val result = for {
      user <- findUser(1)
      email <- findEmail(user)
      confirmation <- sendEmail(email)
    } yield confirmation
    
    println(s"Success case: $result")
    
    val result2 = for {
      user <- findUser(999)  // Returns None
      email <- findEmail(user)
      confirmation <- sendEmail(email)
    } yield confirmation
    
    println(s"Failure case: $result2")
  }
  
  // ============================================================================
  // MONAD LAWS
  // ============================================================================
  
  def demonstrateLaws(): Unit = {
    println("\n=== Monad Laws ===\n")
    
    val M = Monad[Option]
    
    // Law 1: Left identity
    val a = 42
    val f: Int => Option[Int] = x => Some(x * 2)
    
    val leftId1 = M.pure(a).flatMap(f)
    val leftId2 = f(a)
    
    println(s"Left identity: M.pure($a).flatMap(f) = $leftId1")
    println(s"Left identity: f($a) = $leftId2")
    println(s"Equal: ${leftId1 == leftId2}")
    
    // Law 2: Right identity
    val m = Some(42)
    
    val rightId1 = m.flatMap(M.pure)
    val rightId2 = m
    
    println(s"\nRight identity: m.flatMap(M.pure) = $rightId1")
    println(s"Right identity: m = $rightId2")
    println(s"Equal: ${rightId1 == rightId2}")
    
    // Law 3: Associativity
    val g: Int => Option[Int] = x => Some(x + 10)
    
    val assoc1 = m.flatMap(f).flatMap(g)
    val assoc2 = m.flatMap(x => f(x).flatMap(g))
    
    println(s"\nAssociativity: m.flatMap(f).flatMap(g) = $assoc1")
    println(s"Associativity: m.flatMap(x => f(x).flatMap(g)) = $assoc2")
    println(s"Equal: ${assoc1 == assoc2}")
  }
  
  // ============================================================================
  // PRACTICAL APPLICATIONS
  // ============================================================================
  
  // 1. VALIDATION PIPELINE
  
  sealed trait ValidationError
  case class TooShort(field: String) extends ValidationError
  case class TooLong(field: String) extends ValidationError
  case class InvalidFormat(field: String) extends ValidationError
  
  def validateLength(s: String, min: Int, max: Int, field: String): Either[ValidationError, String] = {
    if (s.length < min) Left(TooShort(field))
    else if (s.length > max) Left(TooLong(field))
    else Right(s)
  }
  
  def validateEmail(s: String): Either[ValidationError, String] = {
    if (s.contains("@") && s.contains(".")) Right(s)
    else Left(InvalidFormat("email"))
  }
  
  case class UserInput(username: String, email: String)
  
  def validateUser(input: UserInput): Either[ValidationError, UserInput] = {
    for {
      validUsername <- validateLength(input.username, 3, 20, "username")
      validEmail <- validateEmail(input.email)
    } yield UserInput(validUsername, validEmail)
  }
  
  def demonstrateValidation(): Unit = {
    println("\n=== Validation Pipeline ===\n")
    
    val valid = UserInput("alice", "alice@example.com")
    val invalid1 = UserInput("ab", "alice@example.com")  // Too short
    val invalid2 = UserInput("alice", "invalid")  // Bad email
    
    println(s"Valid: ${validateUser(valid)}")
    println(s"Invalid username: ${validateUser(invalid1)}")
    println(s"Invalid email: ${validateUser(invalid2)}")
  }
  
  // 2. OPTIONAL CHAINING
  
  case class Address(street: String, city: String)
  case class Person(name: String, address: Option[Address])
  case class Company(name: String, ceo: Option[Person])
  
  def getCeoCity(company: Company): Option[String] = {
    for {
      ceo <- company.ceo
      address <- ceo.address
    } yield address.city
  }
  
  def demonstrateOptionalChaining(): Unit = {
    println("\n=== Optional Chaining ===\n")
    
    val company1 = Company(
      "TechCorp",
      Some(Person("Alice", Some(Address("123 Main St", "NYC"))))
    )
    
    val company2 = Company("StartupInc", None)
    
    val company3 = Company(
      "RemoteCo",
      Some(Person("Bob", None))
    )
    
    println(s"Company with CEO and address: ${getCeoCity(company1)}")
    println(s"Company without CEO: ${getCeoCity(company2)}")
    println(s"Company with CEO but no address: ${getCeoCity(company3)}")
    
    // Rust comparison:
    // company.ceo.as_ref()?.address.as_ref()?.city
  }
  
  // 3. ERROR HANDLING PIPELINE
  
  type Result[A] = Either[String, A]
  
  def parseNumber(s: String): Result[Int] = {
    try Right(s.toInt)
    catch { case _: NumberFormatException => Left(s"Invalid number: $s") }
  }
  
  def validatePositive(n: Int): Result[Int] = {
    if (n > 0) Right(n)
    else Left(s"Number must be positive: $n")
  }
  
  def computeSquareRoot(n: Int): Result[Double] = {
    Right(math.sqrt(n.toDouble))
  }
  
  def processInput(input: String): Result[Double] = {
    for {
      number <- parseNumber(input)
      positive <- validatePositive(number)
      result <- computeSquareRoot(positive)
    } yield result
  }
  
  def demonstrateErrorPipeline(): Unit = {
    println("\n=== Error Handling Pipeline ===\n")
    
    println(s"processInput('16') = ${processInput("16")}")
    println(s"processInput('abc') = ${processInput("abc")}")
    println(s"processInput('-4') = ${processInput("-4")}")
  }
  
  // ============================================================================
  // GENERIC MONAD CODE
  // ============================================================================
  
  def sequence[F[_]: Monad, A](list: List[F[A]]): F[List[A]] = {
    list.foldRight(Monad[F].pure(List.empty[A])) { (fa, acc) =>
      for {
        a <- fa
        as <- acc
      } yield a :: as
    }
  }
  
  def demonstrateGenericCode(): Unit = {
    println("\n=== Generic Monad Code ===\n")
    
    val optionList: List[Option[Int]] = List(Some(1), Some(2), Some(3))
    val optionListWithNone: List[Option[Int]] = List(Some(1), None, Some(3))
    
    println(s"sequence(List(Some(1), Some(2), Some(3))) = ${sequence(optionList)}")
    println(s"sequence(List(Some(1), None, Some(3))) = ${sequence(optionListWithNone)}")
    
    val eitherList: List[Either[String, Int]] = List(Right(1), Right(2), Right(3))
    val eitherListWithLeft: List[Either[String, Int]] = List(Right(1), Left("error"), Right(3))
    
    println(s"sequence(List(Right(1), Right(2), Right(3))) = ${sequence(eitherList)}")
    println(s"sequence(List(Right(1), Left('error'), Right(3))) = ${sequence(eitherListWithLeft)}")
  }
  
  // ============================================================================
  // WHY MONAD MATTERS FOR CATS-EFFECT
  // ============================================================================
  
  def whyMonadMatters(): Unit = {
    println("\n=== Why Monad Matters ===\n")
    
    println("""
    |Monad is fundamental to Cats-Effect because:
    |
    |1. IO[A] is a Monad
    |   - Allows sequencing effects with flatMap
    |   - Enables for-comprehensions
    |   - Maintains referential transparency
    |
    |2. Error handling with MonadError
    |   - Extends Monad with error handling capabilities
    |   - Used heavily in Cats-Effect
    |
    |3. Composition
    |   - Build complex programs from simple ones
    |   - Chain operations naturally
    |
    |Preview (Chapter 7):
    |   val program: IO[String] = for {
    |     _ <- IO.println("Enter name:")
    |     name <- IO.readLine
    |     _ <- IO.println(s"Hello, $name!")
    |   } yield name
    |
    |This is possible because IO is a Monad!
    """.stripMargin)
  }
  
  // ============================================================================
  // RUST COMPARISON
  // ============================================================================
  
  def rustComparison(): Unit = {
    println("\n=== Rust Comparison ===\n")
    
    println("""
    |Scala Monad vs Rust patterns:
    |
    |1. Option/Result flatMap = and_then
    |   Scala: Some(42).flatMap(x => Some(x * 2))
    |   Rust:  Some(42).and_then(|x| Some(x * 2))
    |
    |2. For-comprehension = ? operator
    |   Scala: for { x <- opt1; y <- opt2 } yield x + y
    |   Rust:  opt1.and_then(|x| opt2.map(|y| x + y))
    |   Rust:  (|| { let x = opt1?; let y = opt2?; Some(x + y) })()
    |
    |3. Chaining = Iterator chains
    |   Scala: list.flatMap(...)
    |   Rust:  iter.flat_map(...)
    |
    |4. Laws = Rust doesn't enforce, but good patterns follow them
    |
    |Key difference:
    |   - Scala: Monad is a type class (generic)
    |   - Rust: Patterns are per-type (Option, Result, Iterator)
    |   - Scala: Can write generic code over any Monad
    |   - Rust: Need to implement for each type separately
    """.stripMargin)
  }
  
  // ============================================================================
  // DEMO FUNCTION
  // ============================================================================
  
  def main(args: Array[String]): Unit = {
    println("=== Monad Examples ===\n")
    
    demonstrateBasicMonad()
    demonstratePure()
    demonstrateForComprehensions()
    demonstrateChaining()
    demonstrateLaws()
    demonstrateValidation()
    demonstrateOptionalChaining()
    demonstrateErrorPipeline()
    demonstrateGenericCode()
    whyMonadMatters()
    rustComparison()
    
    println("\n--- Summary ---")
    println("Monad provides:")
    println("- flatMap: chain operations that return F[B]")
    println("- pure: lift values into F")
    println("- For-comprehensions: clean syntax")
    println("- Composition: build complex from simple")
    println("- Foundation for IO[A] in Cats-Effect")
  }
}
