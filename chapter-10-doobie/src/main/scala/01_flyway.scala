package doobiebasics

import cats.effect._
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult

/*
 * Chapter 10: Flyway - Database Schema Migrations
 *
 * RUST COMPARISON:
 * Flyway is similar to migration tools in Rust:
 * - sqlx-cli: `sqlx migrate add` and `sqlx migrate run`
 * - diesel_cli: `diesel migration generate` and `diesel migration run`
 *
 * Like these tools, Flyway:
 * - Versions database schema changes
 * - Applies migrations in order
 * - Tracks which migrations have been applied
 * - Supports rollbacks (with versioned scripts)
 *
 * Key Differences:
 * Rust (sqlx-cli):
 *   sqlx migrate add create_users
 *   sqlx migrate run
 *   # Migrations tracked in _sqlx_migrations table
 *
 * Scala (Flyway):
 *   // Create V1__create_users.sql
 *   flyway.migrate()
 *   // Migrations tracked in flyway_schema_history table
 */

object FlywayExample extends IOApp.Simple {
  
  /*
   * DATABASE CONFIGURATION
   * 
   * For this example, you need a PostgreSQL database running.
   * You can use Docker:
   *
   * docker run --name postgres-test \
   *   -e POSTGRES_PASSWORD=password \
   *   -e POSTGRES_DB=testdb \
   *   -p 5432:5432 \
   *   -d postgres:15
   *
   * Or install PostgreSQL locally.
   */
  
  val dbUrl = "jdbc:postgresql://localhost:5432/testdb"
  val dbUser = "postgres"
  val dbPassword = "password"
  
  /*
   * FLYWAY SETUP
   * 
   * Configure Flyway to point to your database and migration files.
   * Migration files should be in src/main/resources/db/migration/
   *
   * Naming convention: V{version}__{description}.sql
   * Examples:
   *   V1__create_users_table.sql
   *   V2__create_books_table.sql
   *   V3__add_user_role.sql
   *
   * Rust (sqlx) equivalent:
   *   migrations/
   *     20240101000001_create_users_table.sql
   *     20240101000002_create_books_table.sql
   */
  
  def createFlyway(): Flyway = {
    Flyway
      .configure()
      .dataSource(dbUrl, dbUser, dbPassword)
      .locations("classpath:db/migration")  // Where migration files are located
      .baselineOnMigrate(true)  // Create flyway_schema_history if it doesn't exist
      .load()
  }
  
  /*
   * RUNNING MIGRATIONS
   * 
   * Flyway.migrate() applies all pending migrations.
   * It's idempotent - safe to run multiple times.
   *
   * Rust equivalent:
   *   sqlx::migrate!("./migrations").run(&pool).await?;
   */
  
  def runMigrations(): IO[MigrateResult] = {
    IO {
      val flyway = createFlyway()
      
      IO.println("Running migrations...") *>
      IO {
        val result = flyway.migrate()
        result
      }
    }.flatten
  }
  
  /*
   * MIGRATION INFO
   * 
   * Check migration status without running them.
   */
  
  def showMigrationInfo(): IO[Unit] = {
    IO {
      val flyway = createFlyway()
      val info = flyway.info()
      
      IO.println("=== Migration Status ===") *>
      IO.println(s"Current version: ${info.current()}") *>
      IO.println(s"Pending migrations: ${info.pending().length}") *>
      IO.println("\nAll migrations:") *>
      IO {
        info.all().foreach { migration =>
          val status = if (migration.getState.isApplied) "✓" else "○"
          println(s"  $status V${migration.getVersion} - ${migration.getDescription} (${migration.getState})")
        }
      }
    }.flatten
  }
  
  /*
   * CLEAN DATABASE (CAUTION!)
   * 
   * Drops all tables. Use only in development/testing!
   * Never use in production.
   *
   * Rust (sqlx) equivalent:
   *   sqlx::migrate!("./migrations").undo(&pool).await?;
   */
  
  def cleanDatabase(): IO[Unit] = {
    IO {
      IO.println("⚠️  WARNING: This will drop all tables!") *>
      IO.println("Only use in development/testing.") *>
      IO {
        val flyway = createFlyway()
        flyway.clean()
      } *>
      IO.println("Database cleaned.")
    }.flatten
  }
  
  /*
   * VALIDATE MIGRATIONS
   * 
   * Check if applied migrations match the migration files.
   * Useful for detecting if someone modified an applied migration.
   */
  
  def validateMigrations(): IO[Unit] = {
    IO {
      val flyway = createFlyway()
      
      IO.println("Validating migrations...") *>
      IO {
        flyway.validate()
      } *>
      IO.println("✓ All migrations are valid")
    }.flatten.handleErrorWith { error =>
      IO.println(s"✗ Migration validation failed: ${error.getMessage}")
    }
  }
  
  /*
   * BASELINE
   * 
   * Creates the schema history table and marks an existing database
   * at a specific version. Useful when adding Flyway to an existing project.
   *
   * Rust equivalent:
   *   sqlx migrate add baseline
   *   # Then manually mark as applied
   */
  
  def baselineDatabase(): IO[Unit] = {
    IO {
      val flyway = createFlyway()
      
      IO.println("Creating baseline...") *>
      IO {
        flyway.baseline()
      } *>
      IO.println("✓ Baseline created")
    }.flatten
  }
  
  /*
   * COMPLETE EXAMPLE: SETUP FROM SCRATCH
   */
  
