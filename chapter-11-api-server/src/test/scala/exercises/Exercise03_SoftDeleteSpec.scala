package exercises

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import apiserver.domain._
import java.time.LocalDateTime

/**
 * Exercise 3: Soft Delete (TDD Style)
 * 
 * GOAL: Implement soft delete - mark users as deleted instead of removing them
 * 
 * WORKFLOW:
 * 1. Run this test - it will FAIL (RED)
 * 2. Add deletedAt field to User model
 * 3. Update queries to filter deleted users
 * 4. Implement soft delete logic
 * 5. Run the test again - it should PASS (GREEN)
 * 
 * Rust Comparison:
 * struct User {
 *     id: i64,
 *     name: String,
 *     email: String,
 *     age: i32,
 *     created_at: DateTime<Utc>,
 *     updated_at: DateTime<Utc>,
 *     deleted_at: Option<DateTime<Utc>>,  // Add this field
 * }
 */
class Exercise03_SoftDeleteSpec extends AnyFlatSpec with Matchers {

  "User model with soft delete" should "have optional deletedAt field" in {
    // TODO: Uncomment once you add deletedAt to User model
    // val now = LocalDateTime.now()
    // val user = User(1, "Alice", "alice@example.com", 30, now, now, None)
    // 
    // user.deletedAt shouldBe None
    
    pending
  }

  it should "be able to mark as deleted" in {
    // TODO: Uncomment once you add deletedAt to User model
    // val now = LocalDateTime.now()
    // val user = User(1, "Alice", "alice@example.com", 30, now, now, None)
    // val deleted = user.copy(deletedAt = Some(LocalDateTime.now()))
    // 
    // deleted.deletedAt shouldBe defined
    
    pending
  }

  "Soft delete helper methods" should "identify deleted users" in {
    // TODO: Add a helper method to User model to check if deleted
    // Example:
    // extension methods or helper in companion object
    // object User {
    //   implicit class UserOps(user: User) {
    //     def isDeleted: Boolean = user.deletedAt.isDefined
    //     def isActive: Boolean = user.deletedAt.isEmpty
    //   }
    // }
    
    // val now = LocalDateTime.now()
    // val activeUser = User(1, "Alice", "alice@example.com", 30, now, now, None)
    // val deletedUser = User(2, "Bob", "bob@example.com", 25, now, now, Some(now))
    // 
    // import User._
    // activeUser.isActive shouldBe true
    // activeUser.isDeleted shouldBe false
    // deletedUser.isActive shouldBe false
    // deletedUser.isDeleted shouldBe true
    
    pending
  }

  "Filter logic" should "exclude deleted users from results" in {
    // TODO: Test that your queries filter out deleted users
    // val now = LocalDateTime.now()
    // val users = List(
    //   User(1, "Alice", "alice@example.com", 30, now, now, None),
    //   User(2, "Bob", "bob@example.com", 25, now, now, Some(now)), // Deleted
    //   User(3, "Charlie", "charlie@example.com", 35, now, now, None)
    // )
    // 
    // import User._
    // val activeUsers = users.filter(_.isActive)
    // activeUsers should have size 2
    // activeUsers.map(_.name) should contain only ("Alice", "Charlie")
    
    pending
  }

  "Soft delete vs hard delete" should "preserve data" in {
    // TODO: This is a conceptual test
    // Think about: How would you implement a separate "hard delete" for admins?
    // Perhaps:
    // - Regular DELETE: Sets deletedAt
    // - Admin DELETE /admin/users/:id: Actually removes from DB
    
    // val now = LocalDateTime.now()
    // val user = User(1, "Alice", "alice@example.com", 30, now, now, Some(now))
    // 
    // // Soft deleted users should still be in database
    // // but marked with deletedAt timestamp
    // user.deletedAt shouldBe defined
    
    pending
  }
}
