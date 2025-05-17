ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.6"
libraryDependencies += "org.postgresql" % "postgresql" % "42.7.2"

lazy val root = (project in file("."))
  .settings(
    name := "scalaProject"
  )
