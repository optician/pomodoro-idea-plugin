package me.optician_owl.intellij.pomodoro

import com.intellij.openapi.components.{ProjectComponent, ServiceManager}
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.NotNull


/**
  * Created by Danila on 29.03.2017.
  */
class PomodoroComponent(val project: Project) extends ProjectComponent {
  override def initComponent(): Unit = {
    // TODO: insert component initialization logic here
  }

  override def disposeComponent(): Unit = {
    // TODO: insert component disposal logic here
    ServiceManager.getService(classOf[PomodoroService]).finish()
  }

  override def getComponentName = "PomodoroComponent"

  override def projectOpened(): Unit = {
    // called when project is opened
  }

  override def projectClosed(): Unit = {
    // called when project is being closed
    ServiceManager.getService(classOf[PomodoroService]).finish()
  }
}
