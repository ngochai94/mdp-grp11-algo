package grp11.robot

sealed abstract class Orientation(val x: Int, val y: Int) {
  def turnLeft: Orientation
  def turnRight: Orientation
}

object Orientation {
  case object Up extends Orientation(0, 1) {
    override def turnLeft: Orientation = Left
    override def turnRight: Orientation = Right
  }

  case object Down extends Orientation(0, -1) {
    override def turnLeft: Orientation = Right
    override def turnRight: Orientation = Left
  }
  case object Left extends Orientation(-1, 0) {
    override def turnLeft: Orientation = Down
    override def turnRight: Orientation = Up
  }

  case object Right extends Orientation(1, 0) {
    override def turnLeft: Orientation = Up
    override def turnRight: Orientation = Down
  }
}
