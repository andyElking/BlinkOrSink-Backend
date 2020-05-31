package actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import Central._
import actors.WSActor.MakeConnection
import controllers.HomeController
import javax.inject.Singleton
import play.api.libs.concurrent.Akka

import scala.collection.mutable

@Singleton
class Central extends Actor{
  val clientActors: mutable.HashMap[String, ActorRef] = scala.collection.mutable.HashMap[String, ActorRef]()
  override def receive: Actor.Receive = {
    case RegisterMe(uuid) =>
      clientActors += uuid -> sender()
    case UnregisterMe(uuid) =>
      clientActors -= uuid
    case SendUpdateTo(uuid: String, msg: String) =>
      clientActors.get(uuid) match {
        case Some(actor) =>
          actor ! MakeConnection(msg)
        case None =>
          throw new NoSuchElementException
      }
  }

}

object Central {
  case class RegisterMe(uuid: String)
  case class UnregisterMe(uuid: String)
  case class SendUpdateTo(uuid: String, msg: String)
  var instance: ActorRef = Actor.noSender
  def init(system: ActorSystem): Unit = {
    instance = system.actorOf(Props(classOf[Central]), name = "Central")
  }
}
