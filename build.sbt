name := "scala-etcd"

crossScalaVersions := Seq("2.10.4", "2.11.5")

organization := "au.id.haworth"

licenses += ("Apache-2.0", url("https://opensource.org/licenses/Apache-2.0"))

libraryDependencies ++= Seq(
  "io.spray"          %% "spray-client"  % "1.3.2",
  "io.spray"          %% "spray-json"    % "1.3.2",
  "net.virtual-void"  %%  "json-lenses"  % "0.6.1",
  "com.typesafe.akka" %% "akka-actor"    % "2.3.6"
)

enablePlugins(GitVersioning)

git.useGitDescribe := true
