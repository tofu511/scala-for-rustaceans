package exercises

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import apiserver.domain._

/**
 * Exercise 1: Pagination Support (TDD Style)
 * 
 * GOAL: Add pagination to the "list users" endpoint
 * 
 * WORKFLOW:
 * 1. Run this test - it will FAIL (RED) because the features don't exist yet
 * 2. Implement the features following the hints in EXERCISES.md
 * 3. Run the test again - it should PASS (GREEN)
 * 4. Refactor if needed
 * 
 * This is Test-Driven Development (TDD)!
 * 
 * Rust Comparison:
 * This is like writing tests first in Rust:
 * 
 * #[test]
 * fn test_pagination() {
 *     let params = PaginationParams { page: 1, page_size: 10 };
 *     assert_eq!(params.offset(), 0);
 *     assert_eq!(params.limit(), 10);
 * }
 * 
 * Then implementing the struct to make the test pass.
 */
class Exercise01_PaginationSpec extends AnyFlatSpec with Matchers {

  "PaginationParams" should "calculate correct offset for page 1" in {
    // TODO: Uncomment this test once you add PaginationParams to Models.scala
    // val params = PaginationParams(page = 1, pageSize = 10)
    // params.offset shouldBe 0
    // params.limit shouldBe 10
    
    pending // Remove this line once you implement PaginationParams
  }

  it should "calculate correct offset for page 2" in {
    // TODO: Uncomment this test
    // val params = PaginationParams(page = 2, pageSize = 10)
    // params.offset shouldBe 10
    // params.limit shouldBe 10
    
    pending
  }

  it should "calculate correct offset for page 3 with page size 5" in {
    // TODO: Uncomment this test
    // val params = PaginationParams(page = 3, pageSize = 5)
    // params.offset shouldBe 10
    // params.limit shouldBe 5
    
    pending
  }

  "PaginatedResponse" should "contain all required fields" in {
    // TODO: Uncomment this test once you add PaginatedResponse to Models.scala
    // import java.time.LocalDateTime
    // val user1 = User(1, "Alice", "alice@example.com", 30, LocalDateTime.now(), LocalDateTime.now())
    // val user2 = User(2, "Bob", "bob@example.com", 25, LocalDateTime.now(), LocalDateTime.now())
    // val response = PaginatedResponse(
    //   data = List(user1, user2),
    //   page = 1,
    //   pageSize = 10,
    //   total = 2
    // )
    // 
    // response.data should have size 2
    // response.page shouldBe 1
    // response.pageSize shouldBe 10
    // response.total shouldBe 2
    
    pending
  }

  it should "work with empty data" in {
    // TODO: Uncomment this test
    // val response = PaginatedResponse[User](
    //   data = List.empty,
    //   page = 1,
    //   pageSize = 10,
    //   total = 0
    // )
    // 
    // response.data shouldBe empty
    // response.total shouldBe 0
    
    pending
  }
}
