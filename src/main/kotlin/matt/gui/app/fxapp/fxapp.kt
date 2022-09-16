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


fun runFXAppBlocking(args: Array<String>, usePreloaderApp: Boolean = false, fxOp: (List<String>)->Unit) {
  fxBlock = fxOp
  Logging.getJavaFXLogger().disableLogging() /* dodge "Unsupported JavaFX configuration..." part 1 */
  println("launching app")
  if (usePreloaderApp) LauncherImpl.launchApplication(MinimalFXApp::class.java, FirstPreloader::class.java, args)
  else Application.launch(MinimalFXApp::class.java, *args)
  println("launched app")
}

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
	this.stage = stage
	stage.scene = createPreloaderScene()
	stage.show()
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
  companion object {
	var fxStop: (() -> Unit)? = null
  }
  override fun start(primaryStage: Stage?) {
	/* dodge "Unsupported JavaFX configuration..." part 2 */


	Logging.getJavaFXLogger().enableLogging()
	println("running fxBlock")
	fxBlock(parameters.raw)
	println("ran fxBlock")
  }

  override fun stop() {
	fxStop?.invoke()
  }
}

