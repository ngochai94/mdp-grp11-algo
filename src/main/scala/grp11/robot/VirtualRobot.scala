package grp11.robot

import grp11.geometry.CellState.Blocked
import grp11.geometry.{Cell, Maze}
import grp11.robot.Orientation.Up

case class VirtualRobot(finalMaze: Maze, sensors: List[Sensor], moveTime: Int, turnTime: Int) extends Robot {
  val perceivedMaze = Maze()
  var position = RobotPosition(Cell(2, 2), Up)

  override def getPosition: RobotPosition = position

  override def sense(): Unit = {
    sensors.flatMap { sensor =>
      val (pos, orientation) = sensor.getState(position)
      sensor.range
        .map(distance => pos + orientation * distance)
        .filter(perceivedMaze.containsCell(_))
        .foldLeft(List[Cell]()) { case (cells, cell) =>
          if (cells.exists(finalMaze.getState(_) == Blocked)) cells
          else cells :+ cell
        }
    }.map(cell => perceivedMaze.setState(cell, finalMaze.getState(cell)))
  }

  override def move(move: Move): Unit = {
    position = position.applyMove(move)
    move match {
      case Move.Forward => Thread.sleep(moveTime)
      case _ => Thread.sleep(turnTime)
    }
  }

  override def getPerceivedMaze: Maze = perceivedMaze

  override def setPosition(newPosition: RobotPosition): Unit = {
    position = newPosition
  }
}
