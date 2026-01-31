package doobiebasics

import cats.effect._
import cats.syntax.all._
import cats.data.EitherT
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import java.time.LocalDateTime

/*
 * Chapter 10: Query Composition and Transactions
 *
 * RUST COMPARISON:
 * Query composition in Doobie is similar to building queries in sqlx:
 * - Fragments for dynamic queries (like QueryBuilder)
 * - Transactions (like sqlx::Transaction)
 * - Batch operations (like sqlx::query batching)
 *
 * Rust (sqlx):
 *   let mut tx = pool.begin().await?;
 *   sqlx::query("INSERT ...").execute(&mut tx).await?;
 *   sqlx::query("UPDATE ...").execute(&mut tx).await?;
 *   tx.commit().await?;
 *
 * Scala (Doobie):
 *   (for {
 *     _ <- sql"INSERT ...".update.run
 *     _ <- sql"UPDATE ...".update.run
 *   } yield ()).transact(xa)  // Atomic transaction
 */

object QueryCompositionExample extends IOApp.Simple {
  
  case class User(
    id: Long,
    name: String,
    email: String,
    age: Int,
    createdAt: LocalDateTime
  )
  
  val dbUrl = "jdbc:postgresql://localhost:5432/testdb"
  val dbUser = "postgres"
  val dbPassword = "password"
  
  def createTransactor(): Resource[IO, Transactor[IO]] = {
    ExecutionContexts.fixedThreadPool[IO](4).map { ec =>
      Transactor.fromDriverManager[IO](
        driver = "org.postgresql.Driver",
        url = dbUrl,
        user = dbUser,
        password = dbPassword,
        logHandler = None
      )
    }
  }
  
  /*
   * QUERY FRAGMENTS
   * 
   * Fragments are reusable query pieces that can be composed.
   * Similar to building queries programmatically in sqlx.
   *
   * Rust equivalent:
   *   let mut query = "SELECT * FROM users WHERE 1=1".to_string();
   *   if let Some(min_age) = min_age {
   *     query.push_str(&format!(" AND age >= {}", min_age));
   *   }
   */
  
  def selectUsers(
    minAge: Option[Int],
    maxAge: Option[Int],
    namePattern: Option[String]
  ): ConnectionIO[List[User]] = {
    
    // Base query
    val base = fr"SELECT id, name, email, age, created_at FROM users"
    
    // Build WHERE clause conditionally
    val ageFilter = (minAge, maxAge) match {
      case (Some(min), Some(max)) => Some(fr"age >= $min AND age <= $max")
      case (Some(min), None) => Some(fr"age >= $min")
      case (None, Some(max)) => Some(fr"age <= $max")
      case (None, None) => None
    }
    
    val nameFilter = namePattern.map { pattern =>
      fr"name ILIKE ${"%" + pattern + "%"}"
    }
    
    // Combine filters
    val filters = List(ageFilter, nameFilter).flatten
    
    val whereClause = filters match {
      case Nil => Fragment.empty
      case h :: t => fr"WHERE" ++ t.foldLeft(h)((acc, f) => acc ++ fr"AND" ++ f)
    }
    
    // Complete query
    val query = base ++ whereClause
    
    query.query[User].to[List]
  }
  
  /*
   * DYNAMIC ORDERING
   */
  
  def selectUsersOrdered(
    orderBy: String,
    ascending: Boolean
  ): ConnectionIO[List[User]] = {
    val orderDir = if (ascending) fr"ASC" else fr"DESC"
    
    val orderFragment = orderBy match {
      case "name" => fr"ORDER BY name" ++ orderDir
      case "age" => fr"ORDER BY age" ++ orderDir
      case "created" => fr"ORDER BY created_at" ++ orderDir
      case _ => fr"ORDER BY id" ++ orderDir
    }
    
    val query = fr"SELECT id, name, email, age, created_at FROM users" ++ orderFragment
    
    query.query[User].to[List]
  }
  
