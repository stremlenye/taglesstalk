
// Workaround for missing certificates.
val bypassTypeSafeProxy = Map(
  Resolver.sbtPluginRepo("releases") -> Resolver.bintrayIvyRepo("sbt", "sbt-plugin-releases"),
  Resolver.typesafeRepo("releases") -> Resolver.bintrayRepo("typesafe", "maven-releases"),
  Resolver.typesafeIvyRepo("releases") -> Resolver.bintrayIvyRepo("typesafe", "ivy-releases"),
).withDefault(identity)

fullResolvers := fullResolvers.value.map(bypassTypeSafeProxy)
libraryDependencies += "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.210"

addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.4.3")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.7.0")
addSbtPlugin("org.wartremover" %% "sbt-wartremover" % "2.4.1")
addSbtPlugin("io.github.davidmweber" % "flyway-sbt" % "5.0.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.4")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.2")
//the plugin below doesn't respect maxErrors (perhaps only since 1.0.x for some or all xes)
//addSbtPlugin("org.duhemm" % "sbt-errors-summary" % "0.6.0")
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.1.0-M9")
addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "0.2.2")
addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.5.1")
addSbtPlugin("com.github.scalaprops" % "sbt-scalaprops" % "0.2.6")
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.3.4")
addSbtPlugin("org.spire-math" % "sbt-javap" % "0.0.1")
