package apiserver.service

import cats.effect.IO
import cats.data.EitherT
import doobie.ConnectionIO
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import apiserver.domain._
import apiserver.repository.UserRepository

/**
 * Unit tests for UserService with mock repository.
 * 
 * Rust Comparison:
 * Similar to Rust testing with trait mocks:
 * 
 * struct MockUserRepository {
 *     users: Vec<User>,
 * }
 * 
 * impl UserRepository for MockUserRepository {
 *     async fn find_all(&self) -> Result<Vec<User>, DomainError> {
 *         Ok(self.users.clone())
 *     }
 * }
 * 
 * #[tokio::test]
 * async fn test_user_service() {
 *     let mock_repo = MockUserRepository { users: vec![] };
 *     let service = UserService::new(mock_repo);
 *     // Test service logic
 * }
 */
class UserServiceSpec extends AnyFlatSpec with Matchers {

  // Mock repository for testing (in-memory implementation)
  class MockUserRepository extends UserRepository[ConnectionIO] {
    private var users: Map[Long, User] = Map.empty
    private var nextId: Long = 1L

    override def findAll(): EitherT[ConnectionIO, DomainError, List[User]] =
      EitherT.rightT(users.values.toList)

    override def findById(id: Long): EitherT[ConnectionIO, DomainError, User] =
      users.get(id) match {
        case Some(user) => EitherT.rightT(user)
        case None => EitherT.leftT(DomainError.UserNotFound(id))
      }

    override def findByEmail(email: String): ConnectionIO[Option[User]] =
      doobie.free.connection.pure(users.values.find(_.email == email))

    override def create(req: CreateUserRequest): EitherT[ConnectionIO, DomainError, User] = {
      val now = java.time.LocalDateTime.now()
      val user = User(nextId, req.name, req.email, req.age, now, now)
      users = users + (nextId -> user)
      nextId += 1
      EitherT.rightT(user)
    }

    override def update(id: Long, req: UpdateUserRequest): EitherT[ConnectionIO, DomainError, User] =
      users.get(id) match {
        case Some(existing) =>
          val updated = existing.copy(
            name = req.name,
            email = req.email,
            age = req.age,
            updatedAt = java.time.LocalDateTime.now()
          )
          users = users + (id -> updated)
          EitherT.rightT(updated)
        case None =>
          EitherT.leftT(DomainError.UserNotFound(id))
      }

    override def delete(id: Long): EitherT[ConnectionIO, DomainError, Unit] =
      if (users.contains(id)) {
        users = users - id
        EitherT.rightT(())
      } else {
        EitherT.leftT(DomainError.UserNotFound(id))
      }

    // Helper for testing
    def clear(): Unit = {
      users = Map.empty
      nextId = 1L
    }
  }

  // Helper to run ConnectionIO in tests (no actual DB needed)
  // Note: For pure unit tests of service logic, we don't need to execute against a real DB
  // The validation tests below test the logic without needing database execution

  val mockRepo = new MockUserRepository()
  val service = new UserServiceImpl(mockRepo)

  override def withFixture(test: NoArgTest) = {
    mockRepo.clear()
    super.withFixture(test)
  }

  "UserService.create" should "reject empty name" in {
    val request = CreateUserRequest("", "alice@example.com", 30)
    
    // Since validation returns Either, we can test synchronously
    val result = service.validateCreateRequest(request)
    
    result shouldBe Left(DomainError.ValidationError("name", "cannot be empty"))
  }

  it should "reject blank name" in {
    val request = CreateUserRequest("   ", "alice@example.com", 30)
    val result = service.validateCreateRequest(request)
    
    result shouldBe Left(DomainError.ValidationError("name", "cannot be empty"))
  }

  it should "reject invalid email" in {
    val request = CreateUserRequest("Alice", "not-an-email", 30)
    val result = service.validateCreateRequest(request)
    
    result shouldBe Left(DomainError.ValidationError("email", "must be valid email address"))
  }

  it should "reject negative age" in {
    val request = CreateUserRequest("Alice", "alice@example.com", -1)
    val result = service.validateCreateRequest(request)
    
    result shouldBe Left(DomainError.ValidationError("age", "must be between 0 and 150"))
  }

  it should "reject age above 150" in {
    val request = CreateUserRequest("Alice", "alice@example.com", 151)
    val result = service.validateCreateRequest(request)
    
    result shouldBe Left(DomainError.ValidationError("age", "must be between 0 and 150"))
  }

  it should "accept valid request" in {
    val request = CreateUserRequest("Alice", "alice@example.com", 30)
    val result = service.validateCreateRequest(request)
    
    result shouldBe Right(request)
  }

  "UserService.update" should "reject empty name" in {
    val request = UpdateUserRequest("", "alice@example.com", 30)
    val result = service.validateUpdateRequest(request)
    
    result shouldBe Left(DomainError.ValidationError("name", "cannot be empty"))
  }

  it should "reject invalid email" in {
    val request = UpdateUserRequest("Alice", "not-an-email", 30)
    val result = service.validateUpdateRequest(request)
    
    result shouldBe Left(DomainError.ValidationError("email", "must be valid email address"))
  }

  it should "reject negative age" in {
    val request = UpdateUserRequest("Alice", "alice@example.com", -1)
    val result = service.validateUpdateRequest(request)
    
    result shouldBe Left(DomainError.ValidationError("age", "must be between 0 and 150"))
  }

  it should "accept valid request" in {
    val request = UpdateUserRequest("Alice", "alice@example.com", 30)
    val result = service.validateUpdateRequest(request)
    
    result shouldBe Right(request)
  }

  "Email validation" should "accept valid emails" in {
    val validEmails = List(
      "alice@example.com",
      "bob.smith@company.co.uk",
      "user+tag@domain.org",
      "test_user@sub.domain.com"
    )

    validEmails.foreach { email =>
      withClue(s"Email: $email") {
        service.isValidEmail(email) shouldBe true
      }
    }
  }

  it should "reject invalid emails" in {
    val invalidEmails = List(
      "not-an-email",
      "@example.com",
      "user@",
      "user name@example.com",
      "user@domain",
      ""
    )

    invalidEmails.foreach { email =>
      withClue(s"Email: $email") {
        service.isValidEmail(email) shouldBe false
      }
    }
  }
}
