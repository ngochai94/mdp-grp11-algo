package grp11.robot
import akka.actor.ActorRef
import grp11.connection._
import grp11.geometry.{Cell, CellState, Maze}
import grp11.robot.Move.{Forward, TurnLeft, TurnRight}
import grp11.robot.Orientation.Up

import scala.util.Random

class RealRobot(connection: RpiConnection, forwarder: ActorRef, block1: Boolean, block2: Boolean) extends Robot {
  private[this] var position = RobotPosition(Cell(2, 2), Up)
  private[this] val perceivedMaze = Maze(block1, block2)
  private[this] val snapshot = Random.nextInt
  import RealRobot._

  override def getPosition: RobotPosition = position

  override def getSensors: List[Sensor] = Sensor.defaultSensors

  override def sense(): Unit = {
    connection.send(ArduinoMessage(senseCommand))
    val receivedMsg = connection.receiveArduino
    val rawSenseResults = receivedMsg.split(senseResultSeparator)
    if (rawSenseResults.lengthCompare(getSensors.length) != 0) {
      println(s"Malformed sense results: $receivedMsg")
      sense()
    } else {
      val senseResults = rawSenseResults.map(_.toInt).toList
      senseResults.zip(getSensors).foreach { case (distance, sensor) =>
        val (pos, orientation) = sensor.getState(position)
        if (sensor.range.lengthCompare(3) <= 0) {
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
        } else { // handle long sensor differently due to lower accuracy
          val trustRange = sensor.range.length
          if (distance == 0 || distance > trustRange) {
            (1 to trustRange).toList
              .map(distance => pos + orientation * distance)
              .filter(perceivedMaze.containsCell)
              .foreach(cell => perceivedMaze.setState(cell, CellState.Empty))
          } else {
            (1 until distance)
              .toList
              .map(distance => pos + orientation * distance)
              .filter(perceivedMaze.containsCell)
              .foreach(cell => perceivedMaze.setState(cell, CellState.Empty))
            val obstacle = pos + orientation * distance
            if (perceivedMaze.containsCell(obstacle)) {
              perceivedMaze.setState(obstacle, CellState.Blocked)
            }
          }
        }
      }
      forwarder ! FwUpdate(snapshot)
      forwarder ! FwMessage(snapshot, ClientBoardRepr.toJson(getPosition, getPerceivedMaze))
    }
  }

  override def move(moves: List[Move]): Unit = {
    //StdIn.readLine
    position = position.applyMoves(moves)
    if (!perceivedMaze.isValidPosition(position)) {
      throw new Exception(s"move to an invalid position $position")
    }
    val arduinoMessage = moves.map {
      case TurnLeft => turnLeftCommand
      case TurnRight => turnRightCommand
      case Forward => goStraightCommand
    }.mkString
    connection.send(ArduinoMessage(arduinoMessage))

    forwarder ! FwUpdate(snapshot)
    forwarder ! FwMessage(snapshot, ClientBoardRepr.toJson(position, perceivedMaze))

    val androidMessage = AndroidBoardRepr.toJson(perceivedMaze.getAndroidMap(position))
    connection.send(AndroidMessage(androidMessage))
  }

  override def getPerceivedMaze: Maze = perceivedMaze

  override def getTurnCost: Double = 1.5
}

object RealRobot {
  val senseCommand = "A"
  val turnLeftCommand = "L"
  val turnRightCommand = "R"
  val goStraightCommand = "F"
  val stopCalibration = "C"
  val senseResultSeparator = ":"
}
