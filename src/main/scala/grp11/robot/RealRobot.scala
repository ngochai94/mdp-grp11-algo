package grp11.robot
import akka.actor.ActorRef
import grp11.connection._
import grp11.geometry.{Cell, CellState, Maze}
import grp11.robot.Move.{Forward, TurnLeft, TurnRight}
import grp11.robot.Orientation.Up

import scala.util.Random

class RealRobot(connection: RpiConnection, forwarder: ActorRef) extends Robot {
  private[this] var position = RobotPosition(Cell(2, 2), Up)
  private[this] val perceivedMaze = Maze()
  private[this] val snapshot = Random.nextInt
  import RealRobot._

  override def getPosition: RobotPosition = position

  override def getSensors: List[Sensor] = Sensor.defaultSensors

  override def sense(): Unit = {
    connection.send(ArduinoMessage(senseCommand))
    val receivedMsg = connection.receiveArduino
    val senseResults = receivedMsg.split(senseResultSeparator).map(_.toInt).toList
    if (senseResults.lengthCompare(getSensors.length) != 0) {
      println(s"Malformed sense results: $receivedMsg")
      sense()
    } else {
      senseResults.zip(getSensors).foreach { case (distance, sensor) =>
        val (pos, orientation) = sensor.getState(position)
        val emptyCells = if (distance == 0) sensor.range.length else distance - 1
        sensor.range
          .map(distance => pos + orientation * distance)
          .take(emptyCells)
          .filter(perceivedMaze.containsCell)
          .foreach(cell => perceivedMaze.setState(cell, CellState.Empty))
        if (distance != 0) {
          val obstacle = pos + orientation * distance
          if (perceivedMaze.containsCell(obstacle)) {
            perceivedMaze.setState(obstacle, CellState.Blocked)
          }
        }
      }
      forwarder ! FwUpdate(snapshot)
      forwarder ! FwMessage(snapshot, ClientBoardRepr.toJson(getPosition, getPerceivedMaze))
    }
  }

  override def move(move: Move): Unit = {
    position = position.applyMove(move)
    if (!perceivedMaze.isValidPosition(position)) {
      throw new Exception("move to a invalid position")
    }
    move match {
      case TurnLeft => connection.send(ArduinoMessage("L"))
      case TurnRight => connection.send(ArduinoMessage("R"))
      case Forward => connection.send(ArduinoMessage("S"))
    }
    forwarder ! FwUpdate(snapshot)
    forwarder ! FwMessage(snapshot, ClientBoardRepr.toJson(getPosition, getPerceivedMaze))
    connection.send(AndroidMessage(getPerceivedMaze.getAndroidMap(getPosition)))
  }

  override def getPerceivedMaze: Maze = perceivedMaze

  override def getTurnCost: Double = 1.0
}

object RealRobot {
  val senseCommand = "sense"
  val senseResultSeparator = ":"
}
