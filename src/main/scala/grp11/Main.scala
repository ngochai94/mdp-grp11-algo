package grp11

import grp11.algo.{Dijkstra, NearestHelpfulCell, WallHugging}
import grp11.geometry.Cell
import grp11.connection._
import grp11.robot.Move.{Forward, TurnLeft, TurnRight}
import grp11.robot.{Move, RealRobot}
import grp11.utils.Utils

import scala.io.StdIn


object Main extends App {
  val task = sys.env.getOrElse("TASK", "default")
  val fakeAndroid = sys.env.get("FAKE_ANDROID").isDefined
  val algo = sys.env.getOrElse("ALGO", "0").toInt
  val burst = sys.env.get("BURST").isDefined

  if (task == "simulation") {
    TaskRunner.testWS()
  } else if (task == "manual") {
    TaskRunner.testConnectRpi(burst)
  } else {
    TaskRunner.realRun(fakeAndroid, algo, burst)
  }
}

object TaskRunner {
  val exploreSignal = "a"
  val shortestPathSignal = "b"
  val wayPointSignal = "c"

  def realRun(fakeAndroid: Boolean, algo: Int, burst: Boolean): Unit = {
    val server = new WebSocketServer
    val rpiConnection = new RpiConnection(RpiConnection.DefaultHost, RpiConnection.DefaultPort, fakeAndroid)
    var robot = new RealRobot(rpiConnection, server.forwarder)
    var wayPoint = Cell(2, 2)
    var explorationDone = false

    while (true) {
      val androidSignal = rpiConnection.receiveAndroid
      if (androidSignal == exploreSignal && !explorationDone) {
        explorationDone = true
        robot = new RealRobot(rpiConnection, server.forwarder)

        val explorer = if (algo == 1) {
          new NearestHelpfulCell(robot)
        } else {
          new WallHugging(robot, burst = burst)
        }

        println("starting real exploration")
        val start = System.currentTimeMillis

        Iterator.continually{
          robot.move(explorer.step)
        }.takeWhile(_ => !explorer.finished)
          .toList

        val elapsed = (System.currentTimeMillis - start) / 1000
        rpiConnection.send(AndroidMessage(AndroidExplorationTimeRepr.toJson(s"${elapsed / 60}m ${elapsed % 60}s")))
        println(robot.getPerceivedMaze.encodeExplored)
        println(robot.getPerceivedMaze.encodeState)
        println(s"finished in ${elapsed / 60}m ${elapsed % 60}s")
        rpiConnection.send(ArduinoMessage("RRRR")) // calibrate after exploration
      } else if (androidSignal == shortestPathSignal) {
        println(s"starting real shortest path with waypoint = $wayPoint")
        val start = System.currentTimeMillis
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

  def testConnectRpi(burst: Boolean): Unit = {
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
    }
  }
}
