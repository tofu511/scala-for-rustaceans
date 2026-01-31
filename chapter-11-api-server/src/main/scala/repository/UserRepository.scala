package apiserver.repository

import cats.data.EitherT
import cats.effect._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import apiserver.domain._
import java.time.LocalDateTime

/*
 * REPOSITORY LAYER
 * 
 * Database operations using Doobie. Returns EitherT[ConnectionIO, DomainError, A].
 *
 * RUST COMPARISON:
 * Similar to repository pattern in Rust:
 * - Encapsulates database operations
 * - Returns Result<T, DomainError>
 * - Uses sqlx or diesel
 *
 * Example Rust equivalent:
 *   impl UserRepository {
 *     async fn find_by_id(&self, id: i64) -> Result<User, DomainError> {
 *       sqlx::query_as("SELECT * FROM users WHERE id = $1")
 *         .bind(id)
 *         .fetch_one(&self.pool)
 *         .await
 *         .map_err(|_| DomainError::UserNotFound(id))
 *     }
 *   }
 */

trait UserRepository[F[_]] {
  def findById(id: Long): EitherT[F, DomainError, User]
  def findAll(): EitherT[F, DomainError, List[User]]
  def findByEmail(email: String): F[Option[User]]
  def create(req: CreateUserRequest): EitherT[F, DomainError, User]
  def update(id: Long, req: UpdateUserRequest): EitherT[F, DomainError, User]
  def delete(id: Long): EitherT[F, DomainError, Unit]
}

class DoobieUserRepository extends UserRepository[ConnectionIO] {
  
  def findById(id: Long): EitherT[ConnectionIO, DomainError, User] = {
    EitherT(
      sql"""SELECT id, name, email, age, created_at, updated_at 
            FROM users 
            WHERE id = $id"""
        .query[User]
        .option
        .attemptSql
        .map {
          case Right(Some(user)) => Right(user)
          case Right(None) => Left(DomainError.UserNotFound(id))
          case Left(error) => Left(DomainError.DatabaseError(error.getMessage))
        }
    )
  }
  
  def findAll(): EitherT[ConnectionIO, DomainError, List[User]] = {
    EitherT(
      sql"""SELECT id, name, email, age, created_at, updated_at 
            FROM users 
            ORDER BY id"""
        .query[User]
        .to[List]
        .attemptSql
        .map {
          case Right(users) => Right(users)
          case Left(error) => Left(DomainError.DatabaseError(error.getMessage))
        }
    )
  }
  
  def findByEmail(email: String): ConnectionIO[Option[User]] = {
    sql"""SELECT id, name, email, age, created_at, updated_at 
          FROM users 
          WHERE email = $email"""
      .query[User]
      .option
  }
  
  def create(req: CreateUserRequest): EitherT[ConnectionIO, DomainError, User] = {
    EitherT(
      sql"""INSERT INTO users (name, email, age) 
            VALUES (${req.name}, ${req.email}, ${req.age})
            RETURNING id, name, email, age, created_at, updated_at"""
        .query[User]
        .unique
        .attemptSql
        .map {
          case Right(user) => Right(user)
          case Left(error) =>
            if (error.getMessage.contains("unique") || error.getMessage.contains("duplicate"))
              Left(DomainError.DuplicateEmail(req.email))
            else
              Left(DomainError.DatabaseError(error.getMessage))
        }
    )
  }
  
  def update(id: Long, req: UpdateUserRequest): EitherT[ConnectionIO, DomainError, User] = {
    for {
      // Check if user exists
      _ <- findById(id)
      // Update
      updated <- EitherT(
        sql"""UPDATE users 
              SET name = ${req.name}, 
                  email = ${req.email}, 
                  age = ${req.age},
                  updated_at = NOW()
              WHERE id = $id
              RETURNING id, name, email, age, created_at, updated_at"""
          .query[User]
          .unique
          .attemptSql
          .map {
            case Right(user) => Right(user): Either[DomainError, User]
            case Left(error) => Left(DomainError.DatabaseError(error.getMessage)): Either[DomainError, User]
          }
      )
    } yield updated
  }
  
  def delete(id: Long): EitherT[ConnectionIO, DomainError, Unit] = {
    for {
      _ <- findById(id)
      _ <- EitherT.right[DomainError](
        sql"DELETE FROM users WHERE id = $id".update.run.map(_ => ())
      )
    } yield ()
  }
}
