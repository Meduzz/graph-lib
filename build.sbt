name := "Graph"

version := "20161228"

scalaVersion := "2.11.7"

organization := "se.kodiak.tools"

credentials += Credentials(Path.userHome / ".ivy2" / ".tools")

publishTo := Some("se.kodiak.tools" at "http://yamr.kodiak.se/maven")

publishArtifact in (Compile, packageDoc) := false

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"
