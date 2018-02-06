package grp11.utils

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
}
