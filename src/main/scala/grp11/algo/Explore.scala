package grp11.algo

import grp11.geometry.Cell
import grp11.robot.{Move, Orientation, Robot, RobotPosition}
import grp11.utils.Utils

import scala.collection.mutable
import scala.util.Random

class Explore(robot: Robot) {
  private[this] val r: Random.type = Random
  private[this] val visited = mutable.HashMap[RobotPosition, Boolean]()
  private[this] val height = robot.getPerceivedMaze.height
  private[this] val width = robot.getPerceivedMaze.width

  for {
    row <- 1 to height
    col <- 1 to width
    orientation <- Orientation.all
  } {
    visited(RobotPosition(Cell(col, row), orientation)) = false
  }

  // initial position is visited
  visited(robot.getPosition) = true

  //  private[this] def printVisited(): Unit = {
  //    (1 to height).toList.reverse.foreach { row =>
  //      (1 to width).foldLeft(()) { case (s, col) =>
  //        visited(Cell(col, row)) match {
  //          case true => print(1)
  //          case false => print(0)
  //        }
  //      }
  //      println("")
  //    }
  //  }

  def step: RobotPosition = {
    robot.sense()
    if (!robot.getPerceivedMaze.fullyExplored) {
      println("-----------------------------------------------")
      println(robot.getPerceivedMaze.draw())
      //      if (robot.getPerceivedMaze.isValidPosition(robot.getPosition.applyMove(Move.Forward))) {
      //        r.nextFloat match {
      //          case f if f < 0.8 => robot.move(Move.Forward)
      //          case f if f < 0.9 => robot.move(Move.TurnRight)
      //          case _ => robot.move(Move.TurnLeft)
      //        }
      //      } else {
      //        r.nextInt(2) match {
      //          case 0 => robot.move(Move.TurnRight)
      //          case _ => robot.move(Move.TurnLeft)
      //        }
      //      }

      visited(robot.getPosition) = true
      val distanceMap = Dijkstra.getDistanceMap(robot.getPerceivedMaze, robot.getPosition, robot.getTurnCost)
//      println(distanceMap.filterKeys(x => !visited(x)).filter(x => x._2._1 < 7))
//      println(robot.getPerceivedMaze.isHelpfulPosition(RobotPosition(Cell(13,3), Orientation.Down)))
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
      //      printVisited()
//      println("distanceMap1 = " + distanceMap.filterKeys(_.center == Cell(3, 19)))
//      println("distanceMap2 = " + distanceMap.filterKeys(_.center == Cell(3, 18)))
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

  def finished: Boolean = robot.getPerceivedMaze.fullyExplored && robot.getPosition.center == Cell(2, 2)
}
