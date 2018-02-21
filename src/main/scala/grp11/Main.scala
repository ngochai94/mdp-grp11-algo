package grp11

import grp11.algo.{Dijkstra, NearestHelpfulCell, WallHugging}
import grp11.geometry.CellState._
import grp11.geometry.{Cell, Maze}
import grp11.connection._
import grp11.robot.Orientation.Up
import grp11.robot.{RealRobot, RobotPosition, Sensor, VirtualRobot}

import scala.io.StdIn


object Main extends App {
  val server = new WebSocketServer
  val rpiConnection = RpiConnection(RpiConnection.DefaultHost, RpiConnection.DefaultPort)
  val robot = new RealRobot(rpiConnection, server.forwarder)

  val explorer = new WallHugging(robot)
  println("starting")
  while (!explorer.finished) {
    for {
      move <- explorer.step
    } {
      robot.move(move)
    }
  }
  println("finished")
}

//--------------------------------------------------------------------------------------------------------------------

object Tmp {
  def testExploration(): Unit = {
      val maze = Maze.emptyMaze
      val robot = new VirtualRobot(maze, Sensor.defaultSensors, 49, 50)
      val explorer = new NearestHelpfulCell(robot)
      println("Starting exploration")
      while (!explorer.finished) {
        val position = explorer.step
        println("robot now at " + position)
      }
      println("Finished exploration")
  }

  def testWS(): Unit = {
    val server = new WebSocketServer
    while (true) {
      val s = StdIn.readLine()
      server ! FwMessage(0, s)
    }
  }
  def testConnectRpi(): Unit = {
    val connection = new RpiConnection(RpiConnection.DefaultHost, RpiConnection.DefaultPort)
    val thread = new Thread {
      override def run(): Unit = {
        var received = " "
        while (received != "") {
          received = connection.receive
          println("Received: " + received)
        }
      }
    }
    thread.start()
    while (true) {
      val msg = StdIn.readLine()
      connection.send(ArduinoMessage(msg))
    }
  }

  def demoDijkstra(): Unit = {
    val grid = List("..........",
      "..........",
      "..........",
      "..xxxxx...",
      "..........",
      "..........",
      "..........",
      "....xxxxxx",
      "..........",
      "..........",
      "..........",
    )
    val maze = Maze(grid.length, grid.head.length)
    for {
      row <- 1 to grid.length
      col <- 1 to grid.head.length
    } {
      val cellState = grid(grid.length - row)(col - 1) match {
        case '.' => Empty
        case 'x' => Blocked
        case '?' => Unknown
      }
      maze.cells(Cell(col, row)) = cellState
    }
    println(maze.draw())
    val path = Dijkstra(maze, RobotPosition(Cell(2, 2), Up), Cell(grid.head.length - 1, grid.length - 1), 1)
    println(path)
    println(maze.draw(path))
  }
}
