package matt.gui.interact

import javafx.application.Platform.runLater
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.WARNING
import javafx.scene.control.ButtonType
import javafx.scene.control.TextInputDialog
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Modality.APPLICATION_MODAL
import javafx.stage.Modality.NONE
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.Window
import kotlinx.serialization.json.Json
import matt.file.construct.mFile
import matt.file.toJioFile
import matt.fx.control.lang.actionbutton
import matt.fx.control.tfx.dialog.alert
import matt.fx.control.wrapper.control.button.ButtonWrapper
import matt.fx.control.wrapper.control.button.button
import matt.fx.control.wrapper.control.choice.choicebox
import matt.fx.control.wrapper.control.text.area.textarea
import matt.fx.control.wrapper.control.text.field.textfield
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.fxthread.ensureInFXThreadInPlace
import matt.fx.graphics.style.border.FXBorder
import matt.fx.graphics.style.border.solidBorder
import matt.fx.graphics.wrapper.imageview.ImageViewWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.disableWhen
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.node.setOnDoubleClick
import matt.fx.graphics.wrapper.pane.anchor.AnchorPaneWrapperImpl
import matt.fx.graphics.wrapper.pane.hbox.hbox
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.fx.graphics.wrapper.stage.StageWrapper
import matt.fx.graphics.wrapper.text.text
import matt.fx.graphics.wrapper.window.WindowWrapper
import matt.gui.bindgeom.bindGeometry
import matt.gui.interact.WinGeom.Centered
import matt.gui.interact.WinOwn.Auto
import matt.gui.mscene.MScene
import matt.gui.mstage.MStage
import matt.gui.mstage.ShowMode
import matt.gui.mstage.ShowMode.DO_NOT_SHOW
import matt.gui.mstage.ShowMode.SHOW
import matt.gui.mstage.ShowMode.SHOW_AND_WAIT
import matt.gui.mstage.WMode
import matt.gui.mstage.WMode.CLOSE
import matt.gui.mstage.WMode.NOTHING
import matt.json.prim.isValidJson
import matt.lang.common.go
import matt.lang.common.noExceptions
import matt.lang.common.nullIfExceptions
import matt.lang.model.file.MacFileSystem
import matt.model.data.rect.DoubleRectSize
import matt.obs.bind.binding
import matt.obs.bindings.bool.ObsB
import matt.obs.bindings.bool.not
import matt.obs.prop.ObsVal
import matt.obs.prop.writable.BindableProperty
import java.net.URI
import java.util.Optional
import java.util.WeakHashMap
import kotlin.jvm.optionals.getOrNull

fun safe(
    s: String,
    op: () -> Unit
): Boolean {
    var r = false
    alert(
        Alert.AlertType.CONFIRMATION,
        header = s,
        content = s,
        owner =
            WindowWrapper.windows().firstOrNull {
                it.focused
            }
    ) {
        if (it.buttonData.isDefaultButton) {
            op()
            r = true
        }
    }
    return r
}

class MDialog<R> internal constructor() : VBoxWrapperImpl<NodeWrapper>() {
    val stg =
        MStage(wMode = CLOSE, EscClosable = true).apply {
            initModality(APPLICATION_MODAL)
            scene = MScene(this@MDialog)
            width = 400.0
            height = 400.0
            setOnCloseRequest {
                println("CLOSE REQUESTED FOR $this")
            }
        }
    lateinit var confirmButton: ButtonWrapper
    fun confirm() = confirmButton.fire()
    val window get() = stg
    var x: Double? = null
    var y: Double? = null
    var owner: Window? = null
    var autoOwner: Boolean = true
    private var resultConverter: () -> R? = { null }
    fun getResult() = resultConverter()
    fun setResultConverter(op: () -> R?) {
        resultConverter = op
    }


    val readyProperty = BindableProperty(true)

    fun readyWhen(o: ObsB) {
        readyProperty.bind(o)
    }

    init {
        exactHeightProperty.bind(stg.heightProperty)
        border = FXBorder.solid(Color.DARKBLUE)
        styleClass.add("MDialog")
    }
}

val aXBindingStrengthener = WeakHashMap<Stage, ObsVal<Double>>()
val aYBindingStrengthener = WeakHashMap<Stage, ObsVal<Double>>()
fun StageWrapper.bindXYToOwnerCenter() {

    requireNotNull(owner) {
        "must use initOwner before bindXYToOwnerCenter"
    }

    val xBinding =
        owner!!.xProperty.binding(owner!!.widthProperty, widthProperty) {
            (owner!!.x + (owner!!.width / 2)) - width / 2
        }

    val yBinding =
        owner!!.yProperty.binding(owner!!.heightProperty, heightProperty) {
            (owner!!.y + (owner!!.height / 2)) - height / 2
        }
    aXBindingStrengthener[node] = xBinding
    aYBindingStrengthener[node] = yBinding
    x = xBinding.value
    xBinding.onChange {
        x = it
    }
    y = yBinding.value
    yBinding.onChange {
        y = it
    }
}

