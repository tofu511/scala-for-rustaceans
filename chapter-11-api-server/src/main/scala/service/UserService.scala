package apiserver.service

import cats.data.EitherT
import cats.effect._
import doobie._
import apiserver.domain._
import apiserver.repository.UserRepository

/*
 * SERVICE LAYER
 * 
 * Business logic and validation. Orchestrates repository operations.
 *
 * RUST COMPARISON:
 * Similar to service layer in Rust:
 * - Business logic separate from database
 * - Input validation
 * - Orchestrates multiple repository calls
 * - Returns Result<T, DomainError>
 *
 * Example Rust equivalent:
 *   impl UserService {
 *     async fn create_user(&self, req: CreateUserRequest) -> Result<User, DomainError> {
 *       self.validate(&req)?;
 *       self.repository.create(req).await
 *     }
 *   }
 */

trait UserService[F[_]] {
  def findById(id: Long): EitherT[F, DomainError, User]
  def findAll(): EitherT[F, DomainError, List[User]]
  def create(req: CreateUserRequest): EitherT[F, DomainError, User]
  def update(id: Long, req: UpdateUserRequest): EitherT[F, DomainError, User]
  def delete(id: Long): EitherT[F, DomainError, Unit]
}

class UserServiceImpl(repository: UserRepository[ConnectionIO]) extends UserService[ConnectionIO] {
  
  // Email validation helper (exposed for testing)
  def isValidEmail(email: String): Boolean = {
    val emailRegex = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".r
    emailRegex.findFirstIn(email).isDefined
  }
  
  // Validation logic (exposed for testing)
  def validateCreateRequest(req: CreateUserRequest): Either[DomainError, CreateUserRequest] = {
    val errors = List(
      if (req.name.trim.isEmpty)
        Some(DomainError.ValidationError("name", "cannot be empty"))
      else None,
      
      if (!isValidEmail(req.email))
        Some(DomainError.ValidationError("email", "must be valid email address"))
      else None,
      
      if (req.age < 0 || req.age > 150)
        Some(DomainError.ValidationError("age", "must be between 0 and 150"))
      else None
    ).flatten
    
    errors.headOption match {
      case Some(error) => Left(error)
      case None => Right(req)
    }
  }
  
  def validateUpdateRequest(req: UpdateUserRequest): Either[DomainError, UpdateUserRequest] = {
    val createReq = CreateUserRequest(req.name, req.email, req.age)
    validateCreateRequest(createReq).map(_ => req)
  }
  
  // Service methods
  def findById(id: Long): EitherT[ConnectionIO, DomainError, User] = {
    repository.findById(id)
  }
  
  def findAll(): EitherT[ConnectionIO, DomainError, List[User]] = {
    repository.findAll()
  }
  
  def create(req: CreateUserRequest): EitherT[ConnectionIO, DomainError, User] = {
    for {
      validated <- EitherT.fromEither[ConnectionIO](validateCreateRequest(req))
      user <- repository.create(validated)
    } yield user
  }
  
  def update(id: Long, req: UpdateUserRequest): EitherT[ConnectionIO, DomainError, User] = {
    for {
      validated <- EitherT.fromEither[ConnectionIO](validateUpdateRequest(req))
      user <- repository.update(id, validated)
    } yield user
  }
  
  def delete(id: Long): EitherT[ConnectionIO, DomainError, Unit] = {
    repository.delete(id)
  }
}
