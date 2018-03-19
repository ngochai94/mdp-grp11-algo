package grp11

import grp11.algo.{Dijkstra, NearestHelpfulCell, WallHugging}
import grp11.geometry.{Cell, CellState, Maze}
import grp11.connection._
import grp11.robot.Move.{Forward, TurnLeft, TurnRight}
import grp11.robot.{RealRobot, Sensor, VirtualRobot}
import grp11.utils.Utils

import scala.io.StdIn


object Main extends App {
  Tmp.realRun
//  Tmp.testWS
//  Tmp.testConnectRpi()
}

object Tmp {
  val exploreSignal = "a"
  val shortestPathSignal = "b"
  val wayPointSignal = "c"

  def realRun(): Unit = {
    val server = new WebSocketServer
    val rpiConnection = new RpiConnection(RpiConnection.DefaultHost, RpiConnection.DefaultPort)
    var robot = new RealRobot(rpiConnection, server.forwarder)
    var wayPoint = Cell(2, 2)
    var explorationDone = false

    while (true) {
      val androidSignal = rpiConnection.receiveAndroid
      if (androidSignal == exploreSignal && !explorationDone) {
        explorationDone = true
        robot = new RealRobot(rpiConnection, server.forwarder)
        val explorer = new WallHugging(robot)
        //        val explorer = new NearestHelpfulCell(robot)
        println("starting real exploration")
        val start = System.currentTimeMillis
        while (!explorer.finished) {
          for {
            move <- explorer.step
          } {
            robot.move(move)
          }
        }
        val elapsed = (System.currentTimeMillis - start) / 1000
        rpiConnection.send(AndroidMessage(AndroidExplorationTimeRepr.toJson(s"${elapsed / 60}m ${elapsed % 60}s")))
        println(robot.getPerceivedMaze.encodeExplored)
        println(robot.getPerceivedMaze.encodeState)
        println(s"finished in $elapsed seconds")
        rpiConnection.send(ArduinoMessage("C")) // calibrate after exploration
      } else if (androidSignal == shortestPathSignal) {
        println(s"starting real shortest path with waypoint = $wayPoint")
        val start = System.currentTimeMillis
        val path = Dijkstra(robot.getPerceivedMaze,
          robot.getPosition,
          robot.getPerceivedMaze.getStop,
          robot.getTurnCost,
          Some(wayPoint)
        )
        println("finished calculation")
        val moves = Utils.path2Moves(path)
        println("finished getting moves")
//        moves.foreach { move =>
//          robot.move(move)
//        }
        val msg = moves.map {
          case Forward => "F"
          case TurnRight => "R"
          case TurnLeft => "L"
        }.mkString
        println(s"Sending bulk msg $msg")
        rpiConnection.send(ArduinoMessage(msg))
        val elapsed = (System.currentTimeMillis - start) / 1000
        rpiConnection.send(AndroidMessage(AndroidShortestPathTimeRepr.toJson(s"${elapsed / 60}m ${elapsed % 60}s")))
        s"Finished shortest path after ${moves.length} moves"
      } else if (androidSignal.startsWith(wayPointSignal)) {
        val coordinates = androidSignal.substring(wayPointSignal.length + 1, androidSignal.length - 1)
          .split(", ").map(_.toInt)
        val x = 1 + coordinates.head
        val y = 20 - coordinates.tail.head
        wayPoint = Cell(x, y)
      } else {
        println("Forwarding msg to arduino...")
        rpiConnection.send(ArduinoMessage(androidSignal))
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
      connection.send(AndroidMessage(msg))
      connection.send(ArduinoMessage(msg))
    }
  }
}
