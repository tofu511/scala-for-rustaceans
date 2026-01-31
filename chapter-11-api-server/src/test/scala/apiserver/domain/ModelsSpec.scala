package apiserver.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Unit tests for domain models and validation logic.
 * 
 * Rust Comparison:
 * Similar to Rust's #[cfg(test)] module tests:
 * 
 * #[cfg(test)]
 * mod tests {
 *     use super::*;
 *     
 *     #[test]
 *     fn test_user_validation() {
 *         let user = User::new("Alice", "alice@example.com", 30);
 *         assert!(user.is_ok());
 *     }
 * }
 */
class ModelsSpec extends AnyFlatSpec with Matchers {

  "CreateUserRequest" should "be valid with correct data" in {
    val request = CreateUserRequest("Alice", "alice@example.com", 30)
    
    request.name shouldBe "Alice"
    request.email shouldBe "alice@example.com"
    request.age shouldBe 30
  }

  it should "accept minimum valid age" in {
    val request = CreateUserRequest("Baby", "baby@example.com", 0)
    request.age shouldBe 0
  }

  it should "accept maximum valid age" in {
    val request = CreateUserRequest("Elder", "elder@example.com", 150)
    request.age shouldBe 150
  }

  "UpdateUserRequest" should "be valid with correct data" in {
    val request = UpdateUserRequest("Bob", "bob@example.com", 25)
    
    request.name shouldBe "Bob"
    request.email shouldBe "bob@example.com"
    request.age shouldBe 25
  }

  "DomainError.ValidationError" should "contain field and issue information" in {
    val error = DomainError.ValidationError("email", "must be valid email address")
    
    error.field shouldBe "email"
    error.issue shouldBe "must be valid email address"
    error.message should include("email")
    error.message should include("must be valid email address")
  }

  "DomainError.UserNotFound" should "contain user ID" in {
    val error = DomainError.UserNotFound(42L)
    
    error.id shouldBe 42L
    error.message should include("42")
  }

  "DomainError.DatabaseError" should "contain error message" in {
    val error = DomainError.DatabaseError("Connection timeout")
    
    error.cause shouldBe "Connection timeout"
    error.message should include("Connection timeout")
  }

  "User" should "have all required fields" in {
    val now = java.time.LocalDateTime.now()
    val user = User(
      id = 1L,
      name = "Alice",
      email = "alice@example.com",
      age = 30,
      createdAt = now,
      updatedAt = now
    )

    user.id shouldBe 1L
    user.name shouldBe "Alice"
    user.email shouldBe "alice@example.com"
    user.age shouldBe 30
    user.createdAt shouldBe now
    user.updatedAt shouldBe now
  }

  "HealthCheckResponse" should "have status and timestamp" in {
    val response = HealthCheckResponse("healthy", 1234567890L)
    
    response.status shouldBe "healthy"
    response.timestamp shouldBe 1234567890L
  }

  "DeleteResponse" should "contain success message" in {
    val response = DeleteResponse("User deleted successfully")
    
    response.message shouldBe "User deleted successfully"
  }
}
