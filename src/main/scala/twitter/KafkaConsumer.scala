package twitter

import consumer.kafka.ReceiverLauncher
import org.apache.spark.storage.StorageLevel
import org.slf4j.LoggerFactory

/**
  * Kafka consumer
  * @author <a href="mailto:airton_liborio@mckinsey.com">Airton Libório</a>
  */
object KafkaConsumer {
  import Configuration._
  private val log = LoggerFactory.getLogger(getClass)
  def main(args: Array[String]) {
    log.info("Starting Spark")
    Setup.ssc.start()
    Setup.ssc.awaitTermination()
  }

  val numberOfReceivers = 3

  import ESLoader._
  val kafkaStream = ReceiverLauncher.launch(Setup.ssc, p, 3, StorageLevel.MEMORY_ONLY)
  kafkaStream.foreachRDD(rdd => {
    rdd.collect()
    log.info("Number of records in this RDD: " + rdd.count())
    rdd.map(t => TweetSerializer.fromJson(new String(t.getPayload)))
      .map(t => new AnalyzedTweet(t.text, t.created, t.location, t.language, t.user, SimpleSentimentAnalysis.classify(t.text)))
      .foreach(saveToES(_))
  })

  log.info("Starting Twitter Kafka consumer stream")
}
