val Http4sVersion = "0.18.8"
val LogbackVersion = "1.2.3"

resolvers += "Deepstream" at "https://dl.bintray.com/deepstreamio/maven"

lazy val root = (project in file("."))
  .settings(
    organization := "io.github.ffossum",
    name := "gamesite-scala",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.5",
    libraryDependencies ++= Seq(
      "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "org.scalatest"   %% "scalatest"           % "3.0.5"  % "test",
      "org.scalacheck"  %% "scalacheck"          % "1.13.4" % "test",
      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion,
      "org.mindrot" % "jbcrypt" % "0.4",
      "io.deepstream" % "deepstream.io-client-java" % "2.2.2",
      "org.flywaydb" % "flyway-core" % "5.0.7"
    )
  )

val circeVersion = "0.9.3"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-generic-extras",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

val doobieVersion = "0.5.2"
libraryDependencies ++= Seq(
  "org.tpolecat" %% "doobie-core"      % doobieVersion,
  "org.tpolecat" %% "doobie-postgres"  % doobieVersion
)

val jwtVersion = "0.16.0"
libraryDependencies ++= Seq(
  "com.pauldijou" %% "jwt-core" % jwtVersion,
  "com.pauldijou" %% "jwt-circe" % jwtVersion,
)