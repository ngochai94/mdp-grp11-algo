package grp11.connection

import com.google.gson.Gson
import grp11.geometry.{Cell, Maze}
import grp11.robot.RobotPosition

import scala.collection.JavaConverters._

case class ClientBoardRepr(
                            robot: RobotPosition,
                            maze: String,
                            path: java.util.List[Cell],
                            wayPoint: java.util.List[Cell]
                          )

object ClientBoardRepr {
  def toJson(robotPosition: RobotPosition, maze: Maze, path: List[Cell] = Nil, wayPoint: List[Cell] = Nil): String = {
    val gson = new Gson
    val repr = ClientBoardRepr(robotPosition, maze.toString, path.asJava, wayPoint.asJava)
    gson.toJson(repr)
  }
}
