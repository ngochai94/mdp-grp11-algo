package grp11.geometry

sealed trait CellState

object CellState {
  case object Empty extends CellState
  case object Blocked extends CellState
  case object Unknown extends CellState
}
