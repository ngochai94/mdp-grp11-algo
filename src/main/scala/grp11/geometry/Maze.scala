package grp11.geometry

import CellState._
import grp11.robot.{RobotPosition, Sensor}
import grp11.utils.Utils

import scala.collection.mutable

class Maze(cells: mutable.HashMap[Cell, CellState], height: Int, width: Int, knownEmptyCells: List[Cell] = Nil) {
  def containsCell(cell: Cell): Boolean = cells.contains(cell)

  def getState(cell: Cell): CellState = cells(cell)

  def getCells: mutable.HashMap[Cell, CellState] = cells
  def getHeight: Int = height
  def getWidth: Int = width

  def setState(cell: Cell, cellState: CellState): Unit = {
    if (cells(cell) != Unknown && cells(cell) != cellState) {
      println(s"Cell $cell was previously ${cells(cell)} and is now set to $cellState")
    }
    if (isKnownEmpty(cell) && cellState == Blocked) {
      println("Attempting to modify start or end area")
      cells(cell) = Empty
    } else if (cells(cell) != Empty) { // always trust the first read of empty cell
      cells(cell) = cellState
    }
  }

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

  def isHelpfulPosition(position: RobotPosition, sensors: List[Sensor]): Boolean = {
    if (!isValidPosition(position)) false
    else {
      sensors.flatMap { sensor =>
        val (pos, orientation) = sensor.getState(position)
        sensor.range
          .map(distance => pos + orientation * distance)
          .filter(isInside)
          .map(cells(_))
          .takeWhile(_ != Blocked)
      }.contains(Unknown)
    }
  }

  def cellsExplored: Int = cells.values.filter(_ != Unknown).toList.length

  def getArea: Int = height * width

  def getCoverage: Double = cellsExplored * 100.0 / getArea

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

  def getAndroidMap(position: RobotPosition): String = {
    (1 to height).toList.reverse.map { row =>
      (1 to width).foldLeft("") { case (s, col) =>
        if (Cell(col, row) == position.getHead) s ++ "3"
        else if (position.contains(Cell(col, row))) s ++ "4"
        else {
          val symbol = cells(Cell(col, row)) match {
            case Unknown => "0"
            case Empty => "1"
            case Blocked => "2"
          }
          s ++ symbol
        }
      }
    }.mkString("")
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

  private[this] def isKnownEmpty(cell: Cell): Boolean = {
    knownEmptyCells.contains(cell)
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

    // Start position is always empty
    for {
      row <- 1 to 3
      col <- 1 to 3
    }{
      cells(Cell(col, row)) = Empty
    }

    val centers = List(Cell(2, 2), Cell(width - 1, height - 1), Cell(2, height - 1), Cell(width - 1, 2))
    val knownEmptyCells = for {
      row <- -1 to 1
      col <- -1 to 1
      center <- centers
    } yield center + Cell(col, row)
    new Maze(cells, height, width, knownEmptyCells.toList)
  }
}
