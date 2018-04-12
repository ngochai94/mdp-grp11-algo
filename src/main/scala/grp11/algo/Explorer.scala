package grp11.algo

import grp11.geometry.Cell
import grp11.robot.Move.{Forward, TurnLeft, TurnRight}
import grp11.robot.{Move, Orientation, Robot, RobotPosition}
import grp11.utils.Utils

import scala.collection.mutable

sealed abstract class Explorer(robot: Robot, coverageLimit: Double, timeLimit: Long) {
  private[algo] val visited = mutable.HashMap[RobotPosition, Int]()
  private[this] val height = robot.getPerceivedMaze.getHeight
  private[this] val width = robot.getPerceivedMaze.getWidth
  private[algo] var forcedStop = false

  for {
    row <- 1 to height
    col <- 1 to width
    orientation <- Orientation.all
  } {
    visited(RobotPosition(Cell(col, row), orientation)) = 0
  }

  private[this] val start = System.currentTimeMillis()
  private[algo] def shouldFinish: Boolean = forcedStop || robot.getPerceivedMaze.getCoverage >= coverageLimit ||
    System.currentTimeMillis() - start >= timeLimit
  def finished: Boolean = shouldFinish && robot.getPosition == RobotPosition(Cell(2, 2), Orientation.Up)

  def getSingleMove(pos: RobotPosition): List[Move] = {
    if (robot.getPerceivedMaze.isValidPosition(pos.applyMove(TurnLeft).applyMove(Forward))) {
      List(TurnLeft, Forward)
    } else if (robot.getPerceivedMaze.isValidPosition(pos.applyMove(Forward))) {
      List(Forward)
    } else {
      List(TurnRight)
    }
  }

  def stepToNearestUnexploredArea(): List[Move] = {
    val distanceMap = Dijkstra.getDistanceMap(robot.getPerceivedMaze, robot.getPosition, robot.getTurnCost)
    val targets = distanceMap.filterKeys(position => visited(position) == 0)
      .filterKeys(position => robot.getPerceivedMaze.isHelpfulPosition(position, robot.getSensors))
    val target = if (targets.isEmpty) {
      forcedStop = true
      RobotPosition(Cell(2, 2), Orientation.Up)
    } else {
      targets.minBy(_._2._1)._1
    }
    val path = Dijkstra.getPathWithDistanceMap(distanceMap, robot.getPosition, target)
    Utils.path2Moves(path)
  }

  def step: List[Move] = {
    visited(robot.getPosition) = visited(robot.getPosition) + 1
    robot.sense()
    if (!shouldFinish) {
      explore()
    } else {
      val distanceMap = Dijkstra.getDistanceMap(robot.getPerceivedMaze, robot.getPosition, robot.getTurnCost)
      val position = RobotPosition(Cell(2, 2), Orientation.Up)
      val path = Dijkstra.getPathWithDistanceMap(distanceMap, robot.getPosition, position)
      val moves = Utils.path2Moves(path)
      moves
    }
  }

  def explore(): List[Move]
}

class NearestHelpfulCell(robot: Robot, coverageLimit: Double = 100.0, timeLimit: Long = 360000)
  extends Explorer(robot, coverageLimit, timeLimit) {

  def explore(): List[Move] = stepToNearestUnexploredArea()
}

class WallHugging(robot: Robot, coverageLimit: Double = 100.0, timeLimit: Long = 360000, burst: Boolean = false)
  extends Explorer(robot, coverageLimit, timeLimit) {

  private[this] var looped = false

  def explore(): List[Move] = {
    if (visited(robot.getPosition) > 1) {
      looped = true
    }
    if (looped) {
      stepToNearestUnexploredArea()
    } else {
      val position = robot.getPosition
      if (!burst) {
        getSingleMove(position)
      } else {
        val preMoves = Iterator.iterate((List[Move](), position)) { case (_, pos) =>
          val moves = getSingleMove(pos)
          (moves, pos.applyMoves(moves))
        }.takeWhile(x => !robot.getPerceivedMaze.isHelpfulPosition(x._2, robot.getSensors))
          .flatMap(_._1)
          .toList
        val target = position.applyMoves(preMoves)
        val distanceMap = Dijkstra.getDistanceMap(robot.getPerceivedMaze, robot.getPosition, robot.getTurnCost)
        val path = Dijkstra.getPathWithDistanceMap(distanceMap, robot.getPosition, target)
        Utils.path2Moves(path) ++ getSingleMove(position.applyMoves(preMoves))
      }
    }
  }
}

class Hybrid(robot: Robot, coverageLimit: Double = 100.0, timeLimit: Long = 360000, switchThreshold: Int = 50)
  extends Explorer(robot, coverageLimit, timeLimit) {

  private[this] var hugging = true

  def explore(): List[Move] = {
    println("==========================")
    println(hugging)
    if (!hugging) {
      stepToNearestUnexploredArea()
    } else {
      println(robot.getPerceivedMaze.draw())
      val position = robot.getPosition
      val preMoves = Iterator.iterate((List[Move](), position)) { case (_, pos) =>
        val moves = getSingleMove(pos)
        (moves, pos.applyMoves(moves))
      }.takeWhile(x => !robot.getPerceivedMaze.isHelpfulPosition(x._2, robot.getSensors))
        .take(switchThreshold)
        .flatMap(_._1)
        .take(switchThreshold)
        .toList

      println(preMoves.length)
      if (preMoves.lengthCompare(switchThreshold) == 0) {
        hugging = false
        println("Switching mode...")
        stepToNearestUnexploredArea()
      } else {
        val target = position.applyMoves(preMoves)
        val distanceMap = Dijkstra.getDistanceMap(robot.getPerceivedMaze, robot.getPosition, robot.getTurnCost)
        val path = Dijkstra.getPathWithDistanceMap(distanceMap, robot.getPosition, target)
        println(s"target = $target")
        println(s"path = $path")
        Utils.path2Moves(path) ++ getSingleMove(position.applyMoves(preMoves))
      }
    }
  }
}
