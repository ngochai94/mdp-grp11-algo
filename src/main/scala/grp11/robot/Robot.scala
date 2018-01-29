package grp11.robot

import grp11.geometry.Maze

trait Robot {
  def getPosition: RobotPosition
  def sense(): Unit
  def move(): Unit
  def getPerceivedMaze: Maze
}
