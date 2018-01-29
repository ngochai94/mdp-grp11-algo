package grp11.geometry

import CellState._

import scala.collection.mutable

class Maze(cells: mutable.HashMap[Cell, CellState], height: Int, width: Int) {
  def containsCell(cell: Cell): Boolean = cells.contains(cell)
  def getState(cell: Cell): CellState = cells(cell)
  def setState(cell: Cell, cellState: CellState): Unit = cells(cell) = cellState
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
