package errorhandling.exercises.solutions

// Copy error types from exercise
sealed trait ValidationError
case class InvalidUsername(reason: String) extends ValidationError
case class InvalidEmail(reason: String) extends ValidationError
case class InvalidAge(reason: String) extends ValidationError

case class Account(username: String, email: String, age: Int)

object Exercise01EitherSolution {
  
  def parsePositiveInt(s: String): Either[String, Int] = {
    try {
      val num = s.toInt
      if (num > 0) Right(num)
      else Left(s"Number must be positive: $num")
    } catch {
      case _: NumberFormatException => Left(s"Invalid number: $s")
    }
  }
  
  def validateEmail(email: String): Either[InvalidEmail, String] = {
    if (email.contains("@") && email.contains(".")) Right(email)
    else Left(InvalidEmail(s"Invalid email format: $email"))
  }
  
  def validateUsername(username: String): Either[InvalidUsername, String] = {
    if (username.length < 3) Left(InvalidUsername("Username too short"))
    else if (username.length > 20) Left(InvalidUsername("Username too long"))
    else if (!username.forall(_.isLetterOrDigit)) 
      Left(InvalidUsername("Username must be alphanumeric"))
    else Right(username)
  }
  
  def validateAge(age: Int): Either[InvalidAge, Int] = {
    if (age < 13) Left(InvalidAge("Must be at least 13 years old"))
    else if (age > 120) Left(InvalidAge("Invalid age"))
    else Right(age)
  }
  
  def createAccount(
    username: String,
    email: String,
    ageStr: String
  ): Either[ValidationError, Account] = {
    for {
      validUsername <- validateUsername(username)
      validEmail <- validateEmail(email)
      age <- parsePositiveInt(ageStr).left.map(msg => InvalidAge(msg))
      validAge <- validateAge(age)
    } yield Account(validUsername, validEmail, validAge)
  }
  
  def main(args: Array[String]): Unit = {
    println("=== Exercise 01 Solution ===\n")
    
    // Test all functions
    println("--- parsePositiveInt ---")
    println(s"parsePositiveInt('42') = ${parsePositiveInt("42")}")
    println(s"parsePositiveInt('0') = ${parsePositiveInt("0")}")
    
    println("\n--- validateEmail ---")
    println(s"validateEmail('user@example.com') = ${validateEmail("user@example.com")}")
    println(s"validateEmail('invalid') = ${validateEmail("invalid")}")
    
    println("\n--- createAccount ---")
    println(s"Valid: ${createAccount("alice", "alice@example.com", "25")}")
    println(s"Invalid username: ${createAccount("ab", "alice@example.com", "25")}")
    println(s"Invalid email: ${createAccount("alice", "invalid", "25")}")
    println(s"Invalid age: ${createAccount("alice", "alice@example.com", "12")}")
  }
}
