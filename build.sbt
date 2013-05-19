name := "scala-parser-server"

version := "0.1"

libraryDependencies ++= Seq(
  "edu.stanford.nlp" % "stanford-parser" % "2.0.5",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test"
)

resolvers ++= Seq("Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
                  "Sonatype releases"  at "http://oss.sonatype.org/content/repositories/releases")

scalacOptions += "-deprecation"
