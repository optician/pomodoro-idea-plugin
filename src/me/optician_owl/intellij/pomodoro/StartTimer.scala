package me.optician_owl.intellij.pomodoro

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, CommonDataKeys, Presentation}
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger

/**
  * Created by Danila on 29.03.2017.
  */
class StartTimer extends AnAction {

  private val logger = Logger.getInstance(this.getClass)

  private lazy val pomodoroService = ServiceManager.getService(classOf[PomodoroService])

  override def actionPerformed(e: AnActionEvent): Unit = {
    val project = e.getData(CommonDataKeys.PROJECT)
    pomodoroService.act(project)
  }

  override def update(e: AnActionEvent): Unit = {
    val presentation = e.getPresentation
    e.getPlace match {
      case "MainMenu" => updateStateInMenu(presentation, pomodoroService.isWorking)
      case bad_place   => logger.error(s"Missed place $bad_place")
    }
  }

  private def updateStateInMenu(p: Presentation, working: Boolean) =
    if (working) {
      p.setText("Stop Pomodoro")
    } else {
      p.setText("Start Pomodoro")
    }
}
