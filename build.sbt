import sbt.ExclusionRule

name := "DevCamp-2016"

version := "0.1"

scalaVersion := "2.11.8"

scalacOptions := Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos",
  "mvn" at "http://maven.twttr.com",
  "twitter" at "http://twitter4j.org/maven2")


val sparkVersion   = "1.6.1"
val kafkaVersion   = "0.8.2.2"
val esVersion      = "2.3.2"
val logbackVersion = "1.1.7"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion excludeAll ExclusionRule("org.slf4j", "slf4j-log4j12"),
  "org.apache.spark" %% "spark-streaming" % sparkVersion excludeAll ExclusionRule("org.slf4j"),
  "org.apache.spark" %% "spark-streaming-twitter" % sparkVersion,
  "org.apache.spark" %% "spark-streaming-kafka" % sparkVersion excludeAll ExclusionRule("org.slf4j", "slf4j-log4j12"),
  "org.apache.kafka" %% "kafka" % kafkaVersion excludeAll ExclusionRule("org.slf4j", "slf4j-log4j12"),
  "org.apache.kafka"  % "kafka-clients" % kafkaVersion excludeAll ExclusionRule("org.slf4j", "slf4j-log4j12"),
  "org.elasticsearch" % "elasticsearch" % esVersion,
  "ch.qos.logback"    % "logback-classic" % logbackVersion,
  "joda-time"         % "joda-time" % "2.9.3",
  "org.json4s"       %% "json4s-jackson" % "3.3.0"
//  "ch.qos.logback"    % "logback-core" % logbackVersion
)

//"org.elasticsearch" % "elasticsearch-hadoop" % "2.3.1",