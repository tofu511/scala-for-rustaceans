package exercises.solutions

import apiserver.domain._

/**
 * SOLUTION: Exercise 02 - Search Functionality
 * 
 * This file contains the complete implementation guide for user search.
 * Don't peek until you've tried it yourself!
 * 
 * NOTE: This file contains documentation and pseudocode.
 * Copy the actual code snippets to the appropriate files in your project.
 */
object Exercise02_Solution {
  
  // ============================================================
  // STEP 1: Repository Layer
  // ============================================================
  
  // Add this method to UserRepository trait:
  //
  // def search(query: String): EitherT[F, DomainError, List[User]]
  //
  // Then implement in DoobieUserRepository:
  /*
  override def search(query: String): EitherT[ConnectionIO, DomainError, List[User]] = {
    // Add % wildcards for LIKE pattern matching
    val pattern = s"%$query%"
    
    val searchQuery = sql"""
      SELECT id, name, email, age, created_at, updated_at
      FROM users
      WHERE LOWER(name) LIKE LOWER($pattern)
         OR LOWER(email) LIKE LOWER($pattern)
      ORDER BY id
    """.query[User]
    
    EitherT.right(searchQuery.to[List])
  }
  */
  
  // ============================================================
  // STEP 2: Service Layer
  // ============================================================
  
  // Add this method to UserService trait:
  //
  // def searchUsers(query: String): EitherT[IO, DomainError, List[User]]
  //
  // Then implement in UserServiceImpl:
  /*
  override def searchUsers(query: String): EitherT[IO, DomainError, List[User]] = {
    // Validate query is not empty
    if (query.trim.isEmpty) {
      EitherT.leftT[IO, List[User]](
        DomainError.ValidationError("query", "Search query cannot be empty")
      )
    } else if (query.length < 2) {
      // Optional: Require minimum length to prevent overly broad searches
      EitherT.leftT[IO, List[User]](
        DomainError.ValidationError("query", "Search query must be at least 2 characters")
      )
    } else {
      repository.search(query).transact(transactor)
    }
  }
  */
  
  // ============================================================
  // STEP 3: HTTP Layer
  // ============================================================
  
  // Update UserRoutes.scala to add search endpoint:
  //
  // Import query parameter matcher:
  // import org.http4s.dsl.impl.QueryParamDecoderMatcher
  //
  // object SearchQueryParamMatcher extends QueryParamDecoderMatcher[String]("q")
  //
  // Then add this route:
  /*
  case GET -> Root / "users" / "search" :? SearchQueryParamMatcher(query) =>
    service.searchUsers(query).foldF(
      error => BadRequest(ErrorResponse(error.getClass.getSimpleName, error.message).asJson),
      users => Ok(users.asJson)
    )
  */
  
  // ============================================================
  // USAGE EXAMPLES
  // ============================================================
  
  // Example 1: Search by name
  // GET /users/search?q=alice
  // Returns: Users with "alice" in name (case-insensitive)
  // [
  //   { "id": 1, "name": "Alice", "email": "alice@example.com", ... },
  //   { "id": 4, "name": "Alice Cooper", "email": "alice.cooper@example.com", ... }
  // ]
  
  // Example 2: Search by email domain
  // GET /users/search?q=gmail.com
  // Returns: Users with "gmail.com" in email
  // [
  //   { "id": 3, "name": "Charlie", "email": "charlie@gmail.com", ... }
  // ]
  
  // Example 3: No matches
  // GET /users/search?q=xyz
  // Returns: []
  
  // Example 4: Case-insensitive
  // GET /users/search?q=ALICE
  // Returns: Same as Example 1 (LOWER() makes it case-insensitive)
  
  // ============================================================
  // KEY POINTS
  // ============================================================
  
  // 1. LIKE pattern matching
  //    - % matches any characters (0 or more)
  //    - _ matches exactly one character
  //    - Example: '%alice%' matches "Alice", "alice123", "My alice"
  //
  // 2. Case-insensitive search
  //    - LOWER(name) LIKE LOWER(pattern)
  //    - Converts both sides to lowercase before comparing
  //    - Works for any language/character set
  //
  // 3. OR condition
  //    - Search in multiple fields: name OR email
  //    - Could extend to other fields (phone, address, etc.)
  //
  // 4. Validation
  //    - Check query is not empty
  //    - Optional: minimum length to prevent performance issues
  //    - Optional: maximum length to prevent abuse
  
  // ============================================================
  // PERFORMANCE CONSIDERATIONS
  // ============================================================
  
  // 1. LIKE with leading % is slow (can't use index)
  //    - '%alice' can't use index on name column
  //    - For large tables, consider full-text search (PostgreSQL's ts_vector)
  //
  // 2. Add index for better performance (if needed)
  //    - CREATE INDEX idx_users_name ON users(LOWER(name));
  //    - CREATE INDEX idx_users_email ON users(LOWER(email));
  //
  // 3. Limit results
  //    - Add LIMIT clause to prevent returning too many rows
  //    - Example: LIMIT 100
  //
  // 4. Combine with pagination
  //    - For production, combine search with pagination
  //    - Example: GET /users/search?q=alice&page=1&pageSize=10
  
  // ============================================================
  // RUST COMPARISON
  // ============================================================
  
  // In Rust with sqlx:
  //
  // async fn search_users(pool: &PgPool, query: &str) -> Result<Vec<User>, sqlx::Error> {
  //     let pattern = format!("%{}%", query);
  //     sqlx::query_as!(
  //         User,
  //         r#"
  //         SELECT id, name, email, age, created_at, updated_at
  //         FROM users
  //         WHERE LOWER(name) LIKE LOWER($1) OR LOWER(email) LIKE LOWER($1)
  //         ORDER BY id
  //         "#,
  //         pattern
  //     )
  //     .fetch_all(pool)
  //     .await
  // }
  //
  // Very similar to Scala/Doobie approach!
}
