package grp11.connection

import akka.actor.{Actor, ActorRef}
import grp11.algo.{Dijkstra, NearestHelpfulCell, WallHugging}
import grp11.geometry.Cell
import grp11.robot.Move.Forward
import grp11.robot.VirtualRobot
import grp11.utils.Utils

class SimulationActor(snapshot: Int, forwarder: ActorRef, robot: VirtualRobot) extends Actor {
  override def receive: Receive = {
    case ExploreStart(coverageLimit, timeLimit, explorerName) =>
      // Only use final maze for virtual sensors
      val start = System.currentTimeMillis()
      var moveCnt = 0
      var turnCnt = 0
      val explorer = explorerName match {
        case "wall" => new WallHugging(robot, coverageLimit, timeLimit)
        case "near" => new NearestHelpfulCell(robot, coverageLimit, timeLimit)
        case _ => throw new Exception(s"Unknown explorerName = $explorerName")
      }
      println(s"Starting exploration with coverageLimit = $coverageLimit% and timeLimit = ${timeLimit / 1000.0}s")
      while (!explorer.finished) {
        for {
          move <- explorer.step
        } {
          move match {
            case Forward => moveCnt = moveCnt + 1
            case _ => turnCnt = turnCnt + 1
          }
          robot.move(move)
          robot.sense()
          forwarder ! FwMessage(snapshot, ClientBoardRepr.toJson(robot.getPosition, robot.getPerceivedMaze))
        }
      }
      val notification = s"Finished exploration with ${robot.getPerceivedMaze.getCoverage}%" +
        s" in ${(System.currentTimeMillis() - start) / 1000.0}s ($moveCnt moves and $turnCnt turns)"
      forwarder ! FwMessage(snapshot, ClientNotificationRepr.toJson(notification))
      println(notification)
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
      val notification = if (path.isEmpty) {
        s"No path found"
      } else {
        val moves = Utils.path2Moves(path)
        moves.foreach { move =>
          robot.move(move)
          forwarder ! FwMessage(
            snapshot,
            ClientBoardRepr.toJson(robot.getPosition, robot.getFinalMaze, path.map(_.center), wayPoint.toList)
          )
        }
        s"Finished shortest path after ${moves.length} moves"
      }
      forwarder ! FwMessage(snapshot, ClientNotificationRepr.toJson(notification))
  }
}

sealed trait SimulationActorMessage
case class ExploreStart(coverageLimit: Double, timeLimit: Long, explorer: String) extends SimulationActorMessage
case class ShortestPath(wayPoint: Option[Cell]) extends SimulationActorMessage
