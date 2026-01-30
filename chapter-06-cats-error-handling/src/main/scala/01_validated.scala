package catserrorhandling

import cats.data.Validated
import cats.data.Validated.{Valid, Invalid}
import cats.instances.list._
import cats.syntax.apply._

/*
 * VALIDATED - ACCUMULATING ERRORS
 * 
 * Validated is like Either, but designed for accumulating errors rather than
 * short-circuiting. Perfect for form validation where you want to show all
 * errors at once, not just the first one.
 *
 * RUST COMPARISON:
 * Rust's Result<T, E> short-circuits on first error (like Either).
 * To accumulate errors in Rust, you'd manually collect them:
 *   let mut errors = Vec::new();
 *   if check1.is_err() { errors.push(...) }
 *   if check2.is_err() { errors.push(...) }
 *   if errors.is_empty() { Ok(value) } else { Err(errors) }
 * 
 * Validated handles this pattern elegantly with type-safe combinators.
 */

object ValidatedBasics {
  
  // Validated[E, A] has two constructors:
  // - Valid(a): Success with value a
  // - Invalid(e): Failure with error(s) e
  
  def demonstrateBasics(): Unit = {
    println("\n=== Validated Basics ===\n")
    
    val validInt: Validated[String, Int] = Valid(42)
    val invalidInt: Validated[String, Int] = Invalid("Not a number")
    
    println(s"Valid(42) = $validInt")
    println(s"Invalid('Not a number') = $invalidInt")
    
    // Pattern matching
    validInt match {
      case Valid(n) => println(s"Got valid number: $n")
      case Invalid(err) => println(s"Got error: $err")
    }
    
    // Convert to Either
    println(s"\nValid(42).toEither = ${validInt.toEither}")
    println(s"Invalid(...).toEither = ${invalidInt.toEither}")
    
    // Rust comparison:
    // Validated is like Result<T, E>, but accumulates errors:
    // let result: Result<i32, String> = Ok(42);
  }
  
  def demonstrateTransformations(): Unit = {
    println("\n=== Transformations ===\n")
    
    val valid: Validated[String, Int] = Valid(10)
    
    // map - transform the valid value
    println(s"Valid(10).map(_ * 2) = ${valid.map(_ * 2)}")
    
    // leftMap - transform the error (if invalid)
    val invalid: Validated[String, Int] = Invalid("error")
    println(s"Invalid('error').leftMap(_.toUpperCase) = ${invalid.leftMap(_.toUpperCase)}")
    
    // bimap - transform both sides
    println(s"Invalid('error').bimap(_.toUpperCase, _ * 2) = ${invalid.bimap(_.toUpperCase, _ * 2)}")
    println(s"Valid(10).bimap(_.toUpperCase, _ * 2) = ${valid.bimap(_.toUpperCase, _ * 2)}")
    
    // Rust comparison:
    // result.map(|x| x * 2)           // Scala: map
    // result.map_err(|e| e.to_uppercase()) // Scala: leftMap
  }
  
  def demonstrateAccumulation(): Unit = {
    println("\n=== Error Accumulation ===\n")
    
    // Using List[String] to accumulate multiple errors
    type ValidationResult[A] = Validated[List[String], A]
    
    // Individual validations
    def validateAge(age: Int): ValidationResult[Int] = {
      if (age >= 18 && age <= 120) Valid(age)
      else Invalid(List(s"Age must be between 18 and 120, got $age"))
    }
    
    def validateName(name: String): ValidationResult[String] = {
      if (name.nonEmpty && name.length <= 100) Valid(name)
      else Invalid(List(s"Name must be 1-100 characters, got length ${name.length}"))
    }
    
    def validateEmail(email: String): ValidationResult[String] = {
      if (email.contains("@")) Valid(email)
      else Invalid(List(s"Email must contain @, got '$email'"))
    }
    
    // Combine validations with mapN (applicative)
    case class User(name: String, email: String, age: Int)
    
    def validateUser(name: String, email: String, age: Int): ValidationResult[User] = {
      (validateName(name), validateEmail(email), validateAge(age)).mapN(User)
    }
    
    println("Valid user:")
    val validUser = validateUser("Alice", "alice@example.com", 25)
    println(s"  $validUser")
    
    println("\nInvalid user (multiple errors):")
    val invalidUser = validateUser("", "invalid-email", 15)
    println(s"  $invalidUser")
    
    println("\nAnother invalid user:")
    val invalidUser2 = validateUser("x" * 101, "noemail", 200)
    println(s"  $invalidUser2")
    
    // Rust comparison:
    // In Rust, you'd need to manually collect errors:
    // let mut errors = Vec::new();
    // if let Err(e) = validate_name(name) { errors.push(e); }
    // if let Err(e) = validate_email(email) { errors.push(e); }
    // if let Err(e) = validate_age(age) { errors.push(e); }
    // if errors.is_empty() { Ok(User { ... }) } else { Err(errors) }
  }
  
  def demonstrateValidatedVsEither(): Unit = {
    println("\n=== Validated vs Either ===\n")
    
    type ValidationResult[A] = Validated[List[String], A]
    
    def checkPositive(n: Int): ValidationResult[Int] = {
      if (n > 0) Valid(n) else Invalid(List(s"$n is not positive"))
    }
    
    def checkEven(n: Int): ValidationResult[Int] = {
      if (n % 2 == 0) Valid(n) else Invalid(List(s"$n is not even"))
    }
    
    // With Validated - accumulates errors (but can't chain)
    println("Validated (accumulates):")
    val result1 = (checkPositive(-2), checkEven(-2)).mapN((_, _))
    println(s"  (-2, -2): $result1")  // Both errors!
    
    // With Either - short-circuits
    println("\nEither (short-circuits):")
    val result2: Either[List[String], (Int, Int)] = for {
      a <- checkPositive(-2).toEither
      b <- checkEven(-2).toEither
    } yield (a, b)
    println(s"  (-2, -2): $result2")  // Only first error!
    
    println("\nKey insight:")
    println("  - Use Either when operations depend on each other (sequential)")
    println("  - Use Validated when validations are independent (parallel)")
    
    // Rust comparison:
    // Either-like: Using ? operator (short-circuit)
    //   let a = validate_a()?;  // Returns on first error
    //   let b = validate_b()?;
    //   Ok((a, b))
    //
    // Validated-like: Manual accumulation
    //   let errors = [validate_a(), validate_b()]
    //     .into_iter()
    //     .filter_map(Result::err)
    //     .collect::<Vec<_>>();
    //   if errors.is_empty() { Ok(data) } else { Err(errors) }
  }
  
  def main(args: Array[String]): Unit = {
    println("=" * 50)
    println("VALIDATED - ACCUMULATING ERRORS")
    println("=" * 50)
    
    demonstrateBasics()
    demonstrateTransformations()
    demonstrateAccumulation()
    demonstrateValidatedVsEither()
    
    println("\n" + "=" * 50)
    println("KEY TAKEAWAYS")
    println("=" * 50)
    println("""
1. Validated accumulates errors (unlike Either)
2. Use mapN to combine independent validations
3. Perfect for form validation scenarios
4. Convert to/from Either as needed
5. Rust: Manual error collection vs Cats: type-safe combinators
    """.trim)
  }
}
