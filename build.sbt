// For project structure see https://github.com/pbassiner/sbt-multi-project-example
name := "simple-bank-system"

organization in ThisBuild:= "gr.fpas"
scalaVersion in ThisBuild := "2.13.1"

version := "0.1"

lazy val root = project.in(file("."))
  .aggregate(
    backend,
    `transactions-client`,
    `scala-intro`
  )

lazy val backend = project
  .settings(commonSettings)

lazy val `transactions-client` = project
  .settings(commonSettings)

lazy val `scala-intro` = project
  .settings(commonSettings)


lazy val commonSettings = Seq(
  libraryDependencies := dependencies
)


lazy val akkaVersion = "2.6.1"

lazy val dependencies = Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % "10.1.11",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.11",

  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson"  % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion,


  // Persistence
  "com.github.dnvriend" %% "akka-persistence-jdbc" % "3.5.2",
  "org.postgresql" % "postgresql" % "42.2.9",


  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  // Testing
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion% Test,
  "org.scalatest" %% "scalatest" % "3.0.8" % Test,
)
