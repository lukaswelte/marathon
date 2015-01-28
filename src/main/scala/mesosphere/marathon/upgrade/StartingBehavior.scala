package mesosphere.marathon.upgrade

import akka.actor.{ Actor, ActorLogging }
import akka.event.EventStream
import mesosphere.marathon.SchedulerActions
import mesosphere.marathon.event.{ HealthStatusChanged, MarathonHealthCheckEvent, MesosStatusUpdateEvent }
import mesosphere.marathon.state.AppDefinition
import mesosphere.marathon.tasks.{ TaskQueue, TaskTracker }
import org.apache.mesos.SchedulerDriver

import scala.concurrent.duration._

trait StartingBehavior { this: Actor with ActorLogging =>
  import context.dispatcher
  import mesosphere.marathon.upgrade.StartingBehavior._

  def eventBus: EventStream
  def scaleTo: Int
  def nrToStart: Int
  def withHealthChecks: Boolean
  def taskQueue: TaskQueue
  def driver: SchedulerDriver
  def scheduler: SchedulerActions
  def taskTracker: TaskTracker

  val app: AppDefinition
  val Version = app.version.toString
  var atLeastOnceHealthyTasks = Set.empty[String]
  var startedRunningTasks = Set.empty[String]
  val AppId = app.id

  def initializeStart(): Unit

  final override def preStart(): Unit = {
    if (withHealthChecks) {
      eventBus.subscribe(self, classOf[MarathonHealthCheckEvent])
    }
    else {
      eventBus.subscribe(self, classOf[MesosStatusUpdateEvent])
    }

    initializeStart()
    checkFinished()

    context.system.scheduler.scheduleOnce(5.seconds, self, Sync)
  }

  final def receive: PartialFunction[Any, Unit] = {
    val behavior =
      if (withHealthChecks) checkForHealthy
      else checkForRunning
    behavior orElse commonBehavior
  }

  final def checkForHealthy: Receive = {
    case HealthStatusChanged(AppId, taskId, Version, true, _, _) if !atLeastOnceHealthyTasks(taskId) =>
      atLeastOnceHealthyTasks += taskId
      log.info(s"$taskId is now healthy")
      checkFinished()
  }

  final def checkForRunning: Receive = {
    case MesosStatusUpdateEvent(_, taskId, "TASK_RUNNING", _, app.`id`, _, _, Version, _, _) if !startedRunningTasks(taskId) => // scalastyle:off line.size.limit
      startedRunningTasks += taskId
      log.info(s"Started $taskId")
      checkFinished()
  }

  def commonBehavior: Receive = {
    case MesosStatusUpdateEvent(_, taskId, "TASK_ERROR" | "TASK_FAILED" | "TASK_LOST" | "TASK_KILLED", _, app.`id`, _, _, Version, _, _) => // scalastyle:off line.size.limit
      log.warning(s"Failed to start $taskId for app ${app.id}. Rescheduling.")
      startedRunningTasks -= taskId
      taskQueue.add(app)

    case Sync =>
      val actualSize = taskQueue.count(app.id) + taskTracker.count(app.id)
      if (actualSize < scaleTo) {
        taskQueue.add(app, scaleTo - actualSize)
      }
      context.system.scheduler.scheduleOnce(5.seconds, self, Sync)
  }

  def checkFinished(): Unit = {
    if (withHealthChecks && atLeastOnceHealthyTasks.size == nrToStart) {
      success()
    }
    else if (startedRunningTasks.size == nrToStart) {
      success()
    }
  }

  def success(): Unit
}

object StartingBehavior {
  case object Sync
}
