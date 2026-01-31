package exercises

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import apiserver.domain._
import apiserver.repository.UserRepository
import cats.data.EitherT
import doobie.ConnectionIO

/**
 * Exercise 2: Search Functionality (TDD Style)
 * 
 * GOAL: Add search capability to find users by name or email
 * 
 * WORKFLOW:
 * 1. Run this test - it will FAIL (RED)
 * 2. Implement the search functionality
 * 3. Run the test again - it should PASS (GREEN)
 */
class Exercise02_SearchSpec extends AnyFlatSpec with Matchers {

  // Mock repository for testing
  class MockSearchRepository extends UserRepository[ConnectionIO] {
    import java.time.LocalDateTime
    
    private val users = List(
      User(1, "Alice", "alice@example.com", 30, LocalDateTime.now(), LocalDateTime.now()),
      User(2, "Bob", "bob@example.com", 25, LocalDateTime.now(), LocalDateTime.now()),
      User(3, "Charlie", "charlie@gmail.com", 35, LocalDateTime.now(), LocalDateTime.now()),
      User(4, "Alice Cooper", "alice.cooper@example.com", 45, LocalDateTime.now(), LocalDateTime.now())
    )

    override def findAll(): EitherT[ConnectionIO, DomainError, List[User]] =
      EitherT.rightT(users)

    override def findById(id: Long): EitherT[ConnectionIO, DomainError, User] =
      users.find(_.id == id) match {
        case Some(user) => EitherT.rightT(user)
        case None => EitherT.leftT(DomainError.UserNotFound(id))
      }

    override def findByEmail(email: String): ConnectionIO[Option[User]] =
      doobie.free.connection.pure(users.find(_.email == email))

    override def create(req: CreateUserRequest): EitherT[ConnectionIO, DomainError, User] =
      EitherT.rightT(users.head) // Not used in search tests

    override def update(id: Long, req: UpdateUserRequest): EitherT[ConnectionIO, DomainError, User] =
      EitherT.rightT(users.head) // Not used in search tests

    override def delete(id: Long): EitherT[ConnectionIO, DomainError, Unit] =
      EitherT.rightT(()) // Not used in search tests

    // Simple mock implementation for testing
    def search(query: String): List[User] = {
      val lowerQuery = query.toLowerCase
      users.filter { user =>
        user.name.toLowerCase.contains(lowerQuery) ||
        user.email.toLowerCase.contains(lowerQuery)
      }
    }
  }

  val mockRepo = new MockSearchRepository()

  "Search functionality" should "find users by name (case-insensitive)" in {
    // TODO: Uncomment once you implement search in the repository
    // val results = mockRepo.search("alice")
    // results should have size 2 // Alice and Alice Cooper
    // results.map(_.name) should contain allOf ("Alice", "Alice Cooper")
    
    pending
  }

  it should "find users by email domain" in {
    // TODO: Uncomment once you implement search
    // val results = mockRepo.search("example.com")
    // results should not be empty
    // results.foreach(_.email should include ("example.com"))
    
    pending
  }

  it should "return empty list when no matches" in {
    // TODO: Uncomment once you implement search
    // val results = mockRepo.search("nonexistent")
    // results shouldBe empty
    
    pending
  }

  it should "be case-insensitive" in {
    // TODO: Uncomment once you implement search
    // val results1 = mockRepo.search("ALICE")
    // val results2 = mockRepo.search("alice")
    // results1 should have size results2.size
    
    pending
  }

  it should "find partial matches" in {
    // TODO: Uncomment once you implement search
    // val results = mockRepo.search("ali")  // Should find "Alice" and "Alice Cooper"
    // results should have size 2
    
    pending
  }
}
