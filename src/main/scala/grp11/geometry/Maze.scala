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
    if (!(center.x > 1 && center.x < height && center.y > 1 && center.y < width)) {
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
      cells(Cell(row, col)) = Unknown
    }
    new Maze(cells, height, width)
  }
}
