name := "chapter-06-cats-error-handling"

version := "0.1.0"

scalaVersion := "2.13.13"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.10.0",
  "org.scalatest" %% "scalatest" % "3.2.17" % Test
)

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-language:higherKinds",
  "-Xfatal-warnings"
)
