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
    case Down => (robotPosition.center + Cell(-relX, -relX), relOrientation.turnBack)
    case Left => (robotPosition.center + Cell(-relY, relX), relOrientation.turnLeft)
    case Right => (robotPosition.center + Cell(relY, -relX), relOrientation.turnRight)
  }
}
