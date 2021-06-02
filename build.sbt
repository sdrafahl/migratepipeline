import java.io.File
import java.nio.file.{Files, Path, StandardCopyOption}
import scala.sys.process._
import xerial.sbt.Sonatype._

val scala3Version = "3.0.0"

credentials += Credentials("Sonatype Nexus Repository Manager", "s01.oss.sonatype.org", "sdrafahl", scala.sys.env("NEXUS_PASSWORD"))
publishTo := sonatypePublishToBundle.value

sonatypeCredentialHost := "s01.oss.sonatype.org"

sonatypeProfileName := "io.github.sdrafahl"
licenses := Seq("MIT" -> url("https://github.com/sdrafahl/migratepipeline/blob/master/LICENSE"))


sonatypeProjectHosting := Some(GitHubHosting("sdrafahl", "migratepipeline", "shanedrafahl@gmail.com"))
usePgpKeyHex("76DA99CA42B1819F85F0F09905F8D10A76F31F69")
ThisBuild / versionScheme := Some("pvp")

sonatypeBundleDirectory := (ThisBuild / baseDirectory).value / target.value.getName / "sonatype-staging" / (ThisBuild / version).value 
val moveBundle = taskKey[Unit]("Moves the build bundle to the top directory")
moveBundle := {
  val bundleDir = ((ThisBuild / baseDirectory).value / target.value.getName / "sonatype-staging" / (ThisBuild / version).value / "migrationpipeline" / "migrationpipeline_3").toPath
  val top = ((ThisBuild / baseDirectory).value / "migrationpipeline_3").toPath
  Files.move(bundleDir, top, StandardCopyOption.ATOMIC_MOVE)
}

val deleteBundle = taskKey[Unit]("deletes bundle")
deleteBundle := {
  val top = ((ThisBuild / baseDirectory).value / "migrationpipeline_3").toPath.toString()
  s"rm -fr ${top}" !!
}

//Global / useGpgPinentry := true

lazy val root = project
  .in(file("."))
  .settings(
    name := "migrationpipeline",
    version := "0.0.1",
    scalaVersion := scala3Version,    
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
    libraryDependencies += "org.typelevel" % "cats-core_2.13" % "2.3.0",
    libraryDependencies += "org.typelevel" %% "cats-effect" % "3.1.1",
    libraryDependencies += "io.circe" %% "circe-core" % "0.14.0-M7",
    libraryDependencies += "io.circe" %% "circe-generic" % "0.14.0-M7"
  )
