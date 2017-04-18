package me.optician_owl.intellij.pomodoro

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.{Executors, ThreadFactory, TimeUnit}

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

import scala.concurrent.{ExecutionContext, Future}

/**
  *
  * @param projectName Project name to which timer is related.
  * @param begin Start point. LocalDateTime.
  * @param duration Total time of activity in seconds.
  * @param isCompleted Status of completion.
  * @param isSuccess None - timer is active. Some(true|false) - result of activity.
  */
case class PomodoroTimer(
    projectName: String,
    begin: LocalDateTime,
    duration: Long = 0L,
    isCompleted: Boolean = false,
    isSuccess: Option[Boolean] = None)

class PomodoroService {

  private val logger = Logger.getInstance(this.getClass)

  /**
    * Turn on happens-before.
    */
  @volatile private var timer: Option[PomodoroTimer] = None

  /**
    * Duration of high concentration stage in seconds
    */
//  private val pomodoroPeriod = 5
  private val pomodoroPeriod = 25 * 60
  private val icon           = Messages.getInformationIcon

  private val step = 1L

  private val threadFactory: ThreadFactory = (r: Runnable) => {
    val t = new Thread(r, "pomodoro-thread")
    t.setDaemon(true)
    t
  }
  private implicit val ex =
    ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor(threadFactory))

  private def checker(): Unit = timer.synchronized {
    timer.fold {
      logger.warn("Timer is not active.")
    } { pt =>
      val currDuration = ChronoUnit.SECONDS.between(pt.begin, LocalDateTime.now())
      if (currDuration >= pomodoroPeriod) {
        timer = Some(pt.copy(duration = currDuration, isCompleted = true, isSuccess = Some(true)))
        finish()
      } else {
        timer = Some(pt.copy(duration = currDuration))
        runCircle()
      }
    }
  }

  private def runCircle(): Unit = {
    Future {
      TimeUnit.SECONDS.sleep(step)
      checker()
    }
    ()
  }

  private def showMessage(prj: String): Unit =
    Messages.showMessageDialog("Pomodoro has been completed. Take a rest.", prj, icon)

  def start(project: Project): Unit =
    timer.synchronized(
      if (timer.isEmpty) {
        timer = Some(PomodoroTimer(project.getName, LocalDateTime.now()))
        runCircle()
      } else logger.info("Timer is already running.")
    )

  def finish(): Unit = timer.synchronized {
    timer.fold {
      logger.warn("Couldn't finish not running timer.")
    } { pt =>
      logger.info(s"stop timer for ${pt.projectName}")
      // Process success result
      ApplicationManager.getApplication.invokeLater(() => showMessage(pt.projectName))
      // Stop scheduler
      // Crop pomodoro
      timer = None
    }
  }

  def isWorking: Boolean = timer.synchronized(timer.isDefined)

}
