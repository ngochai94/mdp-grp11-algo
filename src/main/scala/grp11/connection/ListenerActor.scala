package grp11.connection

import akka.actor.{Actor, ActorRef, Props}
import grp11.geometry.CellState._
import grp11.geometry.{Cell, Maze}
import grp11.robot.{Sensor, VirtualRobot}

import scala.util.Random

class ListenerActor(forwarder: ActorRef) extends Actor {
  var maze: Maze = Maze()
  var moveTime: Int = 100
  var turnTime: Int = 120
  var coverageLimit: Double = 100.0
  var timeLimit: Long = 360000
  var wayPointX: Option[Int] = None
  var wayPointY: Option[Int] = None
  var explorer: String = "wall"

  override def receive: Receive = {
    case "shortestpath" =>
      val robot = new VirtualRobot(maze, Sensor.defaultSensors, moveTime, turnTime)
      val snapshot = Random.nextInt
      forwarder ! FwUpdate(snapshot)
      val simulationActor = context.actorOf(Props(classOf[SimulationActor], snapshot, forwarder, robot))
      val wayPoint = for {
        x <- wayPointX
        y <- wayPointY
      } yield Cell(x, y)
      simulationActor ! ShortestPath(wayPoint)

    case "explore" =>
      val robot = new VirtualRobot(maze, Sensor.defaultSensors, moveTime, turnTime)
      val snapshot = Random.nextInt
      forwarder ! FwUpdate(snapshot)
      val simulationActor = context.actorOf(Props(classOf[SimulationActor], snapshot, forwarder, robot))
      simulationActor ! ExploreStart(coverageLimit, timeLimit, explorer)

    case s: String if s.startsWith("explorer") =>
      explorer = s.split("\n").tail.head
      println(s"Explorer is changed to $explorer")

    case s: String if s.startsWith("movetime") =>
      moveTime = s.split("\n").tail.head.toInt
      println(s"Move time is changed to $moveTime")

    case s: String if s.startsWith("turntime") =>
      turnTime = s.split("\n").tail.head.toInt
      println(s"Turn time is changed to $turnTime")

    case s: String if s.startsWith("coverage") =>
      coverageLimit = s.split("\n").tail.head.toDouble
      println(s"Coverage limit is changed to $coverageLimit")

    case s: String if s.startsWith("time") =>
      timeLimit = s.split("\n").tail.head.toLong * 1000
      println(s"Time limit is changed to $timeLimit")

    case s: String if s.startsWith("waypointx") =>
      val arg = s.split("\n").tail.headOption
      wayPointX = arg.map(_.toInt)
      println(s"Way Point X is changed to $wayPointX")

    case s: String if s.startsWith("waypointy") =>
      val arg = s.split("\n").tail.headOption
      wayPointY = arg.map(_.toInt)
      println(s"Way Point Y is changed to $wayPointY")

    case s: String if s.startsWith("map") =>
      val map = s.split("\n").tail
      for {
        row <- 1 to map.length
        col <- 1 to map(0).length
        state = map(row - 1)(col - 1) match {
          case '0' => Unknown
          case '1' => Empty
          case '2' => Blocked
        }
      } {
        maze.setState(Cell(col, row), state)
      }
    case msg =>
      println(s"Listener got unknown message $msg")
  }

}
