package grp11.algo

import grp11.geometry.{Cell, Maze}
import grp11.robot.Move._
import grp11.robot.{Orientation, RobotPosition}

import scala.collection.mutable

object Dijkstra {
  val Eps = 1e-6
  def apply(maze: Maze, start: RobotPosition, end: Cell, turnCost: Double, wayPoint: Option[Cell] = None): List[RobotPosition] = {
    wayPoint.fold {
      val distanceMap = getDistanceMap(maze, start, turnCost)
      if (distanceMap.filterKeys(_.center == end).isEmpty) {
        println("No path found")
        println(maze)
      }
      val position = distanceMap.filterKeys(_.center == end).minBy(_._2._1)._1
      getPathWithDistanceMap(distanceMap, start, position)
    } { point =>
      println("starting dijkstra")
      val distanceMap = getDistanceMap(maze, start, turnCost)
      if (distanceMap.filterKeys(_.center == point).isEmpty) {
        println("No path found")
        println(maze)
      }
      val position = distanceMap.filterKeys(_.center == point).minBy(_._2._1)._1
      val firstPath = getPathWithDistanceMap(distanceMap, start, position)
      println("first path found")
      val secondPath = apply(maze, position, end, turnCost, None)
      if (secondPath.isEmpty) firstPath
      else firstPath ++ secondPath.tail
    }
  }

  def getPathWithDistanceMap(
    distanceMap: mutable.HashMap[RobotPosition,(Double, RobotPosition)],
    start: RobotPosition,
    target: RobotPosition
  ): List[RobotPosition] = {

    var position = target
    if (distanceMap(position)._1 == Double.MaxValue) {
      Nil
    } else {
      var path = List[RobotPosition]()
      while (position != start) {
        path = path :+ position
        position = distanceMap(position)._2
      }
      path = path :+ position
      path.reverse
    }
  }

  def getDistanceMap(maze: Maze, start: RobotPosition, turnCost: Double): mutable.HashMap[RobotPosition, (Double, RobotPosition)] = {
    val currentMap = mutable.PriorityQueue[(Double, RobotPosition, RobotPosition)]((0.0, start, start))(Ordering.by(-_._1))
    val finalMap = mutable.HashMap[RobotPosition, (Double, RobotPosition)](start -> (0.0, start))
    for {
      row <- 2 until maze.getHeight
      col <- 2 until maze.getWidth
      cell = Cell(col, row)
      orientation <- Orientation.all
      robotPosition = RobotPosition(cell, orientation)
      if robotPosition != start
    } {
      currentMap.enqueue((Double.MaxValue, robotPosition, robotPosition))
      finalMap(robotPosition) = (Double.MaxValue, robotPosition)
    }

    while (currentMap.nonEmpty) {
      val (distance, pos, prevPos) = currentMap.dequeue
      if (finalMap(pos)._1 + Eps >= distance) {
        val forward = pos.applyMove(Forward)
        if (maze.isValidPosition(forward) && finalMap(forward)._1 > distance + 1) {
          currentMap.enqueue((distance + 1, forward, pos))
          finalMap(forward) = (distance + 1, pos)
        }

        val left = pos.applyMove(TurnLeft)
        if (maze.isValidPosition(left) && finalMap(left)._1 > distance + turnCost) {
          currentMap.enqueue((distance + turnCost, left, pos))
          finalMap(left) = (distance + turnCost, pos)
        }
        val right = pos.applyMove(TurnRight)
        if (maze.isValidPosition(right) && finalMap(right)._1 > distance + turnCost) {
          currentMap.enqueue((distance + turnCost, right, pos))
          finalMap(right) = (distance + turnCost, pos)
        }
      }
    }

    finalMap
  }
}
