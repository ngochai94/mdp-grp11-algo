package grp11.connection

import akka.actor.Actor
import akka.routing.{AddRoutee, RemoveRoutee, Routee}

class Forwarder extends Actor {
  var routees: Set[Routee] = Set[Routee]()

  def receive = {
    case ar: AddRoutee => routees = routees + ar.routee
    case rr: RemoveRoutee => routees = routees - rr.routee
    case msg =>
      println(s"Router received msg: $msg")
      routees.foreach(_.send(msg, sender))
  }
}
