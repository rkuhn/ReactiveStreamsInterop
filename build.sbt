name := "reactive-streams-interop"

organization := "com.rolandkuhn"

version := "0.1.0-SNAPSHOT"

resolvers += "jfrog" at "http://oss.jfrog.org/repo"

resolvers += "spring" at "http://repo.spring.io/libs-milestone"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-stream-experimental" % "0.9-SNAPSHOT",
    "io.reactivex" % "rxjava-reactive-streams" % "0.3.0",
    "io.ratpack" % "ratpack-rx" % "0.9.10-SNAPSHOT",
    "io.ratpack" % "ratpack-test" % "0.9.10-SNAPSHOT",
    "org.projectreactor" % "reactor-core" % "2.0.0.M1"
  )
