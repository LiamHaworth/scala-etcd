name := "scala-etcd"

crossScalaVersions := Seq("2.10.4", "2.11.5")

organization := "au.id.haworth"

libraryDependencies ++= Seq(
  "io.spray"          %% "spray-client"  % "1.3.2",
  "io.spray"          %% "spray-json"    % "1.3.1",
  "com.typesafe.akka" %% "akka-actor"    % "2.3.6"
)
