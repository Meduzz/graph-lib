name := "Graph"

version := "20161217"

scalaVersion := "2.11.7"

organization := "se.kodiak.tools"

credentials += Credentials(Path.userHome / ".ivy2" / ".tools")

publishTo := Some("se.kodiak.tools" at "http://yamr.kodiak.se/maven")

publishArtifact in (Compile, packageSrc) := false

publishArtifact in (Compile, packageDoc) := false

resolvers += "rediscala" at "http://dl.bintray.com/etaty/maven"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

libraryDependencies += "com.etaty.rediscala" %% "rediscala" % "1.4.0"
