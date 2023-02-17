package matt.gui.exception

import javafx.scene.Node
import javafx.stage.Stage
import matt.exec.app.App
import matt.file.thismachine.ifMatt
import matt.fx.control.fxapp.ERROR_POP_UP_TEXT
import matt.fx.control.lang.actionbutton
import matt.fx.control.wrapper.control.button.ButtonWrapper
import matt.fx.control.wrapper.control.text.area.textarea
import matt.fx.control.wrapper.label.label
import matt.fx.graphics.wrapper.pane.flow.flowpane
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.gui.app.GuiApp
import matt.gui.interact.openInNewWindow
import matt.gui.mstage.ShowMode.SHOW_AND_WAIT
import matt.http.url.buildQueryURL
import matt.log.profile.err.ExceptionResponse
import matt.log.profile.err.ExceptionResponse.EXIT
import matt.log.profile.err.ExceptionResponse.IGNORE
import matt.log.report.BugReport
import matt.log.taball
import matt.prim.str.urlEncode
import java.awt.Desktop
import java.net.URI
import kotlin.system.exitProcess


/*100 wasn't enough*/
private const val STACK_TRACE_SURFACE_COUNT = 200

fun GuiApp.showExceptionPopup(
  t: Thread,
  e: Throwable,
  shutdown: (App<*>.()->Unit)?,
  st: String
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
	label(ERROR_POP_UP_TEXT) {
	  isWrapText = true
	}
	/*	label("${e::class.simpleName} in $appName") {
		  isWrapText = true
		}
		label("thread=${t.name}") {
		  isWrapText = true
		}*/
	textarea(st)
	flowpane<ButtonWrapper> {
	  ifMatt {
		actionbutton("Run pre-shutdown operation") {
		  shutdown?.invoke(this@showExceptionPopup)
		}
	  }
	  actionbutton("Submit Bug Report") {

		openNewYouTrackIssue(
		  summary = "Bug Report",
		  description = BugReport(t = t, e = e).text
		)

	  }
	  ifMatt {
		actionbutton("print stack trace") {
		  e.printStackTrace()
		}
	  }
	  actionbutton("Exit now") {
		e.printStackTrace()
		exitProcess(1)
	  }
	  ifMatt {
		actionbutton("ignore") {
		  r = IGNORE
		  ((it.target as Node).scene.window as Stage).close()
		}
	  }
	}
  }.openInNewWindow(
	SHOW_AND_WAIT,
  )
  return r
}

fun openNewYouTrackIssue(
  summary: String,
  description: String
) {
  val u = buildQueryURL(
	"https://deephys.youtrack.cloud/newIssue",
	"project" to "D",
	"summary" to summary.urlEncode(),
	"description" to description.urlEncode()
  ).let {
	URI(it)
  }
  Desktop.getDesktop().browse(u)
}



