package matt.gui.app

import com.sun.javafx.util.Logging
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.layout.Region
import javafx.stage.Screen
import javafx.stage.Stage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import matt.auto.exception.MyDefaultUncaughtExceptionHandler.ExceptionResponse
import matt.auto.exception.MyDefaultUncaughtExceptionHandler.ExceptionResponse.EXIT
import matt.exec.app.App
import matt.exec.app.appName
import matt.file.MFile
import matt.fx.graphics.core.scene.MScene
import matt.fx.graphics.mag.NEW_MAC_NOTCH_ESTIMATE
import matt.fx.graphics.mag.NEW_MAX_MENU_Y_ESTIMATE_SECONDARY
import matt.fx.graphics.win.bindgeom.bindGeometry
import matt.fx.graphics.win.stage.MStage
import matt.gui.exception.showExceptionPopup
import matt.hurricanefx.wrapper.FXNodeWrapperDSL
import matt.hurricanefx.wrapper.pane.vbox.VBoxWrapper
import matt.hurricanefx.wrapper.parent.ParentWrapper
import matt.hurricanefx.wrapper.stage.StageWrapper
import kotlin.concurrent.thread
import kotlin.reflect.full.createInstance

@FXNodeWrapperDSL
open class GuiApp(
  args: Array<String> = arrayOf(),
  val screenIndex: Int? = null,
  decorated: Boolean = false,
  private val fxThread: GuiApp.(args: List<String>)->Unit,

  ): App(args) {

  var alwaysOnTop
	get() = stage.isAlwaysOnTop
	set(value) {
	  stage.isAlwaysOnTop = value
	}

  private var javafxRunning = true
  var altPyInterface: (GuiApp.(List<String>)->Unit)? = null

  sealed class InputHandler {
	object FxThread: InputHandler()
	class Alt(val op: GuiApp.(List<String>)->Unit): InputHandler()
  }

  var shutdown: (GuiApp.()->Unit)? = null
  var consumeShudown: (GuiApp.()->Unit)? = null


  var scene: MScene? = null

  val fxThreadW: GuiApp.(List<String>)->Unit = {
	fxThread(it)
	if (scene != null) {
	  stage.apply {
		scene = this@GuiApp.scene!!.node
		(scene.root as Region).apply {
		}

		if (this@GuiApp.screenIndex != null && this@GuiApp.screenIndex < Screen.getScreens().size) {
		  val screen = Screen.getScreens()[this@GuiApp.screenIndex]
		  val menuY = if (screen == Screen.getPrimary()) NEW_MAC_NOTCH_ESTIMATE else NEW_MAX_MENU_Y_ESTIMATE_SECONDARY
		  x = screen.bounds.minX
		  y = screen.bounds.minY + menuY
		  width = screen.bounds.width
		  height = screen.bounds.height - menuY
		}
	  }.show()
	}
  }

  fun scene(op: MScene.()->Unit) {
	scene = MScene(VBoxWrapper()).apply(op) /*vbox is placeholder*/
  }

  inline fun <reified N: ParentWrapper> root(op: N.()->Unit) {
	scene = MScene(N::class.createInstance().apply(op))
  }

  fun start(
	implicitExit: Boolean = true,
	alt_py_interface: InputHandler? = null,
	alt_app_interface: Map<String, App.(String)->Unit>? = null,
	prefx: (App.()->Unit)? = null,
	shutdown: (App.()->Unit)? = null,
	consumeShutdown: (App.()->Unit)? = null,
  ) {


	this.shutdown = shutdown
	this.consumeShudown = consumeShutdown
	this.altPyInterface = alt_py_interface?.let {
	  when (it) {
		is InputHandler.FxThread -> fxThreadW
		is InputHandler.Alt      -> it.op
	  }
	}
	main(
	  alt_app_interface, shutdown, prefx
	)

	Platform.setImplicitExit(implicitExit)
	app = this
	/* dodge "Unsupported JavaFX configuration..." part 1 */
	Logging.getJavaFXLogger().disableLogging()


	//	SvgImageLoaderFactory.install()
	Application.launch(FlowFXApp::class.java, *args)
	javafxRunning = false


  }

  override fun extraShutdownHook(
	t: Thread,
	e: Throwable,
	shutdown: (App.()->Unit)?,
	consumeShutdown: (App.()->Unit)?,
	st: String,
	exceptionFile: MFile
  ): ExceptionResponse {

	/*dont delete until I find source of disappearing exceptions*/
	println("in extraShutdownHook")


	var r = EXIT
	try {
	  if (Platform.isFxApplicationThread()) {
		r = showExceptionPopup(t, e, shutdown, consumeShutdown, st, exceptionFile)
	  }
	} catch (e: Exception) {
	  println("exception in matt.exec.exception.DefaultUncaughtExceptionHandler Exception Dialog:")
	  e.printStackTrace()
	  return EXIT
	}
	return r
  }

  fun setupPythonInterface(handleArgs: GuiApp.(List<String>)->Unit) {
	thread(isDaemon = true) {
	  while (javafxRunning) {
		val stringInput = readLine()
		if (stringInput == null) {
		  println("input null. guess this isn't from python. exiting input thread.")
		  break
		}
		handleArgs(Json.decodeFromString<List<String>>(stringInput).toList())
	  }
	}
  }


  val stage by lazy {
	MStage(decorated = decorated).apply {
	  this@GuiApp.registerMainStage(this, appName)
	}
  }

  fun registerMainStage(stage: StageWrapper, name: String) {
	stage.apply {
	  bindGeometry(name)
	  if (this@GuiApp.consumeShudown != null) {
		require(this@GuiApp.shutdown == null)
		setOnCloseRequest {
		  this@GuiApp.run {
			consumeShudown!!()
		  }
		  it.consume()
		}
	  } else {
		setOnCloseRequest {
		  this@GuiApp.shutdown?.let { sd -> this@GuiApp.sd() }
		}
	  }

	}
  }

}

private var app: GuiApp? = null

class FlowFXApp: Application() {
  override fun start(primaryStage: Stage?) {
	/* dodge "Unsupported JavaFX configuration..." part 2 */
	Logging.getJavaFXLogger().enableLogging()
	app!!.apply { fxThreadW(app!!.args.toList()) }
	if (app!!.altPyInterface != null) {
	  app!!.setupPythonInterface((app!!.altPyInterface)!!)
	}
  }
}



