package exercises.solutions

import apiserver.domain._
import java.time.LocalDateTime

/**
 * SOLUTION: Exercise 03 - Soft Delete
 * 
 * This file contains the complete implementation guide for soft delete.
 * Don't peek until you've tried it yourself!
 * 
 * NOTE: This file contains documentation and pseudocode.
 * Copy the actual code snippets to the appropriate files in your project.
 */
object Exercise03_Solution {
  
  // ============================================================
  // STEP 1: Update Domain Model (domain/Models.scala)
  // ============================================================
  
  case class UserWithSoftDelete(
    id: Long,
    name: String,
    email: String,
    age: Int,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime,
    deletedAt: Option[LocalDateTime] = None  // NEW field
  ) {
    def isDeleted: Boolean = deletedAt.isDefined
    def isActive: Boolean = deletedAt.isEmpty
  }
  
  // ============================================================
  // STEP 2: Database Migration
  // ============================================================
  
  // Create: src/main/resources/db/migration/V3__add_soft_delete.sql
  //
  // -- Add deleted_at column to users table
  // ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP DEFAULT NULL;
  //
  // -- Optional: Add index for better query performance
  // CREATE INDEX idx_users_deleted_at ON users(deleted_at);
  //
  // -- Optional: Add index on active users only (PostgreSQL partial index)
  // CREATE INDEX idx_users_active ON users(id) WHERE deleted_at IS NULL;
  
  // ============================================================
  // STEP 3: Repository Layer
  // ============================================================
  
  // Add these methods to UserRepository trait:
  //
  // def softDelete(id: Long): EitherT[F, DomainError, Unit]
  // def restore(id: Long): EitherT[F, DomainError, User]
  //
  // Update findAll() to filter deleted users:
  /*
  override def findAll(): EitherT[ConnectionIO, DomainError, List[User]] = {
    val query = sql"""
      SELECT id, name, email, age, created_at, updated_at, deleted_at
      FROM users
      WHERE deleted_at IS NULL
      ORDER BY id
    """.query[User]  // NOTE: User model must have deletedAt field first!
    
    EitherT.right(query.to[List])
  }
  */
  
  // Implement soft delete:
  /*
  override def softDelete(id: Long): EitherT[ConnectionIO, DomainError, Unit] = {
    val now = LocalDateTime.now()
    val query = sql"""
      UPDATE users
      SET deleted_at = $now
      WHERE id = $id AND deleted_at IS NULL
    """.update
    
    EitherT(query.run.map { affected =>
      if (affected > 0) Right(())
      else Left(DomainError.UserNotFound(id))
    })
  }
  */
  
  // Implement restore:
  /*
  override def restore(id: Long): EitherT[ConnectionIO, DomainError, User] = {
    val updateQuery = sql"""
      UPDATE users
      SET deleted_at = NULL
      WHERE id = $id AND deleted_at IS NOT NULL
    """.update
    
    EitherT(updateQuery.run.flatMap { affected =>
      if (affected > 0) {
        sql"""
          SELECT id, name, email, age, created_at, updated_at, deleted_at
          FROM users
          WHERE id = $id
        """.query[User].unique.map(Right(_))
      } else {
        doobie.free.connection.pure(Left(DomainError.UserNotFound(id)))
      }
    })
  }
  */
  
  // ============================================================
  // STEP 4: Service Layer
  // ============================================================
  
  // The delete method now calls softDelete:
  /*
  override def delete(id: Long): EitherT[IO, DomainError, Unit] = {
    repository.softDelete(id).transact(transactor)
  }
  */
  
  // Add restore method:
  /*
  def restore(id: Long): EitherT[IO, DomainError, User] = {
    repository.restore(id).transact(transactor)
  }
  */
  
  // ============================================================
  // STEP 5: HTTP Layer
  // ============================================================
  
