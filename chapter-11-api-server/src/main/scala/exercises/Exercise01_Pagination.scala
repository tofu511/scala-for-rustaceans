package exercises

import apiserver.domain._
import cats.effect._
import cats.data.EitherT

/**
 * EXERCISE 01: Pagination Support
 * 
 * OBJECTIVES:
 * - Add pagination parameters to list users endpoint
 * - Calculate correct offset/limit from page numbers
 * - Return paginated response with metadata
 * 
 * RUST COMPARISON:
 * This is similar to pagination in Rust web frameworks:
 * 
 * struct PaginationParams {
 *     page: u32,
 *     page_size: u32,
 * }
 * 
 * impl PaginationParams {
 *     fn offset(&self) -> u32 {
 *         (self.page - 1) * self.page_size
 *     }
 * }
 * 
 * TASKS:
 * 1. Add PaginationParams to domain/Models.scala
 * 2. Add PaginatedResponse[A] to domain/Models.scala
 * 3. Add findAllPaginated to UserRepository
 * 4. Add listUsersPaginated to UserService
 * 5. Update HTTP routes to accept page and pageSize query params
 * 
 * TDD WORKFLOW:
 * 1. Run tests: sbt "testOnly exercises.Exercise01_PaginationSpec"
 * 2. See RED (failures)
 * 3. Implement the code below
 * 4. Run tests again
 * 5. See GREEN (passing)
 */
object Exercise01_Pagination {
  
  // TODO: Add these case classes to src/main/scala/domain/Models.scala
  // 
  // case class PaginationParams(page: Int, pageSize: Int) {
  //   def offset: Int = ???  // Calculate offset: (page - 1) * pageSize
  //   def limit: Int = ???   // Return pageSize
  // }
  // 
  // case class PaginatedResponse[A](
  //   data: List[A],
  //   page: Int,
  //   pageSize: Int,
  //   total: Long
  // )
  
  // HINT: Repository Layer
  // Add this method to UserRepository trait in repository/UserRepository.scala:
  //
  // def findAllPaginated(offset: Int, limit: Int): EitherT[F, DomainError, List[User]]
  // def count(): EitherT[F, DomainError, Long]
  //
  // Then implement in UserRepositoryImpl:
  //
  // override def findAllPaginated(offset: Int, limit: Int): EitherT[ConnectionIO, DomainError, List[User]] = {
  //   val query = sql"""
  //     SELECT id, name, email, age, created_at, updated_at
  //     FROM users
  //     ORDER BY id
  //     LIMIT $limit OFFSET $offset
  //   """.query[User]
  //   
  //   EitherT.right(query.to[List])
  // }
  //
  // override def count(): EitherT[ConnectionIO, DomainError, Long] = {
  //   val query = sql"SELECT COUNT(*) FROM users".query[Long]
  //   EitherT.right(query.unique)
  // }
  
  // HINT: Service Layer
  // Add this method to UserService trait in service/UserService.scala:
  //
  // def listPaginated(params: PaginationParams): EitherT[IO, DomainError, PaginatedResponse[User]]
  //
  // Then implement in UserServiceImpl:
  //
  // override def listPaginated(params: PaginationParams): EitherT[IO, DomainError, PaginatedResponse[User]] = {
  //   for {
  //     users <- repository.findAllPaginated(params.offset, params.limit).transact(transactor)
  //     total <- repository.count().transact(transactor)
  //   } yield PaginatedResponse(users, params.page, params.pageSize, total)
  // }
  
  // HINT: HTTP Layer
  // Update UserRoutes.scala to add query parameter handling:
  //
  // import org.http4s.dsl.impl.{QueryParamDecoderMatcher, OptionalValidatingQueryParamDecoderMatcher}
  //
  // object PageQueryParamMatcher extends QueryParamDecoderMatcher[Int]("page")
  // object PageSizeQueryParamMatcher extends QueryParamDecoderMatcher[Int]("pageSize")
  //
  // Then in your routes:
  //
  // case GET -> Root / "users" :? PageQueryParamMatcher(page) +& PageSizeQueryParamMatcher(pageSize) =>
  //   val params = PaginationParams(page, pageSize)
  //   service.listPaginated(params).foldF(
  //     error => BadRequest(ErrorResponse(error.getClass.getSimpleName, error.message).asJson),
  //     response => Ok(response.asJson)
  //   )
  
  // TESTING:
  // After implementation, run:
  // sbt "testOnly exercises.Exercise01_PaginationSpec"
  //
  // All 5 tests should pass!
  
  // EXPECTED BEHAVIOR:
  // GET /users?page=1&pageSize=10
  // => Returns first 10 users with pagination metadata
  //
  // GET /users?page=2&pageSize=10
  // => Returns next 10 users (offset 10)
}
