package grp11.geometry

case class Cell(x: Int, y: Int) {
  def +(other: Cell): Cell = Cell(x + other.x, y + other.y)
}
