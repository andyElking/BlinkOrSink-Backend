package models

import scala.collection.mutable.Map
import ServerModel._

import scala.collection.mutable
// The model for server side stuff

class ServerModel {
    //Stores the user waiting for an opponent, if there is one
    private var _waitingUser : Option[User] = None

    //This should actually be a bidirectional map but scala doesn't have a standard implementation of one for some reason
    private val connections = mutable.Map[User, User]()

    def waitingUser: Option[User] = _waitingUser

    def enqueue(user: User): Unit = {
        _waitingUser = Some(user)
    }

    def dequeue(): Unit = {
        _waitingUser = None
    }


    def removeFromQueue(user: User): Boolean = {
        waitingUser match {
            case Some(u) if user == u =>
                _waitingUser = None
                println("Removed " + user.toString + " from the waiting list.")
                true
            case _ =>
                println(user.toString + " was not in the waiting list.")
                false
        }
    }

    /** Establishes a connection between two users */
    def connect(user: User, otherUser: User): Unit = {
        connections += (user -> otherUser)
        connections += (otherUser -> user)
    }

    /** Returns the opponent of the user, if one exists */
    def getOpponentOf(user: User): Option[User] = connections.get(user)

    /** Removes any connection the user has with other users on the server */
    def abortConnectionOf(user: User): Boolean = {
        val potentialUser = connections.get(user)
        potentialUser match {
            case Some(otherUser) =>
                connections -= user
                connections -= otherUser
                println("Aborted connection between " + user + " and " + otherUser + ".")
                true
            case None =>
                println("Failed to abort connection because " + user + " is not connected to anyone.")
                false

        }
    }

}

object ServerModel {
    case class User(uuid: String)
}
