# Chapter 10: Doobie & Flyway

Database access with Doobie and schema migrations with Flyway.

## Overview

**Doobie** is a pure functional database library for Scala. It provides type-safe SQL queries and composable database operations using Cats-Effect.

**Flyway** is a database migration tool that manages schema versioning.

**Rust Comparison:**
- Doobie ↔ sqlx (type-safe SQL, connection pooling)
- Flyway ↔ sqlx-cli / diesel migrations

## Prerequisites

PostgreSQL must be running:
```bash
docker run --name postgres-test \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=testdb \
  -p 5432:5432 \
  -d postgres:15
```

## Files

### Examples
1. **01_flyway.scala** - Database migrations
2. **02_doobie_basics.scala** - Basic queries (SELECT, INSERT, UPDATE, DELETE)
3. **03_transactions.scala** - Query composition, fragments, transactions
4. **04_http_integration.scala** - Full integration with http4s

### Migrations
- `src/main/resources/db/migration/V1__create_users_table.sql`
- `src/main/resources/db/migration/V2__create_books_table.sql`

## Running Examples

```bash
cd chapter-10-doobie

# Run migrations first
sbt "runMain doobiebasics.FlywayExample"

# Then run Doobie examples
sbt "runMain doobiebasics.DoobieBasicsExample"
sbt "runMain doobiebasics.QueryCompositionExample"

# Full http4s integration
sbt "runMain doobiebasics.HttpIntegrationExample"
# Test with: curl http://localhost:8080/users
```

## Key Concepts

### Flyway Migrations

Versioned SQL files in `src/main/resources/db/migration/`:
- `V1__description.sql` - Version 1
- `V2__description.sql` - Version 2

```scala
val flyway = Flyway.configure()
  .dataSource(url, user, password)
  .load()

flyway.migrate()  // Apply pending migrations
```

### Doobie Transactor

Manages database connections:
```scala
HikariTransactor.newHikariTransactor[IO](
  driverClassName = "org.postgresql.Driver",
  url = dbUrl,
  user = dbUser,
  pass = dbPassword,
  connectEC = ec
)
```

### Basic Queries

```scala
// SELECT
sql"SELECT id, name, email FROM users WHERE id = $id"
  .query[User]
  .unique  // Expect exactly one

// INSERT
sql"INSERT INTO users (name, email) VALUES ($name, $email)"
  .update
  .withUniqueGeneratedKeys[Long]("id")

// UPDATE
sql"UPDATE users SET name = $name WHERE id = $id"
  .update
  .run  // Returns rows affected

// DELETE
sql"DELETE FROM users WHERE id = $id"
  .update
  .run
```

### Transactions

All ConnectionIO operations in a for-comprehension run in one transaction:
```scala
(for {
  userId <- insertUser(name, email)
  _ <- insertProfile(userId, bio)
} yield userId).transact(xa)  // Atomic
```

### http4s Integration

```scala
class UserRoutes(xa: Transactor[IO]) {
  val routes = HttpRoutes.of[IO] {
    case GET -> Root / "users" / LongVar(id) =>
      UserRepository.findById(id)
        .value
        .transact(xa)
        .flatMap {
          case Right(user) => Ok(user.asJson)
          case Left(error) => errorToResponse(error)
        }
  }
}
```

## Rust Comparison

| Feature | Rust (sqlx) | Scala (Doobie) |
|---------|-------------|----------------|
| **Connection** | `PgPool` | `Transactor[IO]` |
| **Query** | `query!` / `query_as!` | `sql"...".query[T]` |
| **Insert** | `query!(...).execute()` | `.update.withUniqueGeneratedKeys` |
| **Transaction** | `pool.begin().await?` | `.transact(xa)` |
| **Migration** | sqlx-cli | Flyway |

## Production Pattern

**Repository Layer** (database operations):
```scala
def findUser(id: Long): EitherT[ConnectionIO, AppError, User]
```

**Service Layer** (business logic):
```scala
def createUser(req: Request): EitherT[ConnectionIO, AppError, User]
```

**HTTP Layer** (routes):
```scala
UserService.createUser(req)
  .value
  .transact(xa)
  .flatMap {
    case Right(user) => Created(user.asJson)
    case Left(error) => errorToResponse(error)
  }
```

## Tips

1. **Always use transact(xa)** to execute ConnectionIO
2. **Use EitherT** for typed errors
3. **Use .attemptSql** to catch SQL errors
4. **HikariCP** for production (connection pooling)
5. **Run migrations at startup** in production apps

## Common Mistakes

❌ Forgetting `.transact(xa)`:
```scala
findUser(id)  // Returns ConnectionIO[User], not IO[User]
```

✅ Always transact:
```scala
findUser(id).transact(xa)  // Returns IO[User]
```

❌ Not handling SQL errors:
```scala
sql"...".query[User].unique  // Can throw
```

✅ Use attemptSql:
```scala
sql"...".query[User].unique.attemptSql  // Returns Either
```

## Next Steps

**Chapter 11**: Build complete API server with layered architecture

## Resources

- [Doobie Documentation](https://tpolecat.github.io/doobie/)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [HikariCP](https://github.com/brettwooldridge/HikariCP)
