package grp11.connection

import akka.actor.{Actor, ActorRef}
import grp11.algo.{Dijkstra, NearestHelpfulCell, WallHugging}
import grp11.geometry.Cell
import grp11.robot.VirtualRobot
import grp11.utils.Utils

class SimulationActor(snapshot: Int, forwarder: ActorRef, robot: VirtualRobot) extends Actor {
  override def receive: Receive = {
    case ExploreStart(coverageLimit, timeLimit) =>
      // Only use final maze for virtual sensors
      val start = System.currentTimeMillis()
      val explorer = new WallHugging(robot, coverageLimit, timeLimit)
      println(s"Starting exploration with coverageLimit = $coverageLimit% and timeLimit = ${timeLimit}s")
      while (!explorer.finished) {
        for {
          move <- explorer.step
        } {
          robot.move(move)
          robot.sense()
          forwarder ! FwMessage(snapshot, ClientBoardRepr.toJson(robot.getPosition, robot.getPerceivedMaze))
        }
      }
      println(s"Finished exploration with ${robot.getPerceivedMaze.getCoverage}%" +
        s" in ${(System.currentTimeMillis() - start) / 1000.0}s")
      println("Encoded map:\n" + robot.getPerceivedMaze.encodeExplored + "\n" + robot.getPerceivedMaze.encodeState)
    case ShortestPath(wayPoint) =>
      println(s"Starting shortest path with wayPoint = $wayPoint")
      // Always use final maze in shortest path calculation
      val path = Dijkstra(robot.getFinalMaze,
        robot.getPosition,
        robot.getFinalMaze.getStop,
        robot.getTurnCost,
        wayPoint
      )
      val moves = Utils.path2Moves(path)
      moves.foreach { move =>
        robot.move(move)
        forwarder ! FwMessage(
          snapshot,
          ClientBoardRepr.toJson(robot.getPosition, robot.getFinalMaze, path.map(_.center), wayPoint.toList)
        )
      }
  }
}

sealed trait SimulationActorMessage
case class ExploreStart(coverageLimit: Double, timeLimit: Long) extends SimulationActorMessage
case class ShortestPath(wayPoint: Option[Cell]) extends SimulationActorMessage
