package matt.gui.app

import com.sun.javafx.util.Logging
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.layout.Region
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.Window
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import matt.async.thread.daemon
import matt.auto.activateThisProcess
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
import matt.fx.graphics.win.stage.WMode
import matt.fx.graphics.win.stage.WMode.NOTHING
import matt.gui.exception.showExceptionPopup
import matt.hurricanefx.async.runLaterReturn
import matt.hurricanefx.wrapper.FXNodeWrapperDSL
import matt.hurricanefx.wrapper.pane.vbox.VBoxWrapper
import matt.hurricanefx.wrapper.parent.ParentWrapper
import matt.hurricanefx.wrapper.parent.ParentWrapperImpl
import matt.hurricanefx.wrapper.stage.StageWrapper
import matt.hurricanefx.wrapper.wrapped
import matt.stream.message.ACTIVATE
import matt.stream.message.ActionResult
import matt.stream.message.InterAppMessage
import matt.stream.message.NOTHING_TO_SEND
import matt.klib.log.warn
import kotlin.concurrent.thread
import kotlin.reflect.full.createInstance

@FXNodeWrapperDSL open class GuiApp(
  args: Array<String> = arrayOf(),
  val screenIndex: Int? = null,
  decorated: Boolean = false,
  wMode: WMode = NOTHING,
  EscClosable: Boolean = false,
  EnterClosable: Boolean = false,
  private val fxThread: GuiApp.(args: List<String>)->Unit,

  ): App(args) {

  var alwaysOnTop
	get() = stage.isAlwaysOnTop
	set(value) {
	  stage.isAlwaysOnTop = value
	}

  fun requestFocus() = scene!!.root.requestFocus()

  private var javafxRunning = true
  var altPyInterface: (GuiApp.(List<String>)->Unit)? = null

  sealed class InputHandler {
	object FxThread: InputHandler()
	class Alt(val op: GuiApp.(List<String>)->Unit): InputHandler()
  }

  var shutdown: (GuiApp.()->Unit)? = null
  var consumeShudown: (GuiApp.()->Unit)? = null


  var scene: MScene<ParentWrapper>? = null

  val fxThreadW: GuiApp.(List<String>)->Unit = {
	fxThread(it)
	daemon {
	  Window.getWindows().map { it.wrapped() }.forEach {
		if (it.isShowing && it.screen == null && it.pullBackWhenOffScreen) {
		  warn("resetting offscreen window")
		  runLaterReturn {
			it.x = 0.0
			it.y = 0.0
			it.width = 500.0
			it.height = 500.0
		  }
		}
	  }
	  Thread.sleep(5000)
	}
	if (scene != null) {
	  stage.apply {
		scene = this@GuiApp.scene!!.node
		(scene.root as Region).apply { }

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

  fun scene(op: MScene<ParentWrapper>.()->Unit) {
	scene = MScene<ParentWrapper>(VBoxWrapper()).apply(op) /*vbox is placeholder*/
  }

  inline fun <reified N: ParentWrapperImpl<*>> root(op: N.()->Unit) {
	scene = MScene(N::class.createInstance().apply(op))
  }

  fun start(
	implicitExit: Boolean = true,
	alt_py_interface: InputHandler? = null,
	alt_app_interface: (App.(InterAppMessage)->ActionResult)? = null,
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
	  { x: InterAppMessage ->
		when (x) {
		  is ACTIVATE -> {
			activateThisProcess()
			NOTHING_TO_SEND
		  }

		  else        -> alt_app_interface?.invoke(this, x)
		}
	  }, shutdown, prefx
	)

	Platform.setImplicitExit(implicitExit)
	app = this    /* dodge "Unsupported JavaFX configuration..." part 1 */
	Logging.getJavaFXLogger().disableLogging()


	//	SvgImageLoaderFactory.install()
	println("launching!!!")
	Application.launch(FlowFXApp::class.java, *args)
	println("done with launch!!!")
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
		println("showing exception popup for t=$t, e=$e")
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
	MStage(
	  decorated = decorated, wMode = wMode, EscClosable = EscClosable, EnterClosable = EnterClosable
	).apply {
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
  override fun start(primaryStage: Stage?) {    /* dodge "Unsupported JavaFX configuration..." part 2 */
	Logging.getJavaFXLogger().enableLogging()
	app!!.apply { fxThreadW(app!!.args.toList()) }
	if (app!!.altPyInterface != null) {
	  app!!.setupPythonInterface((app!!.altPyInterface)!!)
	}
  }
}



