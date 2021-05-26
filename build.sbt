val scala3Version = "3.0.0-RC3"

githubOwner := "sdrafahl"
githubRepository := "migratepipeline"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala3-simple",
    version := "0.0.1",
    scalaVersion := scala3Version,    
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
    libraryDependencies += "org.typelevel" %% "cats-core" % "2.6.0",
    libraryDependencies += "org.typelevel" %% "cats-effect" % "3.1.0",
    libraryDependencies += "io.circe" %% "circe-core" % "0.14.0-M6",
    libraryDependencies += "io.circe" %% "circe-generic" % "0.14.0-M6"
  )
