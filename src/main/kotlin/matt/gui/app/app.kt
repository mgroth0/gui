package matt.gui.app

import javafx.application.Platform
import javafx.stage.Screen
import javafx.stage.Window
import matt.async.thread.daemon
import matt.exec.app.App
import matt.file.MFile
import matt.file.commons.LogContext
import matt.file.commons.mattLogContext
import matt.fx.control.fxapp.DEFAULT_THROW_ON_APP_THREAD_THROWABLE
import matt.fx.control.fxapp.runFXAppBlocking
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.fxthread.ensureInFXThreadInPlace
import matt.fx.graphics.fxthread.runLaterReturn
import matt.fx.graphics.mag.NEW_MAC_NOTCH_ESTIMATE
import matt.fx.graphics.mag.NEW_MAX_MENU_Y_ESTIMATE_SECONDARY
import matt.fx.graphics.wrapper.FXNodeWrapperDSL
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapperImpl
import matt.fx.graphics.wrapper.pane.vbox.VBoxW
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.gui.app.threadinspectordaemon.ThreadInspectorDaemon
import matt.gui.bindgeom.bindGeometry
import matt.gui.exception.showExceptionPopup
import matt.gui.interact.WindowConfig
import matt.gui.mscene.MScene
import matt.gui.mstage.MStage
import matt.gui.mstage.WMode
import matt.gui.mstage.WMode.NOTHING
import matt.lang.sysprop.props.Monocle
import matt.log.logger.Logger
import matt.log.profile.err.ExceptionResponse
import matt.log.profile.err.ExceptionResponse.EXIT
import matt.log.reporter.TracksTime
import matt.log.warn.warn
import matt.model.code.report.Reporter
import matt.model.flowlogic.singlerunlambda.SingleRunLambda
import matt.rstruct.modID
import kotlin.reflect.full.createInstance

fun startFXWidget(rootOp: VBoxW.() -> Unit) {
    runFXAppBlocking {
        root<VBoxW> {
            rootOp()
        }
    }
}

fun runFXWidgetBlocking(rootOp: VBoxW.() -> Unit) {
    runFXAppBlocking {
        root<VBoxW> {
            rootOp()
        }
    }
}

fun runFXAppBlocking(
    decorated: Boolean = WindowConfig.DEFAULT.decorated,
    fxThread: GuiApp.(args: List<String>) -> Unit
) {
    GuiApp(fxThread = fxThread, decorated = decorated).runBlocking()
}

@FXNodeWrapperDSL
open class GuiApp(
    args: Array<String> = arrayOf(),
    val screenIndex: Int? = null,
    decorated: Boolean = WindowConfig.DEFAULT.decorated,
    wMode: WMode = NOTHING,
    escClosable: Boolean = false,
    enterClosable: Boolean = false,
    requiresBluetooth: Boolean = false,
    private val fxThread: GuiApp.(args: List<String>) -> Unit,

    ) : App<GuiApp>(args, requiresBluetooth = requiresBluetooth) {

    var alwaysOnTop
        get() = stage.isAlwaysOnTop
        set(value) {
            stage.isAlwaysOnTop = value
        }

    fun requestFocus() = scene!!.root.requestFocus()

    var scene: MScene<ParentWrapper<*>>? = null

    val fxThreadW: GuiApp.(List<String>) -> Unit = {
//        val t = tic("fxThreadW", enabled = false)
//        t.toc(0)
        fxThread(it)
//        t.toc(1)
        if (!Monocle.isEnabledInThisRuntime()) {
            println("running window fixer daemon")
            daemon(name = "Window Fixer Daemon") {
                while (true) {
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
            }
        } else {
            println("did not run window fixer daemon")
        }
//        t.toc(2)
        if (scene != null) {
            stage.apply {
                scene = this@GuiApp.scene!!
//                t.toc(2.5)
                if (!Monocle.isEnabledInThisRuntime()) {
                    if (this@GuiApp.screenIndex != null && this@GuiApp.screenIndex < Screen.getScreens().size) {
                        val screen = Screen.getScreens()[this@GuiApp.screenIndex]
                        val menuY =
                            if (screen == Screen.getPrimary()) NEW_MAC_NOTCH_ESTIMATE else NEW_MAX_MENU_Y_ESTIMATE_SECONDARY
                        x = screen.bounds.minX
                        y = screen.bounds.minY + menuY
                        width = screen.bounds.width
                        height = screen.bounds.height - menuY
                    }
                }
//                t.toc(2.6)
            }.show()
//            t.toc(2.7)
        }
//        t.toc(3)
    }


    fun scene(op: MScene<ParentWrapper<*>>.() -> Unit) {
        scene = MScene<ParentWrapper<*>>(VBoxWrapperImpl<NodeWrapper>()).apply(op) /*vbox is placeholder*/
    }

    fun initRoot(n: ParentWrapper<*>) {
        scene = MScene(n)
    }

    inline fun <reified N : ParentWrapperImpl<*, *>> root(op: N.() -> Unit = {}): N {
        val r = N::class.createInstance()
        initRoot(r.apply(op))
        return r
    }

    fun runBlocking(
        implicitExit: Boolean = true,
        preFX: (App<*>.() -> Unit)? = null,
        shutdown: (App<*>.() -> Unit)? = null,
        usePreloaderApp: Boolean = false,
        logContext: LogContext = mattLogContext,
        t: Reporter? = null,
        throwOnApplicationThreadThrowable: Boolean = DEFAULT_THROW_ON_APP_THREAD_THROWABLE
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
            logContext = logContext,
            t = t,
            enableExceptionAndShutdownHandlers = !throwOnApplicationThreadThrowable
        )

        (t as? TracksTime)?.toc("ran main")

        Platform.setImplicitExit(implicitExit)


        (t as? TracksTime)?.toc("about to run FX app blocking")
        (t as? Logger)?.info("launching app (mypid = ${matt.lang.myPid})")
        runFXAppBlocking(
            args = args,
            usePreloaderApp = usePreloaderApp,
            reporter = t,
            throwOnApplicationThreadThrowable = throwOnApplicationThreadThrowable
        ) {
            fxThreadW(args.toList())
        }
        singleRunShutdown()
        ThreadInspectorDaemon.start()
    }

    override fun extraShutdownHook(
        t: Thread,
        e: Throwable,
        shutdown: (App<*>.() -> Unit)?,
        st: String,
        exceptionFile: MFile
    ): ExceptionResponse {
        /*don't delete .. I find source of disappearing exceptions*/
        println("in extraShutdownHook")
        var r = EXIT
        try {
            ensureInFXThreadInPlace {
                println("showing exception popup for t=$t, e=$e")
                r = showExceptionPopup(t, e, shutdown, st)
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
            bindGeometry(modID.appName)
        }
    }
}