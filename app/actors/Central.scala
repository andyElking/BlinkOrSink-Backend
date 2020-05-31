package actors

import java.lang.reflect.Constructor

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import Central._
import actors.WSActor._
import akka.http.scaladsl.model.IllegalResponseException
import controllers.HomeController
import javax.inject.{Inject, Singleton}
import play.api.libs.concurrent.Akka
import akka.pattern.{ask, pipe}
import akka.util.Timeout

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.concurrent.duration._

@Singleton
class Central @Inject() (implicit ec: ExecutionContext) extends Actor {

  val connections = mutable.HashMap[ActorRef,ActorRef]()
  val waitingActors = mutable.Queue[ActorRef]()

  implicit val timeout = Timeout(20.seconds)

  override def receive: Actor.Receive = {
    case Offer(ref,msg) =>
      assert(ref==sender())
      val act1 = sender()
      context.watch(act1)
      findConnection(act1,Offer(ref,msg)).onComplete{
        case Success((act2,reply)) =>
          connect(act1,act2)
          act1 ! reply
        case Failure(_) =>
          waitingActors += act1
      }
    case t: Terminated =>
      connections -= t.actor
      waitingActors -= t.actor
    case _ =>
  }

  def findConnection(act1: ActorRef, offer: Offer): Future[(ActorRef,Any)] = Future {
    var res = (ActorRef.noSender,offer)
    var flag: Boolean = false
    while(waitingActors.size>=1 && !flag) {
      val act2 = waitingActors.dequeue()
      val accept: Future[Any] = ask(act2,offer)
      accept.onComplete{
        case Success(reply) =>
          println(reply.toString)
          res = (act2,offer)
          flag = true
        case Failure(exception) =>
          act2 ! Abort("")
      }
    }
    if (!flag) throw new IllegalArgumentException
    res
  }

  /** Establishes a connection between two users */
  def connect(act1: ActorRef, act2: ActorRef): Unit = {
    connections += (act1 -> act2)
    connections += (act2 -> act1)
  }

}

object Central {
  case class RegisterMe()
  case class UnregisterMe()
  case class ConnectMe()

  var instance: ActorRef = Actor.noSender
  def init(system: ActorSystem)(implicit ec: ExecutionContext): Unit = {
    instance = system.actorOf(Props(new Central), name = "Central")
  }
}
