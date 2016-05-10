name := "DevCamp-2016"

version := "0.1"

scalaVersion := "2.11.8"

scalacOptions := Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.6.1",
  "org.apache.kafka" %% "kafka" % "0.9.0.1",
  "org.elasticsearch" % "elasticsearch" % "2.3.2"
)