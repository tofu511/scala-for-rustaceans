name := "chapter-10-doobie"
version := "0.1.0"
scalaVersion := "2.13.13"

libraryDependencies ++= Seq(
  // Cats Effect
  "org.typelevel" %% "cats-effect" % "3.5.4",
  
  // Doobie for database access
  "org.tpolecat" %% "doobie-core" % "1.0.0-RC4",
  "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC4",
  "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC4",
  
  // Flyway for migrations
  "org.flywaydb" % "flyway-core" % "9.22.3",
  "org.flywaydb" % "flyway-database-postgresql" % "10.4.1",
  
  // PostgreSQL driver
  "org.postgresql" % "postgresql" % "42.7.1",
  
  // http4s for web server (to show integration)
  "org.http4s" %% "http4s-dsl" % "0.23.25",
  "org.http4s" %% "http4s-ember-server" % "0.23.25",
  "org.http4s" %% "http4s-circe" % "0.23.25",
  
  // Circe for JSON
  "io.circe" %% "circe-generic" % "0.14.6",
  
  // Testing
  "org.scalatest" %% "scalatest" % "3.2.17" % Test
)

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-language:higherKinds"
)
