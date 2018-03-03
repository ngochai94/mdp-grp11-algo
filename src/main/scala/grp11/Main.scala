package grp11

import grp11.algo.{Dijkstra, WallHugging}
import grp11.geometry.{Cell, Maze}
import grp11.connection._
import grp11.robot.{RealRobot, Sensor, VirtualRobot}
import grp11.utils.Utils

import scala.io.StdIn


object Main extends App {
  Tmp.realRun
//  Tmp.testWS
}

object Tmp {
  val exploreSignal = "startExploration"
  val shortestPathSignal = "startShortestPath"
  val wayPointSignal = "wayPoint"

  def realRun(): Unit = {
    val server = new WebSocketServer
    val rpiConnection = new RpiConnection(RpiConnection.DefaultHost, RpiConnection.DefaultPort)
    val robot = new RealRobot(rpiConnection, server.forwarder)
//    val robot = new VirtualRobot(Maze.emptyMaze, Sensor.defaultSensors, 500, 600)
    var wayPoint = Cell(2, 2)

    while (true) {
      val androidSignal = rpiConnection.receiveAndroid
      if (androidSignal == exploreSignal) {
        val explorer = new WallHugging(robot)
        println("starting real exploration")
        while (!explorer.finished) {
          for {
            move <- explorer.step
          } {
            robot.move(move)
//            server ! FwUpdate(1)
//            server ! FwMessage(1, ClientBoardRepr.toJson(robot.getPosition, robot.getPerceivedMaze))
//            val androidMessage = AndroidBoardRepr.toJson(
//              robot.getPerceivedMaze.getAndroidMap(robot.getPosition),
//              robot.getPerceivedMaze.encodeExplored,
//              robot.getPerceivedMaze.encodeState
//            )
//            rpiConnection.send(AndroidMessage(androidMessage))
          }
        }
        println("finished")
      } else if (androidSignal == shortestPathSignal) {
        println("starting real shortest path")
        val path = Dijkstra(robot.getPerceivedMaze,
          robot.getPosition,
          robot.getPerceivedMaze.getStop,
          robot.getTurnCost,
          Some(wayPoint)
        )
        val moves = Utils.path2Moves(path)
        moves.foreach { move =>
          robot.move(move)
//          server ! FwUpdate(1)
//          server ! FwMessage(1, ClientBoardRepr.toJson(robot.getPosition, robot.getPerceivedMaze))
//          val androidMessage = AndroidBoardRepr.toJson(
//            robot.getPerceivedMaze.getAndroidMap(robot.getPosition),
//            robot.getPerceivedMaze.encodeExplored,
//            robot.getPerceivedMaze.encodeState
//          )
//          rpiConnection.send(AndroidMessage(androidMessage))
        }
        s"Finished shortest path after ${moves.length} moves"
      } else if (androidSignal.startsWith(wayPointSignal)) {
        val coordinates = androidSignal.substring(wayPointSignal.length + 1, androidSignal.length - 1)
          .split(", ").map(_.toInt)
        val x = 1 + coordinates.head
        val y = 20 - coordinates.tail.head
        wayPoint = Cell(x, y)
      }
    }
  }

  def testWS(): Unit = {
    val server = new WebSocketServer
    while (true) {
      val s = StdIn.readLine()
      server ! FwMessage(0, s)
    }
  }

  def testConnectRpi(): Unit = {
    val connection = new RpiConnection(RpiConnection.DefaultHost, RpiConnection.DefaultPort)
    val thread = new Thread {
      override def run(): Unit = {
        var received = " "
        while (received != "") {
          received = connection.receiveArduino
        }
      }
    }
    thread.start()
    while (true) {
      val msg = StdIn.readLine()
      connection.send(ArduinoMessage(msg))
    }
  }
}
