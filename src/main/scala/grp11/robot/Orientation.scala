package grp11.robot

import grp11.geometry.Cell

case class Orientation(x: Int, y: Int) {
  def turnLeft = Orientation(-y, x)
  def turnRight = Orientation(y, -x)
  def turnBack = Orientation(-y, -x)
  def *(num: Int): Cell = Cell(x * num, y * num)
}

object Orientation {
  object Up extends Orientation(0, 1)
  object Down extends Orientation(0, -1)
  object Left extends Orientation(-1, 0)
  object Right extends Orientation(1, 0)

  val all = List(Up, Down, Left, Right)
}
