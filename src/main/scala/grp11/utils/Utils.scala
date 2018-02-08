package grp11.utils

import grp11.robot.Move._
import grp11.robot.{Move, RobotPosition}

object Utils {
  val hex = "0123456789ABCDEF"

  def dec2Hex(x: Int): Char = hex(x)

  def bins2Hexs(xs: List[Int]): String = {
    xs.foldLeft(("", 0, 0)) { case ((res, cur, cnt), x) =>
      val agg = x * (1 << (3 - cnt)) + cur
      if (cnt < 3) (res, agg, cnt + 1)
      else (res + dec2Hex(agg), 0, 0)
    }._1
  }

  def path2Moves(path: List[RobotPosition]): List[Move] = {
    if (path.lengthCompare(2) < 0) Nil
    else {
      path.tail.foldLeft((List[Move](), path.head)) { case ((moves, lastPos), pos) =>
        if (lastPos.applyMove(Forward) == pos) (moves :+ Forward, pos)
        else if (lastPos.applyMove(TurnRight) == pos) (moves :+ TurnRight, pos)
        else (moves :+ TurnLeft, pos)
      }._1
    }
  }

}
