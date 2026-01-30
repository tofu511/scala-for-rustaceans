package catseffect

import cats.effect.{IO, IOApp, Resource}
import cats.syntax.apply._

/*
 * RESOURCE MANAGEMENT
 *
 * Problem: How to ensure cleanup happens (files, connections, etc.)?
 * Solutions:
 *   1. bracket: IO.bracket(acquire)(use)(release)
 *   2. Resource[IO, A]: Composable resource management
 *
 * RUST COMPARISON:
 * - bracket ~ manually calling drop/cleanup in defer  
 * - Resource ~ RAII (Drop trait) but explicit
 * - Guarantees cleanup even on error/cancellation
 */

object ResourceDemo extends IOApp.Simple {
  
  def demonstrateBracket(): IO[Unit] = {
    println("\n=== Bracket Pattern ===\n")
    
    def openFile(name: String): IO[String] = IO.delay {
      println(s"  → Opening file: $name")
      name
    }
    
    def closeFile(name: String): IO[Unit] = IO.delay {
      println(s"  ← Closing file: $name")
    }
    
    def readFile(name: String): IO[String] = IO.delay {
      println(s"  📖 Reading from $name")
      if (name.contains("error")) throw new Exception("Read error!")
      s"Contents of $name"
    }
    
    // bracket - acquire.bracket(use)(release) - guarantees release
    val safeRead = openFile("data.txt").bracket(readFile)(closeFile)
    
    val safeReadWithError = openFile("error.txt").bracket(readFile)(closeFile)
    
    for {
      _ <- IO.println("Success case:")
      r1 <- safeRead
      _ <- IO.println(s"  Result: $r1\n")
      
      _ <- IO.println("Error case (file still closes!):")
      r2 <- safeReadWithError.attempt
      _ <- IO.println(s"  Result: $r2")
      
      _ <- IO.println("\nRust comparison:")
      _ <- IO.println("  // RAII with Drop trait")
      _ <- IO.println("  let file = File::open('data.txt')?;")
      _ <- IO.println("  // Automatically closed when file goes out of scope")
    } yield ()
  }
  
  def demonstrateResource(): IO[Unit] = {
    println("\n=== Resource Type ===\n")
    
    case class Connection(id: Int) {
      def query(sql: String): IO[List[String]] = IO.delay {
        println(s"  🔍 Query on connection $id: $sql")
        List("row1", "row2")
      }
    }
    
    val connectionResource: Resource[IO, Connection] = Resource.make(
      IO.delay {
        println("  → Acquiring connection")
        Connection(42)
      }
    )(conn => IO.delay {
      println(s"  ← Releasing connection ${conn.id}")
    })
    
    // use - safely uses resource and guarantees cleanup
    val program = connectionResource.use { conn =>
      conn.query("SELECT * FROM users")
    }
    
    for {
      _ <- IO.println("Using Resource:")
      results <- program
      _ <- IO.println(s"  Results: $results")
      _ <- IO.println("  Note: Connection automatically released!")
    } yield ()
  }
  
  def demonstrateComposition(): IO[Unit] = {
    println("\n=== Composing Resources ===\n")
    
    case class Database(name: String)
    case class Cache(name: String)
    
    val dbResource = Resource.make(
      IO.delay { println("  → DB open"); Database("postgres") }
    )(db => IO.delay(println(s"  ← DB ${db.name} closed")))
    
    val cacheResource = Resource.make(
      IO.delay { println("  → Cache open"); Cache("redis") }
    )(c => IO.delay(println(s"  ← Cache ${c.name} closed")))
    
    // Compose with for-comprehension!
    val composed: Resource[IO, (Database, Cache)] = for {
      db <- dbResource
      cache <- cacheResource
    } yield (db, cache)
    
    composed.use { case (db, cache) =>
      IO.println(s"  Using ${db.name} and ${cache.name}")
    } *> IO.println("  (Both automatically cleaned up in reverse order)")
  }
  
  def run: IO[Unit] = {
    for {
      _ <- IO.println("=" * 60)
      _ <- IO.println("RESOURCE MANAGEMENT")
      _ <- IO.println("=" * 60)
      
      _ <- demonstrateBracket()
      _ <- demonstrateResource()
      _ <- demonstrateComposition()
      
      _ <- IO.println("\n" + "=" * 60)
      _ <- IO.println("KEY TAKEAWAYS")
      _ <- IO.println("=" * 60)
      _ <- IO.println("""
1. bracket(acquire)(use)(release) - guarantees cleanup
2. Resource[IO, A] - composable resource management
3. .use(a => IO[B]) - safely uses resource
4. Cleanup happens even on error/cancellation
5. Resources compose with for-comprehension
6. Released in reverse order of acquisition
7. Rust: Similar to RAII/Drop, but explicit
      """.trim)
    } yield ()
  }
}
