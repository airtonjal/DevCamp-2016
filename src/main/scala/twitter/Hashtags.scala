package twitter

import org.apache.spark.streaming.Seconds
import org.slf4j.LoggerFactory

/**
  * Twitter hashtag counting example
  * @author <a href="mailto:airton_liborio@mckinsey.com">Airton Libório</a>
  */
object Hashtags {

  private val log = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]) {
    val WINDOW = 120
    val hashTags = Setup.createStream.flatMap(status => status.getHashtagEntities.map("#" + _.getText))
    val topHashTags = hashTags.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(WINDOW))
      .map{ case (hashTag, count) => (count, hashTag) }
      .transform(_.sortByKey(ascending = false))

    topHashTags.foreachRDD(tweetRDD => {
      val topList = tweetRDD.take(10)
      log.info("-----------------------------------------------------------")
      log.info("Popular topics in last " + WINDOW + " seconds (%s total):".format(tweetRDD.count()))
      topList.foreach{case (count, tag) => log.info("%s (%s tweets)".format(tag, count))}
      log.info("-----------------------------------------------------------")
    })

    Setup.ssc.start()
    Setup.ssc.awaitTermination()
  }


}
