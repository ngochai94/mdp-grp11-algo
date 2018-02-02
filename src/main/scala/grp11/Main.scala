package grp11

import grp11.algo.Dijkstra
import grp11.geometry.CellState._
import grp11.geometry.{Cell, Maze}
import grp11.robot.Orientation.Up
import grp11.robot.RobotPosition

object Main extends App {
  val grid = List("..........",
                  "..........",
                  "..........",
                  "..xxxxx...",
                  "..........",
                  "..........",
                  "..........",
                  "....xxxxxx",
                  "..........",
                  "..........",
                  "..........",
  )
  val maze = Maze(grid.length, grid.head.length)
  for {
    row <- 1 to grid.length
    col <- 1 to grid.head.length
  } {
    val cellState = grid(grid.length - row)(col - 1) match {
      case '.' => Empty
      case 'x' => Blocked
      case '?' => Unknown
    }
    maze.cells(Cell(col, row)) = cellState
  }
  println(maze.draw())
  val path = Dijkstra(maze, RobotPosition(Cell(2, 2), Up), Cell(grid.head.length - 1, grid.length - 1), 1)
  println(path)
  println(maze.draw(path))
}