fun StageWrapper.bindHWToOwner() {
    requireNotNull(owner) {
        "must use initOwner before bindXYToOwnerCenter"
    }
    width = owner!!.width
    owner!!.widthProperty.onChange {
        width = it
    }
    height = owner!!.height
    owner!!.heightProperty.onChange {
        y = it
    }
}


inline fun <reified T> jsonEditor(json: String? = null) =
    dialog<T?> {
        val ta = textarea(json ?: "")

        val goodBind =
            ta.textProperty.binding {

                it != null
                    && it.isValidJson()
                    && noExceptions { Json.decodeFromString<T>(it) }
            }
        readyWhen(goodBind)
        ta.border = Color.BLACK.solidBorder() /*so it does not jitter*/
        goodBind.onChange {
            ta.border = if (it) Color.BLACK.solidBorder() else Color.RED.solidBorder()
        }
        setResultConverter {
            ta.text.takeIf { it.isValidJson() }?.let { nullIfExceptions { Json.decodeFromString<T>(it) } }
        }
    }

fun popupWarning(string: String): Optional<ButtonType> =
    ensureInFXThreadInPlace {
        alert(WARNING, string).showAndWait()
    }

fun popupTextInput(
    prompt: String,
    default: String = ""
) = ensureInFXThreadInPlace {
    dialog {
        text(prompt)
        val t = textfield(default)
        setResultConverter {
            t.text
        }
    }
}

fun <T : Any> popupChoiceBox(
    prompt: String,
    choices: List<T>
) = ensureInFXThreadInPlace {
    dialog {
        text(prompt)
        val cb =
            choicebox<T>(
                values = choices
            )
        setResultConverter {
            cb.value
        }
    }
}

fun <R> NodeWrapper.dialog(
    cfg: MDialog<R>.() -> Unit
): R? =
    globalDialog<R> {
        owner = this@dialog.stage?.node
        cfg.invoke(this)
    }

private inline fun <R> globalDialog(
    crossinline cfg: MDialog<R>.() -> Unit
): R? =
    dialog<R> {
        cfg.invoke(this)
    }

fun <R> dialog(
    cfg: MDialog<R>.() -> Unit
): R? {
    val d = MDialog<R>()
    d.apply(cfg)
    (d.owner ?: if (d.autoOwner) Window.getWindows().firstOrNull() else null)?.go {
        d.stg.initOwner(it)
    } ?: d.stg.initNoOwner()

    if (d.stg.owner != null) {
        Centered().applyTo(d.stg)
    }
    var r: R? = null
    d.hbox<NodeWrapper> {
        prefWidthProperty.bind(d.widthProperty)
        alignment = Pos.CENTER
        actionbutton("cancel") {
            styleClass += "CancelButton"
            d.stg.close()
        }
        d.confirmButton =
            button("confirm") {
                styleClass += "ConfirmButton"
                disableWhen { d.readyProperty.not() }
                setOnAction {
                    r = d.getResult()
                    d.stg.close()
                }
            }
        d.scene!!.addEventFilter(KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.ENTER) {
                if (d.readyProperty.value) {
                    d.confirmButton.fire()
                }
            }
        }
    }
    d.window.showAndWait()
    return r
}


sealed class WinGeom {


    class Bound(val key: String) : WinGeom() {
        override fun applyTo(win: StageWrapper) {
            win.bindGeometry(key)
        }
    }

    class ManualOr0(
        val x: Double = 0.0,
        val y: Double = 0.0,
        val size: DoubleRectSize = DoubleRectSize.ZERO
    ) : WinGeom() {
        override fun applyTo(win: StageWrapper) {
            win.x = x
            win.y = y
            win.height = size.height
            win.width = size.width
        }
    }

    class ManualOrOwner(
        val x: Double? = null,
        val y: Double? = null,
        val size: DoubleRectSize? = null
    ) : WinGeom() {
        override fun applyTo(win: StageWrapper) {
            requireNotNull(win.owner) { "use initOwner first" }
            win.x = x ?: win.owner!!.x
            win.y = y ?: win.owner!!.y
            win.height = size?.height ?: win.owner!!.height
            win.width = size?.width ?: win.owner!!.width
        }
    }

    class Centered(
        val width: Double = 400.0,
        val height: Double = 400.0,
        val bind: Boolean = true
    ) : WinGeom() {
        override fun applyTo(win: StageWrapper) {
            win.width = width
            win.height = height
            if (win.owner == null) {
                win.centerOnScreen()
            } else {
                if (bind) win.bindXYToOwnerCenter()
            }
            /*require(win.owner != null) { "use initOwner first" }*/
        }
    }

    object Max : WinGeom() {
        override fun applyTo(win: StageWrapper) {
            win.isMaximized = true
            /*require(win.owner != null) { "use initOwner first" }*/
        }
    }

    object FullScreen : WinGeom() {
        override fun applyTo(win: StageWrapper) {
            win.isFullScreen = true
        }
    }


