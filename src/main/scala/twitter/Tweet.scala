package twitter

import java.nio.charset.Charset

import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}
import java.util.Date

/**
  * A tweet message
  * @author <a href="mailto:airton_liborio@mckinsey.com">Airton Libório</a>
  */
case class Tweet(text: String, created: Date, location: Option[Map[String, Double]], language: String, user: String)

case class AnalyzedTweet(text: String, created: Date, location: Option[Map[String, Double]], language: String, user: String,
                 sentiment: String)

object TweetSerializer {
  implicit val formats = Serialization.formats(NoTypeHints)

  val UTF8: Charset = Charset.forName("UTF-8")

  def toJson(tweet: Tweet) = new String(write(tweet).getBytes(UTF8), UTF8)
  def fromJson(json: String) = read[Tweet](json)

  def toMap(tweet: AnalyzedTweet) = Map(
    "text"      -> tweet.text,
    "created"   -> tweet.created,
    "location"  -> tweet.location.orNull,
    "language"  -> tweet.language,
    "user"      -> tweet.user,
    "sentiment" -> tweet.sentiment)

}