package grp11.robot

import grp11.geometry.Cell

sealed abstract class Orientation(val x: Int, val y: Int) {
  def turnLeft: Orientation
  def turnRight: Orientation
  def turnBack: Orientation
  def *(num: Int): Cell = Cell(x * num, y * num)
}

object Orientation {
  case object Up extends Orientation(0, 1) {
    override def turnLeft: Orientation = Left
    override def turnRight: Orientation = Right
    override def turnBack: Orientation = Down
  }

  case object Down extends Orientation(0, -1) {
    override def turnLeft: Orientation = Right
    override def turnRight: Orientation = Left
    override def turnBack: Orientation = Up
  }
  case object Left extends Orientation(-1, 0) {
    override def turnLeft: Orientation = Down
    override def turnRight: Orientation = Up
    override def turnBack: Orientation = Right
  }

  case object Right extends Orientation(1, 0) {
    override def turnLeft: Orientation = Up
    override def turnRight: Orientation = Down
    override def turnBack: Orientation = Left
  }
}
