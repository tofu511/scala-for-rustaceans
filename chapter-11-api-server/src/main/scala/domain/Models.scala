package apiserver.domain

import java.time.LocalDateTime

/*
 * DOMAIN LAYER
 * 
 * Domain models and errors. This layer has no dependencies on infrastructure.
 *
 * RUST COMPARISON:
 * Similar to domain models in Rust:
 * - case classes ~ structs
 * - sealed traits ~ enums
 * - No external dependencies (pure domain logic)
 */

// Domain Models
case class User(
  id: Long,
  name: String,
  email: String,
  age: Int,
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime
)

case class CreateUserRequest(
  name: String,
  email: String,
  age: Int
)

case class UpdateUserRequest(
  name: String,
  email: String,
  age: Int
)

// Domain Errors
sealed trait DomainError {
  def message: String
}

object DomainError {
  case class UserNotFound(id: Long) extends DomainError {
    def message: String = s"User with id $id not found"
  }
  
  case class ValidationError(field: String, issue: String) extends DomainError {
    def message: String = s"Validation failed for $field: $issue"
  }
  
  case class DuplicateEmail(email: String) extends DomainError {
    def message: String = s"User with email $email already exists"
  }
  
  case class DatabaseError(cause: String) extends DomainError {
    def message: String = s"Database error: $cause"
  }
}

// HTTP Response Models
case class ErrorResponse(error: String, message: String, details: Option[Map[String, String]] = None)
case class SuccessResponse(message: String)
case class HealthCheckResponse(status: String, timestamp: Long)
case class DeleteResponse(message: String)
