package me.optician_owl.intellij.pomodoro

import java.time.{Duration, LocalDateTime}
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
    isSuccess: Option[Boolean] = None) {
  override def toString: String = {
    val d    = Duration.ofSeconds(duration)
    val mins = d.toMinutes
    val secs = d.minusMinutes(mins).getSeconds
    val completion = isSuccess match {
      case Some(bool) if bool  => "Pomodoro completed"
      case Some(bool) if !bool => "Pomodoro failed"
      case _                   => "Pomodoro in progress"
    }
    s"$completion; Duration $mins min $secs sec"
  }
}

class PomodoroService {

  private val logger = Logger.getInstance(this.getClass)

  private var timer: Option[PomodoroTimer] = None

  /**
    * Duration of high concentration stage in seconds
    */
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
    timer.synchronized {
      timer.fold(logger.warn("Timer is empty when showMessage is invoked."))(t =>
        Messages.showMessageDialog(s"Project $prj; $t.", prj, icon))
    }

  private def start(project: Project): Unit =
    timer.synchronized(
      if (timer.isEmpty) {
        timer = Some(PomodoroTimer(project.getName, LocalDateTime.now()))
        runCircle()
      } else logger.info("Timer is already running.")
    )

  private def finish(): Unit =
    ApplicationManager.getApplication.invokeLater(() =>
      timer.synchronized {
        timer.fold {
          logger.warn("Couldn't finish not running timer.")
        } { pt =>
          logger.debug(s"stop timer for ${pt.projectName}")
          val currDuration = ChronoUnit.SECONDS.between(pt.begin, LocalDateTime.now())
          timer =
            Some(pt.copy(duration = currDuration, isCompleted = true, isSuccess = Some(false)))
          showMessage(pt.projectName)
          timer = None
        }
    })

  def act(project: Project): Unit = timer.synchronized {
    if (timer.isDefined) finish()
    else start(project)
  }

  def isWorking: Boolean = timer.synchronized(timer.isDefined)

}