  def setupDatabase(): IO[Unit] = {
    for {
      _ <- IO.println("=== Database Setup ===")
      _ <- IO.println("\n1. Checking migration status...")
      _ <- showMigrationInfo()
      
      _ <- IO.println("\n2. Running migrations...")
      result <- runMigrations()
      _ <- IO.println(s"✓ Migrations complete: ${result.migrationsExecuted} executed")
      
      _ <- IO.println("\n3. Final status:")
      _ <- showMigrationInfo()
      
      _ <- IO.println("\n4. Validating migrations...")
      _ <- validateMigrations()
      
      _ <- IO.println("\n✓ Database setup complete!")
    } yield ()
  }
  
  def run: IO[Unit] = {
    // Check if database is accessible
    IO.println("=== Flyway Migration Example ===") *>
    IO.println(s"Database: $dbUrl") *>
    IO.println("\nMake sure PostgreSQL is running:") *>
    IO.println("  docker run --name postgres-test \\") *>
    IO.println("    -e POSTGRES_PASSWORD=password \\") *>
    IO.println("    -e POSTGRES_DB=testdb \\") *>
    IO.println("    -p 5432:5432 \\") *>
    IO.println("    -d postgres:15") *>
    IO.println("\nOr use existing PostgreSQL installation.") *>
    IO.println("\nRunning setup...\n") *>
    setupDatabase().handleErrorWith { error =>
      IO.println(s"\n✗ Error: ${error.getMessage}") *>
      IO.println("\nMake sure PostgreSQL is running and accessible.")
    }
  }
}

/*
 * EXAMPLE OUTPUT:
 * 
 * === Database Setup ===
 * 
 * 1. Checking migration status...
 * === Migration Status ===
 * Current version: None
 * Pending migrations: 2
 * 
 * All migrations:
 *   ○ V1 - create_users_table (Pending)
 *   ○ V2 - create_books_table (Pending)
 * 
 * 2. Running migrations...
 * ✓ Migrations complete: 2 executed
 * 
 * 3. Final status:
 * === Migration Status ===
 * Current version: 2
 * Pending migrations: 0
 * 
 * All migrations:
 *   ✓ V1 - create_users_table (Success)
 *   ✓ V2 - create_books_table (Success)
 * 
 * 4. Validating migrations...
 * ✓ All migrations are valid
 * 
 * ✓ Database setup complete!
 */

/*
 * MIGRATION FILE EXAMPLES:
 * 
 * File: src/main/resources/db/migration/V1__create_users_table.sql
 * 
 * CREATE TABLE users (
 *     id BIGSERIAL PRIMARY KEY,
 *     name VARCHAR(255) NOT NULL,
 *     email VARCHAR(255) NOT NULL UNIQUE,
 *     age INT NOT NULL,
 *     created_at TIMESTAMP NOT NULL DEFAULT NOW()
 * );
 * 
 * CREATE INDEX idx_users_email ON users(email);
 * 
 * 
 * File: src/main/resources/db/migration/V2__create_books_table.sql
 * 
 * CREATE TABLE books (
 *     id BIGSERIAL PRIMARY KEY,
 *     title VARCHAR(500) NOT NULL,
 *     author VARCHAR(255) NOT NULL,
 *     isbn VARCHAR(20) NOT NULL UNIQUE,
 *     published_year INT NOT NULL,
 *     created_at TIMESTAMP NOT NULL DEFAULT NOW()
 * );
 * 
 * CREATE INDEX idx_books_isbn ON books(isbn);
 */

/*
 * KEY CONCEPTS FOR RUST DEVELOPERS:
 * 
 * 1. **Versioned Migrations**:
 *    - V1__, V2__, V3__ (Flyway)
 *    - Timestamp-based (sqlx-cli)
 *    - Both track applied migrations
 * 
 * 2. **Migration Tracking**:
 *    - flyway_schema_history table (Flyway)
 *    - _sqlx_migrations table (sqlx)
 *    - Stores version, checksum, execution time
 * 
 * 3. **Idempotency**:
 *    - Safe to run migrations multiple times
 *    - Only applies new migrations
 *    - Similar to sqlx behavior
 * 
 * 4. **Validation**:
 *    - Checks if files match applied migrations
 *    - Detects tampering
 *    - Like sqlx checksum verification
 * 
 * 5. **Baseline**:
 *    - For adding migrations to existing database
 *    - Similar to manually marking sqlx migrations as applied
 * 
 * 6. **Clean (Development Only)**:
 *    - Drops all tables
 *    - Use carefully!
 *    - No direct sqlx equivalent (manual DROP)
 * 
 * BEST PRACTICES:
 * 
 * 1. **Never modify applied migrations**
 *    - Create new migration instead
 *    - Validation will fail if you change them
 * 
 * 2. **Use descriptive names**
 *    - V1__create_users_table.sql (good)
 *    - V1__changes.sql (bad)
 * 
 * 3. **One logical change per migration**
 *    - Makes rollback easier
 *    - Easier to understand
 * 
 * 4. **Test migrations on copy of production**
 *    - Before applying to production
 *    - Check for performance issues
 * 
 * 5. **Include rollback scripts (optional)**
 *    - U1__undo_create_users.sql
 *    - Flyway Teams feature
 * 
 * 6. **Run migrations at application startup**
 *    - Ensures database is up to date
 *    - Similar to sqlx::migrate!().run(&pool)
 * 
 * PRODUCTION PATTERN:
 * 
 * // In your application startup:
 * for {
 *   _ <- runMigrations()  // Apply migrations first
 *   xa <- createTransactor()  // Then create Doobie transactor
 *   _ <- startServer(xa)  // Finally start server
 * } yield ()
 * 
 * NEXT: 02_doobie_basics.scala covers connecting to database with Doobie
 */
