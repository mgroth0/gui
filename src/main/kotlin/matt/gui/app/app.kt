package matt.gui.app

import javafx.application.Platform
import javafx.stage.Screen
import javafx.stage.Window
import matt.async.thread.daemon
import matt.auto.myPid
import matt.exec.app.App
import matt.exec.app.appName
import matt.file.MFile
import matt.fx.control.fxapp.runFXAppBlocking
import matt.fx.control.mscene.MScene
import matt.fx.control.mstage.MStage
import matt.fx.control.mstage.WMode
import matt.fx.control.mstage.WMode.NOTHING
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.fxthread.runLaterReturn
import matt.fx.graphics.mag.NEW_MAC_NOTCH_ESTIMATE
import matt.fx.graphics.mag.NEW_MAX_MENU_Y_ESTIMATE_SECONDARY
import matt.fx.graphics.win.bindgeom.bindGeometry
import matt.fx.graphics.wrapper.FXNodeWrapperDSL
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapperImpl
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.gui.app.threadinspectordaemon.ThreadInspectorDaemon
import matt.gui.exception.showExceptionPopup
import matt.log.logger.Logger
import matt.log.profile.err.ExceptionResponse
import matt.log.profile.err.ExceptionResponse.EXIT
import matt.log.profile.stopwatch.tic
import matt.log.reporter.TracksTime
import matt.log.warn.warn
import matt.model.flowlogic.singlerunlambda.SingleRunLambda
import matt.model.report.Reporter
import kotlin.reflect.full.createInstance

@FXNodeWrapperDSL open class GuiApp(
  args: Array<String> = arrayOf(),
  val screenIndex: Int? = null,
  decorated: Boolean = false,
  wMode: WMode = NOTHING,
  escClosable: Boolean = false,
  enterClosable: Boolean = false,
  requiresBluetooth: Boolean = false,
  private val fxThread: GuiApp.(args: List<String>)->Unit,

  ): App<GuiApp>(args, requiresBluetooth = requiresBluetooth) {

  var alwaysOnTop
	get() = stage.isAlwaysOnTop
	set(value) {
	  stage.isAlwaysOnTop = value
	}

  fun requestFocus() = scene!!.root.requestFocus()

  var scene: MScene<ParentWrapper<*>>? = null

  val fxThreadW: GuiApp.(List<String>)->Unit = {
	val t = tic("fxThreadW", enabled = false)
	t.toc(0)
	fxThread(it)
	t.toc(1)
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
	t.toc(2)
	if (scene != null) {
	  stage.apply {
		scene = this@GuiApp.scene!!
		t.toc(2.5)
		if (this@GuiApp.screenIndex != null && this@GuiApp.screenIndex < Screen.getScreens().size) {
		  val screen = Screen.getScreens()[this@GuiApp.screenIndex]
		  val menuY = if (screen == Screen.getPrimary()) NEW_MAC_NOTCH_ESTIMATE else NEW_MAX_MENU_Y_ESTIMATE_SECONDARY
		  x = screen.bounds.minX
		  y = screen.bounds.minY + menuY
		  width = screen.bounds.width
		  height = screen.bounds.height - menuY
		}
		t.toc(2.6)
	  }.show()
	  t.toc(2.7)
	}
	t.toc(3)
  }


  fun scene(op: MScene<ParentWrapper<*>>.()->Unit) {
	scene = MScene<ParentWrapper<*>>(VBoxWrapperImpl<NodeWrapper>()).apply(op) /*vbox is placeholder*/
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
	usePreloaderApp: Boolean = false,
	t: Reporter? = null
  ) {


	(t as? TracksTime)?.toc("starting GuiApp")



	(t as? TracksTime)?.toc("installed WrapperService")

	val singleRunShutdown = SingleRunLambda {
	  shutdown?.invoke(this)
	}



	main(
	  {
		singleRunShutdown.invoke()
	  },
	  preFX,
	  t = t
	)

	(t as? TracksTime)?.toc("ran main")

	Platform.setImplicitExit(implicitExit)


	(t as? TracksTime)?.toc("about to run FX app blocking")
	(t as? Logger)?.info("launching app (mypid = $myPid)")
	runFXAppBlocking(args = args, usePreloaderApp = usePreloaderApp, reporter = t) {
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
	  println("exception in DefaultUncaughtExceptionHandler Exception Dialog:")
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