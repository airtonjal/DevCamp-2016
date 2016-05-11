package twitter

import java.io.IOException
import java.util.{MissingResourceException, Properties}

import kafka.consumer.ConsumerConfig
import kafka.producer.{KeyedMessage, Producer, ProducerConfig}
import org.slf4j.LoggerFactory
import twitter4j.{TwitterObjectFactory, TwitterStreamFactory}

import scala.io.Source
import scala.util.Random

/**
  * Loads Twitter data from Kafka
  *
  * @author <a href="mailto:airton_liborio@mckinsey.com">Airton Libório</a>
  */
object KafkaProducer {
  import KafkaProperties._
  private val log = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]) {
    log.info("Creating Kafka producer")

    val stream = Setup.twitterStream

    stream.map { tweet =>
      val location = tweet.getGeoLocation match {
        case null => None
        case gl => Some(Map("lat" -> gl.getLatitude, "lon" -> gl.getLongitude))
      }
      Map("text"     -> tweet.getText,
        "created_at" -> tweet.getCreatedAt,
        "location"   -> location,
        "language"   -> tweet.getLang,
        "user"       -> tweet.getUser.getName)
        .filter(kv => kv._2 != null && kv._2 != None)
    }.foreachRDD(rdd =>
      rdd.foreachPartition { partition =>
        val producerConfig = new ProducerConfig(p)
        val producer: Producer[String, String] = new Producer[String, String](producerConfig)

        partition.foldLeft(0){(accum, status) =>
          println(status)
          producer.send(
            new KeyedMessage[String, String]("twitter",(accum % NUMBER_OF_PARTITIONS).toString, status.toString()))
          accum + 1
        }

        producer.close()
      }
    )

    Setup.ssc.start()
    Setup.ssc.awaitTermination()
  }
}


object KafkaProperties {

  private val log = LoggerFactory.getLogger(getClass)
  val p = new Properties

  val NUMBER_OF_PARTITIONS = 10

  // Randomly chooses a groupId name to avoid polluting zookeeper data
  val groupId = getClass.getName + "" + new Random().nextInt(100000)
  p.put("group.id", groupId)
  val ZOOKEEPER_PROPERTY = "zookeeper.connect"

  // Producer properties
  val BROKER_PROP      = "metadata.broker.list"
  val SERIALIZER_PROP  = "serializer.class"
  val PARTITIONER_PROP = "partitioner.class"
  val COMPRESSION_PROP = "compression.codec"

  // Consumer properties
  val ZOOKEEPER_PROP = "zookeeper.connect"
  val REQUIRED_PROPS = Array(ZOOKEEPER_PROP, OFFSET_PROP)
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

  val OFFSET_PROP    = "auto.offset.reset"
  val filename       = "kafka.properties"
  
  try {
    log.info(s"Attempting to read $filename as stream from resources directory")
    val stream = Source.fromURL(Source.getClass.getResource("/" + filename))

    p.load(stream.bufferedReader())

    log.info(s"$filename read and loaded")
  } catch {
    case ioe: IOException =>
      throw new MissingResourceException(s"$filename property file not found in resources dir", getClass.getName, filename)
  }

  def zookeeper = p.containsKey(ZOOKEEPER_PROPERTY) match {
    case false =>
      throw new MissingResourceException(s"$ZOOKEEPER_PROPERTY property not set in $filename", getClass.getName, ZOOKEEPER_PROPERTY)
    case true => p.getProperty(ZOOKEEPER_PROPERTY)
  }
}

