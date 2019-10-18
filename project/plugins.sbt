resolvers := Seq(
  "typesafe" at "http://repo.typesafe.com/typesafe/releases/"
)
addSbtPlugin("com.typesafe.sbt" % "sbt-multi-jvm" % "0.4.0")
addSbtPlugin("com.dwijnand" % "sbt-dynver" % "1.1.1")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0-M1")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
// defined in project/plugins.sbt
addSbtPlugin("ch.epfl.scala" % "sbt-release-early" % "2.1.1")

