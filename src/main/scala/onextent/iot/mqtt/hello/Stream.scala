package onextent.iot.mqtt.hello

import scala.util.{Success, Failure}
import akka.{Done, NotUsed}
import akka.stream.ThrottleMode
import akka.stream.alpakka.mqtt._
import akka.stream.alpakka.mqtt.scaladsl.MqttSink
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import onextent.iot.mqtt.hello.Conf._
import onextent.iot.mqtt.hello.models.SayHello

import scala.concurrent.Future
import scala.language.implicitConversions

object Stream extends LazyLogging {

  def throttlingFlow[T]: Flow[T, T, NotUsed] =
    Flow[T].throttle(
      elements = 1,
      per = intervalSeconds,
      maximumBurst = 0,
      mode = ThrottleMode.Shaping
    )

  def helloMqttMessage(): SayHello => MqttMessage =
    (h: SayHello) => {
      logger.debug(h.hello())
      MqttMessage(mqttPublishTopic,
                  ByteString(h.asJson()),
                  Some(MqttQoS.AtLeastOnce),
                  retained = true)
    }

  def apply(): Unit = {

    logger.info(s"stream starting...")

    val helloSource = Source.fromGraph(new HelloSource()).via(throttlingFlow)

    val r: Future[Done] = helloSource
      .map(helloMqttMessage())
      .runWith(MqttSink(sinkSettings, MqttQoS.atLeastOnce))

    r onComplete {
      case Success(_) =>
        logger.warn("success. but stream should not end!")
        throw new Exception("stream should not end")
      case Failure(e) =>
        logger.error(s"failure. stream should not end! $e", e)
        throw e
    }

  }

}
