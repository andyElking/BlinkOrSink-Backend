package actors

import akka.actor.{Actor, ActorRef, Props}
import play.api.libs.json.{JsValue, Json}
import WSActor._
import actors.Central.{RegisterMe, UnregisterMe}
import akka.http.scaladsl.model
import models.ServerModel
import models.ServerModel.User
import play.api.libs.json._
import akka.pattern.{ask, pipe}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class WSActor (val uuid: String, out: ActorRef)(implicit ec: ExecutionContext) extends Actor {
  var opponent: ActorRef = ActorRef.noSender
  override def receive: Receive = {
    case Offer(ref,msg) =>
      opponent = ref
      println(s"connected to opponent with msg $msg")
      // Start WebRTC
      out ! msg
    case Abort(msg) =>
      out ! msg
      context.stop(out)
      context.stop(self)
    case Accept(ref,msg) =>
      opponent = ref
      // start WebRTC
      println(s"connected to opponent with msg $msg")
      out ! msg
    case Rematch(msg) =>
      out ! msg
    case Message(msg) =>
      out ! msg
    case msg: String =>
      executeCommand(msg)
  }

  def thisUser = User(uuid)

  /*
  JSON: {
    "command" : "offer" / "accept" / "rematch" / "abort" / "message",
    "msg" : // whatever will be transmitted to the other client
  }
  */

  def executeCommand(s: String): Unit = {
    val js: JsValue = Json.parse(s)
    val msg = (js \ "msg").get.toString()
    (js \ "command").get match {
      case JsString("offer") =>
        Central.instance ! Offer(self,msg)
      case JsString("accept") =>
        Central.instance ! Accept(self,msg)
      case JsString("rematch") =>
        opponent ! Rematch(msg)
      case JsString("abort") =>
        context.stop(self)
      case JsString("message") =>
        opponent ! Message(msg)
      case _ =>
        opponent ! Message(s)
    }
  }


  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()
    Central.instance ! RegisterMe()
  }

}

object WSActor {
  //implicit val ec = ExecutionContext.global
  sealed trait Command
  case class Offer(ref: ActorRef, msg: String) extends Command
  case class Accept(ref: ActorRef, msg: String) extends Command
  case class Rematch(msg: String) extends Command
  case class Abort(msg: String) extends Command
  case class Message(msg: String) extends Command
  def props(uuid: String, clientActorRef: ActorRef)(implicit ec: ExecutionContext): Props = Props(new WSActor(uuid, clientActorRef))
}
