import com.typesafe.sbt.SbtMultiJvm.multiJvmSettings
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm
import sbt.url
import sbt._
import Keys._
import sbtassembly.AssemblyPlugin.autoImport._

val akkaVersion = "2.5.8"

publishMavenStyle := true
releaseEarlyWith in Global := SonatypePublisher
pgpPublicRing := file("./travis/local.pubring.asc")
pgpSecretRing := file("./travis/local.secring.asc")

lazy val appResolvers = Seq(
  "typesafe" at "https://repo.typesafe.com/typesafe/releases/",
  "hydrasonatype" at "https://oss.sonatype.org/content/groups/staging/"
)
lazy val `hydra-cluster-scala` = project
  .in(file("."))
  .settings(multiJvmSettings: _*)
  .settings(
    name := "Hydra",
    version := "0.2.2.1",
    organization := "io.github.wherby",
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
      "com.typesafe.akka" %% "akka-http"   % "10.1.0-RC1",
      "com.typesafe.akka" %% "akka-stream" % akkaVersion, // or whatever the latest version is
      "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.scalatest" %% "scalatest" % "3.0.1" % Test,
      "com.typesafe.play" %% "play-json" % "2.6.6",
      "org.scalaj" %% "scalaj-http" % "2.3.0",
      "io.spray" %%  "spray-json" % "1.3.3", // for json format in akka http
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.0-RC1",
      "io.github.wherby"%%"hydracommon"%"0.1.2",
      "io.kamon" % "sigar-loader" % "1.6.6-rev002"),
    fork in run := false,   //###If the value is true, Ctrl + C may only kill JVM and not kill Akka. Set to false to kill togother.
    mainClass in (Compile, run) := Some("hydra.cluster.app.SimpleClusterApp"),
    mainClass in assembly := Some("hydra.cluster.app.SimpleClusterApp"),//object with,
    // disable parallel tests
    parallelExecution in Test := false,
    resolvers ++= appResolvers,
    licenses := Seq(      "AGPL-V3" -> url("https://www.gnu.org/licenses/agpl-3.0.txt"),
      "Commercial-License"->url("https://github.com/wherby/dora/tree/master/docs/license/CommercialLicense.md")),
    publishArtifact := true,
  )
  .configs (MultiJvm)

assemblyMergeStrategy in assembly := {
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}


publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.contains("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

licenses := Seq("GPL-3.0" -> url("https://opensource.org/licenses/GPL-3.0"))

homepage := Some(url("https://github.com/wherby/Hydra"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/wherby/Hydra.git"),
    "scm:git@github.com:wherby/Hydra.git"
  )
)

developers := List(
  Developer(
    id    = "wherby",
    name  = "Tao Zhou",
    email = "187225577@qq.com",
    url   = url("https://github.com/wherby")
  )
)