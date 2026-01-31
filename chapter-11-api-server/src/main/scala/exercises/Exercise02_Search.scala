package exercises

import apiserver.domain._
import cats.effect._
import cats.data.EitherT

/**
 * EXERCISE 02: Search Functionality
 * 
 * OBJECTIVES:
 * - Add search capability to find users by name or email
 * - Use SQL LIKE for pattern matching
 * - Make search case-insensitive
 * 
 * RUST COMPARISON:
 * Similar to implementing search in Rust with sqlx:
 * 
 * async fn search_users(pool: &PgPool, query: &str) -> Result<Vec<User>> {
 *     sqlx::query_as!(
 *         User,
 *         "SELECT * FROM users WHERE LOWER(name) LIKE LOWER($1) OR LOWER(email) LIKE LOWER($1)",
 *         format!("%{}%", query)
 *     )
 *     .fetch_all(pool)
 *     .await
 * }
 * 
 * TASKS:
 * 1. Add search method to UserRepository
 * 2. Add search method to UserService
 * 3. Add search endpoint to HTTP routes
 * 
 * TDD WORKFLOW:
 * 1. Run tests: sbt "testOnly exercises.Exercise02_SearchSpec"
 * 2. See RED (failures)
 * 3. Implement the code below
 * 4. Run tests again - see GREEN
 */
object Exercise02_Search {
  
  // HINT: Repository Layer
  // Add this method to UserRepository trait:
  //
  // def search(query: String): EitherT[F, DomainError, List[User]]
  //
  // Then implement in UserRepositoryImpl:
  //
  // override def search(query: String): EitherT[ConnectionIO, DomainError, List[User]] = {
  //   val pattern = s"%$query%"
  //   val sql = sql"""
  //     SELECT id, name, email, age, created_at, updated_at
  //     FROM users
  //     WHERE LOWER(name) LIKE LOWER($pattern)
  //        OR LOWER(email) LIKE LOWER($pattern)
  //     ORDER BY id
  //   """.query[User]
  //   
  //   EitherT.right(sql.to[List])
  // }
  
  // HINT: Service Layer
  // Add this method to UserService trait:
  //
  // def searchUsers(query: String): EitherT[IO, DomainError, List[User]]
  //
  // Then implement in UserServiceImpl:
  //
  // override def searchUsers(query: String): EitherT[IO, DomainError, List[User]] = {
  //   if (query.trim.isEmpty) {
  //     EitherT.leftT[IO, List[User]](
  //       DomainError.ValidationError("query", "Search query cannot be empty")
  //     )
  //   } else {
  //     repository.search(query).transact(transactor)
  //   }
  // }
  
  // HINT: HTTP Layer
  // Update UserRoutes.scala to add search endpoint:
  //
  // object SearchQueryParamMatcher extends QueryParamDecoderMatcher[String]("q")
  //
  // Then in your routes:
  //
  // case GET -> Root / "users" / "search" :? SearchQueryParamMatcher(query) =>
  //   service.searchUsers(query).foldF(
  //     error => BadRequest(ErrorResponse(error.getClass.getSimpleName, error.message).asJson),
  //     users => Ok(users.asJson)
  //   )
  
  // TESTING:
  // After implementation, run:
  // sbt "testOnly exercises.Exercise02_SearchSpec"
  //
  // All 5 tests should pass!
  
  // EXPECTED BEHAVIOR:
  // GET /users/search?q=alice
  // => Returns users with "alice" in name or email (case-insensitive)
  //
  // GET /users/search?q=example.com
  // => Returns users with "example.com" in email
  //
  // GET /users/search?q=XYZ
  // => Returns empty list (no matches)
  
  // SQL TIPS:
  // - LOWER() converts strings to lowercase
  // - LIKE with % matches any characters
  // - Example: LOWER(name) LIKE LOWER('%alice%') matches "Alice", "ALICE", "alice"
  
  // COMMON MISTAKES:
  // ❌ Forgetting to add % wildcards: LIKE 'alice' (only exact match)
  // ✅ Correct: LIKE '%alice%' (matches anywhere in string)
  //
  // ❌ Case-sensitive search: name LIKE '%Alice%' (misses "alice")
  // ✅ Case-insensitive: LOWER(name) LIKE LOWER('%Alice%')
}
