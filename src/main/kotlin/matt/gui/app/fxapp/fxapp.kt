package matt.gui.app.fxapp

import com.sun.javafx.application.LauncherImpl
import com.sun.javafx.util.Logging
import javafx.application.Application
import javafx.application.Preloader
import javafx.application.Preloader.StateChangeNotification.Type.BEFORE_START
import javafx.scene.Scene
import javafx.scene.control.ProgressBar
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import matt.async.thread.daemon
import matt.auto.myPid
import matt.log.profile.Stopwatch


fun runFXAppBlocking(
  args: Array<String>,
  usePreloaderApp: Boolean = false,
  t: Stopwatch? = null,
  fxOp: (List<String>)->Unit,

  ) {
  t?.toc("running FX App")
  fxBlock = fxOp
  daemon {
	Logging.getJavaFXLogger().disableLogging() /* dodge "Unsupported JavaFX configuration..." part 1 */
  }
  t?.toc("started disabling FX logging")
  println("launching app (mypid = ${myPid})")
  fxStopwatch = t
  if (usePreloaderApp) {
	t?.toc("launching preloader")
	LauncherImpl.launchApplication(MinimalFXApp::class.java, FirstPreloader::class.java, args)
  } else {
	t?.toc("launching app")
	Application.launch(MinimalFXApp::class.java, *args)
  }
  println("main thread has exited from Application.launch")
}

private var fxStopwatch: Stopwatch? = null
private lateinit var fxBlock: (List<String>)->Unit


class FirstPreloader: Preloader() {
  private var bar: ProgressBar? = null
  var stage: Stage? = null
  private fun createPreloaderScene(): Scene {
	bar = ProgressBar()
	val p = BorderPane()
	p.center = bar
	return Scene(p, 300.0, 150.0)
  }

  override fun start(stage: Stage) {
	fxStopwatch?.toc("starting preloader app")
	this.stage = stage
	stage.scene = createPreloaderScene()
	stage.show()
	fxStopwatch?.toc("finished starting preloader app")
  }

  override fun handleProgressNotification(pn: ProgressNotification) {
	bar!!.progress = pn.progress
  }

  override fun handleStateChangeNotification(evt: StateChangeNotification) {
	if (evt.type == BEFORE_START) {
	  stage!!.hide()
	}
  }
}


class MinimalFXApp: Application() {
  //  companion object {
  //	var fxStop: (() -> Unit)? = null
  //  }
  override fun start(primaryStage: Stage?) {
	/* dodge "Unsupported JavaFX configuration..." part 2 */
	fxStopwatch?.toc("starting main app")

	Logging.getJavaFXLogger().enableLogging()
	println("running fxBlock")
	fxBlock(parameters.raw)
	println("ran fxBlock")
	fxStopwatch?.toc("finished starting main app")
  }

  /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
  /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
  /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
  /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
  /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
  /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
  override fun stop() {
	/*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
	/*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
	/*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
	/*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
	/*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
	/*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
  }
  /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
  /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
  /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
  /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
  /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
  /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
}

