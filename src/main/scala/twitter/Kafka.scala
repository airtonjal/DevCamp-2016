package twitter

import java.io.IOException
import java.util.{MissingResourceException, Properties}

import io.netty.handler.codec.string.StringDecoder
import kafka.consumer.ConsumerConfig
import kafka.producer.{KeyedMessage, Producer, ProducerConfig}
import kafka.serializer.StringDecoder
import kafka.utils.ZkUtils
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.slf4j.LoggerFactory

import scala.io.Source
import scala.util.Random

/**
  * Tweets Kafka producer
  *
  * @author <a href="mailto:airton_liborio@mckinsey.com">Airton Libório</a>
  */
object KafkaProducer {
  import KafkaProperties._
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
      case gl => Some(Map("lat" -> gl.getLatitude, "lon" -> gl.getLongitude))
    }
    location.foreach(l => println(l))

    Tweet(tweet.getText, tweet.getCreatedAt, location, tweet.getLang, tweet.getUser.getName)
    //        .filter(kv => kv._2 != null && kv._2 != None)
  }.foreachRDD(rdd =>
    rdd.foreachPartition { partition =>
      val producerConfig = new ProducerConfig(p)
      val producer = new Producer[String, String](producerConfig)

      partition.foreach{ tweet =>
        producedTweets += 1
        producer.send(
          new KeyedMessage[String, String](TOPIC, (producedTweets.localValue % PARTITIONS).toString, TweetSerializer.toJson(tweet)))
      }

      producer.close()
    }
  )

  log.info("Starting Twitter Kafka producer stream")

}

object KafkaConsumer {
  import KafkaProperties._
  private val log = LoggerFactory.getLogger(getClass)
  def main(args: Array[String]) {
    Setup.ssc.start()
    Setup.ssc.awaitTermination()
  }

  // Set up the input DStream to read from Kafka (in parallel)
  val kafkaStream = {
    val kafkaParams = Map(
      "zookeeper.connect" -> zookeeper,
      "group.id" -> groupId,
      "zookeeper.connection.timeout.ms" -> "5000",
      "auto.offset.reset" -> "smallest",
      "metadata.broker.list" -> "localhost:9092",
      "socket.receive.buffer.bytes" -> socketBufferSizeOpt,
      "socket.timeout.ms" -> socketTimeoutMsOpt,
      "fetch.message.max.bytes" -> fetchSizeOpt,
      "fetch.min.bytes" -> minFetchBytesOpt,
      "fetch.wait.max.ms" -> maxWaitMsOpt,
      "auto.commit.enable" -> "true",
      "auto.commit.interval.ms" -> autoCommitIntervalOpt,
      "consumer.timeout.ms" -> consumerTimeoutMsOpt,
      "refresh.leader.backoff.ms" -> refreshMetadataBackoffMsOpt,
      "auto.offset.reset" -> "smallest")

    ZkUtils.maybeDeletePath(zookeeper, "/consumers/" + groupId)

    val streams = (1 to PARTITIONS) map { _ =>
      log.info("here")
//      KafkaUtils.createStream[String, String, StringDecoder, StringDecoder](
//        Setup.ssc, kafkaParams, Map(TOPIC -> PARTITIONS), StorageLevel.MEMORY_ONLY_SER).map(_._2)

      Setup.ssc.checkpoint("checkpoint")

//      KafkaUtils.createStream(Setup.ssc, zookeeper, groupId, Map(TOPIC -> PARTITIONS)).map(_._2)
//      KafkaUtils.createDirectStream(Setup.ssc, kafkaParams String, String, StringDecoder, StringDecoder, kafkaParams, Set(TOPIC))
      KafkaUtils.createDirectStream(Setup.ssc, kafkaParams, Set(TOPIC))

//      KafkaUtils.createStream(
//        Setup.ssc, zookeeper, groupId, Map(TOPIC -> 1), StorageLevel.MEMORY_ONLY_SER)
    }

    Setup.ssc.union(streams) // Join partition streams
  }

  kafkaStream.foreachRDD(rdd => rdd.foreach(tweet => println(tweet)))
}


object KafkaProperties {

  private val log = LoggerFactory.getLogger(getClass)
  val p = new Properties

  val TOPIC = "tweets"
  val PARTITIONS = 10

  // Randomly chooses a groupId name to avoid polluting zookeeper data
//  val groupId = getClass.getName + "" + new Random().nextInt(100000)
  val groupId = "kafka-spark-streaming"
  p.put("group.id", groupId)
  val ZOOKEEPER_PROPERTY = "zookeeper.connect"

  // Producer properties
  val BROKER_PROP      = "metadata.broker.list"
  val SERIALIZER_PROP  = "serializer.class"
  val PARTITIONER_PROP = "partitioner.class"
  val COMPRESSION_PROP = "compression.codec"

  // Consumer properties
  val ZOOKEEPER_PROP = "zookeeper.connect"
  // Other properties, not currently set, change if testing needs to be performed
  val fetchSizeOpt = (1024 * 1024).toString
  val minFetchBytesOpt = 1.toString
  val maxWaitMsOpt = 100.toString
  val socketBufferSizeOpt = (2 * 1024 * 1024).toString
  val socketTimeoutMsOpt = ConsumerConfig.SocketTimeout.toString
  val refreshMetadataBackoffMsOpt = ConsumerConfig.RefreshMetadataBackoffMs.toString
  val consumerTimeoutMsOpt = (-1).toString
  val autoCommitIntervalOpt = ConsumerConfig.AutoCommitInterval.toString

  p.put("socket.receive.buffer.bytes", socketBufferSizeOpt)
  p.put("socket.timeout.ms", socketTimeoutMsOpt)
  p.put("fetch.message.max.bytes", fetchSizeOpt)
  p.put("fetch.min.bytes", minFetchBytesOpt)
  p.put("fetch.wait.max.ms", maxWaitMsOpt)
  p.put("auto.commit.enable", "true")
  p.put("auto.commit.interval.ms", autoCommitIntervalOpt)
  p.put("consumer.timeout.ms", consumerTimeoutMsOpt)
  p.put("refresh.leader.backoff.ms", refreshMetadataBackoffMsOpt)
  p.put("auto.offset.reset", "smallest")

  val OFFSET_PROP    = "auto.offset.reset"
  val filename       = "kafka.properties"

  try {
    log.info(s"Attempting to read $filename as stream from resources directory")
    val stream = Source.fromURL(Source.getClass.getResource("/" + filename))

    p.load(stream.bufferedReader())

    log.info(s"$filename read and loaded")
  } catch {
    case ioe: IOException => throw new MissingResourceException(s"$filename property file not found in resources dir", getClass.getName, filename)
  }

  def zookeeper = p.containsKey(ZOOKEEPER_PROPERTY) match {
    case false =>
      throw new MissingResourceException(s"$ZOOKEEPER_PROPERTY property not set in $filename", getClass.getName, ZOOKEEPER_PROPERTY)
    case true => p.getProperty(ZOOKEEPER_PROPERTY)
  }
}

