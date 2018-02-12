package grp11.connection

import akka.actor.{Actor, ActorRef, Props}
import grp11.geometry.CellState._
import grp11.geometry.{Cell, Maze}
import grp11.robot.{Sensor, VirtualRobot}

import scala.util.Random

class ListenerActor(forwarder: ActorRef) extends Actor {
  var maze: Maze = Maze()
  var moveTime: Int = 300
  var turnTime: Int = 300

  override def receive: Receive = {
    case "shortestpath" =>
      val robot = new VirtualRobot(maze, Sensor.defaultSensors, moveTime, turnTime)
      val snapshot = Random.nextInt
      forwarder ! FwUpdate(snapshot)
      val simulationActor = context.actorOf(Props(classOf[SimulationActor], snapshot, forwarder, robot))
      simulationActor ! ShortestPath

    case "explore" =>
      val robot = new VirtualRobot(maze, Sensor.defaultSensors, moveTime, turnTime)
      val snapshot = Random.nextInt
      forwarder ! FwUpdate(snapshot)
      val simulationActor = context.actorOf(Props(classOf[SimulationActor], snapshot, forwarder, robot))
      simulationActor ! ExploreStart

    case s: String if s.startsWith("movetime") =>
      moveTime = s.split("\n").tail.head.toInt
      println(s"Move time is changed to $moveTime")

    case s: String if s.startsWith("turntime") =>
      turnTime = s.split("\n").tail.head.toInt
      println(s"Turn time is changed to $turnTime")

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