    object CenteredMinWrapContent : WinGeom() {
        override fun applyTo(win: StageWrapper) {
            requireNotNull(win.owner) { "use initOwner first" }

            win.bindXYToOwnerCenter()
        }
    }

    class MatchOwner : WinGeom() {
        override fun applyTo(win: StageWrapper) {
            requireNotNull(win.owner) { "use initOwner first" }
            win.bindXYToOwnerCenter()
            win.bindHWToOwner()
        }
    }

    abstract fun applyTo(win: StageWrapper)
}

sealed class WinOwn {
    object None : WinOwn() {
        override fun applyTo(win: StageWrapper) {
            win.initNoOwner()
        }
    }

    class Owner(val owner: WindowWrapper<*>) : WinOwn() {
        override fun applyTo(win: StageWrapper) {
            win.initOwner(owner)
        }
    }

    object Auto : WinOwn() {
        override fun applyTo(win: StageWrapper) {
            Window.getWindows().firstOrNull()?.go {
                win.initOwner(it)
            } ?: win.initNoOwner()
        }
    }

    abstract fun applyTo(win: StageWrapper)
}

data class WindowConfig(
    val showMode: ShowMode = SHOW,
    val modality: Modality = NONE,
    val wMode: WMode = NOTHING,
    val EscClosable: Boolean = false,
    val EnterClosable: Boolean = false,
    val own: WinOwn = Auto,
    val geom: WinGeom = Centered(),
    val mScene: Boolean = true,
    val border: Boolean = true,
    val decorated: Boolean = false,
    val alwaysOnTop: Boolean = false,
    val title: String? = null,
    val beforeShowing: StageWrapper.() -> Unit = {}
) {
    companion object {
        val DEFAULT by lazy {
            WindowConfig()
        }
    }

    fun createWindow(root: ParentWrapper<*>) =
        MStage(
            wMode = wMode,
            EscClosable = EscClosable,
            EnterClosable = EnterClosable,
            decorated = decorated
        ).also {
            applyToAlreadyCreated(it, root)
        }

    fun applyTo(
        window: MStage,
        root: ParentWrapper<*>
    ) {
        window.wMode = wMode
        window.EscClosable = EscClosable
        window.EnterClosable = EnterClosable
        window.decorated = decorated
        applyToAlreadyCreated(window, root)
    }

    private fun applyToAlreadyCreated(
        window: MStage,
        root: ParentWrapper<*>
    ) {
        window.initModality(modality)
        window.apply {
            isAlwaysOnTop = alwaysOnTop
            if (this@WindowConfig.title != null) {
                require(decorated)
                this.title = this@WindowConfig.title
            }
            scene = if (mScene) MScene(root) else Scene(root.node).wrapped()
            own.applyTo(this)
            geom.applyTo(this)
            if (border) {
                (root as RegionWrapper).border = Color.DARKBLUE.solidBorder()
            }
            beforeShowing()
            when (showMode) {
                SHOW -> show()
                SHOW_AND_WAIT -> showAndWait()
                DO_NOT_SHOW -> Unit
            }
        }
    }
}


fun ParentWrapper<*>.openInNewWindow(
    showMode: ShowMode = SHOW,
    wMode: WMode = NOTHING,
    EscClosable: Boolean = false,
    EnterClosable: Boolean = false,
    own: WinOwn = Auto,
    geom: WinGeom = Centered(),
    mScene: Boolean = true,
    border: Boolean = true,
    decorated: Boolean = false,
    alwaysOnTop: Boolean = false,
    title: String? = null,
    beforeShowing: StageWrapper.() -> Unit = {}
) = WindowConfig(
    showMode = showMode,
    wMode = wMode,
    EscClosable = EscClosable,
    EnterClosable = EnterClosable,
    own = own,
    geom = geom,
    mScene = mScene,
    border = border,
    decorated = decorated,
    alwaysOnTop = alwaysOnTop,
    title = title,
    beforeShowing = beforeShowing
).createWindow(this@openInNewWindow)


fun matt.file.JioFile.openImageInWindow() {
    AnchorPaneWrapperImpl<NodeWrapper>(
        ImageViewWrapper(this@openImageInWindow.toUri().toString()).apply {
            isPreserveRatio = true
            runLater {
                fitHeightProperty.bind(scene!!.window!!.heightProperty.cast())
                fitWidthProperty.bind(scene!!.window!!.widthProperty.cast())
                setOnDoubleClick { (scene!!.window as StageWrapper).close() }
            }
        }
    ).openInNewWindow()
}

fun ImageViewWrapper.doubleClickToOpenInWindow() {
    setOnDoubleClick { with(MacFileSystem) { mFile(URI(this@doubleClickToOpenInWindow.image!!.url)) }.toJioFile().openImageInWindow() }
}

fun NodeWrapper.textInput(
    default: String = "insert default here",
    prompt: String = "insert prompt here"
): String? =
    TextInputDialog(default).apply {
        initOwner(stage?.node)
        contentText = prompt
        initStyle(StageStyle.UTILITY)
    }.showAndWait().getOrNull()

