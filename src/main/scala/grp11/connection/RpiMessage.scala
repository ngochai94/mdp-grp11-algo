package grp11.connection

sealed trait RpiMessage

case class ArduinoMessage(msg: String) extends RpiMessage {
  override def toString: String = {
    msg
  }
}

case class AndroidMessage(msg: String) extends RpiMessage {
  override def toString: String = {
    "AN.*" + msg
  }
}
