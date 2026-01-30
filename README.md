# Scala for Rustaceans

A comprehensive hands-on learning guide for Rust developers transitioning to Scala, with a focus on functional programming using Cats and Cats-Effect.

## 🎯 Who This Is For

This guide is designed for **intermediate Rust developers** who:
- Are comfortable with Rust's ownership, traits, and error handling
- Understand async/await and concurrent programming concepts
- Need to maintain or develop Scala services using Cats and Cats-Effect
- Want to understand functional programming patterns in Scala through a Rust lens

## 📚 What You'll Learn

- **Scala Fundamentals**: Type system, pattern matching, and core language features
- **Error Handling Evolution**: From `Either` and `Try` to `Future`, and why they're not enough
- **Cats Library**: Type classes, functors, monads, and functional abstractions
- **Cats-Effect**: Pure functional effects, concurrency, and resource management
- **Real-world Project**: Build a complete REST API with http4s, Doobie, and PostgreSQL
- **Testing**: ScalaTest for unit testing and ScalaCheck for property-based testing
- **Database Migrations**: Using Flyway for version-controlled schema management

## 🚀 Prerequisites

### Required
- **Rust knowledge**: Intermediate level (ownership, traits, Result/Option, async/await)
- **Terminal**: Basic command-line proficiency
- **Git**: For cloning and navigating the repository
- **Docker**: For running PostgreSQL (used in later chapters)

### Installation

This project uses **SDKMAN** for managing Java, Scala, and sbt versions. SDKMAN is a popular tool in the JVM ecosystem for managing multiple SDK versions.

#### Step 1: Install SDKMAN

```bash
# macOS and Linux
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Verify installation
sdk version
```

