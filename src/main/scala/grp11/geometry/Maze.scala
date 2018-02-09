package grp11.geometry

import CellState._
import grp11.robot.RobotPosition
import grp11.utils.Utils

import scala.collection.mutable

case class Maze(cells: mutable.HashMap[Cell, CellState], height: Int, width: Int) {
  def containsCell(cell: Cell): Boolean = cells.contains(cell)
  def getState(cell: Cell): CellState = cells(cell)
  def setState(cell: Cell, cellState: CellState): Unit = cells(cell) = cellState

  def isInside(cell: Cell): Boolean = {
    cell.x >= 1 && cell.x <= width && cell.y >= 1 && cell.y <= height
  }

  def isValidPosition(position: RobotPosition): Boolean = {
    val center = position.center
    val cellsUnder = for {
      x <- -1 to 1
      y <- -1 to 1
      cell = center + Cell(x, y)
    } yield {
      if (!isInside(cell) || cells(cell) != Empty) false
      else true
    }
    !cellsUnder.contains(false)
  }

  // TODO: make this more accurate
  def isHelpfulPosition(position: RobotPosition): Boolean = {
    if (!isValidPosition(position)) false
    else {
      val center = position.center
      val cellsUnder = for {
        x <- -2 to 2
        y <- -2 to 2
        cell = center + Cell(x, y)
        if isInside(cell)
      } yield {
        if (cells(cell) == Unknown) false
        else true
      }
      cellsUnder.contains(false)
    }
  }

  def getCoverage: Int = cells.values.filter(_ != Unknown).toList.length

  def getArea: Int = height * width

  def fullyExplored: Boolean = getCoverage == getArea

  def getStop: Cell = Cell(width - 1, height - 1)

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

  def encodeExplored: String = {
    val exploredBin = List(1, 1) ++ (1 to height).toList.flatMap { row =>
      (1 to width).toList.map { col =>
        cells(Cell(col, row)) match {
          case Unknown => 0
          case _ => 1
        }
      }
    } ++ List(1, 1)
    Utils.bins2Hexs(exploredBin)
  }

  def encodeState: String = {
    val stateBin = (1 to height).toList.flatMap { row =>
      (1 to width).toList.map { col =>
        cells(Cell(col, row)) match {
          case Unknown => -1
          case Empty => 0
          case Blocked => 1
        }
      }
    }.filter(_ >= 0)
    val padding = (8 - stateBin.length % 8) % 8
    val paddedStateBin = stateBin ++ List.fill(padding)(0)
    Utils.bins2Hexs(paddedStateBin)
  }

  override def toString: String = {
    (1 to height).toList.reverse.map { row =>
      (1 to width).foldLeft("") { case (s, col) =>
        val symbol = cells(Cell(col, row)) match {
          case Unknown => "0"
          case Empty => "1"
          case Blocked => "2"
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
    // Start pos is always empty
    for {
      row <- 1 to 3
      col <- 1 to 3
    } {
      cells(Cell(col, row)) = Empty
    }
    new Maze(cells, height, width)
  }

  def emptyMaze = {
    val cells = mutable.HashMap[Cell, CellState]()
    for {
      row <- 1 to Height
      col <- 1 to Width
    } {
      cells(Cell(col, row)) = Empty
    }
    new Maze(cells, Height, Width)
  }
}
