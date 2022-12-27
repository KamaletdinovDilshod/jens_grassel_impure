ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

val akkaHttpJsonSerializersVersion = "1.39.2"
val circeVersion = "0.14.3"
val AkkaVersion = "2.7.0"
val akkaHttpVersion = "10.4.0"
val Http4sVersion = "1.0.0-M21"
val Fs2Version = "3.4.0"
val DoobieVersion = "1.0.0-RC1"
val NewTypeVersion = "0.4.4"

lazy val root = (project in file("."))
  .settings(
    name := "jens_grassel_impure",
    libraryDependencies ++= Seq(
      "org.typelevel"             %% "cats-core"                      % "2.9.0",
      "org.typelevel"             %% "cats-effect"                    % "3.4.1",
      "org.typelevel"             %% "kittens"                        % "3.0.0",
      "io.circe"                  %% "circe-core"                     % circeVersion,
      "io.circe"                  %% "circe-generic"                  % circeVersion,
      "io.circe"                  %% "circe-refined"                  % circeVersion,
      "io.circe"                  %% "circe-parser"                   % circeVersion,
      "eu.timepit"                %% "refined"                        % "0.10.1",
      "eu.timepit"                %% "refined-cats"                   % "0.10.1",
      "eu.timepit"                %% "refined-pureconfig"             % "0.10.1",
      "com.typesafe.slick"        %% "slick"                          % "3.3.3",
      "org.postgresql"             % "postgresql"                     % "42.5.1",
      "com.typesafe.slick"        %% "slick-hikaricp"                 % "3.3.3",
      "com.github.tminglei"       %% "slick-pg"                       % "0.20.3",
      "com.github.tminglei"       %% "slick-pg_play-json"             % "0.20.3",
      "org.flywaydb"               % "flyway-core"                    % "6.0.1",
      "com.typesafe.akka"         %% "akka-actor-typed"               % AkkaVersion,
      "com.typesafe.akka"         %% "akka-stream"                    % AkkaVersion,
      //akka http
      "com.typesafe.akka"         %% "akka-http"                      % akkaHttpVersion,
      "com.typesafe.akka"         %% "akka-http-spray-json"           % akkaHttpVersion,
      "de.heikoseeberger"         %% "akka-http-circe"                % akkaHttpJsonSerializersVersion,
      // Akka testKit
      "com.typesafe.akka"         %% "akka-testkit"                   % AkkaVersion % Test,
      // Pure config
      "com.github.pureconfig"     %% "pureconfig"                     % "0.17.2",
      "co.fs2"                    %% "fs2-core"                       % Fs2Version,
      "org.tpolecat"              %% "doobie-core"                    % DoobieVersion,
      "org.tpolecat"              %% "doobie-postgres"                % DoobieVersion,
      "org.tpolecat"              %% "doobie-hikari"                  % DoobieVersion,
      "org.tpolecat"              %% "doobie-refined"                 % DoobieVersion,
      "io.estatico"               %% "newtype"                        % NewTypeVersion,
      "org.http4s"                %% "http4s-blaze-server"            % Http4sVersion,
      "org.http4s"                %% "http4s-blaze-client"            % Http4sVersion,
      "org.http4s"                %% "http4s-circe"                   % Http4sVersion,
      "org.http4s"                %% "http4s-dsl"                     % Http4sVersion,
      // ScalaCheck
      "org.scalacheck"            %% "scalacheck"                     % "1.14.1"        % "test",
      "org.scalatest"             %% "scalatest"                      % "3.0.8",


    )
  )
