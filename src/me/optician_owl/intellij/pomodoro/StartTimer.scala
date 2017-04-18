package me.optician_owl.intellij.pomodoro

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, CommonDataKeys}
import com.intellij.openapi.components.ServiceManager

/**
  * Created by Danila on 29.03.2017.
  */
class StartTimer extends AnAction {

  private lazy val pomodoroService = ServiceManager.getService(classOf[PomodoroService])

  override def actionPerformed(e: AnActionEvent): Unit = {
    val project = e.getData(CommonDataKeys.PROJECT)
    pomodoroService.start(project)
  }

  override def update(e: AnActionEvent): Unit = {
    super.update(e)
  }

}
