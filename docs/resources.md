# Additional Learning Resources

Curated resources for Rust developers learning Scala, Cats, and Cats-Effect.

## Official Documentation

### Scala
- [Scala Documentation](https://docs.scala-lang.org/) - Official Scala docs
- [Scala Book](https://docs.scala-lang.org/scala3/book/introduction.html) - Comprehensive introduction
- [Scala Standard Library API](https://www.scala-lang.org/api/current/) - API reference
- [Tour of Scala](https://docs.scala-lang.org/tour/tour-of-scala.html) - Quick language overview

### sbt
- [sbt Documentation](https://www.scala-sbt.org/documentation.html) - Official sbt docs
- [sbt by Example](https://www.scala-sbt.org/1.x/docs/sbt-by-example.html) - Hands-on tutorial

### Cats
- [Cats Documentation](https://typelevel.org/cats/) - Official docs
- [Cats API Docs](https://typelevel.org/cats/api/) - API reference
- [Herding Cats](http://eed3si9n.com/herding-cats/) - Cats tutorial series

### Cats-Effect
- [Cats-Effect Documentation](https://typelevel.org/cats-effect/) - Official docs
- [Cats-Effect Tutorial](https://typelevel.org/cats-effect/docs/tutorial) - Getting started
- [Cats-Effect API Docs](https://typelevel.org/cats-effect/api/) - API reference

### http4s
- [http4s Documentation](https://http4s.org/) - Official docs
- [http4s Quickstart](https://http4s.org/v0.23/quickstart/) - Getting started

### Doobie
- [Doobie Documentation](https://tpolecat.github.io/doobie/) - Official docs
- [Doobie Book of Knowledge](https://tpolecat.github.io/doobie/docs/01-Introduction.html) - Comprehensive guide

## Books

### For Scala Beginners
- **[Programming in Scala](https://www.artima.com/shop/programming_in_scala_5ed)** by Martin Odersky et al.
  - Comprehensive Scala introduction by the language creator
  - Good for understanding language fundamentals

- **[Scala with Cats](https://www.scalawithcats.com/)** by Noel Welsh and Dave Gurnell
  - FREE online book
  - Perfect for Rust developers learning functional programming
  - Covers Cats type classes in depth

### For Functional Programming
- **[Functional Programming in Scala](https://www.manning.com/books/functional-programming-in-scala-second-edition)** by Michael Pilquist et al.
  - The "Red Book" - classic FP text
  - Deep dive into FP concepts
  - Can be challenging but very rewarding

- **[Essential Scala](https://underscore.io/books/essential-scala/)** by Noel Welsh and Dave Gurnell
  - FREE online book
  - Teaches Scala through practical exercises
  - Good for beginners

### For Cats-Effect
- **[Practical FP in Scala](https://leanpub.com/pfp-scala)** by Gabriel Volpe
  - Building real-world applications with Cats-Effect, http4s, and more
  - Practical, production-ready patterns

## Interactive Learning

### Exercises and Tutorials
- [Scala Exercises](https://www.scala-exercises.org/) - Interactive exercises for Scala, Cats, and more
- [Tour of Scala](https://tourofscala.com/) - Interactive Scala tutorial
- [Cats Exercises](https://www.scala-exercises.org/cats) - Hands-on Cats practice

### Video Courses
- [Rock the JVM](https://rockthejvm.com/) - Comprehensive Scala courses
  - Scala for Beginners
  - Advanced Scala
  - Cats and Cats-Effect courses
  
- [Functional Programming in Scala Specialization](https://www.coursera.org/specializations/scala) (Coursera)
  - By Martin Odersky (Scala creator)
  - FREE to audit

## Articles and Blog Posts

### Transitioning from Rust
- [Rust vs Scala](https://www.becomebetterprogrammer.com/rust-vs-scala/) - Language comparison
- [From Rust to Scala](https://medium.com/@jkpl/from-rust-to-scala-type-classes-4e5bb4f09c8d) - Type classes comparison

### Scala Fundamentals
- [Scala Best Practices](https://github.com/alexandru/scala-best-practices) - Community-driven best practices
- [Scala School by Twitter](https://twitter.github.io/scala_school/) - Practical Scala lessons

### Cats and Functional Programming
- [Cats Infographic](https://github.com/tpolecat/cats-infographic) - Visual guide to Cats type classes
- [Scala Type Classes](https://scalac.io/blog/typeclasses-in-scala/) - Deep dive into type classes
- [Why Referential Transparency Matters](https://www.inner-product.com/posts/referential-transparency/) - RT explained

### Cats-Effect
- [Intro to Cats-Effect](https://typelevel.org/blog/2018/10/06/intro-to-cats-effect.html) - Getting started
- [Fibers in Cats-Effect](https://typelevel.org/blog/2021/02/21/fibers-fast-mkay.html) - Understanding fibers
- [Resource Management](https://typelevel.org/cats-effect/docs/std/resource) - Safe resource handling

### Database and Web
- [http4s Tutorial](https://http4s.org/v0.23/docs/quickstart.html) - Building APIs
- [Doobie vs other DB libraries](https://softwaremill.com/comparing-scala-database-libraries/) - Choosing a DB library

## Community

### Forums and Chat
- [Scala Users Discourse](https://users.scala-lang.org/) - Scala community forum
- [Typelevel Discord](https://discord.gg/XF3CXcMzqD) - Cats, Cats-Effect, http4s, and more
- [Reddit r/scala](https://www.reddit.com/r/scala/) - Scala subreddit

### Conferences (Recordings Available)
- [Scala Days](https://www.youtube.com/c/ScalaDays) - Annual Scala conference
- [Typelevel Summit](https://www.youtube.com/@typelevel_org) - Functional programming conference
- [Scala Love](https://www.youtube.com/@ScalaLove) - Scala conference talks

## Tool Recommendations

### IDEs and Editors
- **IntelliJ IDEA** with Scala plugin
  - Most popular IDE for Scala
  - Excellent code completion and refactoring
  - [Download](https://www.jetbrains.com/idea/)

- **Visual Studio Code** with Metals
  - Lightweight alternative
  - Good Scala support via Metals language server
  - [Metals Setup Guide](https://scalameta.org/metals/docs/editors/vscode.html)

- **Vim/Neovim** with Metals
  - For terminal enthusiasts
  - [Setup Guide](https://scalameta.org/metals/docs/editors/vim.html)

### Build Tools
- **sbt** - Standard Scala build tool (used in this course)
- **Mill** - Alternative build tool, simpler than sbt
- **Gradle** - Can also build Scala projects

### Other Tools
- **scalafmt** - Code formatter (like `rustfmt`)
- **scalafix** - Linting and refactoring tool
- **Ammonite** - Enhanced Scala REPL
- **sbt-revolver** - Automatic recompilation for development

## Comparison Resources

### Coming from Other Languages
- [Scala for Java Developers](https://docs.scala-lang.org/tutorials/scala-for-java-programmers.html)
- [Scala for Python Programmers](https://github.com/scoverage/scoverage-maven-plugin/wiki/Scala-for-Python-Programmers)
- [Scala for Haskell Developers](https://github.com/jdegoes/scalaworld-2015) - Conference talk

### Type Systems
- [Comparing Rust and Scala Type Systems](https://depth-first.com/articles/2020/01/27/rust-and-the-case-for-memory-safety/)
- [Higher-Kinded Types Explained](https://www.atlassian.com/blog/archives/scala-types-of-a-higher-kind)

## Ecosystem Libraries

### Worth Knowing About
- **circe** - JSON library (like serde_json)
- **fs2** - Streaming library (like tokio streams)
- **refined** - Refinement types (compile-time validation)
- **log4cats** - Logging for Cats-Effect
- **http4s-blaze** - HTTP server/client
- **pureconfig** - Configuration loading
- **tapir** - Type-safe API definitions

## Cheat Sheets

- [Scala Cheat Sheet](https://docs.scala-lang.org/cheatsheets/) - Quick syntax reference
- [Cats Cheat Sheet](https://arosien.github.io/cats-cheatsheet/) - Cats type class hierarchy
- [sbt Cheat Sheet](https://github.com/limansky/sbt-cheat-sheet) - Common sbt commands

## Practice Projects

### Ideas for After This Course
1. **CLI Tool** - Build a command-line app with decline library
2. **REST API** - Extend the final project with more features
3. **GraphQL Server** - Use Caliban or Sangria
4. **Streaming Pipeline** - Process data with fs2
5. **gRPC Service** - Use fs2-grpc
6. **Event Sourcing** - Build with cats-effect and event store

### Open Source Contributions
Contributing to open source is a great way to learn:
- [Cats](https://github.com/typelevel/cats) - Good first issues available
- [Cats-Effect](https://github.com/typelevel/cats-effect) - Documentation improvements
- [http4s](https://github.com/http4s/http4s) - Examples and docs
- [Doobie](https://github.com/tpolecat/doobie) - Documentation and examples

## Stay Updated

### Blogs to Follow
- [Typelevel Blog](https://typelevel.org/blog/)
- [Scala Times](https://scalatimes.com/) - Weekly newsletter
- [This Week in Scala](https://medium.com/disney-streaming/tagged/thisweekinscala)

### Twitter/X Accounts
- [@typelevel](https://twitter.com/typelevel) - Typelevel organization
- [@scala_lang](https://twitter.com/scala_lang) - Official Scala
- [@odersky](https://twitter.com/odersky) - Martin Odersky (Scala creator)

---

## Tips for Learning

1. **Don't try to learn everything at once** - Focus on one concept at a time
2. **Write code, don't just read** - Hands-on practice is essential
3. **Use the REPL** - Quick feedback loop with `sbt console`
4. **Read library source code** - Cats and Cats-Effect have excellent code
5. **Join the community** - Ask questions on Discord/Discourse
6. **Compare with Rust** - Use your existing knowledge as foundation
7. **Be patient with the type system** - It's powerful but takes time to master

**Remember**: You already know many concepts from Rust. You're not starting from scratch! 🚀
