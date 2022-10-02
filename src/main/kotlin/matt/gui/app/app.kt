package matt.gui.app

import javafx.application.Platform
import javafx.stage.Screen
import javafx.stage.Window
import matt.async.thread.daemon
import matt.auto.exception.MyDefaultUncaughtExceptionHandler.ExceptionResponse
import matt.auto.exception.MyDefaultUncaughtExceptionHandler.ExceptionResponse.EXIT
import matt.exec.app.App
import matt.exec.app.appName
import matt.file.MFile
import matt.fx.graphics.core.scene.MScene
import matt.fx.graphics.fxthread.runLaterReturn
import matt.fx.graphics.mag.NEW_MAC_NOTCH_ESTIMATE
import matt.fx.graphics.mag.NEW_MAX_MENU_Y_ESTIMATE_SECONDARY
import matt.fx.graphics.win.bindgeom.bindGeometry
import matt.fx.graphics.win.stage.MStage
import matt.fx.graphics.win.stage.WMode
import matt.fx.graphics.win.stage.WMode.NOTHING
import matt.gui.app.fxapp.runFXAppBlocking
import matt.gui.app.threadinspectordaemon.ThreadInspectorDaemon
import matt.gui.exception.showExceptionPopup
import matt.hurricanefx.wrapper.FXNodeWrapperDSL
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapperImpl
import matt.hurricanefx.wrapper.wrapped
import matt.log.warn
import matt.model.flowlogic.singlerunlambda.SingleRunLambda
import kotlin.reflect.full.createInstance

@FXNodeWrapperDSL open class GuiApp(
  args: Array<String> = arrayOf(),
  val screenIndex: Int? = null,
  decorated: Boolean = false,
  wMode: WMode = NOTHING,
  escClosable: Boolean = false,
  enterClosable: Boolean = false,
  private val fxThread: GuiApp.(args: List<String>)->Unit,

  ): App<GuiApp>(args) {

  var alwaysOnTop
	get() = stage.isAlwaysOnTop
	set(value) {
	  stage.isAlwaysOnTop = value
	}

  fun requestFocus() = scene!!.root.requestFocus()

  var scene: MScene<ParentWrapper<*>>? = null

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
		scene = this@GuiApp.scene!!

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


  fun scene(op: MScene<ParentWrapper<*>>.()->Unit) {
	scene = MScene<ParentWrapper<*>>(VBoxWrapper<NodeWrapper>()).apply(op) /*vbox is placeholder*/
  }

  fun initRoot(n: ParentWrapper<*>) {
	scene = MScene(n)
  }

  inline fun <reified N: ParentWrapperImpl<*, *>> root(op: N.()->Unit = {}): N {
	val r = N::class.createInstance()
	initRoot(r.apply(op))
	return r
  }

  fun start(
	implicitExit: Boolean = true,
	preFX: (App<*>.()->Unit)? = null,
	shutdown: (App<*>.()->Unit)? = null,
	usePreloaderApp: Boolean = false
  ) {


	val singleRunShutdown = SingleRunLambda {
	  shutdown?.invoke(this)
	}

	main({
	  singleRunShutdown.invoke()
	}, preFX)

	Platform.setImplicitExit(implicitExit)

	runFXAppBlocking(args = args, usePreloaderApp = usePreloaderApp) {
	  fxThreadW(this@GuiApp.args.toList())
	}
	singleRunShutdown()
	ThreadInspectorDaemon.start()
  }

  override fun extraShutdownHook(
	t: Thread, e: Throwable, shutdown: (App<*>.()->Unit)?, st: String, exceptionFile: MFile
  ): ExceptionResponse {

	/*dont delete until I find source of disappearing exceptions*/
	println("in extraShutdownHook")


	var r = EXIT
	try {
	  if (Platform.isFxApplicationThread()) {
		println("showing exception popup for t=$t, e=$e")
		r = showExceptionPopup(t, e, shutdown, st, exceptionFile)
	  }
	} catch (e: Exception) {
	  println("exception in matt.exec.exception.DefaultUncaughtExceptionHandler Exception Dialog:")
	  e.printStackTrace()
	  return EXIT
	}
	return r
  }

  val stage by lazy {
	MStage(
	  decorated = decorated, wMode = wMode, EscClosable = escClosable, EnterClosable = enterClosable
	).apply {
	  bindGeometry(appName)
	}
  }
}