package mesosphere.marathon.upgrade

import akka.actor.{ Actor, ActorLogging }
import akka.event.EventStream
import mesosphere.marathon.state.AppDefinition
import mesosphere.marathon.tasks.{ TaskTracker, TaskQueue }
import mesosphere.marathon.{ SchedulerActions, TaskUpgradeCanceledException }
import org.apache.mesos.SchedulerDriver

import scala.concurrent.Promise

class TaskStartActor(
    val driver: SchedulerDriver,
    val scheduler: SchedulerActions,
    val taskQueue: TaskQueue,
    val taskTracker: TaskTracker,
    val eventBus: EventStream,
    val app: AppDefinition,
    val scaleTo: Int,
    val withHealthChecks: Boolean,
    promise: Promise[Unit]) extends Actor with ActorLogging with StartingBehavior {

  val nrToStart: Int = scaleTo - taskQueue.count(app.id) - taskTracker.count(app.id)

  override def initializeStart(): Unit = {
    if (nrToStart > 0)
      taskQueue.add(app, nrToStart)
  }

  override def postStop(): Unit = {
    eventBus.unsubscribe(self)
    if (!promise.isCompleted)
      promise.tryFailure(
        new TaskUpgradeCanceledException(
          "The task upgrade has been cancelled"))
  }

  override def success(): Unit = {
    log.info(s"Successfully started $nrToStart instances of ${app.id}")
    promise.success(())
    context.stop(self)
  }

}
