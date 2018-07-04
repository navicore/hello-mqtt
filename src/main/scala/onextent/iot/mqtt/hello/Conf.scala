package onextent.iot.mqtt.hello

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.alpakka.mqtt.MqttConnectionSettings
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import scala.concurrent.duration._

object Conf extends Conf with LazyLogging {

  implicit val actorSystem: ActorSystem = ActorSystem("HelloMqtt")

  val decider: Supervision.Decider = { e: Throwable =>
    logger.error(s"decider can not decide: $e - restarting...", e)
    Supervision.Restart
  }

  implicit val materializer: ActorMaterializer = ActorMaterializer(
    ActorMaterializerSettings(actorSystem).withSupervisionStrategy(decider))
}

trait Conf extends LazyLogging {

  val conf: Config = ConfigFactory.load()

  val myName: String = conf.getString("main.myName")

  val intervalSeconds: FiniteDuration =
    Duration(conf.getInt("main.intervalSeconds"), SECONDS)

  val mqttPublishClientId: String = conf.getString("mqtt.publish.clientId")
  val mqttPublishUrl: String = conf.getString("mqtt.publish.url")
  val mqttPublishUser: String = conf.getString("mqtt.publish.user")
  val mqttPublishPwd: String = conf.getString("mqtt.publish.pwd")
  val mqttPublishTopic: String = conf.getString("mqtt.publish.topic")

  logger.info(s"publishing to $mqttPublishUrl")
  val pubSettings: MqttConnectionSettings =
    MqttConnectionSettings(
      mqttPublishUrl,
      mqttPublishClientId,
      new MemoryPersistence
    ).withAuth(mqttPublishUser, mqttPublishPwd)

  val sinkSettings: MqttConnectionSettings =
    pubSettings.withClientId(clientId = mqttPublishClientId)
}
