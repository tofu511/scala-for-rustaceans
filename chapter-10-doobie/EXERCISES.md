# Chapter 10: Exercises Quick Reference

## Prerequisites

Start PostgreSQL:
```bash
docker run --name postgres-test \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=testdb \
  -p 5432:5432 \
  -d postgres:15
```

Run migrations:
```bash
sbt "runMain doobiebasics.FlywayExample"
```

## Exercise 01: Basic Doobie Queries

**File:** `src/main/scala/exercises/Exercise01_BasicQueries.scala`  
**Solution:** `src/main/scala/exercises/solutions/Exercise01_Solution.scala`  
**Estimated Time:** 30-45 minutes

### Objectives
- Create Doobie transactor
- Write CRUD queries
- Execute with .transact(xa)

### Tasks
1. Implement `findAll()` - Return all books
2. Implement `insertBook()` - Insert and return ID
3. Implement `updateBook()` - Update title
4. Implement `deleteBook()` - Delete by ID

### Hints

**Find All:**
```scala
sql"SELECT id, title, author FROM books"
  .query[Book]
  .to[List]
```

**Insert:**
```scala
sql"INSERT INTO books (title, author) VALUES ($title, $author)"
  .update
  .withUniqueGeneratedKeys[Long]("id")
```

**Update:**
```scala
sql"UPDATE books SET title = $title WHERE id = $id"
  .update
  .run
```

**Delete:**
```scala
sql"DELETE FROM books WHERE id = $id"
  .update
  .run
```

## Key Concepts

### ConnectionIO[A]
Description of database operation. Execute with `.transact(xa)`:
```scala
val query: ConnectionIO[User] = findUser(1)
val io: IO[User] = query.transact(xa)
```

### Query Methods
- `.unique` - Expect exactly 1 result
- `.option` - 0 or 1 result (Option[A])
- `.to[List]` - Collect all results
- `.stream` - Stream results

### Update Methods
- `.run` - Return rows affected (Int)
- `.withUniqueGeneratedKeys[T](cols)` - Return generated keys

### Transactions
All ConnectionIO in for-comprehension = single transaction:
```scala
(for {
  id <- insertUser(...)
  _ <- insertProfile(id, ...)
} yield id).transact(xa)  // Atomic
```

## Rust Comparison

```rust
// sqlx
let books: Vec<Book> = sqlx::query_as("SELECT * FROM books")
  .fetch_all(&pool)
  .await?;

let id: i64 = sqlx::query_scalar(
  "INSERT INTO books (title, author) VALUES ($1, $2) RETURNING id"
)
  .bind(title)
  .bind(author)
  .fetch_one(&pool)
  .await?;
```

```scala
// Doobie
val books: IO[List[Book]] = 
  sql"SELECT id, title, author FROM books"
    .query[Book]
    .to[List]
    .transact(xa)

val id: IO[Long] =
  sql"INSERT INTO books (title, author) VALUES ($title, $author)"
    .update
    .withUniqueGeneratedKeys[Long]("id")
    .transact(xa)
```

## Common Mistakes

❌ Forgetting .transact:
```scala
findAll()  // Returns ConnectionIO[List[Book]]
```

✅ Always transact:
```scala
findAll().transact(xa)  // Returns IO[List[Book]]
```

❌ Wrong query method:
```scala
sql"SELECT * FROM users WHERE id = $id"
  .query[User]
  .to[List]  // Returns List even if 0 or 1 result
```

✅ Use appropriate method:
```scala
sql"SELECT * FROM users WHERE id = $id"
  .query[User]
  .option  // Returns Option[User]
```

## Testing Checklist

- [ ] Can list all books
- [ ] Can insert a book and get ID back
- [ ] Can update a book's title
- [ ] Can delete a book
- [ ] All operations execute successfully

## Key Takeaways

✅ **Transactor** manages database connections  
✅ **ConnectionIO** describes database operations  
✅ **.transact(xa)** executes ConnectionIO  
✅ **sql"..."** for parameterized queries (safe!)  
✅ **For-comprehension** for transactions  
✅ **attemptSql** for error handling  

**Next:** Chapter 11 builds a complete API server!
