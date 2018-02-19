package grp11.algo

import grp11.geometry.Cell
import grp11.robot.Move.{Forward, TurnLeft, TurnRight}
import grp11.robot.{Move, Orientation, Robot, RobotPosition}
import grp11.utils.Utils

import scala.collection.mutable

sealed trait Explorer {
  def step: List[Move]
  def finished: Boolean
}

class NearestHelpfulCell(robot: Robot, coverageLimit: Double = 100.0, timeLimit: Long = 360000) extends Explorer {
  private[this] val visited = mutable.HashMap[RobotPosition, Boolean]()
  private[this] val height = robot.getPerceivedMaze.height
  private[this] val width = robot.getPerceivedMaze.width
  private[this] val start = System.currentTimeMillis()

  for {
    row <- 1 to height
    col <- 1 to width
    orientation <- Orientation.all
  } {
    visited(RobotPosition(Cell(col, row), orientation)) = false
  }

  def step: List[Move] = {
    robot.sense()
    if (!shouldFinish) {
      visited(robot.getPosition) = true
      val distanceMap = Dijkstra.getDistanceMap(robot.getPerceivedMaze, robot.getPosition, robot.getTurnCost)
      val target = distanceMap.filterKeys(position => !visited(position))
        .filterKeys(position => robot.getPerceivedMaze.isHelpfulPosition(position, robot.getSensors))
        .minBy(_._2._1)._1
      val path = Dijkstra.getPathWithDistanceMap(distanceMap, robot.getPosition, target)
      val moves = Utils.path2Moves(path)
      List(moves.head)
    } else {
      println("Finding way back")
      val path = Dijkstra(robot.getPerceivedMaze, robot.getPosition, Cell(2, 2), robot.getTurnCost)
      val moves = Utils.path2Moves(path)
      List(moves.head)
    }
  }

  private[this] def shouldFinish: Boolean = robot.getPerceivedMaze.getCoverage >= coverageLimit ||
    System.currentTimeMillis() - start >= timeLimit

  def finished: Boolean = shouldFinish && robot.getPosition.center == Cell(2, 2)
}

class WallHugging(robot: Robot, coverageLimit: Double = 100.0, timeLimit: Long = 360000) extends Explorer {
  private[this] val visited = mutable.HashMap[RobotPosition, Boolean]()
  private[this] val height = robot.getPerceivedMaze.height
  private[this] val width = robot.getPerceivedMaze.width
  private[this] val start = System.currentTimeMillis()
  private[this] var looped = false
//  private[this] val initialPosition = robot.getPosition

  for {
    row <- 1 to height
    col <- 1 to width
    orientation <- Orientation.all
  } {
    visited(RobotPosition(Cell(col, row), orientation)) = false
  }

  def step: List[Move] = {
    robot.sense()
    if (visited(robot.getPosition)) {
      looped = true
    }
    if (!shouldFinish) {
      if (looped) {
        visited(robot.getPosition) = true
        val distanceMap = Dijkstra.getDistanceMap(robot.getPerceivedMaze, robot.getPosition, robot.getTurnCost)
        val target = distanceMap.filterKeys(position => !visited(position))
          .filterKeys(position => robot.getPerceivedMaze.isHelpfulPosition(position, robot.getSensors))
          .minBy(_._2._1)._1
        val path = Dijkstra.getPathWithDistanceMap(distanceMap, robot.getPosition, target)
        val moves = Utils.path2Moves(path)
        List(moves.head)
      } else {
        val position = robot.getPosition
        visited(position) = true
        if (robot.getPerceivedMaze.isValidPosition(position.applyMove(TurnLeft).applyMove(Forward))) {
          List(TurnLeft, Forward)
        } else if (robot.getPerceivedMaze.isValidPosition(position.applyMove(Forward))) {
          List(Forward)
        } else {
          List(TurnRight)
        }
      }
    } else {
      println("Finding way back")
      val path = Dijkstra(robot.getPerceivedMaze, robot.getPosition, Cell(2, 2), robot.getTurnCost)
      val moves = Utils.path2Moves(path)
      List(moves.head)
    }
  }

  private[this] def shouldFinish: Boolean = robot.getPerceivedMaze.getCoverage >= coverageLimit ||
    System.currentTimeMillis() - start >= timeLimit

  def finished: Boolean = shouldFinish && robot.getPosition.center == Cell(2, 2)
}
