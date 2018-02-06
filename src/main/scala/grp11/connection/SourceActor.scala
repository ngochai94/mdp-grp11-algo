package grp11.connection

import akka.actor.ActorRef
import akka.routing.{ActorRefRoutee, AddRoutee}
import akka.stream.actor.ActorPublisher

class SourceActor(actor: ActorRef) extends ActorPublisher[String] {
  override def preStart(): Unit = {
    actor ! AddRoutee(ActorRefRoutee(self))
  }
  override def receive: Receive = {
    case msg: String => onNext(msg)
    case msg =>
      println(s"Source Actor received unknown msg $msg")
  }
}
