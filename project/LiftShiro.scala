import sbt._
import Keys._

object BuildSettings {
  import Default._

  val bsEtbLiftShiro = scalaSettings ++ Seq(
    name    := "lift-shiro",
    version := "0.2.6"
  )
}

object Publications {
  val liftShiro       = "hr.element.etb"  %  "lift-shiro"
}

object Dependencies {
  import Publications._

  val liftVersion = "2.4"
  val liftJson   = "net.liftweb" %% "lift-json"   % liftVersion
  val liftCommon = "net.liftweb" %% "lift-common" % liftVersion
  val liftUtil   = "net.liftweb" %% "lift-util"   % liftVersion
  val liftWebkit = "net.liftweb" %% "lift-webkit" % liftVersion
  val liftNamedComet = "fmpwizard" %% "lift-named-comet" % "0.2"

  val ehcacheVersion = "2.5.1"
  val ehcache = Seq(
    "net.sf.ehcache" % "ehcache-core" % ehcacheVersion
  , "javax" % "javaee-web-api" % "6.0"
  )

  // shiro
  val shiroVersion = "1.2.0"
  val shiro = Seq(
    "org.apache.shiro" % "shiro-core" % shiroVersion
  , "org.apache.shiro" % "shiro-web" % shiroVersion
  , "org.apache.shiro" % "shiro-ehcache" % shiroVersion
  , "net.sf.ehcache" % "ehcache-terracotta" % ehcacheVersion
  , "commons-logging" % "commons-logging" % "1.1.1"
  ) ++ ehcache

  // Logging
  val logback = "ch.qos.logback" % "logback-classic" % "1.0.0" % "compile->default"
  val log4jOverSlf4j = "org.slf4j" % "log4j-over-slf4j" % "1.6.4"

  val pgJdbc4 = "postgresql" % "postgresql" % "9.0-801.jdbc4"

  val scalaTest = "org.scalatest" %% "scalatest" % "1.6-SNAPSHOT"

  val jodaTime = Seq(
    "org.joda" % "joda-convert" % "1.2",
    "joda-time" % "joda-time" % "2.0"
  )
}

import Implicits._

object ProjectDeps {
  import Dependencies._
  import Publications._

  val depsEtbLiftShiro = libDeps(
    liftWebkit
  , shiro
  )

}

//  ---------------------------------------------------------------------------

object EtbLiftShiro extends Build {
  import BuildSettings._
  import ProjectDeps._

  lazy val etbLiftShiro = Project(
    "lift-shiro",
    file("lift-shiro"),
    settings = bsEtbLiftShiro :+ depsEtbLiftShiro
  )

}

//  ---------------------------------------------------------------------------

object Repositories {
  val ElementNexus     = "Element Nexus"     at "http://maven.element.hr/nexus/content/groups/public/"
  val ElementReleases  = "Element Releases"  at "http://maven.element.hr/nexus/content/repositories/releases/"
  val ElementSnapshots = "Element Snapshots" at "http://maven.element.hr/nexus/content/repositories/snapshots/"
}

//  ---------------------------------------------------------------------------

object Resolvers {
  import Repositories._

  val settings = Seq(
    resolvers := Seq(ElementNexus, ElementReleases, ElementSnapshots),
    externalResolvers <<= resolvers map { rs =>
      Resolver.withDefaultResolvers(rs, mavenCentral = false, scalaTools = false)
    }
  )
}

//  ---------------------------------------------------------------------------

object Publishing {
  import Repositories._

  val settings = Seq(
    publishTo <<= (version) { version => Some(
      if (version.endsWith("SNAPSHOT")) ElementSnapshots else ElementReleases
    )},
    credentials += Credentials(Path.userHome / ".publish" / "element.credentials"),
    publishArtifact in (Compile, packageDoc) := false
  )
}

//  ---------------------------------------------------------------------------

object Default {
  val scalaSettings =
    Defaults.defaultSettings ++
    Resolvers.settings ++
    Publishing.settings ++ Seq(
      organization := "hr.element.etb",
      crossScalaVersions := Seq("2.9.1", "2.9.0-1", "2.9.0"),
      scalaVersion <<= (crossScalaVersions) { versions => versions.head },
      scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "UTF-8", "-optimise"), // , "-Yrepl-sync"
      unmanagedSourceDirectories in Compile <<= (scalaSource in Compile)( _ :: Nil),
      unmanagedSourceDirectories in Test    <<= (scalaSource in Test   )( _ :: Nil)
    )
}


object Implicits {
  implicit def depToFunSeq(m: ModuleID) = Seq((_: String) => m)
  implicit def depFunToSeq(fm: String => ModuleID) = Seq(fm)
  implicit def depSeqToFun(mA: Seq[ModuleID]) = mA.map(m => ((_: String) => m))

  def libDeps(deps: (Seq[String => ModuleID])*) = {
    libraryDependencies <++= scalaVersion( sV =>
      for (depSeq <- deps; dep <- depSeq) yield dep(sV)
    )
  }
}





