package matt.gui.exception

import javafx.scene.Node
import javafx.stage.Stage
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
import matt.log.profile.err.ExceptionResponse
import matt.log.profile.err.ExceptionResponse.EXIT
import matt.log.profile.err.ExceptionResponse.IGNORE
import matt.log.taball
import kotlin.system.exitProcess

/*100 wasn't enough*/
private const val STACK_TRACE_SURFACE_COUNT = 200

fun GuiApp.showExceptionPopup(
  t: Thread,
  e: Throwable,
  shutdown: (App<*>.()->Unit)?,
  st: String,
  exceptionFile: MFile
): ExceptionResponse {
  var r = EXIT
  val stackTraceDepth = e.stackTrace.size
  val stackTraceToShow = e.stackTrace.take(100)
  val stackTraceLeft = stackTraceDepth - stackTraceToShow.size
  val stackTraceSurface = if (stackTraceLeft > 0) {
	e.stackTrace.takeLast(kotlin.math.min(STACK_TRACE_SURFACE_COUNT, stackTraceLeft))
  } else listOf()
  val stackTraceInBetween = stackTraceLeft - stackTraceSurface.size
  taball("stacktrace", stackTraceToShow)
  if (stackTraceInBetween > 0) {
	println("\t... and $stackTraceInBetween in between")
  }
  if (stackTraceSurface.isNotEmpty()) {
	taball("stacktrace SURFACE", stackTraceSurface)
  }
  VBoxWrapperImpl<RegionWrapper<*>>().apply {
	text("${e::class.simpleName} in $appName")
	text("thread=${t.name}")
	textarea(st)
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