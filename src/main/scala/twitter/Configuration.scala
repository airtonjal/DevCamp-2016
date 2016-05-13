package twitter

import java.io.IOException
import java.util.{MissingResourceException, Properties}

import kafka.consumer.ConsumerConfig
import org.slf4j.LoggerFactory

import scala.util.Random

/**
  * Application config
  * @author <a href="mailto:airton_liborio@mckinsey.com">Airton Libório</a>
  */
object Configuration {
  private val log = LoggerFactory.getLogger(getClass)
  val p = new Properties

  val TOPIC = "tweets-test"
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
