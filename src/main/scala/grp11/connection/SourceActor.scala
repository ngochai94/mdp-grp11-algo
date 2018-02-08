package grp11.connection

import akka.actor.ActorRef
import akka.routing.{ActorRefRoutee, AddRoutee, RemoveRoutee}
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.Request

class SourceActor(forwarder: ActorRef) extends ActorPublisher[String] {
  override def preStart(): Unit = {
    forwarder ! AddRoutee(ActorRefRoutee(self))
  }
  override def receive: Receive = {
    case msg: String => onNext(msg)
    case Request(_) => // Do nothing
    case msg =>
      println(s"Source Actor received unknown msg $msg")
  }

  override def postStop(): Unit = {
    forwarder ! RemoveRoutee(ActorRefRoutee(self))
  }
}
