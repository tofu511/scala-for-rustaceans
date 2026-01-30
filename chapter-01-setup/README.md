# Chapter 01: Setup and Getting Started

Welcome to your Scala journey! This chapter will get you set up with the Scala toolchain and introduce you to the basics of sbt (Scala Build Tool).

## Learning Objectives

By the end of this chapter, you will be able to:
- Install and verify Scala, Java, and sbt
- Understand basic Scala project structure
- Use essential sbt commands
- Write and run your first Scala program
- Use the Scala REPL for experimentation

## Prerequisites

You should have already installed the tools via SDKMAN. Verify your installation:

```bash
java -version   # Should show Java 17
scala -version  # Should show Scala 2.13.13
sbt --version   # Should show sbt 1.9.9
```

If not installed yet, follow the installation instructions in the [main README](../README.md#installation).

### Quick Install with SDKMAN

```bash
# Install SDKMAN if not already installed
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Java, Scala, and sbt
sdk install java 17.0.10-tem
sdk install scala 2.13.13
sdk install sbt 1.9.9
```

## Scala Project Structure

A typical Scala project structure looks like this:

```
project-name/
├── build.sbt              # Build configuration (like Cargo.toml)
├── project/               # sbt plugins and build configuration
│   └── build.properties   # sbt version
├── src/
│   ├── main/
│   │   ├── scala/         # Your Scala source code
│   │   └── resources/     # Configuration files, assets
│   └── test/
│       └── scala/         # Test code
└── target/                # Build output (like target/ in Rust)
```

## Comparison with Rust

| Aspect | Rust (Cargo) | Scala (sbt) |
|--------|--------------|-------------|
| Build file | `Cargo.toml` | `build.sbt` |
| Source code | `src/*.rs` | `src/main/scala/*.scala` |
| Test code | `#[test]` in same file or `tests/` | `src/test/scala/` |
| Build output | `target/` | `target/` |
| REPL | None (third-party) | `scala` or `sbt console` |
| Compile | `cargo build` | `sbt compile` |
| Run | `cargo run` | `sbt run` |
| Test | `cargo test` | `sbt test` |

## sbt Commands Reference

### Essential Commands

```bash
# Compile the project
sbt compile

# Run the main application (if Main is defined)
sbt run

# Run tests
sbt test

# Clean build artifacts
sbt clean

# Start interactive sbt shell (faster for multiple commands)
sbt
# Then inside sbt:
> compile
> run
> test
```

### Advanced Commands

```bash
# Continuous compilation (recompile on file save)
sbt ~compile

# Continuous testing
sbt ~test

# Start Scala REPL with project classes loaded
sbt console

# Show dependency tree
sbt dependencyTree

# Format code (requires scalafmt plugin)
sbt scalafmt

# Show available tasks
sbt tasks
```

### sbt Shell vs Command Line

You can run sbt in two ways:

1. **One-off commands**: `sbt compile` (slower, starts JVM each time)
2. **Interactive shell**: `sbt` then `compile` (faster, keeps JVM warm)

For development, use the interactive shell!

## Your First Scala Program

Let's create a simple "Hello, World!" program. This directory contains a working example.

### Project Structure

```
chapter-01-setup/hello-world/
├── build.sbt
└── src/
    └── main/
        └── scala/
            └── HelloWorld.scala
```

### The Code

See [`hello-world/src/main/scala/HelloWorld.scala`](hello-world/src/main/scala/HelloWorld.scala):

```scala
object HelloWorld {
  def main(args: Array[String]): Unit = {
    println("Hello, Rustacean! Welcome to Scala!")
    
    // Variables
    val immutable = "Cannot change"      // like: let x = ...
    var mutable = "Can change"           // like: let mut x = ...
    
    println(s"Immutable: $immutable")
    println(s"Mutable: $mutable")
    
    mutable = "Changed!"
    println(s"Mutable after change: $mutable")
    
    // Collections
    val numbers = List(1, 2, 3, 4, 5)
    val doubled = numbers.map(_ * 2)
    println(s"Doubled: $doubled")
  }
}
```

### Rust Comparison

```rust
// Rust equivalent
fn main() {
    println!("Hello, Rustacean! Welcome to Scala!");
    
    // Variables
    let immutable = "Cannot change";      
    let mut mutable = "Can change";       
    
    println!("Immutable: {}", immutable);
    println!("Mutable: {}", mutable);
    
    mutable = "Changed!";
    println!("Mutable after change: {}", mutable);
    
    // Collections
    let numbers = vec![1, 2, 3, 4, 5];
    let doubled: Vec<i32> = numbers.iter().map(|x| x * 2).collect();
    println!("Doubled: {:?}", doubled);
}
```

### Running the Program

```bash
cd hello-world
sbt run
```

You should see:
```
Hello, Rustacean! Welcome to Scala!
Immutable: Cannot change
Mutable: Can change
Mutable after change: Changed!
Doubled: List(2, 4, 6, 8, 10)
```

## Using the Scala REPL

The REPL (Read-Eval-Print Loop) is great for experimentation:

```bash
# Start basic Scala REPL
scala

# Or start REPL with your project loaded
cd hello-world
sbt console
```

Try these in the REPL:

```scala
scala> val x = 42
val x: Int = 42

scala> x * 2
val res0: Int = 84

scala> val list = List(1, 2, 3)
val list: List[Int] = List(1, 2, 3)

scala> list.map(_ + 1)
val res1: List[Int] = List(2, 3, 4)

scala> :type list
List[Int]

scala> :help
// Shows available commands

scala> :quit
// Exit REPL
```

## Key Differences from Rust

### 1. Object vs fn main
- **Rust**: `fn main()` is the entry point
- **Scala**: `object` with `def main(args: Array[String]): Unit`
- **Note**: Scala 3 allows simpler `@main def` syntax, but we're using Scala 2.13

### 2. String Interpolation
- **Rust**: `println!("Value: {}", x)` with format macros
- **Scala**: `println(s"Value: $x")` with `s` prefix (s-interpolation)

### 3. Collections
- **Rust**: `Vec<T>` (owned, resizable), `&[T]` (borrowed slice)
- **Scala**: `List[A]` (immutable linked list), `Vector[A]` (immutable indexed), `Array[A]` (mutable)

### 4. Type Inference
- **Rust**: `let x = 42;` (type inferred)
- **Scala**: `val x = 42` (type inferred)
- **Both**: Can explicitly annotate: `val x: Int = 42`, `let x: i32 = 42;`

### 5. Lambda Syntax
- **Rust**: `|x| x * 2`
- **Scala**: `x => x * 2` or `_ * 2` (underscore for single-use parameters)

## Exercises

### Exercise 1: Modify Hello World
Edit `hello-world/src/main/scala/HelloWorld.scala` to:
1. Add your name to the greeting
2. Create a list of your favorite programming languages
3. Use `filter` to show only languages with more than 4 characters

### Exercise 2: sbt Practice
1. Start sbt in interactive mode
2. Run `compile`, `run`, `clean`, `compile` again
3. Try `~compile` and save the file to see automatic recompilation
4. Use `console` to experiment with Scala code

### Exercise 3: REPL Exploration
In the Scala REPL, try:
1. Create a `Map[String, Int]` with some key-value pairs
2. Use `for-comprehension` to iterate over it
3. Use `:type` to check types of expressions
4. Try defining a simple function

<details>
<summary>💡 Hint for Exercise 3</summary>

```scala
val ages = Map("Alice" -> 30, "Bob" -> 25)

for {
  (name, age) <- ages
} println(s"$name is $age years old")

def greet(name: String): String = s"Hello, $name!"
greet("Rustacean")
```
</details>

## Common Issues and Solutions

### Issue: "No main class detected" when running `sbt run`
**Solution**: Make sure you're in the correct directory! You need to be inside the `hello-world` directory, not `chapter-01-setup`:

```bash
# Wrong - will fail
cd chapter-01-setup
sbt run

# Correct - will work
cd chapter-01-setup/hello-world
sbt run
```

Each Scala project needs its own `build.sbt` file. The `sbt` command looks for `build.sbt` in the current directory.

### Issue: "Not a valid command: compile"
**Solution**: You're not in a directory with `build.sbt`. Navigate to a project directory (e.g., `hello-world/`) that contains `build.sbt`.

### Issue: "sdk: command not found"
**Solution**: Install SDKMAN first: `curl -s "https://get.sdkman.io" | bash`

### Issue: "java: command not found"
**Solution**: Install Java via SDKMAN: `sdk install java 17.0.10-tem`

### Issue: "sbt: command not found"
**Solution**: Install sbt via SDKMAN: `sdk install sbt 1.9.9`

### Issue: Slow sbt startup
**Solution**: Use interactive sbt shell (`sbt` then commands) instead of one-off commands

## Next Steps

Now that you have Scala set up and can run programs, you're ready to dive into Scala fundamentals in [Chapter 02](../chapter-02-fundamentals/).

## Additional Resources

- [sbt Documentation](https://www.scala-sbt.org/documentation.html)
- [Scala REPL Guide](https://docs.scala-lang.org/overviews/repl/overview.html)
- [Getting Started with Scala](https://docs.scala-lang.org/getting-started/)

---

**Time to complete**: ~30 minutes  
**Next chapter**: [Chapter 02: Scala Fundamentals](../chapter-02-fundamentals/)
