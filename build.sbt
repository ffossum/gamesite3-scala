val Http4sVersion = "0.18.8"
val LogbackVersion = "1.2.3"

lazy val root = (project in file("."))
  .settings(
    organization := "no.ffossum",
    name := "gamesite-scala",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.5",
    libraryDependencies ++= Seq(
      "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "org.scalatest"   %% "scalatest"           % "3.0.5" % "test",
      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion
    )
  )

