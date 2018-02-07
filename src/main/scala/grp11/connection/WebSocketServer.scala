package grp11.connection

import akka.NotUsed
import akka.stream.{ActorMaterializer, FlowShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Sink, Source}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage, UpgradeToWebSocket}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.model.HttpMethods._
import akka.actor.{ActorSystem, Props}

import scala.concurrent.duration.HOURS


class WebSocketServer {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val forwarder = system.actorOf(Props[Forwarder], "router")

  def handler: Flow[Message, Message, NotUsed] = {
    Flow.fromGraph(GraphDSL.create() { implicit b: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      // TODO: change to Source.actorRef since actorPublisher is deprecated form 2.5.0
      val source = Source.actorPublisher[String](Props(classOf[SourceActor], forwarder))
      val forwardedSource = b.add(source)
      val merge = b.add(Merge[String](2))
      val filter = b.add(Flow[String].filter(_ => false))
      val broadcast = b.add(Broadcast[String](2))
      val mapMsgToString = b.add(Flow[Message].map[String] {
        case TextMessage.Strict(txt) => txt
        case msg =>
          println(s"handler got unprocessed msg $msg")
          ""
      })
      val filterNotEmpty = b.add(Flow[String].filter(_ != ""))
      val mapStringToMsg = b.add(Flow[String].map[Message](TextMessage.Strict(_)))
      val listenerSink = Sink.actorRef(system.actorOf(Props(classOf[ListenerActor], forwarder)), "Done")

                                          broadcast ~> listenerSink
      mapMsgToString ~> filterNotEmpty ~> broadcast ~> filter ~> merge
                                              forwardedSource ~> merge ~> mapStringToMsg

      FlowShape(mapMsgToString.in, mapStringToMsg.out)
    })
  }

  val requestHandler: HttpRequest => HttpResponse = {
    case req @ HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      req.header[UpgradeToWebSocket] match {
        case Some(upgrade) => upgrade.handleMessages(handler)
        case None          => HttpResponse(400, entity = "Not a valid websocket request!")
      }
    case r: HttpRequest =>
      r.discardEntityBytes()
      HttpResponse(404, entity = "Unknown resource!")
  }

  Http().bindAndHandleSync(requestHandler, interface = "localhost", port = 8080)
  println(s"Server online at http://localhost:8080/\nPress Ctrl+C to stop...")

  def !(msg: Any): Unit = {
    forwarder ! msg
  }
}
