package actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import Central._
import actors.WSActor._

import javax.inject.{Inject, Singleton}
import akka.util.Timeout

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Singleton
class Central @Inject() (implicit ec: ExecutionContext, system: ActorSystem) extends Actor {

  val connections: mutable.Map[ActorRef, ActorRef] = mutable.HashMap[ActorRef,ActorRef]()
  val waitingActors: mutable.Queue[ActorRef] = mutable.Queue[ActorRef]()
  // allActors is not used anywhere, but is well maintained throughout
  val allActors: mutable.Set[ActorRef] = mutable.HashSet[ActorRef]()

  // This timeout is relevant for testing purposes, otherwise should be <1second
  implicit val timeout: Timeout = Timeout(10.seconds)

  /** Receive and handle commands from other actors */
  override def receive: Actor.Receive = {
    case Tick() => keepPlayersAlive() // Just a helper for keep-alive

    case DisconnectMe(ref) =>
      // removes ref from connections, but keeps the rest
      disconnect(ref)

    case RegisterMe(ref) =>
      allActors.add(ref)
      // Subscribes to deathwatch for each actor (see the Terminated case below)
      context.watch(ref)

    case Match(ref) if waitingActors.isEmpty =>
      waitingActors.enqueue(ref)
      sender() ! None

    case Match(ref) if waitingActors.nonEmpty && !waitingActors.contains(ref) =>
      val act2 = waitingActors.dequeue()
      sender() ! Some(act2)

    case Match(ref) if waitingActors.length==1 && waitingActors.contains(ref) =>
      sender() ! None

    case Match(ref) if waitingActors.length>=2 && waitingActors.contains(ref) =>
      waitingActors-=ref
      val act2 = waitingActors.dequeue()
      sender() ! Some(act2)

    case MakeConnection(act1,act2) => connect(act1,act2)

    case t: Terminated =>
      disconnect(t.actor)
      waitingActors -= t.actor
      allActors.remove(t.actor)
    case _ =>
  }

  // call keepPlayersAlive every minute
  system.scheduler.scheduleAtFixedRate(1.minutes,1.minutes,self, Tick())

  /** All sockets are pinged to prevent them from closing */
  def keepPlayersAlive(): Unit = {
    connections.foreach(a => a._1 ! Message("Stay alive"))
    // Also
  }

  /** Establishes a connection between two users */
  def connect(act1: ActorRef, act2: ActorRef): Unit = {
    connections += (act1 -> act2)
    connections += (act2 -> act1)
  }

  /** Aborts the connection, but keeps the users alive */
  def disconnect(act1: ActorRef): Unit = {
    connections.get(act1) match {
      case Some(act2) =>
        connections --= Seq(act1,act2)
        act2 ! Disconnect()
      case None => ()
    }

  }

}

object Central {
  /** Commands that Central accepts */
  sealed trait CCommand
  case class Tick() extends CCommand
  case class RegisterMe(ref: ActorRef) extends CCommand
  case class Match(ref: ActorRef) extends CCommand
  case class MakeConnection(act1: ActorRef, act2: ActorRef) extends CCommand
  case class DisconnectMe(ref: ActorRef) extends CCommand

  var instance: ActorRef = Actor.noSender
  def init(implicit ec: ExecutionContext, system: ActorSystem): Unit = {
    instance = system.actorOf(Props(new Central), name = "Central")
  }
}
