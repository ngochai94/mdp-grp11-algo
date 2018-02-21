package grp11.connection

import java.net._
import java.io._

case class RpiConnection(host: String, port: Int) {
  val s = new Socket(InetAddress.getByName(host), port)
  println("RPI is connected!")
  val in = new DataInputStream(s.getInputStream)
  val out = new PrintStream(s.getOutputStream)

  def send(msg: RpiMessage): Unit = {
    out.print(msg.toString)
    out.flush()
    println(s"Sent $msg")
  }

  def receive: String = {
    val b = Array.ofDim[Byte](RpiConnection.MaxLength)
    in.read(b)
    val s = b.map(_.toChar).mkString
    println(s"Received |$s|")
    s.trim
  }

  def close(): Unit = s.close()
}

object RpiConnection {
//  val DefaultHost = "172.21.147.242"
  val DefaultHost = "127.0.0.1"
  val DefaultPort = 8080
  val MaxLength = 1024
}
