import com.typesafe.sbt.SbtMultiJvm.multiJvmSettings
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm

val akkaVersion = "2.5.4"

lazy val appResolvers = Seq(
  "typesafe" at "http://repo.typesafe.com/typesafe/releases/",
  "hydrasonatype" at "https://oss.sonatype.org/content/groups/staging/"
)
lazy val `hydra-cluster-scala` = project
  .in(file("."))
  .settings(multiJvmSettings: _*)
  .settings(
    organization := "org.hydra.cluster",
    scalaVersion := "2.12.2",
    scalacOptions in Compile ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint"),
    javacOptions in Compile ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    javaOptions in run ++= Seq("-Xms128m", "-Xmx1024m", "-Djava.library.path=./target/native"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-remote" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
      "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion,
      "com.typesafe.akka" %% "akka-distributed-data" % akkaVersion,
      "org.scalatest" %% "scalatest" % "3.0.1" % Test,
      "com.typesafe.play" %% "play-json" % "2.6.6",
      "org.scalaj" %% "scalaj-http" % "2.3.0",
      "io.github.wherby"%%"hydracommon"%"0.1-SNAPSHOT",
      "io.kamon" % "sigar-loader" % "1.6.6-rev002"),
    fork in run := true,
    mainClass in (Compile, run) := Some("hydra.cluster.simple.SimpleClusterApp"),
    // disable parallel tests
    parallelExecution in Test := false,
    resolvers ++= appResolvers,
    licenses := Seq(("CC0", url("http://creativecommons.org/publicdomain/zero/1.0")))
  )
  .configs (MultiJvm)
