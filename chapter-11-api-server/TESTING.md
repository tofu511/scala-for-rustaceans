# Testing Guide for Chapter 11

## Overview

This chapter demonstrates **Test-Driven Development (TDD)** practices for Scala developers, with a focus on patterns familiar to Rust developers.

**Current Test Status:**
- ✅ **Unit Tests**: 22/22 passing
  - Domain layer: 10 tests
  - Service layer: 12 tests
- ⚠️ **Integration Tests**: Limited by H2 compatibility
  - Repository: Needs PostgreSQL-specific features (RETURNING)
  - HTTP: Needs working repository layer

## Test Structure

```
src/test/
├── resources/
│   ├── application-test.conf    # Test configuration
│   └── db/migration/            # Test migrations
└── scala/apiserver/
    ├── domain/
    │   └── ModelsSpec.scala     # ✅ Domain model tests
    ├── service/
    │   └── UserServiceSpec.scala # ✅ Service logic tests
    ├── repository/
    │   └── UserRepositorySpec.scala # ⚠️ Needs real PostgreSQL
    └── http/
        └── UserRoutesSpec.scala # ⚠️ Depends on repository
```

## Running Tests

```bash
# Run all tests
sbt test

# Run specific test suite
sbt "testOnly apiserver.domain.ModelsSpec"
sbt "testOnly apiserver.service.UserServiceSpec"

# Run with verbose output
sbt "testOnly apiserver.* -- -oD"

# Run in watch mode (re-run on file changes)
sbt ~test
```

## Unit Tests (Fast, No External Dependencies)

### Domain Layer Tests

**File**: `src/test/scala/apiserver/domain/ModelsSpec.scala`

Tests pure domain logic without any external dependencies:

```scala
"CreateUserRequest" should "be valid with correct data" in {
  val request = CreateUserRequest("Alice", "alice@example.com", 30)
  
  request.name shouldBe "Alice"
  request.email shouldBe "alice@example.com"
  request.age shouldBe 30
}
```

**Rust Comparison:**
```rust
#[test]
fn test_create_user_request() {
    let request = CreateUserRequest {
        name: "Alice".to_string(),
        email: "alice@example.com".to_string(),
        age: 30,
    };
    
    assert_eq!(request.name, "Alice");
    assert_eq!(request.email, "alice@example.com");
    assert_eq!(request.age, 30);
}
```

**What We Test:**
- ✅ Request/Response model creation
- ✅ Error types and messages
- ✅ Domain invariants

**Run:** `sbt "testOnly apiserver.domain.ModelsSpec"`

### Service Layer Tests

**File**: `src/test/scala/apiserver/service/UserServiceSpec.scala`

Tests business logic using a **mock repository** (no database required):

```scala
class MockUserRepository extends UserRepository[ConnectionIO] {
  private var users: Map[Long, User] = Map.empty
  // In-memory implementation...
}

val mockRepo = new MockUserRepository()
val service = new UserServiceImpl(mockRepo)

"UserService.create" should "reject empty name" in {
  val request = CreateUserRequest("", "alice@example.com", 30)
  val result = service.validateCreateRequest(request)
  
  result shouldBe Left(DomainError.ValidationError("name", "cannot be empty"))
}
```

**Rust Comparison:**
```rust
struct MockUserRepository {
    users: Arc<Mutex<HashMap<i64, User>>>,
}

#[async_trait]
impl UserRepository for MockUserRepository {
    async fn find_all(&self) -> Result<Vec<User>, DomainError> {
        Ok(self.users.lock().unwrap().values().cloned().collect())
    }
}

#[tokio::test]
async fn test_service_validation() {
    let mock_repo = MockUserRepository::new();
    let service = UserService::new(mock_repo);
    
    let request = CreateUserRequest {
        name: "".to_string(),
        email: "alice@example.com".to_string(),
        age: 30,
    };
    
    let result = service.validate(&request);
    assert!(result.is_err());
}
```

**What We Test:**
- ✅ Validation logic (empty names, invalid emails, age ranges)
- ✅ Email format validation
- ✅ Business rules
- ✅ Error messages

**Run:** `sbt "testOnly apiserver.service.UserServiceSpec"`

## Integration Tests (With Database)

### Repository Layer Tests

**File**: `src/test/scala/apiserver/repository/UserRepositorySpec.scala`

**⚠️ Current Status:** These tests require PostgreSQL-specific features (RETURNING clause) that H2 doesn't support fully.

**For Production Use:**
1. Use **Testcontainers** with real PostgreSQL
2. Or use manual Docker setup for integration tests

