package grp11.connection

import akka.actor.Actor
import akka.routing.{AddRoutee, RemoveRoutee, Routee}

class Forwarder extends Actor {
  var routees: Set[Routee] = Set[Routee]()
  var snapshot: Int = 0

  override def receive: Receive = {
    case ar: AddRoutee => routees = routees + ar.routee
    case rr: RemoveRoutee => routees = routees - rr.routee
    case FwUpdate(sn) => snapshot = sn
    case FwMessage(sn, msg) if sn == snapshot =>
      routees.foreach(_.send(msg, sender))
    case msg =>
      println(s"Forwarder discarded msg: $msg")
  }
}

sealed trait ForwarderMessage
case class FwUpdate(snapshot: Int) extends ForwarderMessage
case class FwMessage(snapshot: Int, msg: String) extends ForwarderMessage