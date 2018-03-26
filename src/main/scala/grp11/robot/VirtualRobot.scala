package grp11.robot

import grp11.geometry.CellState.Blocked
import grp11.geometry.{Cell, Maze}
import grp11.robot.Orientation.Up

class VirtualRobot(finalMaze: Maze, sensors: List[Sensor], moveTime: Int, turnTime: Int) extends Robot {
  private[this] val perceivedMaze = Maze()
  private[this] var position = RobotPosition(Cell(2, 2), Up)

  override def getPosition: RobotPosition = position

  override def getSensors: List[Sensor] = sensors

  override def sense(): Unit = {
    sensors.flatMap { sensor =>
      val (pos, orientation) = sensor.getState(position)
      sensor.range
        .map(distance => pos + orientation * distance)
        .filter(perceivedMaze.containsCell)
        .foldLeft(List[Cell]()) { case (cells, cell) =>
          if (cells.exists(finalMaze.getState(_) == Blocked)) cells
          else cells :+ cell
        }
    }.foreach(cell => perceivedMaze.setState(cell, finalMaze.getState(cell)))
  }

  override def move(moves: List[Move]): Unit = {
    position = position.applyMoves(moves)
    if (!finalMaze.isValidPosition(position)) {
      throw new Exception("move to a invalid position")
    }

    moves.foreach {
      case Move.Forward => Thread.sleep(moveTime)
      case _ => Thread.sleep(turnTime)
    }
  }

  override def getPerceivedMaze: Maze = perceivedMaze

  override def getTurnCost: Double = 1.0 * turnTime / moveTime

  def getFinalMaze: Maze = finalMaze
}
