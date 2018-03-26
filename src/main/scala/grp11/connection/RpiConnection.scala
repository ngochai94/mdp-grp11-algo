package grp11.connection

import java.net._
import java.io._
import java.util.concurrent.ConcurrentLinkedQueue

import scala.io.StdIn

class RpiConnection(host: String, port: Int, fakeAndroid: Boolean = false) {
  var s = new Socket(InetAddress.getByName(host), port)
  val in = new DataInputStream(s.getInputStream)
  val out = new PrintStream(s.getOutputStream)

  val androidBuffer = new ConcurrentLinkedQueue[String]()
  val arduinoBuffer = new ConcurrentLinkedQueue[String]()


  def send(msg: RpiMessage): Unit = {
    try {
      out.println(msg.toString)
      out.flush()
      msg match {
        case ArduinoMessage(s) => println(s"Sent $msg")
        case _ =>
      }
    } catch {
      case e: SocketException =>
        println(e)
        s = new Socket(InetAddress.getByName(host), port)
        send(msg)
    }
  }

  private[this] def receive: String = {
    try {
      val b = Array.ofDim[Byte](RpiConnection.MaxLength)
      in.read(b)
      val s = b.map(_.toChar).mkString
      s.trim
    } catch {
      case e: SocketException =>
        println(e)
        s = new Socket(InetAddress.getByName(host), port)
        receive
    }
  }

  def receiveAndroid: String = {
    if (fakeAndroid) {
      StdIn.readLine
    } else {
      while (androidBuffer.peek == null) {}
      androidBuffer.poll
    }
  }

  def receiveArduino: String = {
    while (arduinoBuffer.peek == null) {}
    arduinoBuffer.poll
  }

  def close(): Unit = s.close()

  val thread = new Thread {
    override def run(): Unit = {
      while (true) {
        val received = receive
        if (received != "") {
          println(s"Received |$received|")
          if (received.startsWith("AN.*")) {
            androidBuffer.add(received.substring(4))
          } else {
            arduinoBuffer.add(received)
          }
        }
      }
    }
  }
  thread.start()

  println("RPI is connected!")
}

object RpiConnection {
  val DefaultHost = "192.168.200.11"
  val DefaultPort = 8080
  val MaxLength = 1024
}
