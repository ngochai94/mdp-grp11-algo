package grp11.geometry

import CellState._

class Maze(cells: Array[Array[Cell]]) {
  def height(): Int = cells.length
  def width(): Int = cells(0).length
}

object Maze {
  val Height = 20
  val Width = 15

  def apply(): Maze = {
    apply(Height, Width)
  }

  def apply(height: Int, width: Int): Maze = {
    val cells = Array.ofDim[Cell](height, width)
    for {
      row <- 1 to height
      col <- 1 to width
    }{
      cells(row)(col) = Cell(row, col, Unknown)
    }
    new Maze(cells)
  }
}
