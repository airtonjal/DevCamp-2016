package twitter

import com.typesafe.config.ConfigFactory
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.LoggerFactory

/**
  * Sets up Twitter stream
  * @author <a href="mailto:airton_liborio@mckinsey.com">Airton Libório</a>
  */
object Setup {

  private val log = LoggerFactory.getLogger(getClass)
  val config = ConfigFactory.load()

  log.info("Setting up Twitter credentials")
  setupTwitter(config.getString("oauth.consumerKey"), config.getString("oauth.consumerSecret"),
               config.getString("oauth.accessToken"), config.getString("oauth.accessTokenSecret"))

  log.info("Starting Spark")
  val conf = new SparkConf()
    .setMaster("local[2]")
    .setAppName("Twitter pipeline")
    .set("spark.executor.memory", "1g")
    .set("spark.rdd.compress", "true")
    .set("spark.storage.memoryFraction", "1")
    .set("spark.streaming.unpersist", "true")
    .set("spark.streaming.receiver.writeAheadLog.enable", "false")
  val sc   = new SparkContext(conf)
  val ssc  = new StreamingContext(sc, Seconds(1))

  conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
  conf.registerKryoClasses(Array(classOf[Tweet]))

  def setupTwitter(consumerKey: String, consumerSecret: String, accessToken: String, accessTokenSecret: String) ={
    // Set up the system properties for twitter
    System.setProperty("twitter4j.oauth.consumerKey", consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret)
    // https:  all kinds of fun
    System.setProperty("twitter4j.restBaseURL", "https://api.twitter.com/1.1/")
    System.setProperty("twitter4j.streamBaseURL", "https://stream.twitter.com/1.1/")
    System.setProperty("twitter4j.siteStreamBaseURL", "https://sitestream.twitter.com/1.1/")
    System.setProperty("twitter4j.userStreamBaseURL", "https://userstream.twitter.com/1.1/")
    System.setProperty("twitter4j.oauth.requestTokenURL", "https://api.twitter.com/oauth/request_token")
    System.setProperty("twitter4j.oauth.accessTokenURL", "https://api.twitter.com/oauth/access_token")
    System.setProperty("twitter4j.oauth.authorizationURL", "https://api.twitter.com/oauth/authorize")
    System.setProperty("twitter4j.oauth.authenticationURL", "https://api.twitter.com/oauth/authenticate")
    System.setProperty("sync.numThreads", "4")
    System.setProperty("jsonStoreEnabled", "true")
  }

  def createStream = {
    log.info("Creating Twitter stream")
    TwitterUtils.createStream(ssc, None)
  }
}
