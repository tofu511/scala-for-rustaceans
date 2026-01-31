package exercises

import apiserver.domain._
import cats.effect._
import cats.data.EitherT
import java.time.LocalDateTime

/**
 * EXERCISE 03: Soft Delete
 * 
 * OBJECTIVES:
 * - Add deletedAt field to User model
 * - Implement soft delete (mark as deleted, don't remove)
 * - Filter out deleted users from normal queries
 * - Allow recovery of soft-deleted users
 * 
 * RUST COMPARISON:
 * Similar to soft delete pattern in Rust:
 * 
 * struct User {
 *     id: i64,
 *     name: String,
 *     email: String,
 *     age: i32,
 *     created_at: DateTime<Utc>,
 *     updated_at: DateTime<Utc>,
 *     deleted_at: Option<DateTime<Utc>>,  // None = active, Some = deleted
 * }
 * 
 * impl User {
 *     fn is_deleted(&self) -> bool {
 *         self.deleted_at.is_some()
 *     }
 * }
 * 
 * TASKS:
 * 1. Update User model to add deletedAt field
 * 2. Create database migration to add deleted_at column
 * 3. Update all queries to filter out deleted users
 * 4. Change DELETE endpoint to soft delete
 * 5. (Optional) Add restore endpoint
 * 
 * TDD WORKFLOW:
 * 1. Run tests: sbt "testOnly exercises.Exercise03_SoftDeleteSpec"
 * 2. See RED (compilation errors and test failures)
 * 3. Implement the changes below
 * 4. Run tests again - see GREEN
 */
object Exercise03_SoftDelete {
  
  // STEP 1: Update User Model
  // In src/main/scala/domain/Models.scala, change User to:
  //
  // case class User(
  //   id: Long,
  //   name: String,
  //   email: String,
  //   age: Int,
  //   createdAt: LocalDateTime,
  //   updatedAt: LocalDateTime,
  //   deletedAt: Option[LocalDateTime] = None  // Add this field
  // ) {
  //   def isDeleted: Boolean = deletedAt.isDefined
  // }
  
  // STEP 2: Create Database Migration
  // Create file: src/main/resources/db/migration/V3__add_soft_delete.sql
  //
  // ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP DEFAULT NULL;
  //
  // This adds the column without affecting existing data
  
  // STEP 3: Update Repository Queries
  // In UserRepositoryImpl, add WHERE clause to filter deleted users:
  //
  // OLD:
  // sql"SELECT id, name, email, age, created_at, updated_at FROM users"
  //
  // NEW:
  // sql"""
  //   SELECT id, name, email, age, created_at, updated_at, deleted_at
  //   FROM users
  //   WHERE deleted_at IS NULL
  // """
  //
  // Update ALL queries: findAll, findById, findByEmail, search, etc.
  
  // STEP 4: Implement Soft Delete Method
  // Add to UserRepository trait:
  //
  // def softDelete(id: Long): EitherT[F, DomainError, Unit]
  //
  // Implement in UserRepositoryImpl:
  //
  // override def softDelete(id: Long): EitherT[ConnectionIO, DomainError, Unit] = {
  //   val now = LocalDateTime.now()
  //   val query = sql"""
  //     UPDATE users
  //     SET deleted_at = $now
  //     WHERE id = $id AND deleted_at IS NULL
  //   """.update
  //   
  //   EitherT(query.run.map { affected =>
  //     if (affected > 0) Right(())
  //     else Left(DomainError.UserNotFound(id))
  //   })
  // }
  
  // STEP 5: Update Service Layer
  // Change UserService.deleteUser to use soft delete:
  //
  // override def deleteUser(id: Long): EitherT[IO, DomainError, Unit] = {
  //   repository.softDelete(id).transact(transactor)
  // }
  
  // STEP 6: (Optional) Add Restore Method
  // Add to UserRepository trait:
  //
  // def restore(id: Long): EitherT[F, DomainError, User]
  //
  // Implement in UserRepositoryImpl:
  //
  // override def restore(id: Long): EitherT[ConnectionIO, DomainError, User] = {
  //   val query = sql"""
  //     UPDATE users
  //     SET deleted_at = NULL
  //     WHERE id = $id AND deleted_at IS NOT NULL
  //   """.update
  //   
  //   EitherT(query.run.flatMap { affected =>
  //     if (affected > 0) {
  //       // Fetch the restored user
  //       sql"""
  //         SELECT id, name, email, age, created_at, updated_at, deleted_at
  //         FROM users
  //         WHERE id = $id
  //       """.query[User].unique.map(Right(_))
  //     } else {
  //       doobie.free.connection.pure(Left(DomainError.UserNotFound(id)))
  //     }
  //   })
  // }
  //
  // Add HTTP endpoint:
  // POST /users/:id/restore
  
  // TESTING:
  // After implementation, run:
  // sbt "testOnly exercises.Exercise03_SoftDeleteSpec"
  //
  // All 5 tests should pass!
  
  // EXPECTED BEHAVIOR:
  // DELETE /users/1
  // => Sets deleted_at to current time (user still in database)
  //
  // GET /users/1
  // => Returns 404 Not Found (deleted users are filtered)
  //
  // GET /users
  // => Returns only active users (deleted_at IS NULL)
  //
  // POST /users/1/restore (optional)
  // => Sets deleted_at back to NULL (user is active again)
  
  // WHY SOFT DELETE?
  // ✅ Data recovery - Can restore accidentally deleted users
  // ✅ Audit trail - Keep history of what was deleted and when
  // ✅ Referential integrity - Foreign keys don't break
  // ✅ Analytics - Can analyze deleted users
  //
  // ❌ Downsides:
  // - Database grows larger
  // - Must remember to filter in all queries
  // - Unique constraints need special handling
  
  // COMMON MISTAKES:
  // ❌ Forgetting WHERE deleted_at IS NULL in queries
  // ❌ Not updating all queries (findAll, search, etc.)
  // ❌ Forgetting to add deleted_at to SELECT clause
  // ✅ Use views or query fragments to avoid repetition
}
