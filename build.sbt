import Dependencies.*
import sbt.ThisBuild

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.14"

val akkaHttpJsonSerializersVersion = "1.39.2"
val circeVersion                   = "0.14.3"
val AkkaVersion                    = "2.7.0"
val akkaHttpVersion                = "10.4.0"
val Http4sVersion                  = "1.0.0-M21"
val Fs2Version                     = "3.4.0"
val DoobieVersion                  = "1.0.0-RC1"
val NewTypeVersion                 = "0.4.4"

lazy val root = (project in file("."))
  .settings(
    name := "jens_grassel_impure",
    testFrameworks += new TestFramework("weaver.framework.CatsEffect")
  )
  .aggregate(
    impure,
    integration
  )
  .settings(
    addCommandAlias("run", "impure/run")
  )

lazy val integration = (project in file("integration"))
  .settings(
    publish / skip           := true,
    Test / parallelExecution := false,
    libraryDependencies ++= integrationDependencies
  )
  .dependsOn(
    impure % "test->test"
  )

lazy val impure = (project in file("impure"))
  .settings(
    name                 := "impure",
    libraryDependencies ++= rootDependencies,
    Compile / run / fork := true
  )
