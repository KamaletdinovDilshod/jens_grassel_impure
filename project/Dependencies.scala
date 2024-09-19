import sbt.*

object Dependencies {

  object Versions {
    val akka                    = "2.8.6"
    val akkaHttpJsonSerializers = "1.39.2"
    val circe                   = "0.14.9"
    val akkaHttp                = "10.5.3"
    val pureConfig              = "0.17.7"
    val refinedCore             = "0.11.1"
    val refined                 = "0.11.2"
    val slick                   = "3.5.0"
    val slickHikari             = "3.5.1"
    val postgresql              = "42.7.3"
    val slickPg                 = "0.22.2"
    val flyway                  = "10.15.2"
    val scalacheck              = "1.18.1"
    val scalatest               = "3.2.19"
  }

  object Libraries {
    def circe(artifact: String): ModuleID                 = "io.circe"          %% artifact          % Versions.circe
    def akka(artifact: String, version: String): ModuleID = "com.typesafe.akka" %% s"akka-$artifact" % version

    val akkaActorTyped    = akka("actor-typed", Versions.akka)
    val akkaStream        = akka("stream", Versions.akka)
    val akkaTestKit       = akka("testkit", Versions.akka) % Test
    val akkaHttp          = akka("http", Versions.akkaHttp)
    val akkaHttpSprayJson = akka("http-spray-json", Versions.akkaHttp)

    val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % Versions.akkaHttpJsonSerializers

    val circeCore    = circe("circe-core")
    val circeGeneric = circe("circe-generic")
    val circeParser  = circe("circe-parser")
    val circeRefined = circe("circe-refined")

    val refinedCore = "eu.timepit" %% "refined"      % Versions.refinedCore
    val refinedCats = "eu.timepit" %% "refined-cats" % Versions.refined

    val slick           = "com.typesafe.slick"  %% "slick"              % Versions.slick
    val slickHikaricp   = "com.typesafe.slick"  %% "slick-hikaricp"     % Versions.slickHikari
    val slickPg         = "com.github.tminglei" %% "slick-pg"           % Versions.slickPg
    val slickPgPlayJson = "com.github.tminglei" %% "slick-pg_play-json" % Versions.slickPg

    val postgresql = "org.postgresql" % "postgresql" % Versions.postgresql

    val flyway         = "org.flywaydb" % "flyway-core"                % "10.15.2"
    val flywayPostgres = "org.flywaydb" % "flyway-database-postgresql" % "10.15.2"

    val logback = "ch.qos.logback" % "logback-classic" % "1.5.6" % Runtime
    val slf4j   = "org.slf4j"      % "slf4j-api"       % "2.0.12"

    val scalacheck    = "org.scalacheck"    %% "scalacheck"      % Versions.scalacheck % "test"
    val scalatest     = "org.scalatest"     %% "scalatest"       % Versions.scalatest
    val scalatestPlus = "org.scalatestplus" %% "scalacheck-1-17" % "3.2.18.0"          % Test
  }

  lazy val rootDependencies: Seq[ModuleID] = Seq(
    Libraries.akkaActorTyped,
    Libraries.akkaStream,
    Libraries.akkaTestKit,
    Libraries.akkaHttp,
    Libraries.akkaHttpSprayJson,
    Libraries.akkaHttpCirce,
    Libraries.circeCore,
    Libraries.circeGeneric,
    Libraries.circeParser,
    Libraries.circeRefined,
    Libraries.refinedCore,
    Libraries.refinedCats,
    Libraries.slick,
    Libraries.slickHikaricp,
    Libraries.slickPg,
    Libraries.slickPgPlayJson,
    Libraries.postgresql,
    Libraries.flyway,
    Libraries.scalacheck,
    Libraries.scalatest,
    Libraries.scalatestPlus,
    Libraries.flywayPostgres,
    Libraries.logback,
    Libraries.slf4j
  )

  val integrationDependencies: Seq[ModuleID] = Seq(
    Libraries.scalacheck,
    Libraries.scalatest,
    Libraries.scalatestPlus
  )

}
