package grp11.robot

import grp11.geometry.Cell
import grp11.robot.Move._

case class RobotPosition(center: Cell, orientation: Orientation) {
  def applyMove(move: Move): RobotPosition = move match {
    case Forward => RobotPosition(Cell(center.x + orientation.x, center.y + orientation.y), orientation)
    case TurnLeft => RobotPosition(center, orientation.turnLeft)
    case TurnRight => RobotPosition(center, orientation.turnRight)
  }

  def contains(cell: Cell): Boolean = {
    Math.abs(cell.x - center.x) <= 1 && Math.abs(cell.y - center.y) <= 1
  }

  def getHead: Cell = center + orientation * 1
}
