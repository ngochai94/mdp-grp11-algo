package grp11.connection

import com.google.gson.Gson
import grp11.geometry.{Cell, Maze}
import grp11.robot.RobotPosition

import scala.collection.JavaConverters._

case class ClientBoardRepr(
  mType: String,
  robot: RobotPosition,
  maze: String,
  path: java.util.List[Cell],
  wayPoint: java.util.List[Cell]
)

object ClientBoardRepr {
  def toJson(robotPosition: RobotPosition, maze: Maze, path: List[Cell] = Nil, wayPoint: List[Cell] = Nil): String = {
    val gson = new Gson
    val repr = ClientBoardRepr("board", robotPosition, maze.toString, path.asJava, wayPoint.asJava)
    gson.toJson(repr)
  }
}

case class ClientNotificationRepr(
  mType: String,
  notification: String
)

object ClientNotificationRepr {
  def toJson(notification: String): String = {
    val gson = new Gson
    val repr = ClientNotificationRepr("notification", notification)
    gson.toJson(repr)
  }
}

case class AndroidBoardRepr(m: String, d: String, e: String)

object AndroidBoardRepr {
  def toJson(maze: String, desc1: String = "", desc2: String = ""): String = {
    val gson = new Gson
    val repr = AndroidBoardRepr(maze, desc1, desc2)
    gson.toJson(repr)
  }
}
