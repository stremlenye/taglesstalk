import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.7",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "taglesstalk",
    scalacOptions += "-Ypartial-unification",
    resolvers += Resolver.sonatypeRepo("releases"),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9"),
    libraryDependencies += scalaTest % Test,
    libraryDependencies +=  "org.scalaz" %% "scalaz-core" % "7.2.27",
    libraryDependencies +=  "org.scalaz" %% "scalaz-concurrent" % "7.2.27",
    libraryDependencies += "org.typelevel" %% "cats-core" % "1.6.0",
    libraryDependencies += "org.typelevel" %% "cats-mtl-core" % "0.4.0"
)
