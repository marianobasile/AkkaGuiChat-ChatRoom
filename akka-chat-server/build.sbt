name := """akka-remote-chat-service"""

version := "2.4.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.0",
  "com.typesafe.akka" %% "akka-remote" % "2.4.0"
)

mainClass in (Compile,run) := Some("remote/RemoteChatServiceActor")

fork in run := true
