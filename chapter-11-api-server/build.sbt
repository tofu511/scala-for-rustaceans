name := "chapter-11-api-server"
version := "0.1.0"
scalaVersion := "2.13.13"

libraryDependencies ++= Seq(
  // Cats Effect
  "org.typelevel" %% "cats-effect" % "3.5.4",
  
  // http4s
  "org.http4s" %% "http4s-dsl" % "0.23.25",
  "org.http4s" %% "http4s-ember-server" % "0.23.25",
  "org.http4s" %% "http4s-circe" % "0.23.25",
  
  // Circe for JSON
  "io.circe" %% "circe-generic" % "0.14.6",
  "io.circe" %% "circe-parser" % "0.14.6",
  "io.circe" %% "circe-config" % "0.10.1",
  
  // Doobie for database
  "org.tpolecat" %% "doobie-core" % "1.0.0-RC4",
  "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC4",
  "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC4",
  
  // Flyway for migrations
  "org.flywaydb" % "flyway-core" % "9.22.3",
  "org.flywaydb" % "flyway-database-postgresql" % "10.4.1",
  
  // PostgreSQL driver
  "org.postgresql" % "postgresql" % "42.7.1",
  
  // Logging
  "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
  "ch.qos.logback" % "logback-classic" % "1.4.14",
  
  // Testing
  "org.scalatest" %% "scalatest" % "3.2.17" % Test,
  "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0" % Test,
  "org.tpolecat" %% "doobie-scalatest" % "1.0.0-RC4" % Test,
  "org.tpolecat" %% "doobie-h2" % "1.0.0-RC4" % Test,
  "com.h2database" % "h2" % "2.2.224" % Test,
  "org.http4s" %% "http4s-client" % "0.23.25" % Test
)

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-language:higherKinds"
)
