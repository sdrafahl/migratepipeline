val scala3Version = "3.0.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "migrationpipeline",
    version := "0.0.2",
    scalaVersion := scala3Version,    
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
    libraryDependencies += "org.typelevel" % "cats-core_2.13" % "2.3.0",
    libraryDependencies += "org.typelevel" %% "cats-effect" % "3.1.1",
    libraryDependencies += "io.circe" %% "circe-core" % "0.14.0-M7",
    libraryDependencies += "io.circe" %% "circe-generic" % "0.14.0-M7"
  )
