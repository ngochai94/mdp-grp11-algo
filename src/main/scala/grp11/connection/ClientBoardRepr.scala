package grp11.connection

import com.google.gson.Gson
import grp11.geometry.Maze
import grp11.robot.RobotPosition

case class ClientBoardRepr(robot: RobotPosition, maze: String)

object ClientBoardRepr {
  def toJson(robotPosition: RobotPosition, maze: Maze): String = {
    val gson = new Gson
    val repr = ClientBoardRepr(robotPosition, maze.toString)
    gson.toJson(repr)
  }
}
