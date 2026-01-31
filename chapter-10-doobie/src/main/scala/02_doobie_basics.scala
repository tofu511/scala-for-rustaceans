package doobiebasics

import cats.effect._
import cats.syntax.all._
import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._
import doobie.hikari.HikariTransactor
import java.time.LocalDateTime

/*
 * Chapter 10: Doobie Basics - Connecting and Querying
 *
 * RUST COMPARISON:
 * Doobie is similar to sqlx in Rust:
 * - Type-safe SQL queries
 * - Composable query fragments
 * - Connection pooling (HikariCP like sqlx's Pool)
 * - Async execution with IO (like sqlx with tokio)
 *
 * Key Differences:
 * Rust (sqlx):
 *   let pool = PgPoolOptions::new().connect(&url).await?;
 *   let user: User = sqlx::query_as("SELECT * FROM users WHERE id = $1")
 *     .bind(id)
 *     .fetch_one(&pool)
 *     .await?;
 *
 * Scala (Doobie):
 *   val xa: Transactor[IO] = Transactor.fromDriverManager(...)
 *   val user: IO[User] = sql"SELECT * FROM users WHERE id = $id"
 *     .query[User]
 *     .unique
 *     .transact(xa)
 */

object DoobieBasicsExample extends IOApp.Simple {
  
  /*
   * DOMAIN MODELS
   * 
   * Case classes that map to database tables.
   * Similar to Rust structs with sqlx::FromRow.
   */
  
  case class User(
    id: Long,
    name: String,
    email: String,
    age: Int,
    createdAt: LocalDateTime
  )
  
  case class Book(
    id: Long,
    title: String,
    author: String,
    isbn: String,
    publishedYear: Int,
    createdAt: LocalDateTime
  )
  
  /*
   * DATABASE CONFIGURATION
   */
  
  val dbUrl = "jdbc:postgresql://localhost:5432/testdb"
  val dbUser = "postgres"
  val dbPassword = "password"
  
  /*
   * CREATING A TRANSACTOR
   * 
   * A Transactor manages database connections and transactions.
   * Similar to sqlx::Pool in Rust.
   *
   * There are two main ways to create a transactor:
   * 1. DriverManagerTransactor - Simple, for learning/testing
   * 2. HikariTransactor - Production-ready with connection pooling
   *
   * Rust (sqlx) equivalent:
   *   let pool = PgPoolOptions::new()
   *     .max_connections(5)
   *     .connect(&database_url)
   *     .await?;
   */
  
  // Simple transactor (no pooling)
  def createSimpleTransactor(): Resource[IO, Transactor[IO]] = {
    for {
      xa <- ExecutionContexts.fixedThreadPool[IO](4).map { ec =>
        Transactor.fromDriverManager[IO](
          driver = "org.postgresql.Driver",
          url = dbUrl,
          user = dbUser,
          password = dbPassword,
          logHandler = None  // Set to Some(LogHandler.jdkLogHandler) for SQL logging
        )
      }
    } yield xa
  }
  
  // Production transactor with HikariCP connection pooling
  def createPooledTransactor(): Resource[IO, HikariTransactor[IO]] = {
    for {
      ec <- ExecutionContexts.fixedThreadPool[IO](10)  // Thread pool for JDBC
      xa <- HikariTransactor.newHikariTransactor[IO](
        driverClassName = "org.postgresql.Driver",
        url = dbUrl,
        user = dbUser,
        pass = dbPassword,
        connectEC = ec
      )
    } yield xa
  }
  
  /*
   * BASIC QUERIES
   * 
   * Doobie uses string interpolation with sql"..." for queries.
   * Variables are automatically parameterized (prevents SQL injection).
   *
   * Rust (sqlx) equivalent:
   *   sqlx::query_as::<_, User>("SELECT * FROM users WHERE id = $1")
   *     .bind(id)
   *     .fetch_one(&pool)
   *     .await?
   */
  
  // Select one user by ID
  def findUser(id: Long): ConnectionIO[Option[User]] = {
    sql"SELECT id, name, email, age, created_at FROM users WHERE id = $id"
      .query[User]
      .option  // Returns Option[User]
  }
  
  // Select all users
  def listUsers(): ConnectionIO[List[User]] = {
    sql"SELECT id, name, email, age, created_at FROM users"
      .query[User]
      .to[List]  // Accumulate results into List
  }
  
