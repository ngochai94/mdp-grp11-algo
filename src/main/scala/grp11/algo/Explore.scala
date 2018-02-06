package grp11.algo

import grp11.geometry.Cell
import grp11.robot.{Move, Robot, RobotPosition}

case class Explore(robot: Robot) {
  val r = scala.util.Random
  def step: RobotPosition = {
    robot.sense()
    if (robot.getPerceivedMaze.getCoverage != 300) {
      if (robot.getPerceivedMaze.isValidPosition(robot.getPosition.applyMove(Move.Forward))) {
        r.nextFloat match {
          case f if f < 0.8 => robot.move(Move.Forward)
          case f if f < 0.9 => robot.move(Move.TurnRight)
          case _ => robot.move(Move.TurnLeft)
        }
      } else {
        r.nextInt(2) match {
          case 0 => robot.move(Move.TurnRight)
          case _ => robot.move(Move.TurnLeft)
        }
      }
    } else {
      val path = Dijkstra(robot.getPerceivedMaze, robot.getPosition, Cell(2, 2), 1)
      robot.setPosition(path.tail.head)
    }
    robot.getPosition
  }
}
