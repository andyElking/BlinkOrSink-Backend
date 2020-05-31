package actors

import akka.actor.{Actor, ActorRef, Props}
import play.api.libs.json.{JsValue, Json}
import WSActor._
import actors.Central.{RegisterMe, SendUpdateTo, UnregisterMe}
import akka.http.scaladsl.model
import models.ServerModel
import models.ServerModel.User

class WSActor (uuid: String, out: ActorRef, model: ServerModel) extends Actor {
  override def receive: Receive = {
    case MakeConnection(msg) =>
      out ! msg
    case msg: String =>
      val partner = model.getOpponentOf(thisUser)
      partner match {
        case Some(oppuuid) =>
          //
        case None =>
          matchmake(msg)
      }
  }

  def thisUser = User(uuid)

  def matchmake(msg: String): Unit = {
    val potentialOpponent = model.waitingUser
    potentialOpponent match {
      case Some(otherUser) =>
        /** Relay the offer sent by the user to the otherUser.

            The otherUser should reply with an accept message, which the server should send to the user, afterwards webRTC takes care of stuff.

            If the server successfully sends an accept message to the user, it can update the connection mapping.
         */
        model.dequeue()
        Central.instance ! SendUpdateTo(otherUser.uuid,msg)
        model.connect(User(uuid), otherUser)
      case None =>
        /** If no opponent is yet found, ignore the ping sent by the user and add them to the waiting list. */
        model.enqueue(User(uuid))

    }
  }

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()
    Central.instance ! RegisterMe(uuid)
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    Central.instance ! UnregisterMe(uuid)
    model.abortConnectionOf(User(uuid))
    model.removeFromQueue(User(uuid))
    super.postStop()
  }
}

object WSActor {
  case class MakeConnection(msg: String)
  def props(uuid: String, clientActorRef: ActorRef, model: ServerModel): Props = Props(new WSActor(uuid, clientActorRef, model))
}
