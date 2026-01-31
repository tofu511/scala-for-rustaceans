name := "chapter-09-http4s"
version := "0.1.0"
scalaVersion := "2.13.13"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.5.4",
  "org.http4s" %% "http4s-dsl" % "0.23.25",
  "org.http4s" %% "http4s-ember-server" % "0.23.25",
  "org.http4s" %% "http4s-ember-client" % "0.23.25",
  "org.http4s" %% "http4s-circe" % "0.23.25",
  "io.circe" %% "circe-generic" % "0.14.6",
  "io.circe" %% "circe-parser" % "0.14.6",
  "org.scalatest" %% "scalatest" % "3.2.17" % Test
)

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-language:higherKinds"
)
