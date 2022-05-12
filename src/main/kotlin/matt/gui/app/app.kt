package matt.gui.app

import com.jthemedetecor.OsThemeDetector
import com.sun.javafx.util.Logging
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextArea
import javafx.scene.layout.FlowPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.stage.Screen
import javafx.stage.Stage
import matt.auto.SublimeText
import matt.auto.openInIntelliJ
import matt.exec.app.App
import matt.exec.app.appName
import matt.exec.exception.DefaultUncaughtExceptionHandler.ExceptionResponse
import matt.exec.exception.DefaultUncaughtExceptionHandler.ExceptionResponse.EXIT
import matt.exec.exception.DefaultUncaughtExceptionHandler.ExceptionResponse.IGNORE
import matt.gui.core.scene.MScene
import matt.gui.lang.ActionButton
import matt.gui.loop.runLater
import matt.gui.win.bindgeom.bindGeometry
import matt.gui.win.interact.openInNewWindow
import matt.gui.win.stage.MStage
import matt.json.prim.gson
import java.io.File
import javax.swing.SwingUtilities
import kotlin.concurrent.thread
import kotlin.system.exitProcess


val NEW_MAC_MENU_Y_ESTIMATE = 37.0
val NEW_MAX_MENU_Y_ESTIMATE_SECONDARY = 25.0

class GuiApp(
  args: Array<String> = arrayOf(),
  val screenIndex: Int? = null,
  private val fxThread: GuiApp.(args: List<String>)->Unit,

  ): App(args) {
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
		scene = this@GuiApp.scene
		(scene.root as Region).apply {
		}
		if (screenIndex != null && screenIndex < Screen.getScreens().size) {
		  val screen = Screen.getScreens()[screenIndex]
		  val menuY = if (screen == Screen.getPrimary()) NEW_MAC_MENU_Y_ESTIMATE else NEW_MAX_MENU_Y_ESTIMATE_SECONDARY
		  x = screen.bounds.minX
		  y = screen.bounds.minY + menuY
		  width = screen.bounds.width
		  height = screen.bounds.height - menuY
		}
	  }.show()
	}
  }

  fun scene(op: MScene.()->Unit) {
	scene = MScene(VBox()).apply(op) /*vbox is placeholder*/
  }

  fun rootVbox(op: VBox.()->Unit) {
	scene = MScene(VBox().apply(op))
  }

  fun rootPane(op: Pane.()->Unit) {

	scene = MScene(Pane().apply(op))
  }

  fun rootHbox(op: HBox.()->Unit) {
	scene = MScene(HBox().apply(op))
  }

  fun rootScrollpane(op: ScrollPane.()->Unit) {
	scene = MScene(ScrollPane().apply(op))
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



	Application.launch(FlowFXApp::class.java, *args)
	javafxRunning = false





  }

  override fun extraShutdownHook(
	t: Thread,
	e: Throwable,
	shutdown: (App.()->Unit)?,
	consumeShutdown: (App.()->Unit)?,
	st: String,
	exception_file: File
  ): ExceptionResponse {

	/*dont delete until I find source of disappearing exceptions*/
	println("in extraShutdownHook")


	var r = EXIT
	try {

	  if (Platform.isFxApplicationThread()) {
		println("setting up runLaterReturn for scepction dialog")
		//		runLaterReturn {
		//		println("in runLaterReturn for exception dialog")
		VBox(
		  TextArea(st),
		  FlowPane(
			ActionButton("Open stacktrace in IntelliJ") {
			  exception_file.openInIntelliJ()
			},
			ActionButton("Open stacktrace in Sublime Text") {
			  SublimeText.open(exception_file)
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
		).openInNewWindow(
		  wait = true
		)


	  }
	} catch (e: Exception) {
	  println("exception in matt.exec.exception.DefaultUncaughtExceptionHandler Exception Dialog:")
	  e.printStackTrace()
	  return EXIT
	}

	return r

  }

  fun setup_python_interface(handle_args: GuiApp.(List<String>)->Unit) {
	thread(isDaemon = true) {
	  while (javafxRunning) {
		val stringInput = readLine()
		if (stringInput == null) {
		  println("input null. guess this isn't from python. exiting input thread.")
		  break
		}
		handle_args(gson.fromJson(stringInput, arrayOf<String>()::class.java).toList())
	  }
	}
  }


  val stage by lazy {
	MStage().apply {
	  registerMainStage(this, appName)
	}
  }

  fun registerMainStage(stage: Stage, name: String) {
	stage.apply {
	  bindGeometry(name)
	  if (consumeShudown != null) {
		require(shutdown == null)
		setOnCloseRequest {
		  consumeShudown!!()
		  it.consume()
		}
	  } else {
		setOnCloseRequest {
		  shutdown?.let { sd -> sd() }
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
	  app!!.setup_python_interface((app!!.altPyInterface)!!)
	}
  }
}



