package grp11.robot

sealed trait Move

object Move {
  case object Forward extends Move
  case object TurnLeft extends Move
  case object TurnRight extends Move
}
