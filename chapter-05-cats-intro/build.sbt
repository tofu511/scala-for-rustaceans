name := "scala-cats-intro"
version := "0.1.0"
scalaVersion := "2.13.13"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.10.0",
  "org.scalatest" %% "scalatest" % "3.2.17" % Test
)

scalacOptions ++= Seq(
  "-language:higherKinds",
  "-Xfatal-warnings"
)