```scala
// Example with Testcontainers (add dependency)
val postgres = new PostgreSQLContainer()
postgres.start()

val transactor = Transactor.fromDriverManager[IO](
  "org.postgresql.Driver",
  postgres.getJdbcUrl,
  postgres.getUsername,
  postgres.getPassword
)
```

**Rust Comparison:**
```rust
#[sqlx::test]
async fn test_repository_create_user(pool: PgPool) {
    let repo = UserRepository::new(pool);
    
    let user = repo.create(CreateUserRequest {
        name: "Alice".to_string(),
        email: "alice@example.com".to_string(),
        age: 30,
    }).await.unwrap();
    
    assert_eq!(user.name, "Alice");
}
```

### HTTP Layer Tests

**File**: `src/test/scala/apiserver/http/UserRoutesSpec.scala`

Tests full HTTP request/response cycle.

**Example:**
```scala
"POST /users" should "create a new user" in {
  val createReq = CreateUserRequest("Alice", "alice@example.com", 30)
  val request = Request[IO](Method.POST, uri"/users")
    .withEntity(createReq.asJson)

  val response = routes.run(request).unsafeRunSync()
  response.status shouldBe Status.Created
  
  val user = response.as[User].unsafeRunSync()
  user.name shouldBe "Alice"
}
```

**Rust Comparison:**
```rust
#[tokio::test]
async fn test_create_user_endpoint() {
    let app = create_app();
    
    let response = app
        .oneshot(
            Request::builder()
                .uri("/users")
                .method("POST")
                .header("content-type", "application/json")
                .body(Body::from(r#"{"name":"Alice","email":"alice@example.com","age":30}"#))
                .unwrap()
        )
        .await
        .unwrap();
    
    assert_eq!(response.status(), StatusCode::CREATED);
}
```

## Test-Driven Development (TDD) Workflow

### The Red-Green-Refactor Cycle

```
1. 🔴 RED: Write a failing test
   ↓
2. 🟢 GREEN: Write minimal code to pass
   ↓
3. 🔵 REFACTOR: Improve the code
   ↓
4. Repeat
```

### Example: Adding Email Uniqueness Check

**Step 1: Write the Test (RED)**

```scala
"UserService.create" should "reject duplicate email" in {
  val request1 = CreateUserRequest("Alice", "alice@example.com", 30)
  val request2 = CreateUserRequest("Bob", "alice@example.com", 25)
  
  val result = (for {
    _ <- service.create(request1)
    attempt <- service.create(request2)
  } yield attempt).value
  
  result should matchPattern { 
    case Left(DomainError.DuplicateEmail("alice@example.com")) => 
  }
}
```

**Run test:** `sbt test` → Test FAILS ❌

**Step 2: Implement Feature (GREEN)**

```scala
// In UserServiceImpl
def create(req: CreateUserRequest): EitherT[ConnectionIO, DomainError, User] = {
  for {
    validated <- EitherT.fromEither[ConnectionIO](validateCreateRequest(req))
    existing <- EitherT.liftF(repository.findByEmail(req.email))
    _ <- EitherT.fromEither[ConnectionIO](
      if (existing.isDefined) 
        Left(DomainError.DuplicateEmail(req.email))
      else 
        Right(())
    )
    user <- repository.create(validated)
  } yield user
}
```

**Run test:** `sbt test` → Test PASSES ✅

**Step 3: Refactor**

Extract duplicate check into helper method, improve error messages, etc.

## Test Patterns and Best Practices

### 1. Use Descriptive Test Names

```scala
// ❌ Bad
"test1" should "work" in { ... }

// ✅ Good
"UserService.create" should "reject empty name" in { ... }
"UserRepository.findById" should "return UserNotFound when ID doesn't exist" in { ... }
```

### 2. Arrange-Act-Assert Pattern

```scala
"create user" should "succeed with valid data" in {
  // Arrange
  val request = CreateUserRequest("Alice", "alice@example.com", 30)
  
  // Act
  val result = service.create(request)
  
  // Assert
  result shouldBe Right(...)
}
```

### 3. Test One Thing Per Test

```scala
// ❌ Bad - tests multiple things
"user validation" should "work" in {
  // Tests name validation
  // Tests email validation
  // Tests age validation
}

// ✅ Good - one assertion per test
"validation" should "reject empty name" in { ... }
"validation" should "reject invalid email" in { ... }
"validation" should "reject negative age" in { ... }
```

### 4. Use Test Fixtures for Setup

