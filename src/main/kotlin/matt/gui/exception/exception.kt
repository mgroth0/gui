package matt.gui.exception

import javafx.scene.Node
import javafx.stage.Stage
import matt.auto.exception.MyDefaultUncaughtExceptionHandler.ExceptionResponse
import matt.auto.exception.MyDefaultUncaughtExceptionHandler.ExceptionResponse.EXIT
import matt.auto.exception.MyDefaultUncaughtExceptionHandler.ExceptionResponse.IGNORE
import matt.auto.macapp.SublimeText
import matt.auto.openInIntelliJ
import matt.exec.app.App
import matt.exec.app.appName
import matt.file.MFile
import matt.fx.control.lang.actionbutton
import matt.fx.control.mstage.ShowMode.SHOW_AND_WAIT
import matt.fx.control.win.interact.openInNewWindow
import matt.fx.control.wrapper.control.button.ButtonWrapper
import matt.fx.control.wrapper.control.text.area.textarea
import matt.fx.graphics.wrapper.pane.flow.flowpane
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.fx.graphics.wrapper.text.text
import matt.gui.app.GuiApp
import matt.log.taball
import kotlin.system.exitProcess

fun GuiApp.showExceptionPopup(
  t: Thread,
  e: Throwable,
  shutdown: (App<*>.()->Unit)?,
  st: String,
  exceptionFile: MFile
): ExceptionResponse {
  var r = EXIT
  taball("stacktrace", e.stackTrace)
  println("setting up matt.fx.graphics.fxthread.runLaterReturn for exception dialog")
  VBoxWrapperImpl<RegionWrapper<*>>().apply {
	println("setting up vbox1")
	text("${e::class.simpleName} in $appName")
	println("setting up vbox2")
	text("thread=${t.name}")
	println("setting up vbox3: $st")
	textarea(st)
	println("setting up vbox4")
	flowpane<ButtonWrapper> {
	  actionbutton("Open stacktrace in IntelliJ") {
		exceptionFile.openInIntelliJ()
	  }
	  actionbutton("Open stacktrace in Sublime Text") {
		SublimeText.open(exceptionFile)
	  }
	  actionbutton("Run pre-shutdown operation") {
		shutdown?.invoke(this@showExceptionPopup)
	  }
	  actionbutton("print stack trace") {
		e.printStackTrace()
	  }
	  actionbutton("Exit now") {
		e.printStackTrace()
		exitProcess(1)
	  }
	  actionbutton("ignore") {
		r = IGNORE
		((it.target as Node).scene.window as Stage).close()
	  }
	}
  }.openInNewWindow(SHOW_AND_WAIT)
  return r
}