package grp11.robot

import grp11.geometry.Maze

trait Robot {
  def getPosition: RobotPosition
  def getSensors: List[Sensor]
  def sense(): Unit
  def move(move: List[Move]): Unit
  def getPerceivedMaze: Maze
  def getTurnCost: Double
}