  // Select users with filter
  def findUsersByAge(minAge: Int): ConnectionIO[List[User]] = {
    sql"SELECT id, name, email, age, created_at FROM users WHERE age >= $minAge"
      .query[User]
      .to[List]
  }
  
  /*
   * INSERT QUERIES
   * 
   * Inserting data and optionally returning generated keys.
   *
   * Rust (sqlx) equivalent:
   *   let id: i64 = sqlx::query_scalar(
   *     "INSERT INTO users (name, email, age) VALUES ($1, $2, $3) RETURNING id"
   *   )
   *   .bind(name)
   *   .bind(email)
   *   .bind(age)
   *   .fetch_one(&pool)
   *   .await?;
   */
  
  // Insert user and return generated ID
  def insertUser(name: String, email: String, age: Int): ConnectionIO[Long] = {
    sql"INSERT INTO users (name, email, age) VALUES ($name, $email, $age)"
      .update
      .withUniqueGeneratedKeys[Long]("id")  // Return the generated ID
  }
  
  // Insert user and return the complete user
  def insertUserReturning(name: String, email: String, age: Int): ConnectionIO[User] = {
    sql"""INSERT INTO users (name, email, age) 
          VALUES ($name, $email, $age)
          RETURNING id, name, email, age, created_at"""
      .query[User]
      .unique
  }
  
  /*
   * UPDATE QUERIES
   * 
   * Updating existing records.
   *
   * Rust (sqlx) equivalent:
   *   let rows_affected = sqlx::query("UPDATE users SET name = $1 WHERE id = $2")
   *     .bind(new_name)
   *     .bind(id)
   *     .execute(&pool)
   *     .await?
   *     .rows_affected();
   */
  
  def updateUserName(id: Long, newName: String): ConnectionIO[Int] = {
    sql"UPDATE users SET name = $newName WHERE id = $id"
      .update
      .run  // Returns number of rows affected
  }
  
  def updateUser(id: Long, name: String, email: String, age: Int): ConnectionIO[Int] = {
    sql"""UPDATE users 
          SET name = $name, email = $email, age = $age 
          WHERE id = $id"""
      .update
      .run
  }
  
  /*
   * DELETE QUERIES
   * 
   * Deleting records.
   *
   * Rust (sqlx) equivalent:
   *   let rows = sqlx::query("DELETE FROM users WHERE id = $1")
   *     .bind(id)
   *     .execute(&pool)
   *     .await?
   *     .rows_affected();
   */
  
  def deleteUser(id: Long): ConnectionIO[Int] = {
    sql"DELETE FROM users WHERE id = $id"
      .update
      .run
  }
  
  /*
   * RUNNING QUERIES
   * 
   * ConnectionIO[A] is a description of a database operation.
   * To execute it, use .transact(xa) to get IO[A].
   *
   * Rust equivalent:
   *   // sqlx queries return Future directly
   *   let user = query.fetch_one(&pool).await?;
   */
  
  def exampleUsage(xa: Transactor[IO]): IO[Unit] = {
    for {
      _ <- IO.println("=== Doobie Basics Example ===\n")
      
      // Insert users
      _ <- IO.println("1. Inserting users...")
      id1 <- insertUser("Alice", "alice@example.com", 30).transact(xa)
      _ <- IO.println(s"   Inserted Alice with ID: $id1")
      
      id2 <- insertUser("Bob", "bob@example.com", 25).transact(xa)
      _ <- IO.println(s"   Inserted Bob with ID: $id2")
      
      user3 <- insertUserReturning("Charlie", "charlie@example.com", 35).transact(xa)
      _ <- IO.println(s"   Inserted Charlie: $user3")
      
      // Find user
      _ <- IO.println("\n2. Finding user by ID...")
      maybeUser <- findUser(id1).transact(xa)
      _ <- IO.println(s"   Found: $maybeUser")
      
      // List all users
      _ <- IO.println("\n3. Listing all users...")
      users <- listUsers().transact(xa)
      _ <- users.traverse(u => IO.println(s"   - ${u.name} (${u.email}), age ${u.age}"))
      
      // Filter users
      _ <- IO.println("\n4. Finding users age >= 30...")
      olderUsers <- findUsersByAge(30).transact(xa)
      _ <- olderUsers.traverse(u => IO.println(s"   - ${u.name}, age ${u.age}"))
      
      // Update user
      _ <- IO.println("\n5. Updating user...")
      rowsUpdated <- updateUserName(id1, "Alice Smith").transact(xa)
      _ <- IO.println(s"   Updated $rowsUpdated row(s)")
      
      updatedUser <- findUser(id1).transact(xa)
      _ <- IO.println(s"   New name: ${updatedUser.map(_.name)}")
      
      // Delete user
      _ <- IO.println("\n6. Deleting user...")
      rowsDeleted <- deleteUser(id2).transact(xa)
      _ <- IO.println(s"   Deleted $rowsDeleted row(s)")
      
      remainingUsers <- listUsers().transact(xa)
      _ <- IO.println(s"   Remaining users: ${remainingUsers.size}")
      
      _ <- IO.println("\n✓ Example complete!")
    } yield ()
  }
  
