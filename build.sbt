import com.typesafe.sbt.SbtStartScript


// Basic Settings

name := "scala-parser-server"

version := "0.1"

scalaVersion := "2.10.1"

// Dependencies

libraryDependencies ++= Seq(
  "edu.stanford.nlp" % "stanford-parser" % "2.0.5",
  "edu.stanford.nlp" % "stanford-parser" % "2.0.5" classifier "models",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test"
)

resolvers ++= Seq("Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
                  "Sonatype releases"  at "http://oss.sonatype.org/content/repositories/releases")

// Compile Options

scalacOptions += "-deprecation"

scalacOptions += "-feature"

scalacOptions += "-language:postfixOps"

// Start Script Plugin

seq(SbtStartScript.startScriptForJarSettings: _*)
