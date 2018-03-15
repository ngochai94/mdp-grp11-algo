package grp11.robot
import akka.actor.ActorRef
import grp11.connection._
import grp11.geometry.{Cell, CellState, Maze}
import grp11.robot.Move.{Forward, TurnLeft, TurnRight}
import grp11.robot.Orientation.Up

import scala.io.StdIn
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
          if (distance == 0 || distance > 4) {
            List(1, 2, 3, 4)
              .map(distance => pos + orientation * distance)
              .filter(perceivedMaze.containsCell)
              .foreach(cell => perceivedMaze.setState(cell, CellState.Empty))
          //} else if (distance == 3 || distance == 4) {
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

  override def move(move: Move): Unit = {
    //StdIn.readLine
    position = position.applyMove(move)
    if (!perceivedMaze.isValidPosition(position)) {
      throw new Exception(s"move to an invalid position $position")
    }
    move match {
      case TurnLeft => connection.send(ArduinoMessage(turnLeftCommand))
      case TurnRight => connection.send(ArduinoMessage(turnRightCommand))
      case Forward => connection.send(ArduinoMessage(goStraightCommand))
    }
    forwarder ! FwUpdate(snapshot)
    forwarder ! FwMessage(snapshot, ClientBoardRepr.toJson(position, perceivedMaze))

    val androidMessage = AndroidBoardRepr.toJson(
      perceivedMaze.getAndroidMap(position), perceivedMaze.encodeExplored, perceivedMaze.encodeState)
    connection.send(AndroidMessage(androidMessage))
  }

  override def getPerceivedMaze: Maze = perceivedMaze

  override def getTurnCost: Double = 1.0
}

object RealRobot {
  val senseCommand = "A"
  val turnLeftCommand = "L"
  val turnRightCommand = "R"
  val goStraightCommand = "F"
  val senseResultSeparator = ":"
}
