package grp11.connection

import akka.NotUsed
import akka.stream.{ActorMaterializer, ClosedShape, FlowShape}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Source}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage, UpgradeToWebSocket}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.model.HttpMethods._
import akka.actor.{ActorSystem, Props}


object WebSocketServer {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val forwarder = system.actorOf(Props[Forwarder], "router")
  val source = Source.actorPublisher[String](Props(classOf[SourceActor], forwarder))

  def handler: Flow[Message, Message, NotUsed] = {
    Flow.fromGraph(GraphDSL.create() { implicit b: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val merge = b.add(Merge[String](2))
      val filter = b.add(Flow[String].filter(_ => false))
      val mapMsgToString = b.add(Flow[Message].map[String](_ => ""))
      val mapStringToMsg = b.add(Flow[String].map[Message](TextMessage.Strict(_)))
      val statsSource = b.add(source)

      mapMsgToString ~> filter ~> merge
      statsSource ~> merge ~> mapStringToMsg

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
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

  def !(msg: String): Unit = {
    forwarder ! msg
  }
}
