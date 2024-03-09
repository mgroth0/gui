package matt.gui.exception

import javafx.geometry.Insets
import javafx.geometry.Pos.CENTER
import kotlinx.coroutines.runBlocking
import matt.async.thread.daemon
import matt.async.thread.namedThread
import matt.exec.app.App
import matt.fx.control.fxapp.ERROR_POP_UP_TEXT
import matt.fx.control.lang.actionbutton
import matt.fx.control.wrapper.control.button.button
import matt.fx.control.wrapper.control.text.area.textarea
import matt.fx.control.wrapper.label.label
import matt.fx.graphics.fxthread.runLater
import matt.fx.graphics.wrapper.pane.spacer
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.fx.graphics.wrapper.pane.vbox.v
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.gui.app.GuiApp
import matt.gui.interact.openInNewWindow
import matt.gui.mstage.ShowMode.SHOW_AND_WAIT
import matt.http.method.HTTPMethod.POST
import matt.http.url.MURL
import matt.http.url.query.buildQueryURL
import matt.log.profile.err.ExceptionResponse
import matt.log.profile.err.ExceptionResponse.EXIT
import matt.log.report.desktop.BugReport
import matt.log.taball
import matt.model.code.errreport.createThrowReport
import matt.prim.str.urlEncode
import matt.rstruct.desktop.extraValues
import java.awt.Desktop
import java.net.URI
import kotlin.system.exitProcess

val deephysSite by lazy {
    extraValues["DEEPHYS_URL"]!!
}

/*100 wasn't enough*/
private const val STACK_TRACE_SURFACE_COUNT = 200

fun GuiApp.showExceptionPopup(
    t: Thread,
    e: Throwable,
    @Suppress("UNUSED_PARAMETER") shutdown: (App.() -> Unit)?,
    st: String
): ExceptionResponse {
    val r = EXIT
    val stackTraceDepth = e.stackTrace.size
    val stackTraceToShow = e.stackTrace.take(100)
    val stackTraceLeft = stackTraceDepth - stackTraceToShow.size
    val stackTraceSurface =
        if (stackTraceLeft > 0) {
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
        }    /*	label("${e::class.simpleName} in $appName") {
		  isWrapText = true
		}
		label("thread=${t.name}") {
		  isWrapText = true
		}*/
        spacer()
        textarea(st)
        v {
            spacing = 10.0
            padding = Insets(10.0)
            isFillWidth = true
            alignment = CENTER
            /*  ifMatt {
                actionbutton("Run pre-shutdown operation") {
                  shutdown?.invoke(this@showExceptionPopup)
                }
              }*/
            button("Submit Bug Report") {

                setOnAction {
                    isDisable = true

                    text = "submitting (please wait)..."
                    namedThread("Submit Bug Report Thread") {
                        try {
                            val u = MURL(deephysSite)/*.productionHost*/ + "issue"
                            val url =
                                runBlocking {
                                    matt.http.http(u) {
                                        method = POST
                                        data = BugReport(t = t, e = e).text.encodeToByteArray()
                                    }.requireSuccessful().text()
                                }
                            runLater {
                                text = "view submitted bug"
                                isDisable = false

                                setOnAction {

                                    isDisable = true
                                    daemon(name = "view bug") {
                                        /*ON LINUX THIS MUST OCCUR IN ANOTHER THREAD*/
                                        Desktop.getDesktop().browse(URI(url))
                                        runLater {
                                            isDisable = false
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            createThrowReport(e).print()
                            runLater {
                                text = "failed to submit. Please copy and paste the error and send to matt"
                            }
                        }
                    }
                }
            }
            actionbutton("Exit now") {
                e.printStackTrace()
                exitProcess(1)
            }        /*  ifMatt {
			actionbutton("ignore") {
			  r = IGNORE
			  ((it.target as Node).scene.window as Stage).close()
			}
		  }*/
        }
    }.openInNewWindow(
        SHOW_AND_WAIT
    )
    return r
}

fun openNewYouTrackIssue(
    summary: String,
    description: String
) {
    val u =
        buildQueryURL(
            "https://deephys.youtrack.cloud/newIssue",
            "project" to "D",
            "summary" to summary.urlEncode(),
            "description" to description.urlEncode()
        ).let {
            URI(it.path)
        }
    Desktop.getDesktop().browse(u)
}



