package grp11.algo

import grp11.geometry.Cell
import grp11.robot.{Move, Robot, RobotPosition}
import grp11.utils.Utils

import scala.util.Random

case class Explore(robot: Robot) {
  private[this] val r: Random.type = Random

  def step: RobotPosition = {
    robot.sense()
    if (!robot.getPerceivedMaze.fullyExplored) {
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
      println("Finding way back")
      val path = Dijkstra(robot.getPerceivedMaze, robot.getPosition, Cell(2, 2), 1)
      println(path)
      val moves = Utils.path2Moves(path)
      println(moves)
      robot.move(moves.head)
    }
    println(robot.getPosition)
    robot.getPosition
  }

  def finished: Boolean = robot.getPerceivedMaze.fullyExplored && robot.getPosition.center == Cell(2, 2)
}
