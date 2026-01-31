# Chapter 11: Exercises (TDD Style)

## TDD Approach

These exercises follow **Test-Driven Development**:

```
1. 🔴 RED: Run test - it FAILS
2. 🟢 GREEN: Write code to make it PASS
3. 🔵 REFACTOR: Improve code
```

## Exercise Files

**Implementation Stubs** (where YOU write code):
- `src/main/scala/exercises/Exercise01_Pagination.scala` - Pagination hints
- `src/main/scala/exercises/Exercise02_Search.scala` - Search hints
- `src/main/scala/exercises/Exercise03_SoftDelete.scala` - Soft delete hints

**Test Files** (run these to see RED/GREEN):
- `src/test/scala/exercises/Exercise01_PaginationSpec.scala` - 5 tests (pending)
- `src/test/scala/exercises/Exercise02_SearchSpec.scala` - 5 tests (pending)
- `src/test/scala/exercises/Exercise03_SoftDeleteSpec.scala` - 5 tests (pending)

**Solutions** (don't peek!):
- `src/main/scala/exercises/solutions/Exercise01_Solution.scala`
- `src/main/scala/exercises/solutions/Exercise02_Solution.scala`
- `src/main/scala/exercises/solutions/Exercise03_Solution.scala`

---

## Workflow

### Step 1: Read the Exercise File

Start with `Exercise01_Pagination.scala`. It contains:
- Clear objectives
- Rust comparisons
- Step-by-step hints
- Code snippets to copy

### Step 2: Verify Tests are Pending

```bash
sbt "testOnly exercises.Exercise01_PaginationSpec"
```

Output: `Tests: succeeded 0, failed 0, canceled 0, ignored 0, pending 5`

### Step 3: Uncomment First Test

Edit `Exercise01_PaginationSpec.scala` and uncomment the first test (lines 36-38).

### Step 4: Run - See it FAIL (RED) 🔴

```bash
sbt "testOnly exercises.Exercise01_PaginationSpec"
```

Compilation error or test failure = Good! This is RED.

### Step 5: Implement Feature

Follow hints in `Exercise01_Pagination.scala`:
1. Add domain models to `domain/Models.scala`
2. Add repository methods
3. Add service methods
4. Update HTTP routes

### Step 6: Run - See it PASS (GREEN) 🟢

```bash
sbt "testOnly exercises.Exercise01_PaginationSpec"
```

Test passes! 🎉

### Step 7: Refactor (BLUE) 🔵

Improve code quality without breaking tests.

### Step 8: Repeat

Move to next test in the same file, then next exercise file.

---

## Exercise 1: Pagination

**Goal**: Add pagination to list users endpoint

**File**: `src/main/scala/exercises/Exercise01_Pagination.scala`

**Tests**: 5 pending tests for:
- PaginationParams offset calculation
- PaginatedResponse structure
- Repository pagination
- Service validation
- HTTP endpoint

**Key Concepts**:
- SQL LIMIT/OFFSET
- Query parameters in HTTP
- Response metadata (page, pageSize, total)

---

## Exercise 2: Search

**Goal**: Find users by name or email

**File**: `src/main/scala/exercises/Exercise02_Search.scala`

**Tests**: 5 pending tests for:
- Case-insensitive search
- Partial matching
- Multiple field search
- Empty results
- Validation

**Key Concepts**:
- SQL LIKE with wildcards (`%`)
- LOWER() for case-insensitive matching
- Query parameter handling

---

## Exercise 3: Soft Delete

**Goal**: Mark users as deleted instead of removing

**File**: `src/main/scala/exercises/Exercise03_SoftDelete.scala`

**Tests**: 5 pending tests for:
- User model with deletedAt
- Soft delete behavior
- Filtering deleted users
- Restore capability
- Migration

**Key Concepts**:
- Optional fields (Option[LocalDateTime])
- Database migration (ALTER TABLE)
- WHERE clauses to filter
- UPDATE vs DELETE

---

## Running Tests

```bash
# All exercises
sbt "testOnly exercises.*"

# Specific exercise
sbt "testOnly exercises.Exercise01_PaginationSpec"

# Watch mode (auto-rerun on changes)
sbt "~testOnly exercises.Exercise01_PaginationSpec"

# Check pending count
sbt "testOnly exercises.*" | grep pending
```

---

## Tips

### When Stuck

1. Read the exercise file carefully (lots of hints!)
2. Check the test file to see what's expected
3. Review main implementation in `src/main/scala/`
4. Look at solution file (but try first!)

### TDD Best Practices

✅ **DO**:
- Uncomment tests one at a time
- Run tests frequently (after each small change)
- Write minimal code to make test pass
- Refactor after test passes

❌ **DON'T**:
- Uncomment all tests at once
- Write code without running tests
- Skip the refactor step
- Peek at solutions too early

### Compilation Errors are OK!

In TDD, compilation errors are part of RED phase:
- Test expects `PaginationParams` but it doesn't exist yet
- That's the signal to create it!

---

## Benefits of TDD

✅ **Fewer bugs** - Tests catch issues immediately  
✅ **Better design** - Tests force you to think about interfaces  
✅ **Confidence to refactor** - Tests ensure nothing breaks  
✅ **Tests as documentation** - Tests show how code should be used  

This is how professional Scala/Rust developers work!

---

## After Completing Exercises

You'll have learned:
- ✅ Pagination patterns for REST APIs
- ✅ Search functionality with SQL
- ✅ Soft delete (common production pattern)
- ✅ TDD workflow (RED→GREEN→REFACTOR)
- ✅ Full-stack feature implementation (domain → repository → service → HTTP)

Ready to build production Scala services! 🚀