  /*
   * TRANSACTIONS
   * 
   * All operations in a for-comprehension are executed in a single transaction
   * when you call .transact(xa). If any operation fails, all are rolled back.
   *
   * Rust (sqlx) equivalent:
   *   let mut tx = pool.begin().await?;
   *   let user_id = sqlx::query_scalar("INSERT ...")
   *     .execute(&mut tx)
   *     .await?;
   *   sqlx::query("INSERT ...")
   *     .bind(user_id)
   *     .execute(&mut tx)
   *     .await?;
   *   tx.commit().await?;
   */
  
  def createUserWithProfile(
    name: String,
    email: String,
    age: Int
  ): ConnectionIO[Long] = {
    for {
      // Insert user and get ID
      userId <- sql"""INSERT INTO users (name, email, age) 
                      VALUES ($name, $email, $age)""".update
        .withUniqueGeneratedKeys[Long]("id")
      
      // In real app, might insert into profile table here
      _ <- sql"".update.run.map(_ => ())  // Placeholder for additional operation
      _ <- doobie.free.connection.delay(println(s"User $userId created"))
      
    } yield userId
  }
  
  // Transfer operation (atomic)
  def transferUserAge(fromId: Long, toId: Long, years: Int): ConnectionIO[Unit] = {
    for {
      // Decrease age of first user
      _ <- sql"UPDATE users SET age = age - $years WHERE id = $fromId".update.run
      
      // Increase age of second user
      _ <- sql"UPDATE users SET age = age + $years WHERE id = $toId".update.run
      
      // Both succeed or both fail (atomic)
    } yield ()
  }
  
  /*
   * BATCH OPERATIONS
   * 
   * Execute multiple updates efficiently in a batch.
   *
   * Rust (sqlx) equivalent:
   *   for user in users {
   *     sqlx::query("INSERT ...").bind(user).execute(&pool).await?;
   *   }
   */
  
  def insertUsersBatch(users: List[(String, String, Int)]): ConnectionIO[Int] = {
    val sql = "INSERT INTO users (name, email, age) VALUES (?, ?, ?)"
    Update[(String, String, Int)](sql).updateMany(users)
  }
  
  /*
   * COMPOSING WITH EITHERT
   * 
   * Integrate Doobie with EitherT for typed error handling.
   * This is the production pattern!
   *
   * Rust equivalent:
   *   async fn find_user(id: i64, pool: &Pool) -> Result<User, AppError> {
   *     sqlx::query_as("SELECT ...").fetch_one(pool).await
   *       .map_err(|e| AppError::NotFound(id))?
   *   }
   */
  
  sealed trait AppError
  case class UserNotFound(id: Long) extends AppError
  case class DatabaseError(msg: String) extends AppError
  
  def findUserSafe(id: Long): EitherT[ConnectionIO, AppError, User] = {
    EitherT(
      sql"SELECT id, name, email, age, created_at FROM users WHERE id = $id"
        .query[User]
        .option
        .attemptSql
        .map {
          case Right(Some(user)) => Right(user)
          case Right(None) => Left(UserNotFound(id))
          case Left(error) => Left(DatabaseError(error.getMessage))
        }
    )
  }
  
  def updateUserSafe(id: Long, name: String): EitherT[ConnectionIO, AppError, Unit] = {
    for {
      _ <- findUserSafe(id)  // Check if exists
      _ <- EitherT.right[AppError](
        sql"UPDATE users SET name = $name WHERE id = $id".update.run.void
      )
    } yield ()
  }
  
  /*
   * STREAMING RESULTS
   * 
   * For large result sets, stream instead of loading all into memory.
   *
   * Rust (sqlx) equivalent:
   *   let mut stream = sqlx::query_as::<_, User>("SELECT ...")
   *     .fetch(&pool);
   *   while let Some(user) = stream.try_next().await? {
   *     process(user);
   *   }
   */
  
  def processUsersStream(xa: Transactor[IO]): IO[Unit] = {
    sql"SELECT id, name, email, age, created_at FROM users"
      .query[User]
      .stream
      .transact(xa)
      .evalMap { user =>
        IO.println(s"Processing: ${user.name}")
      }
      .compile
      .drain
  }
  
  /*
   * EXAMPLES
   */
  
