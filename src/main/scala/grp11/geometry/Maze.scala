package grp11.geometry

import CellState._
import grp11.robot.RobotPosition

import scala.collection.mutable

case class Maze(cells: mutable.HashMap[Cell, CellState], height: Int, width: Int) {
  def containsCell(cell: Cell): Boolean = cells.contains(cell)
  def getState(cell: Cell): CellState = cells(cell)
  def setState(cell: Cell, cellState: CellState): Unit = cells(cell) = cellState

  def isValidPosition(position: RobotPosition): Boolean = {
    val center = position.center
    if (!(center.x > 1 && center.x < width && center.y > 1 && center.y < height)) {
      false
    } else {
      val cellsUnder = for {
        x <- -1 to 1
        y <- -1 to 1
      } yield {
        if (cells(Cell(x + center.x, y + center.y)) != Empty) false
        else true
      }
      !cellsUnder.contains(false)
    }
  }

  def draw(path: List[RobotPosition] = Nil): String = {
    (1 to height).toList.reverse.map { row =>
      (1 to width).foldLeft("") { case (s, col) =>
        val symbol = cells(Cell(col, row)) match {
          case _ if path.exists(_.center == Cell(col, row)) => "o"
          case Unknown => "?"
          case Empty => "."
          case Blocked => "x"
        }
        s ++ symbol
      }
    }.mkString("\n")
  }
}

object Maze {
  val Height = 20
  val Width = 15

  def apply(): Maze = {
    apply(Height, Width)
  }

  def apply(height: Int, width: Int): Maze = {
    val cells = mutable.HashMap[Cell, CellState]()
    for {
      row <- 1 to height
      col <- 1 to width
    }{
      cells(Cell(col, row)) = Unknown
    }
    new Maze(cells, height, width)
  }
}
