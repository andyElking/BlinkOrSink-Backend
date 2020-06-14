package controllers

import scala.concurrent.ExecutionContext
import actors.{Central, WSActor}
import javax.inject._
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import akka.stream.{ActorMaterializer, Materializer}
import akka.actor.ActorSystem

@Singleton
class HomeController @Inject() (cc: ControllerComponents)
                               (implicit actorSystem: ActorSystem
                                , mat: Materializer
                                , executionContext: ExecutionContext) extends AbstractController(cc) {

  Central.init

  // uuids are not used by the backend, but can be useful for logging and might be used by the frontend
  var uuidGen: Int = 1

  def ws(): WebSocket = WebSocket.accept[String, String] { _ =>
    ActorFlow.actorRef{ actorRef =>
      uuidGen+=1
      WSActor.props(uuidGen.toString,actorRef)
    }
  }

}
