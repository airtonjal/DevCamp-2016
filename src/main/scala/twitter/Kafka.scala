package twitter

import java.io.IOException
import java.util.{MissingResourceException, Properties}

import consumer.kafka.ReceiverLauncher
import kafka.consumer.ConsumerConfig
import kafka.producer.{KeyedMessage, Producer, ProducerConfig}
import kafka.utils.ZkUtils
import org.apache.spark.storage.StorageLevel
import org.slf4j.LoggerFactory

import scala.util.Random

/**
  * Tweets Kafka producer
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

object KafkaConsumer {
  import KafkaProperties._
  private val log = LoggerFactory.getLogger(getClass)
  def main(args: Array[String]) {
    log.info("Starting Spark")
    Setup.ssc.start()
    Setup.ssc.awaitTermination()
  }
  
  val numberOfReceivers = 3


//  ZkUtils.maybeDeletePath("localhost:2181", "/consumers/" + groupId)
  import ESLoader._
  val kafkaStream = ReceiverLauncher.launch(Setup.ssc, p, 3, StorageLevel.MEMORY_ONLY)
  kafkaStream.foreachRDD(rdd => {
    rdd.collect()
    log.info("Number of records in this RDD: " + rdd.count())
    rdd.map(t => TweetSerializer.fromJson(new String(t.getPayload))).foreach(saveToES(_))

  })

  log.info("Starting Twitter Kafka consumer stream")
}


object KafkaProperties {
  private val log = LoggerFactory.getLogger(getClass)
  val p = new Properties

  val TOPIC = "tweets"
  val PARTITIONS = 10

  // Randomly chooses a groupId name to avoid polluting zookeeper data
//  val groupId = "kafka-spark-consumer-" + new Random().nextInt(100000)
  val groupId = "kafka-spark-streaming"
//  p.put()
  val ZOOKEEPER_PROPERTY = "zookeeper.connect"

  // Producer properties
  val BROKER_PROP      = "metadata.broker.list"
  val SERIALIZER_PROP  = "serializer.class"
  val PARTITIONER_PROP = "partitioner.class"
  val COMPRESSION_PROP = "compression.codec"

  // Consumer properties
  val ZOOKEEPER_PROP = "zookeeper.connect"
  // Other properties, not currently set, change if testing needs to be performed

  val kafkaProperties: Map[String, String] = Map(
    "zookeeper.hosts" -> "localhost",
    "zookeeper.port" -> "2181",
    "zookeeper.broker.path" -> "/brokers",
    "kafka.topic" -> TOPIC,
    "zookeeper.consumer.connection" -> "localhost:2181",
    "zookeeper.consumer.path" -> "/consumers",
    "kafka.consumer.id" -> new Random().nextInt(100000).toString,
    //optional properties
    "consumer.forcefromstart" -> "true",
    "consumer.backpressure.enabled" -> "true",
    "consumer.fetchsizebytes" -> "1048576",
    "consumer.fillfreqms" -> "250",
    "kafka.message.handler.class" -> "consumer.kafka.IdentityMessageHandler",
    "socket.receive.buffer.bytes" -> (2 * 1024 * 1024).toString,
    "socket.timeout.ms" -> ConsumerConfig.SocketTimeout.toString,
    "fetch.message.max.bytes" -> (1024 * 1024).toString,
    "fetch.min.bytes" -> 1.toString,
    "fetch.wait.max.ms" -> 100.toString,
    "auto.commit.enable" -> "true",
    "auto.commit.interval.ms" -> ConsumerConfig.AutoCommitInterval.toString,
    "consumer.timeout.ms" -> (-1).toString,
    "refresh.leader.backoff.ms" -> ConsumerConfig.RefreshMetadataBackoffMs.toString,
    "auto.offset.reset" -> "smallest")

  val filename       = "kafka.properties"

  try {
    log.info(s"Attempting to read $filename as stream from resources directory")
    val stream = getClass.getResourceAsStream(s"/$filename")

    p.load(stream)
    kafkaProperties.foreach{ case(key, value) => p.put(key, value)}

    log.info(s"$filename read and loaded")
  } catch {
    case ioe: IOException => throw new MissingResourceException(s"$filename property file not found in resources dir", getClass.getName, filename)
  }

}

// Using spark streaming:
//// Set up the input DStream to read from Kafka (in parallel)
//val kafkaStream = {
//  val kafkaParams = Map(
//  "zookeeper.connect" -> zookeeper,
//  "group.id" -> groupId,
//  "zookeeper.connection.timeout.ms" -> "5000",
//  "auto.offset.reset" -> "smallest",
//  "metadata.broker.list" -> "localhost:9092",
//  "socket.receive.buffer.bytes" -> socketBufferSizeOpt,
//  "socket.timeout.ms" -> socketTimeoutMsOpt,
//  "fetch.message.max.bytes" -> fetchSizeOpt,
//  "fetch.min.bytes" -> minFetchBytesOpt,
//  "fetch.wait.max.ms" -> maxWaitMsOpt,
//  "auto.commit.enable" -> "true",
//  "auto.commit.interval.ms" -> autoCommitIntervalOpt,
//  "consumer.timeout.ms" -> consumerTimeoutMsOpt,
//  "refresh.leader.backoff.ms" -> refreshMetadataBackoffMsOpt,
//  "auto.offset.reset" -> "smallest")
//
//  ZkUtils.maybeDeletePath(zookeeper, "/consumers/" + groupId)
//
//  val streams = (1 to PARTITIONS) map { _ =>
//  log.info("here")
//  //      KafkaUtils.createStream[String, String, StringDecoder, StringDecoder](
//  //        Setup.ssc, kafkaParams, Map(TOPIC -> PARTITIONS), StorageLevel.MEMORY_ONLY_SER).map(_._2)
//
//  Setup.ssc.checkpoint("checkpoint")
//
//  //      KafkaUtils.createStream(Setup.ssc, zookeeper, groupId, Map(TOPIC -> PARTITIONS)).map(_._2)
//  //      KafkaUtils.createDirectStream(Setup.ssc, kafkaParams String, String, StringDecoder, StringDecoder, kafkaParams, Set(TOPIC))
//  KafkaUtils.createDirectStream(Setup.ssc, kafkaParams, Set(TOPIC))
//
//  //      KafkaUtils.createStream(
//  //        Setup.ssc, zookeeper, groupId, Map(TOPIC -> 1), StorageLevel.MEMORY_ONLY_SER)
//}
//
//  Setup.ssc.union(streams) // Join partition streams
//}
//kafkaStream.foreachRDD(rdd => rdd.foreach(tweet => println(tweet)))
