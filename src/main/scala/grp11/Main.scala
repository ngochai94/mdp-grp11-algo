package grp11

import grp11.algo.{Dijkstra, Hybrid, NearestHelpfulCell, WallHugging}
import grp11.geometry.Cell
import grp11.connection._
import grp11.robot.Move.{Forward, TurnLeft, TurnRight}
import grp11.robot.RealRobot
import grp11.utils.Utils

import scala.io.StdIn


object Main extends App {
  val task = sys.env.getOrElse("TASK", "default")
  val fakeAndroid = sys.env.get("FAKE_ANDROID").isDefined
  val fakeRpi = sys.env.get("FAKE_RPI").isDefined
  val algo = sys.env.getOrElse("ALGO", "0").toInt
  val burst = sys.env.get("BURST").isDefined

  if (task == "simulation") {
    TaskRunner.testWS()
  } else if (task == "manual") {
    TaskRunner.testConnectRpi(burst)
  } else {
    TaskRunner.realRun(fakeAndroid, fakeRpi, algo)
  }
}

object TaskRunner {
  val exploreSignal = "a"
  val shortestPathSignal = "b"
  val wayPointSignal = "c"
  val restartSignal = "d"

  def realRun(fakeAndroid: Boolean, fakeRpi: Boolean, algo: Int): Unit = {
    val server = new WebSocketServer

    val rpiConnection = if (fakeRpi) new RpiConnection(RpiConnection.LocalHost, RpiConnection.DefaultPort, fakeAndroid)
    else new RpiConnection(RpiConnection.DefaultHost, RpiConnection.DefaultPort, fakeAndroid)

    Iterator.iterate((new RealRobot(rpiConnection, server.forwarder), Cell(2, 2), false)) {
      case (robot, wayPoint, blockExploration) =>
        process(robot, wayPoint, blockExploration, rpiConnection, server, algo, rpiConnection.receiveAndroid)
    }.toList
  }

  private[this] def process(
    robot: RealRobot,
    wayPoint: Cell,
    blockExploration: Boolean,
    rpiConnection: RpiConnection,
    server: WebSocketServer,
    algo: Int,
    androidSignal: String)
  : (RealRobot, Cell, Boolean) = {
    androidSignal match {
      case `exploreSignal` if !blockExploration =>
        val robot = new RealRobot(rpiConnection, server.forwarder)
        val explorer = algo match {
          case 1 => new NearestHelpfulCell(robot)
          case 2 => new Hybrid(robot)
          case _ => new WallHugging(robot, burst = true)
        }
        println("starting real exploration")
        val start = System.currentTimeMillis

        Iterator.continually{
          robot.move(explorer.step)
        }.takeWhile(_ => !explorer.finished)
          .toList

        val elapsed = (System.currentTimeMillis - start) / 1000
        println(robot.getPerceivedMaze.encodeExplored)
        println(robot.getPerceivedMaze.encodeState)
        println(s"finished in ${elapsed / 60}m ${elapsed % 60}s")

        rpiConnection.send(ArduinoMessage(RealRobot.stopCalibration))
        rpiConnection.send(AndroidMessage(AndroidBoardRepr.toJson(
          robot.getPerceivedMaze.getAndroidMap(robot.getPosition),
          robot.getPerceivedMaze.encodeExplored,
          robot.getPerceivedMaze.encodeState
        )))
        (robot, wayPoint, true)

      case `shortestPathSignal` =>
        println(s"starting real shortest path with waypoint = $wayPoint")
        val path = Dijkstra(robot.getPerceivedMaze,
          robot.getPosition,
          robot.getPerceivedMaze.getStop,
          robot.getTurnCost,
          Some(wayPoint)
        )
        val moves = Utils.path2Moves(path)
        val msg = moves.map {
          case Forward => RealRobot.goStraightCommand
          case TurnRight => RealRobot.turnRightCommand
          case TurnLeft => RealRobot.turnLeftCommand
        }.mkString
        println(s"Sending bulk msg $msg")
        rpiConnection.send(ArduinoMessage(msg))
        (robot, wayPoint, blockExploration)

      case _ if androidSignal.startsWith(wayPointSignal) =>
        val coordinates = androidSignal.substring(wayPointSignal.length + 1, androidSignal.length - 1)
          .split(", ").map(_.toInt)
        // This extra computation arises from the mismatched representation between Algo and Android teams
        val x = 1 + coordinates.head
        val y = 20 - coordinates.tail.head
        (robot, Cell(x, y), blockExploration)

      case `restartSignal` =>
        println("Resetting...")
        (robot, wayPoint, false)

      case _ =>
        println("Forwarding msg to arduino...")
        rpiConnection.send(ArduinoMessage(androidSignal))
        (robot, wayPoint, blockExploration)
    }
  }

  def testWS(): Unit = {
    val server = new WebSocketServer
    while (true) {
      val s = StdIn.readLine()
      server ! FwMessage(0, s)
    }
  }

  def testConnectRpi(burst: Boolean): Unit = {
    val connection = new RpiConnection(RpiConnection.DefaultHost, RpiConnection.DefaultPort)
    Iterator.continually{
      val msg = StdIn.readLine()
      if (!burst) {
        for {
          c <- msg
        } {
          connection.send(ArduinoMessage("" + c))
          Thread.sleep(100)
        }
      } else {
        connection.send(ArduinoMessage(msg))
      }
    }.toList
  }
}
