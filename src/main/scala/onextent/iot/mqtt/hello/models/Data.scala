package onextent.iot.mqtt.hello.models

sealed trait Command

case class SayHello(myName: String) extends Command {
  def hello(): String = s"Hiya from $myName"
  def asJson(): String = s"""{"msg": "${hello()}"}"""
}