  // The existing DELETE endpoint now does soft delete automatically.
  // No changes needed!
  //
  // Add restore endpoint:
  /*
  case POST -> Root / "users" / LongVar(id) / "restore" =>
    service.restore(id).foldF(
      error => NotFound(ErrorResponse(error.getClass.getSimpleName, error.message).asJson),
      user => Ok(user.asJson)
    )
  */
  
  // ============================================================
  // USAGE EXAMPLES
  // ============================================================
  
  // Example 1: Soft delete a user
  // DELETE /users/1
  // Response: { "message": "User 1 deleted (soft delete)" }
  // Database: deleted_at is set to current timestamp
  
  // Example 2: Verify user is hidden
  // GET /users/1
  // Response: 404 Not Found (user is filtered out)
  
  // Example 3: List users (deleted are hidden)
  // GET /users
  // Response: [user2, user3, ...] (user1 not in list)
  
  // Example 4: Restore deleted user
  // POST /users/1/restore
  // Response: { "id": 1, "name": "Alice", ..., "deletedAt": null }
  
  // Example 5: Admin view all users
  // GET /admin/users
  // Response: [
  //   { "id": 1, "name": "Alice", ..., "deletedAt": "2024-01-15T10:30:00" },
  //   { "id": 2, "name": "Bob", ..., "deletedAt": null },
  //   ...
  // ]
  
  // ============================================================
  // KEY POINTS
  // ============================================================
  
  // 1. Soft delete vs Hard delete
  //    - Soft: Set deleted_at timestamp (data remains)
  //    - Hard: DELETE FROM users WHERE id = ? (data lost forever)
  //
  // 2. Filter deleted users in queries
  //    - WHERE deleted_at IS NULL (only active users)
  //    - Must remember to add this to ALL queries!
  //
  // 3. Optional fields in Scala
  //    - Option[LocalDateTime] represents nullable column
  //    - None = active, Some(timestamp) = deleted
  //
  // 4. Migration is non-destructive
  //    - ALTER TABLE ADD COLUMN is safe (doesn't delete data)
  //    - DEFAULT NULL means existing rows get NULL automatically
  
  // ============================================================
  // BENEFITS OF SOFT DELETE
  // ============================================================
  
  // ✅ Data recovery
  //    - Undo accidental deletions
  //    - Restore user accounts on request
  //
  // ✅ Audit trail
  //    - Know what was deleted and when
  //    - Track deletion patterns
  //
  // ✅ Referential integrity
  //    - Foreign keys don't break
  //    - Related data remains valid
  //
  // ✅ Analytics
  //    - Analyze churned users
  //    - Understand why users leave
  
  // ============================================================
  // DRAWBACKS
  // ============================================================
  
  // ❌ Database size
  //    - Deleted data still takes space
  //    - May need cleanup job for old deleted records
  //
  // ❌ Query complexity
  //    - Must remember WHERE deleted_at IS NULL everywhere
  //    - Easy to forget and leak deleted data
  //
  // ❌ Unique constraints
  //    - Email uniqueness with soft delete is tricky
  //    - Solution: Use partial unique index
  //    - CREATE UNIQUE INDEX ON users(email) WHERE deleted_at IS NULL;
  
  // ============================================================
  // RUST COMPARISON
  // ============================================================
  
  // In Rust with sqlx:
  //
  // struct User {
  //     id: i64,
  //     name: String,
  //     email: String,
  //     age: i32,
  //     created_at: DateTime<Utc>,
  //     updated_at: DateTime<Utc>,
  //     deleted_at: Option<DateTime<Utc>>,
  // }
  //
  // impl User {
  //     fn is_deleted(&self) -> bool {
  //         self.deleted_at.is_some()
  //     }
  // }
  //
  // async fn soft_delete(pool: &PgPool, id: i64) -> Result<(), sqlx::Error> {
  //     sqlx::query!(
  //         "UPDATE users SET deleted_at = NOW() WHERE id = $1 AND deleted_at IS NULL",
  //         id
  //     )
  //     .execute(pool)
  //     .await?;
  //     Ok(())
  // }
}
