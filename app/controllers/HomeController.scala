package controllers

import scala.concurrent.ExecutionContext
import actors.{Central, WSActor}
import javax.inject._
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import akka.stream.{ActorMaterializer, Materializer}
import akka.actor.ActorSystem
import models.ServerModel



@Singleton
class HomeController @Inject()(cc: ControllerComponents) (implicit actorSystem: ActorSystem,
                                                          mat: Materializer,
                                                          executionContext: ExecutionContext)
  extends AbstractController(cc) {
  Central.init(actorSystem)
  var uuidGen: Int = 1
  val model = new ServerModel

  def ws(): WebSocket = WebSocket.accept[String, String] { _ =>
    ActorFlow.actorRef{ actorRef =>
      uuidGen+=1
      WSActor.props(uuidGen.toString,actorRef,model)
    }
  }

 /* def index = Action { request =>
    Ok("Got request [" + request + "]")
  }*/

}
