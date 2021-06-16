import java.io.File
import java.nio.file.{Files, Path, StandardCopyOption}
import scala.sys.process._
import xerial.sbt.Sonatype._
import scala.util.Try

val scala3Version = "3.0.0"

credentials += Credentials("Sonatype Nexus Repository Manager", "s01.oss.sonatype.org", "sdrafahl", Try(scala.sys.env("NEXUS_PASSWORD")).getOrElse(""))

organization := "io.github.sdrafahl"
organizationName := "sdrafahl"
organizationHomepage := Some(url("https://github.com/sdrafahl"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/sdrafahl/migratepipeline"),
    "scm:git@github.com:sdrafahl/migratepipeline.git"
  )
)
developers := List(
  Developer(
    id    = "sdrafahl",
    name  = "Shane Drafahl",
    email = "shanedrafahl@gmail.com",
    url   = url("https://github.com/sdrafahl/migratepipeline")
  )
)

description := "Library for migrations" +  ""
licenses := Seq("MIT" -> url("https://github.com/sdrafahl/migratepipeline/blob/master/LICENSE"))
homepage := Some(url("https://github.com/sdrafahl/migratepipeline"))

pomIncludeRepository := { _ => false }
publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "service/local/staging/deploy/maven2")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

sonatypeProjectHosting := Some(GitHubHosting("sdrafahl", "migratepipeline", "shanedrafahl@gmail.com"))
usePgpKeyHex("76DA99CA42B1819F85F0F09905F8D10A76F31F69")
ThisBuild / versionScheme := Some("pvp")

lazy val root = project
  .in(file("."))
  .settings(
    name := "migrationpipeline",
    version := "0.1.0",
    scalaVersion := scala3Version,    
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
    libraryDependencies += "org.typelevel" % "cats-core_2.13" % "2.3.0",
    libraryDependencies += "org.typelevel" %% "cats-effect" % "3.1.1",
    libraryDependencies += "io.circe" %% "circe-core" % "0.14.0-M7",
    libraryDependencies += "io.circe" %% "circe-generic" % "0.14.0-M7"
  )
