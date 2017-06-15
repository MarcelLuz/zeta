package de.htwg.zeta.generatorControl.actors.frontend

import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import de.htwg.zeta.common.models.frontend.Connected
import de.htwg.zeta.common.models.frontend.DeveloperRequest
import de.htwg.zeta.common.models.frontend.DeveloperResponse
import de.htwg.zeta.common.models.frontend.Disconnected
import de.htwg.zeta.common.models.frontend.MessageEnvelope
import de.htwg.zeta.common.models.frontend.ToolDeveloper

import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

private case object RegisterDeveloperFrontend

/**
 * Actor to connect a tool developer to the backend
 */

object DeveloperFrontend {
  def props(out: ActorRef, backend: ActorRef, userId: UUID) = Props(new DeveloperFrontend(out, backend, userId))
}

class DeveloperFrontend(out: ActorRef, backend: ActorRef, userId: UUID) extends Actor with ActorLogging {
  private val instance = ToolDeveloper(out, userId)
  private val registerTask = context.system.scheduler.schedule(Duration(1, TimeUnit.SECONDS), Duration(30, TimeUnit.SECONDS), self, RegisterDeveloperFrontend)

  override def postStop() = {
    backend ! MessageEnvelope(userId, Disconnected(instance))
    registerTask.cancel()
  }
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.error(reason, "Restarting due to [{}] when processing [{}]", reason.getMessage, message.getOrElse(""))
  }
  def receive = {
    case RegisterDeveloperFrontend =>
      backend ! MessageEnvelope(userId, Connected(instance))
    case request: DeveloperRequest =>
      backend ! MessageEnvelope(userId, request)
    case response: DeveloperResponse =>
      out ! response
  }
}