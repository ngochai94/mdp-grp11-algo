package grp11.connection

import akka.actor.{Actor, ActorRef}
import grp11.algo.{Dijkstra, Explore}
import grp11.robot.VirtualRobot
import grp11.utils.Utils

class SimulationActor(snapshot: Int, forwarder: ActorRef, robot: VirtualRobot) extends Actor {
  override def receive: Receive = {
    case ExploreStart(coverageLimit, timeLimit) =>
      // Only use final maze for virtual sensors
      val start = System.currentTimeMillis()
      val explorer = new Explore(robot, coverageLimit, timeLimit)
      println("Starting exploration")
      while (!explorer.finished) {
        val position = explorer.step
        forwarder ! FwMessage(snapshot, ClientBoardRepr.toJson(position, robot.getPerceivedMaze))
      }
      println(s"Finished exploration with ${robot.getPerceivedMaze.getCoverage}%" +
        s" in ${(System.currentTimeMillis() - start) / 1000.0}s")
      println("Encoded map:\n" + robot.getPerceivedMaze.encodeExplored + "\n" + robot.getPerceivedMaze.encodeState)
    case ShortestPath =>
      println("Starting shortest path...")
      // Always use final maze in shortest path calculation
      val path = Dijkstra(robot.getFinalMaze,
        robot.getPosition,
        robot.getFinalMaze.getStop,
        robot.getTurnCost
      )
      val moves = Utils.path2Moves(path)
      moves.foreach { move =>
        robot.move(move)
        forwarder ! FwMessage(snapshot, ClientBoardRepr.toJson(robot.getPosition, robot.getFinalMaze))
      }
  }
}

sealed trait SimulationActorMessage
case class ExploreStart(coverageLimit: Double, timeLimit: Long) extends SimulationActorMessage
case object ShortestPath extends SimulationActorMessage
