name := "AkkaEventHubsConsumer"

fork := true
javaOptions in test ++= Seq(
  "-Xms512M", "-Xmx2048M",
  "-XX:MaxPermSize=2048M",
  "-XX:+CMSClassUnloadingEnabled"
)

parallelExecution in test := false

version := "1.0"

scalaVersion := "2.12.3"
val akkaVersion = "2.5.4"
val akkaHttpVersion = "10.0.9"

libraryDependencies ++=
  Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "com.typesafe" % "config" % "1.2.1",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",

    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,

    "org.json4s" %% "json4s-native" % "3.5.3",
    "com.github.nscala-time" %% "nscala-time" % "2.16.0",

    "org.scalatest" %% "scalatest" % "3.0.1" % "test",

    // msft TMP deps until a jar is pulished to maven
    "com.microsoft.azure" % "azure-eventhubs" % "0.13.0",
    "com.microsoft.azure" % "azure-storage" % "5.0.0",
    "com.datastax.cassandra" % "cassandra-driver-core" % "3.1.4",
    "org.json4s" %% "json4s-jackson" % "3.5.3"
  )

dependencyOverrides ++= Set(
  "com.typesafe.akka" %% "akka-actor"  % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion
)

mainClass in assembly := Some("onextent.akka.eventhubs.consumer.Main")
assemblyJarName in assembly := "AkkaEventHubsConsumer.jar"

assemblyMergeStrategy in assembly := {
  case x if x.endsWith("io.netty.versions.properties") => MergeStrategy.first
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

