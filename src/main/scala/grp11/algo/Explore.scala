package grp11.algo

import grp11.geometry.Cell
import grp11.robot.{Orientation, Robot, RobotPosition}
import grp11.utils.Utils

import scala.collection.mutable
import scala.util.Random

class Explore(robot: Robot, coverageLimit: Double = 100.0, timeLimit: Long = 360000) {
  private[this] val r: Random.type = Random
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

  // initial position is visited
  visited(robot.getPosition) = true

  def step: RobotPosition = {
    robot.sense()
    if (!shouldFinish) {
      println("-----------------------------------------------")
      println(robot.getPerceivedMaze.draw())
      visited(robot.getPosition) = true
      val distanceMap = Dijkstra.getDistanceMap(robot.getPerceivedMaze, robot.getPosition, robot.getTurnCost)
      val target = distanceMap.filterKeys(position => !visited(position))
        .filterKeys(position => robot.getPerceivedMaze.isHelpfulPosition(position, robot.getSensors))
        .minBy(_._2._1)._1
      val position = RobotPosition(Cell(14, 19), Orientation.Down)
      val cellsUnder = for {
        sensor <- robot.getSensors
        (sensorCell, orientation) = sensor.getState(position)
        cell = sensorCell + orientation * 1
        if robot.getPerceivedMaze.isInside(cell)
        _ = println(cell)
        _ = println(sensorCell)
        _ = println(orientation)
      } yield robot.getPerceivedMaze.cells(cell)
      println(cellsUnder)
      val path = Dijkstra.getPathWithDistanceMap(distanceMap, robot.getPosition, target)
      val moves = Utils.path2Moves(path)
      println("path = " + path)
      println("moves = " + moves)
      val move = moves.head
      robot.move(move)
    } else {
      println("Finding way back")
      val path = Dijkstra(robot.getPerceivedMaze, robot.getPosition, Cell(2, 2), robot.getTurnCost)
      println(path)
      val moves = Utils.path2Moves(path)
      println(moves)
      robot.move(moves.head)
    }
    println(robot.getPosition)
    robot.getPosition
  }

  private[this] def shouldFinish: Boolean = robot.getPerceivedMaze.getCoverage >= coverageLimit ||
    System.currentTimeMillis() - start >= timeLimit

  def finished: Boolean = shouldFinish && robot.getPosition.center == Cell(2, 2)
}
