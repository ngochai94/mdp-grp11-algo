package grp11.connection

import akka.actor.{Actor, ActorRef}
import grp11.algo.{Dijkstra, Explore}
import grp11.robot.VirtualRobot
import grp11.utils.Utils

class SimulationActor(snapshot: Int, forwarder: ActorRef, robot: VirtualRobot) extends Actor {
  override def receive: Receive = {
    case ExploreStart =>
      // Only use final maze for virtual sensors
      val explorer = Explore(robot)
      println("Starting exploration")
      while (!explorer.finished) {
        val position = explorer.step
        forwarder ! FwMessage(snapshot, ClientBoardRepr.toJson(position, robot.getPerceivedMaze))
      }
      println("Finished exploration")
    case ShortestPath =>
      println("Starting shortest path...")
      // Always use final maze in shortest path calculation
      val path = Dijkstra(robot.finalMaze,
        robot.getPosition,
        robot.finalMaze.getStop,
        1.0 * robot.turnTime / robot.moveTime
      )
      val moves = Utils.path2Moves(path)
      moves.foreach { move =>
        robot.move(move)
        forwarder ! FwMessage(snapshot, ClientBoardRepr.toJson(robot.getPosition, robot.finalMaze))
      }
  }
}

sealed trait SimulationActorMessage
case object ExploreStart extends SimulationActorMessage
case object ShortestPath extends SimulationActorMessage
