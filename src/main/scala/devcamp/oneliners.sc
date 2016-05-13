import scala.io.Source
import scala.xml.XML

(1 to 10) map { _ * 2 }

(1 to 1000).reduceLeft( _ + _ )
(1 to 1000).sum

List(14, 35, -7, 46, 98).reduceLeft ( _ min _ )
List(14, 35, -7, 46, 98).min

// Verify if words exists in a String
val wordList = List("scala", "akka", "play framework", "sbt", "typesafe")
val tweet = "This is an example tweet talking about scala and sbt"

wordList.foldLeft(false)(_ || tweet.contains(_))
wordList.exists(tweet.contains)

// Every letter in the alphabet
val pangram = "The quick brown fox jumps over the lazy dog"
(pangram split " ") filter (_ contains 'o')

val m = pangram filter (_.isLetter) groupBy (_.toUpper) mapValues (_.size)
m.toSeq sortBy (_._2)
m.toSeq sortWith (_._2 > _._2)
m.filter(_._2 > 1).toSeq sortWith (_._2 > _._2) mkString "\n"

Source.fromURL("https://github.com/humans.txt").take(335).mkString

(1 to 4).map { i => "Happy Birthday " + (if (i == 3) "dear NAME" else "to You") }.mkString("\n")

//val fileText  = Source.fromFile("/Users/airton/dev/DevCamp-2016/src/main/resources/negative-words.txt").mkString
//val fileLines = Source.fromFile("/Users/airton/dev/DevCamp-2016/src/main/resources/positive-words.txt").getLines.toList