  def exampleUsage(xa: Transactor[IO]): IO[Unit] = {
    for {
      _ <- IO.println("=== Query Composition & Transactions ===\n")
      
      // Clean up
      _ <- sql"DELETE FROM users".update.run.transact(xa)
      
      // Insert test data
      _ <- IO.println("1. Inserting test users...")
      _ <- insertUsersBatch(List(
        ("Alice", "alice@example.com", 30),
        ("Bob", "bob@example.com", 25),
        ("Charlie", "charlie@example.com", 35),
        ("David", "david@example.com", 28),
        ("Eve", "eve@example.com", 32)
      )).transact(xa)
      
      // Dynamic query
      _ <- IO.println("\n2. Dynamic query: age between 25 and 30...")
      users1 <- selectUsers(Some(25), Some(30), None).transact(xa)
      _ <- users1.traverse(u => IO.println(s"   - ${u.name}, age ${u.age}"))
      
      _ <- IO.println("\n3. Dynamic query: name contains 'a'...")
      users2 <- selectUsers(None, None, Some("a")).transact(xa)
      _ <- users2.traverse(u => IO.println(s"   - ${u.name}"))
      
      // Ordering
      _ <- IO.println("\n4. Ordered by age (descending)...")
      users3 <- selectUsersOrdered("age", ascending = false).transact(xa)
      _ <- users3.traverse(u => IO.println(s"   - ${u.name}, age ${u.age}"))
      
      // Transaction
      _ <- IO.println("\n5. Transaction: transfer age (years)...")
      _ <- transferUserAge(1L, 2L, 5).transact(xa)
      alice <- sql"SELECT name, age FROM users WHERE id = 1".query[(String, Int)].unique.transact(xa)
      bob <- sql"SELECT name, age FROM users WHERE id = 2".query[(String, Int)].unique.transact(xa)
      _ <- IO.println(s"   Alice: ${alice._2} years old")
      _ <- IO.println(s"   Bob: ${bob._2} years old")
      
      // EitherT pattern
      _ <- IO.println("\n6. EitherT error handling...")
      result1 <- findUserSafe(1L).value.transact(xa)
      _ <- IO.println(s"   Found user: ${result1.map(_.name)}")
      
      result2 <- findUserSafe(999L).value.transact(xa)
      _ <- IO.println(s"   User 999: $result2")
      
      _ <- IO.println("\n7. Streaming results...")
      _ <- processUsersStream(xa)
      
      _ <- IO.println("\n✓ Example complete!")
    } yield ()
  }
  
  def run: IO[Unit] = {
    createTransactor().use { xa =>
      exampleUsage(xa)
    }.handleErrorWith { error =>
      IO.println(s"Error: ${error.getMessage}")
    }
  }
}

/*
 * KEY CONCEPTS FOR RUST DEVELOPERS:
 * 
 * 1. **Fragments (fr"...")**:
 *    - Composable query pieces
 *    - Combine with ++ operator
 *    - Like building query strings but type-safe
 * 
 * 2. **Transactions**:
 *    - All ConnectionIO in for-comprehension = single transaction
 *    - Rollback on any failure
 *    - Like sqlx::Transaction
 * 
 * 3. **Batch Updates**:
 *    - Update.updateMany for multiple rows
 *    - More efficient than individual updates
 *    - Like sqlx batch execution
 * 
 * 4. **EitherT Integration**:
 *    - Wrap ConnectionIO in EitherT for typed errors
 *    - Production pattern!
 *    - Similar to Result<T, E> in Rust
 * 
 * 5. **Streaming**:
 *    - .stream for large result sets
 *    - Process one row at a time
 *    - Like sqlx::query().fetch() stream
 * 
 * PRODUCTION PATTERNS:
 * 
 * 1. **Dynamic Queries**: Use fragments for flexible querying
 * 2. **Atomic Operations**: Use transactions for multi-step operations
 * 3. **Type-Safe Errors**: Use EitherT[ConnectionIO, E, A]
 * 4. **Batch Operations**: Use updateMany for bulk inserts
 * 5. **Streaming**: Use .stream for large datasets
 * 
 * NEXT: 04_http_integration.scala covers integrating Doobie with http4s
 */