```scala
class UserServiceSpec extends AnyFlatSpec {
  val mockRepo = new MockUserRepository()
  val service = new UserServiceImpl(mockRepo)
  
  override def beforeEach(): Unit = {
    mockRepo.clear() // Reset state before each test
  }
}
```

### 5. Test Error Cases

```scala
// Don't just test the happy path!
"findById" should "return UserNotFound when ID doesn't exist" in {
  val result = repository.findById(999L).value.transact(xa).unsafeRunSync()
  
  result should matchPattern { 
    case Left(DomainError.UserNotFound(999L)) => 
  }
}
```

## Mocking Strategies

### Manual Mocks (Recommended for Simple Cases)

```scala
class MockUserRepository extends UserRepository[ConnectionIO] {
  private var users: Map[Long, User] = Map.empty
  
  override def findAll(): EitherT[ConnectionIO, DomainError, List[User]] =
    EitherT.rightT(users.values.toList)
  
  // ... other methods
}
```

**Advantages:**
- Full control
- Type-safe
- Easy to understand

**Rust Equivalent:**
```rust
struct MockUserRepository {
    users: HashMap<i64, User>,
}
```

### ScalaMock (For Complex Scenarios)

```scala
// Add dependency: "org.scalamock" %% "scalamock" % "5.2.0" % Test

val mockRepo = mock[UserRepository[ConnectionIO]]

(mockRepo.findById _)
  .expects(1L)
  .returning(EitherT.rightT(user))
  .once()
```

**Rust Equivalent:** mockall crate

## Continuous Integration

### GitHub Actions Example

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: postgres
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    
    steps:
    - uses: actions/checkout@v3
    - uses: coursier/setup-action@v1
      with:
        jvm: adopt:17
    - name: Run tests
      run: sbt test
```

## Performance Tips

### 1. Parallel Test Execution

```scala
// build.sbt
Test / parallelExecution := true
Test / fork := true
```

### 2. Use In-Memory Database for Unit Tests

```scala
// Fast H2 setup for unit tests only
val testTransactor = Transactor.fromDriverManager[IO](
  "org.h2.Driver",
  "jdbc:h2:mem:test;MODE=PostgreSQL",
  "sa",
  ""
)
```

### 3. Test Only What Changed

```bash
# Run specific test suite
sbt "testOnly *UserServiceSpec"

# Run tests matching pattern
sbt "testOnly *Service*"
```

## Common Issues and Solutions

### Issue: Tests are slow

**Solution:** Use mocks for unit tests, real DB only for integration tests.

### Issue: H2 syntax errors with PostgreSQL-specific features

**Solution:** 
- Use Testcontainers with real PostgreSQL for integration tests
- Or document workarounds for H2 limitations
- Keep unit tests database-independent

### Issue: Flaky tests

**Solution:**
- Reset state in `beforeEach`
- Use unique test data
- Avoid time-dependent tests
- Use proper transaction isolation

## Summary

**What We've Learned:**

✅ **Unit Testing**: Fast, isolated, no external dependencies  
✅ **Integration Testing**: Real database, full stack  
✅ **TDD Workflow**: Red-Green-Refactor cycle  
✅ **Mocking**: Both manual and library-based approaches  
✅ **Rust Comparisons**: Similar patterns in both ecosystems  

**Test Pyramid:**
```
      /\
     /  \  E2E Tests (Few)
    /____\
   /      \
  / Integr \ Integration Tests (Some)
 /  ation  \
/___________\
/           \
/   Unit     \ Unit Tests (Many)
/   Tests    \
/_____________\
```

**Next Steps:**
- Add more edge case tests
- Implement property-based testing (ScalaCheck - Chapter 12)
- Add performance tests
- Set up CI/CD pipeline

## Rust vs Scala Testing Comparison

| Aspect | Rust | Scala |
|--------|------|-------|
| **Test Framework** | Built-in `#[test]` | ScalaTest, Specs2, munit |
| **Async Tests** | `#[tokio::test]` | cats-effect-testing-scalatest |
| **Mocking** | mockall | ScalaMock, manual |
| **Property Testing** | proptest, quickcheck | ScalaCheck |
| **Test Location** | `#[cfg(test)]` mod, `tests/` | `src/test/scala` |
| **Assertions** | `assert!`, `assert_eq!` | `shouldBe`, `shouldEqual` |
| **Setup/Teardown** | No built-in | `beforeEach`, `afterEach` |

Both ecosystems emphasize:
- Fast unit tests
- Integration tests with real dependencies
- TDD/BDD practices
- Type-safe mocking
