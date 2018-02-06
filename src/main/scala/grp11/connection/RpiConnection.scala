package grp11.connection

import java.net._
import java.io._

case class RpiConnection(host: String, port: Int) {
  val s = new Socket(InetAddress.getByName(host), port)
  println("Connected!")
  val in = new DataInputStream(s.getInputStream)
  val out = new PrintStream(s.getOutputStream)

  def send(msg: String): Unit = {
    out.print(msg)
    out.flush()
  }

  def receive: String = {
    val b = Array.ofDim[Byte](RpiConnection.MaxLength)
    in.read(b)
    b.map(_.toChar).mkString
  }

  def close(): Unit = s.close()
}

object RpiConnection {
  val DefaultHost = "172.21.147.242"
  val DefaultPort = 8080
  val MaxLength = 1024
}