  def run: IO[Unit] = {
    createSimpleTransactor().use { xa =>
      exampleUsage(xa)
    }.handleErrorWith { error =>
      IO.println(s"Error: ${error.getMessage}") *>
      IO.println("\nMake sure:") *>
      IO.println("1. PostgreSQL is running") *>
      IO.println("2. Migrations have been applied (run 01_flyway.scala first)")
    }
  }
}

/*
 * EXAMPLE OUTPUT:
 * 
 * === Doobie Basics Example ===
 * 
 * 1. Inserting users...
 *    Inserted Alice with ID: 1
 *    Inserted Bob with ID: 2
 *    Inserted Charlie: User(3,Charlie,charlie@example.com,35,2024-01-31T...)
 * 
 * 2. Finding user by ID...
 *    Found: Some(User(1,Alice,alice@example.com,30,2024-01-31T...))
 * 
 * 3. Listing all users...
 *    - Alice (alice@example.com), age 30
 *    - Bob (bob@example.com), age 25
 *    - Charlie (charlie@example.com), age 35
 * 
 * 4. Finding users age >= 30...
 *    - Alice, age 30
 *    - Charlie, age 35
 * 
 * 5. Updating user...
 *    Updated 1 row(s)
 *    New name: Some(Alice Smith)
 * 
 * 6. Deleting user...
 *    Deleted 1 row(s)
 *    Remaining users: 2
 * 
 * ✓ Example complete!
 */

/*
 * KEY CONCEPTS FOR RUST DEVELOPERS:
 * 
 * 1. **Transactor vs Pool**:
 *    - Transactor[IO] ~ Arc<sqlx::Pool>
 *    - Manages connections and transactions
 *    - Use Resource for proper cleanup
 * 
 * 2. **ConnectionIO[A]**:
 *    - Description of a database operation
 *    - Composable (like Rust's Future)
 *    - Execute with .transact(xa)
 * 
 * 3. **SQL Interpolation**:
 *    - sql"SELECT * FROM users WHERE id = $id"
 *    - Automatically parameterized (safe)
 *    - Like sqlx::query! but runtime-checked
 * 
 * 4. **Query Methods**:
 *    - .query[T] - Map result to type T
 *    - .unique - Expect exactly one result
 *    - .option - Return Option (0 or 1 result)
 *    - .to[List] - Collect all results
 * 
 * 5. **Update Methods**:
 *    - .update.run - Return rows affected
 *    - .withUniqueGeneratedKeys[T](cols) - Return generated keys
 * 
 * 6. **Composition**:
 *    - Combine multiple ConnectionIO operations
 *    - All run in same transaction when transact(xa)
 *    - Similar to sqlx transaction blocks
 * 
 * TIPS:
 * 
 * 1. **Type Safety**: Doobie checks types at runtime
 *    - Use .query[User] to map to case class
 *    - Field names must match (order doesn't matter with 1.0.0-RC4+)
 * 
 * 2. **Error Handling**: ConnectionIO can fail
 *    - Use .attempt or .handleErrorWith
 *    - Similar to Result in Rust
 * 
 * 3. **Connection Pooling**: Use HikariTransactor in production
 *    - Better performance
 *    - Automatic connection management
 * 
 * 4. **NULL Handling**: Use Option[T] for nullable columns
 *    - Option[String] for VARCHAR NULL
 *    - Similar to Option in Rust
 * 
 * NEXT: 03_query_composition.scala covers composing complex queries
 */
