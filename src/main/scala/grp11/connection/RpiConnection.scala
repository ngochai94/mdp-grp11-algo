package grp11.connection

import java.net._
import java.io._
import java.util.concurrent.ConcurrentLinkedQueue

import scala.io.StdIn

class RpiConnection(host: String, port: Int) {
  val s = new Socket(InetAddress.getByName(host), port)
  println("RPI is connected!")
  val in = new DataInputStream(s.getInputStream)
  val out = new PrintStream(s.getOutputStream)
  val androidBuffer = new ConcurrentLinkedQueue[String]()
  val arduinoBuffer = new ConcurrentLinkedQueue[String]()

  def send(msg: RpiMessage): Unit = {
    out.println(msg.toString)
    out.flush()
    msg match {
      case ArduinoMessage(s) => println(s"Sent $msg")
      case _ =>
    }
//    println(s"Sent $msg")
  }

  private[this] def receive: String = {
    val b = Array.ofDim[Byte](RpiConnection.MaxLength)
    in.read(b)
    val s = b.map(_.toChar).mkString
    s.trim
  }

  def receiveAndroid: String = {
    while (androidBuffer.peek == null) {}
    androidBuffer.poll
//    StdIn.readLine
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
}

object RpiConnection {
  val DefaultHost = "192.168.200.11"
//  val DefaultHost = "172.21.147.242"
//  val DefaultHost = "127.0.0.1"
  val DefaultPort = 8080
  val MaxLength = 1024
}
