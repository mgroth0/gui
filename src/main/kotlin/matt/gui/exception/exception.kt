package matt.gui.exception

import javafx.beans.value.ChangeListener
import javafx.scene.Node
import javafx.scene.control.TextArea
import javafx.scene.layout.FlowPane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Stage
import matt.auto.SublimeText
import matt.auto.openInIntelliJ
import matt.exec.app.App
import matt.exec.app.appName
import matt.exec.exception.MyDefaultUncaughtExceptionHandler.ExceptionResponse
import matt.exec.exception.MyDefaultUncaughtExceptionHandler.ExceptionResponse.EXIT
import matt.exec.exception.MyDefaultUncaughtExceptionHandler.ExceptionResponse.IGNORE
import matt.gui.app.GuiApp
import matt.gui.lang.ActionButton
import matt.gui.win.interact.openInNewWindow
import java.io.File
import kotlin.system.exitProcess

fun GuiApp.showExceptionPopup(
  t: Thread,
  e: Throwable,
  shutdown: (App.()->Unit)?,
  consumeShutdown: (App.()->Unit)?,
  st: String,
  exceptionFile: File
): ExceptionResponse {

//  ChangeListener

  var r = EXIT
  println("setting up runLaterReturn for exception dialog")
  VBox(
	Text("${e::class.simpleName} in $appName"),
	Text("thread=${t.name}"),
	TextArea(st),
	FlowPane(
	  ActionButton("Open stacktrace in IntelliJ") {
		exceptionFile.openInIntelliJ()
	  },
	  ActionButton("Open stacktrace in Sublime Text") {
		SublimeText.open(exceptionFile)
	  },
	  ActionButton("Run pre-shutdown operation") {
		shutdown?.invoke(this)
		consumeShutdown?.invoke(this)
	  },
	  ActionButton("print stack trace") {
		e.printStackTrace()
	  },
	  ActionButton("Exit now") {
		e.printStackTrace()
		exitProcess(1)
	  },
	  ActionButton("ignore") {
		r = IGNORE
		((it.target as Node).scene.window as Stage).close()
	  }

	)
  ).openInNewWindow(wait = true)
  return r
}