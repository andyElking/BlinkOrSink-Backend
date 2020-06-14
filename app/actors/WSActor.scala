package actors

import akka.actor.{Actor, ActorRef, Props}
import play.api.libs.json.{JsValue, Json}
import WSActor._
import actors.Central._
import play.api.libs.json._
import akka.pattern.{AskTimeoutException, ask}
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class WSActor @Inject() (val uuid: String, out: ActorRef)(implicit ec: ExecutionContext) extends Actor {

  var opponent: Option[ActorRef] = None
  var snd: ActorRef = ActorRef.noSender

  /** Handles commands received from other actors */
  override def receive: Receive = {
    case Offer(ref,msg) if opponent.isEmpty =>
      // Got an offer from another client
      snd = sender()  // Keep the sender hook to reply to its question
      opponent = Some(ref)
      // Start WebRTC
      out ! msg
    case Abort(msg) =>
      // Abort coming from the server (currently this never happens)
      out ! msg
      context.stop(out)
      context.stop(self)
    case Rematch(msg) =>
      out ! msg
    case Message(msg) =>
      out ! msg
    case Disconnect() =>
      opponent = None
      // Abort WebRTC
      out ! "Opponent disconnected"  // Or something
    case msg: String =>
      // This is reserved for WebSocket input
      // All communication between actors is handled through Commands
      executeCommand(msg)
  }

  /* JSON input format:
    {
      "command" : "offer" / "accept" / "rematch" / "abort" / "message",
      "msg" : // whatever will be transmitted to the other client
    }
  */

  /** JSON commands received via the WebSocket are parsed and executed */
  def executeCommand(s: String): Unit = {
    val js: JsValue = Json.parse(s)

    // We don't read the message, it's just sent to the (potential) opponent
    val msg = (js \ "msg").get.toString()

    // Read the command from Json and handle it
    (js \ "command").get match {
      case JsString("offer") =>
        // Offer initializes matchmaking and breaks the
        // current connection if it exists
        opponent = None
        Central.instance ! DisconnectMe(self)
        findMatch(msg)

      case JsString("accept") if opponent.nonEmpty =>
        // Sends an accept message to the actor who offered to connect
        snd ! Accept(self,msg)

      case JsString("rematch") if opponent.nonEmpty =>
        // Only applicable if there is an opponent to Rematch
        // For now this has no side effects and is equivalent to message
        opponent.get ! Rematch(msg)

      case JsString("abort") =>
        // Central watches actors and removes them from all comms when they die
        context.stop(self)

      case JsString("message") if opponent.nonEmpty =>
        // Just send a message to opponent without side-effects
        opponent.get ! Message(msg)

      case _ if opponent.nonEmpty =>
        // If Json is uninterpretable, then the whole string is sent to the opponent
        opponent.get ! Message(s)

      case _ => // catch all
    }
  }

  /** In a non-blocking way finds a match or gets added to the waiting list */
  def findMatch(msg: String): Future[Unit] = ask(Central.instance, Match(self))(100.millis)
    // Central had 100 ms to respond (time can be increased, not important)
    .map{
    case None => () // Queue is empty, this actor was added. May repeat the call again
    case Some(ref: ActorRef) =>
      // Central gave back a potential opponent, so try to establish connection
      ask(ref,Offer(self,msg))(10.seconds).map {
        // should reply with an accept, otherwise something is wrong
        case Accept(_,msg2) =>
          out ! msg2
          initRoom(ref)

        case _ =>
          ref ! Abort("Expected Accept, aborting")
          throw new IllegalArgumentException

      }.recoverWith {
        // Terminate the unresponsive Client and attempt to find a new match
        case _: AskTimeoutException =>
          ref ! Abort("Took too long to respond, aborting")
          findMatch(msg)

        case _: IllegalArgumentException => findMatch(msg)
      }
  }

  /** Updates opponent and signals to Central to make a connection */
  def initRoom(act: ActorRef): Unit = {
    opponent = Some(act)
    Central.instance ! MakeConnection(self, act)
  }

  /** Registers the actor in Central's database upon creation */
  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()
    Central.instance ! RegisterMe(self)
  }

}

object WSActor {
  /** Commands accepted by the WSActor*/
  sealed trait WSCommand
  case class Disconnect() extends WSCommand
  case class Offer(ref: ActorRef,msg: String) extends WSCommand
  case class Accept(ref: ActorRef,msg: String) extends WSCommand
  case class Rematch(msg: String) extends WSCommand
  case class Abort(msg: String) extends WSCommand
  case class Message(msg: String) extends WSCommand

  def props(uuid: String, clientActorRef: ActorRef)(implicit ec: ExecutionContext): Props = Props(new WSActor(uuid, clientActorRef))
}
