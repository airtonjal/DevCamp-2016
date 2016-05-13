package twitter

import kafka.producer.{KeyedMessage, Producer, ProducerConfig}
import org.slf4j.LoggerFactory

/**
  * Kafka producer
  * @author <a href="mailto:airton_liborio@mckinsey.com">Airton Libório</a>
  */
object KafkaProducer {
  import Configuration._
  private val log = LoggerFactory.getLogger(getClass)
  def main(args: Array[String]) {
    Setup.ssc.start()
    Setup.ssc.awaitTermination()
  }

  log.info("Creating Kafka producer")

  val stream         = Setup.createStream
  val producedTweets = Setup.ssc.sparkContext.accumulator(0L, "Kafka produced Tweets")

  stream.map { tweet =>
    val location = tweet.getGeoLocation match {
      case null => None
      case gl   => Some(Map("lat" -> gl.getLatitude, "lon" -> gl.getLongitude))
    }

    Tweet(tweet.getText, tweet.getCreatedAt, location, tweet.getLang, tweet.getUser.getName)
  }.foreachRDD(rdd => {
    log.info(s"RDD size: ${rdd.count()}")
    log.info(s"Total tweets produced: ${producedTweets.value}")
    rdd.foreachPartition { partition =>
      val producerConfig = new ProducerConfig(p)
      val producer = new Producer[String, String](producerConfig)

      partition.foreach{ tweet =>
        producedTweets += 1
        producer.send(
          new KeyedMessage[String, String](TOPIC, TweetSerializer.toJson(tweet)))
      }

      producer.close()
    }
  })

  log.info("Starting Twitter Kafka producer stream")
}
