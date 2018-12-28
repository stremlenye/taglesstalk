import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.7",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "taglesstalk",
    libraryDependencies += scalaTest % Test,
    libraryDependencies +=  "org.scalaz" %% "scalaz-core" % "7.2.27",
    libraryDependencies +=  "org.scalaz" %% "scalaz-concurrent" % "7.2.27"
)
