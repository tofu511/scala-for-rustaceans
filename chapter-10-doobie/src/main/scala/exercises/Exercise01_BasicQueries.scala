package doobiebasics.exercises

import cats.effect._
import doobie._
import doobie.implicits._

/*
 * EXERCISE: Basic Doobie Queries
 * 
 * TASKS:
 * 1. Create transactor
 * 2. Write findAll query
 * 3. Write insert query
 * 4. Write update query
 * 5. Write delete query
 * 
 * See solutions/Exercise01_Solution.scala for complete implementation
 */

object Exercise01BasicQueries extends IOApp.Simple {
  
  case class Book(id: Long, title: String, author: String)
  
  val dbUrl = "jdbc:postgresql://localhost:5432/testdb"
  val dbUser = "postgres"
  val dbPassword = "password"
  
  def createTransactor(): Resource[IO, Transactor[IO]] = {
    ExecutionContexts.fixedThreadPool[IO](4).map { ec =>
      Transactor.fromDriverManager[IO](
        driver = "org.postgresql.Driver",
        url = dbUrl,
        user = dbUser,
        password = dbPassword,
        logHandler = None
      )
    }
  }
  
  // TODO: Implement these methods
  def findAll(): ConnectionIO[List[Book]] = ???
  def insertBook(title: String, author: String): ConnectionIO[Long] = ???
  def updateBook(id: Long, title: String): ConnectionIO[Int] = ???
  def deleteBook(id: Long): ConnectionIO[Int] = ???
  
  def run: IO[Unit] = {
    IO.println("See solutions/Exercise01_Solution.scala")
  }
}
