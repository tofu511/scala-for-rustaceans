package exercises.solutions

import apiserver.domain._

/**
 * SOLUTION: Exercise 01 - Pagination
 * 
 * This file contains the complete implementation guide for pagination.
 * Don't peek until you've tried it yourself!
 * 
 * NOTE: This file contains documentation and pseudocode.
 * Copy the actual code snippets to the appropriate files in your project.
 */
object Exercise01_Solution {
  
  // ============================================================
  // STEP 1: Domain Models (add to domain/Models.scala)
  // ============================================================
  
  case class PaginationParams(page: Int, pageSize: Int) {
    def offset: Int = (page - 1) * pageSize
    def limit: Int = pageSize
  }
  
  case class PaginatedResponse[A](
    data: List[A],
    page: Int,
    pageSize: Int,
    total: Long
  )
  
  // ============================================================
  // STEP 2: Repository Layer
  // ============================================================
  
  // Add these methods to UserRepository trait in repository/UserRepository.scala:
  //
  // def findAllPaginated(offset: Int, limit: Int): EitherT[F, DomainError, List[User]]
  // def count(): EitherT[F, DomainError, Long]
  //
  // Then implement in DoobieUserRepository:
  /*
  override def findAllPaginated(offset: Int, limit: Int): EitherT[ConnectionIO, DomainError, List[User]] = {
    val query = sql"""
      SELECT id, name, email, age, created_at, updated_at
      FROM users
      ORDER BY id
      LIMIT $limit OFFSET $offset
    """.query[User]
    
    EitherT.right(query.to[List])
  }
  
  override def count(): EitherT[ConnectionIO, DomainError, Long] = {
    val query = sql"SELECT COUNT(*) FROM users".query[Long]
    EitherT.right(query.unique)
  }
  */
  
  // ============================================================
  // STEP 3: Service Layer
  // ============================================================
  
  // Add this method to UserService trait in service/UserService.scala:
  //
  // def listPaginated(params: PaginationParams): EitherT[IO, DomainError, PaginatedResponse[User]]
  //
  // Then implement in UserServiceImpl:
  /*
  override def listPaginated(params: PaginationParams): EitherT[IO, DomainError, PaginatedResponse[User]] = {
    // Validate page and pageSize
    if (params.page < 1) {
      EitherT.leftT[IO, PaginatedResponse[User]](
        DomainError.ValidationError("page", "must be >= 1")
      )
    } else if (params.pageSize < 1 || params.pageSize > 100) {
      EitherT.leftT[IO, PaginatedResponse[User]](
        DomainError.ValidationError("pageSize", "must be between 1 and 100")
      )
    } else {
      for {
        users <- repository.findAllPaginated(params.offset, params.limit).transact(transactor)
        total <- repository.count().transact(transactor)
      } yield PaginatedResponse(users, params.page, params.pageSize, total)
    }
  }
  */
  
  // ============================================================
  // STEP 4: HTTP Layer
  // ============================================================
  
  // Update UserRoutes.scala to add pagination support:
  //
  // Import query parameter matchers:
  // import org.http4s.dsl.impl.QueryParamDecoderMatcher
  //
  // object PageQueryParamMatcher extends QueryParamDecoderMatcher[Int]("page")
  // object PageSizeQueryParamMatcher extends QueryParamDecoderMatcher[Int]("pageSize")
  //
  // Then add this route:
  /*
  case GET -> Root / "users" :? PageQueryParamMatcher(page) +& PageSizeQueryParamMatcher(pageSize) =>
    val params = PaginationParams(page, pageSize)
    service.listPaginated(params).foldF(
      error => BadRequest(ErrorResponse(error.getClass.getSimpleName, error.message).asJson),
      response => Ok(response.asJson)
    )
  */
  
  // ============================================================
  // USAGE EXAMPLE
  // ============================================================
  
  // GET /users?page=1&pageSize=10
  // Response:
  // {
  //   "data": [
  //     { "id": 1, "name": "Alice", ... },
  //     { "id": 2, "name": "Bob", ... },
  //     ...
  //   ],
  //   "page": 1,
  //   "pageSize": 10,
  //   "total": 42
  // }
  
  // GET /users?page=2&pageSize=10
  // Response:
  // {
  //   "data": [
  //     { "id": 11, "name": "Kevin", ... },
  //     ...
  //   ],
  //   "page": 2,
  //   "pageSize": 10,
  //   "total": 42
  // }
  
  // ============================================================
  // KEY POINTS
  // ============================================================
  
  // 1. PaginationParams calculates offset from page number
  //    - Page 1: offset 0
  //    - Page 2: offset 10 (if pageSize=10)
  //    - Page 3: offset 20
  //
  // 2. SQL LIMIT and OFFSET
  //    - LIMIT: how many rows to return
  //    - OFFSET: how many rows to skip
  //
  // 3. Total count for UI pagination
  //    - Total pages = ceil(total / pageSize)
  //    - Has next page = (page * pageSize) < total
  //
  // 4. Validation
  //    - page >= 1
  //    - pageSize between 1 and 100 (prevent abuse)
}
