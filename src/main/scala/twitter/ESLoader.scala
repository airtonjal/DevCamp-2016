package twitter

import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType.{DateType, GeoPointType, StringType}
import org.elasticsearch.common.geo.GeoPoint
import org.elasticsearch.common.settings.Settings

/**
  * Elasticsearch {@link twitter.Tweet} loader
  * @author <a href="mailto:airton_liborio@mckinsey.com">Airton Libório</a>
  */
object ESLoader {

  val settings = Settings.builder
    .put("http.enabled", "false")
    .put("cluster.name", "elasticsearch_airton").build()

  val uri = ElasticsearchClientUri("elasticsearch://localhost:9300")
  val client = ElasticClient.transport(settings, uri)

  def saveToES(tweet: AnalyzedTweet) = client.execute {index into "twitter2/tweets" fields toMap(tweet) }.await

  def toMap(tweet: AnalyzedTweet) = {
    val l = tweet.location match {
      case Some(location) => new GeoPoint(location("lat"), location("lon")).geohash()
      case None           => null
    }
    Map(
      "text"      -> tweet.text,
      "created"   -> tweet.created,
      "location"  -> l,
      "language"  -> tweet.language,
      "user"      -> tweet.user,
      "sentiment" -> tweet.sentiment)
  }

  def createIndex = client.execute {
    create index "twitter2" mappings(
      "tweets" as (
        "text"      typed StringType,
        "language"  typed StringType,
        "user"      typed StringType,
        "sentiment" typed StringType index "not_analyzed",
        "location"  typed GeoPointType,
        "created"   typed DateType
        ))
  }.await

  def main(args: Array[String]) = {
    println("kdkvadvad")
    createIndex
    println("kdkvadvad")
  }

}
