package grp11.robot

import grp11.geometry.Cell
import grp11.robot.Orientation._

/**
  * relX, relY, relOrientation define the sensor's relative position
  * and orientation when the robot is facing up
  */
case class Sensor(relX: Int, relY: Int, relOrientation: Orientation, range: List[Int]) {
  def getState(robotPosition: RobotPosition): (Cell, Orientation) = robotPosition.orientation match {
    case Up => (robotPosition.center + Cell(relX, relY), relOrientation)
    case Down => (robotPosition.center + Cell(-relX, -relY), relOrientation.turnBack)
    case Left => (robotPosition.center + Cell(-relY, relX), relOrientation.turnLeft)
    case Right => (robotPosition.center + Cell(relY, -relX), relOrientation.turnRight)
  }
}

object Sensor {
  val defaultSensors = List(
    Sensor(1, 1, Up, List(1, 2, 3)), // front right
    Sensor(0, 1, Up, List(1, 2, 3)), // front middle
    Sensor(-1, 1, Up, List(1, 2, 3)), // front left
    Sensor(-1, 1, Left, List(1, 2, 3)), // left front
    Sensor(-1, -1, Left, List(1, 2, 3)), // left back
    Sensor(1, 1, Right, List(1, 2, 3, 4, 5)) // right
  )
}