For Windows, use [SDKMAN on Windows](https://sdkman.io/install) or WSL2.

#### Step 2: Install Java, Scala, and sbt

```bash
# Install Java 17 (Temurin distribution)
sdk install java 17.0.10-tem

# Install Scala 2.13.13
sdk install scala 2.13.13

# Install sbt 1.9.9
sdk install sbt 1.9.9
```

#### Step 3: Verify Installation

```bash
java -version   # Should show Java 17 (Temurin)
scala -version  # Should show Scala 2.13.13
sbt --version   # Should show sbt 1.9.9
```

#### Alternative: Using Homebrew (macOS only)

If you prefer Homebrew:

```bash
# Install Java
brew install openjdk@17

# Install Scala and sbt
brew install scala@2.13 sbt

# Link Java
sudo ln -sfn $(brew --prefix)/opt/openjdk@17/libexec/openjdk.jdk \
  /Library/Java/JavaVirtualMachines/openjdk-17.jdk
```

#### Optional: mise for sbt only

This project includes a `mise.toml` for managing sbt version only:

```bash
# Install mise
brew install mise  # macOS
# Or see https://mise.jdx.dev/getting-started.html

# Install sbt via mise
mise install
```

**Note**: We recommend SDKMAN for Java and Scala as mise can have issues with Java installation on some systems.

## 📖 Learning Paths

This curriculum is designed with flexibility in mind. Choose the path that fits your schedule and learning goals:

### 🏃 Fast Track (1-2 days)
**Goal**: Quickly understand enough Scala and Cats-Effect to read and modify existing codebases

- **Chapters**: 1-11 (core content only)
- **Approach**: Focus on understanding concepts by running provided solutions
- **Skip**: Most exercises, deep dives in Chapter 4
- **Best for**: Developers who need to start working with Scala code immediately

### 🎓 Comprehensive (2-3 days)
**Goal**: Build solid understanding with hands-on practice

- **Chapters**: 1-11 with all exercises + 1-2 optional chapters
- **Approach**: Complete exercises, experiment with code, understand the "why"
- **Include**: Full Chapter 4 (referential transparency), choose from Chapters 12-14
- **Best for**: Developers who want to write new Scala code confidently

### 🚀 Advanced (3+ days)
**Goal**: Deep mastery of functional programming in Scala

- **Chapters**: All 15 chapters
- **Approach**: Complete all exercises, extend the final project
- **Include**: Property-based testing, advanced concurrency, type-level programming
- **Best for**: Developers who want to become proficient Scala/Cats developers

## 📋 Curriculum Overview

### Module 1: Environment Setup & Scala Basics (~2 hours)
- **[Chapter 01: Setup](chapter-01-setup/)** - Installation, tooling, and your first Scala program
- **[Chapter 02: Fundamentals](chapter-02-fundamentals/)** - Types, pattern matching, case classes, Options

### Module 2: Error Handling Evolution (~2 hours)
- **[Chapter 03: Error Handling](chapter-03-error-handling/)** - Either, Try, Future, and for-comprehensions
- **[Chapter 04: Referential Transparency](chapter-04-referential-transparency/)** - Why Future is problematic

### Module 3: Cats Fundamentals (~3 hours)
- **[Chapter 05: Introduction to Cats](chapter-05-cats-intro/)** - Type classes, Semigroup, Monoid, Functor, Monad
- **[Chapter 06: Cats Error Handling](chapter-06-cats-error-handling/)** - Either extensions, Validated, MonadError

### Module 4: Cats-Effect (~3 hours)
- **[Chapter 07: IO Monad Basics](chapter-07-cats-effect-basics/)** - Pure effects, IO construction and execution
- **[Chapter 08: Concurrency](chapter-08-cats-effect-concurrency/)** - Fibers, parallelism, Resource management

### Module 5: Final Project (~4 hours)
- **[Chapter 09: HTTP with http4s](chapter-09-http4s/)** - Building REST APIs
- **[Chapter 10: Database with Doobie & Flyway](chapter-10-doobie/)** - Database access and migrations
- **[Chapter 11: Complete API Server](chapter-11-api-server/)** - Full-stack application

### Module 6: Advanced Topics (Optional, ~3-4 hours)
- **[Chapter 12: Property-Based Testing](chapter-12-property-testing/)** (Optional) - ScalaCheck and law testing
- **[Chapter 13: Advanced Cats-Effect](chapter-13-advanced-ce/)** (Optional) - Deferred, Queue, fs2 streaming
- **[Chapter 14: Type-Level Programming](chapter-14-type-level/)** (Optional) - Higher-kinded types, Tagless Final
- **[Chapter 15: Production Considerations](chapter-15-production/)** (Optional) - Logging, metrics, observability

## 🔧 How to Use This Guide

### Working Through a Chapter

1. **Read the Chapter README**: Each chapter has a detailed README explaining concepts with Rust comparisons
2. **Run the Examples**: Execute the provided code examples with `sbt run`
3. **Complete Exercises**: Try the exercises (solutions provided in `solutions/` directory)
4. **Run Tests**: Verify your understanding with `sbt test`
5. **Experiment**: Modify the code and see what happens!

### sbt Quick Reference

```bash
# Compile the project
sbt compile

# Run the main application
sbt run

# Run tests
sbt test

# Start an interactive Scala REPL with project classes loaded
sbt console

# Continuous compilation (recompiles on file changes)
sbt ~compile

# Clean build artifacts
sbt clean
```

### Getting Help

- **Rust ⟷ Scala Comparisons**: See [docs/rust-scala-comparison.md](docs/rust-scala-comparison.md)
- **Glossary**: See [docs/glossary.md](docs/glossary.md) for Scala/FP terminology
- **Resources**: See [docs/resources.md](docs/resources.md) for additional learning materials

## 🗺️ Rust to Scala Concept Map

| Rust Concept | Scala Equivalent | Notes |
|--------------|------------------|-------|
| `Result<T, E>` | `Either[L, R]` | Right-biased, used for success/failure |
| `Option<T>` | `Option[A]` | Same concept, similar API |
| `async/await` | `IO[A]` (Cats-Effect) | IO provides referential transparency |
| `trait` | `trait` + type classes | Different implementation patterns |
| `impl Trait for Type` | Implicit instances | Type class pattern |
| `Vec<T>`, `HashMap<K,V>` | `List[A]`, `Map[K,V]` | Immutable by default in Scala |
| `Arc<Mutex<T>>` | `Ref[F, A]` | Thread-safe mutable state in IO |
| `spawn`, `join` | `Fiber`, `start`, `join` | Concurrent task execution |
| `Drop` (RAII) | `Resource[F, A]` | Automatic resource management |
| Cargo | sbt | Build tool and dependency manager |
| `#[test]`, `assert!` | ScalaTest specs | Testing frameworks |
| proptest, quickcheck | ScalaCheck | Property-based testing |

## 🤝 Contributing

Found an error or have a suggestion? Contributions are welcome! Please open an issue or submit a pull request.

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

This guide draws inspiration from the excellent Scala and functional programming community, particularly:
- The Cats and Cats-Effect maintainers
- Scala Exercises and other educational resources
- The Rust community's excellent documentation practices

---

**Ready to begin?** Start with [Chapter 01: Setup](chapter-01-setup/) 🚀

> **Note**: If you're already in plan mode, press **Shift+Tab** to exit before starting your learning journey!
