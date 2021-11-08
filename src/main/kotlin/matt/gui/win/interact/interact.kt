package matt.gui.win.interact

import javafx.beans.binding.DoubleBinding
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.TextInputDialog
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.Window
import matt.gui.core.scene.MScene
import matt.gui.lang.actionbutton
import matt.gui.style.borderFill
import matt.gui.win.bindgeom.bindGeometry
import matt.gui.win.stage.MStage
import matt.gui.win.stage.WMode
import matt.gui.win.stage.WMode.CLOSE
import matt.gui.win.stage.WMode.NOTHING
import matt.hurricanefx.exactHeightProperty
import matt.hurricanefx.eye.lang.BProp
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.eye.prop.doubleBinding
import matt.hurricanefx.tornadofx.async.runLater
import matt.hurricanefx.tornadofx.control.button
import matt.hurricanefx.tornadofx.dialog.alert
import matt.hurricanefx.tornadofx.layout.hbox
import matt.hurricanefx.tornadofx.nodes.disableWhen
import matt.hurricanefx.tornadofx.nodes.onDoubleClick
import java.io.File
import java.net.URI
import java.util.Optional
import java.util.WeakHashMap

fun safe(s: String, op: ()->Unit): Boolean {
  var r = false
  alert(
	Alert.AlertType.CONFIRMATION,
	header = s,
	content = s,
	owner = Stage.getWindows().firstOrNull {
	  it.isFocused
	}
  ) {
	if (it.buttonData.isDefaultButton) {
	  op()
	  r = true
	}
  }
  return r
}

class MDialog<R> internal constructor(): VBox() {
  val stage = MStage(wMode = CLOSE, EscClosable = true).apply {
	scene = MScene(this@MDialog)
	width = 400.0
	height = 400.0
  }
  val window get() = stage
  var x: Double? = null
  var y: Double? = null
  var owner: Window? = null
  var autoOwner: Boolean = true
  private var resultConverter: ()->R? = { null }
  fun getResult() = resultConverter()
  fun setResultConverter(op: ()->R?) {
	resultConverter = op
  }

  val readyProperty = BProp(true)

  fun readyWhen(o: ObservableValue<Boolean>) {
	readyProperty.bind(o)
  }

  init {
	exactHeightProperty().bind(stage.heightProperty())
	borderFill = Color.DARKBLUE
  }
}

val aXBindingStrengthener = WeakHashMap<Stage, DoubleBinding>()
val aYBindingStrengthener = WeakHashMap<Stage, DoubleBinding>()
fun Stage.bindXYToOwnerCenter() {
  require(owner != null) {
	"must use initOwner before bindXYToOwnerCenter"
  }
  val xBinding = owner.xProperty().doubleBinding(owner.widthProperty(), this.widthProperty()) {
	(owner.x + (owner.width/2)) - width/2
  }
  val yBinding = owner.yProperty().doubleBinding(owner.heightProperty(), this.heightProperty()) {
	(owner.y + (owner.height/2)) - height/2
  }
  aXBindingStrengthener[this] = xBinding
  aYBindingStrengthener[this] = yBinding
  x = xBinding.value
  xBinding.onChange {
	x = it
  }
  y = yBinding.value
  yBinding.onChange {
	y = it
  }
}

fun Stage.bindHWToOwner() {
  require(owner != null) {
	"must use initOwner before bindXYToOwnerCenter"
  }
  width = owner.width
  owner.widthProperty().onChange {
	width = it
  }
  height = owner.height
  owner.heightProperty().onChange {
	y = it
  }
}


fun <R> dialog(
  cfg: MDialog<R>.()->Unit
): R? {
  val d = MDialog<R>()
  d.apply(cfg)
  d.stage.initOwner(d.owner ?: if (d.autoOwner) Window.getWindows().firstOrNull() else null)
  if (d.stage.owner != null) {
	WinGeom.Centered().applyTo(d.stage)
  } // d.stage.initAndCenterToOwner(own)
  var r: R? = null
  d.hbox {
	prefWidthProperty().bind(d.widthProperty())
	alignment = Pos.CENTER
	actionbutton("cancel") {
	  styleClass += "CancelButton"
	  d.stage.close()
	}
	val confirmButton = button("confirm") {
	  styleClass += "ConfirmButton"
	  disableWhen { d.readyProperty.not() }
	  setOnAction {
		r = d.getResult()
		d.stage.close()
	  }
	}
	d.scene.addEventFilter(KeyEvent.KEY_PRESSED) {
	  if (it.code == KeyCode.ENTER) {
		if (d.readyProperty.value) {
		  confirmButton.fire()
		}
	  }
	}
  }
  d.window.showAndWait()
  return r
}


sealed class WinGeom {
  class Bound(val key: String): WinGeom() {
	override fun applyTo(win: Stage) {
	  win.bindGeometry(key)
	}
  }

  class ManualOr0(
	val x: Double = 0.0,
	val y: Double = 0.0,
	val width: Double = 0.0,
	val height: Double = 0.0
  ): WinGeom() {
	override fun applyTo(win: Stage) {
	  win.x = x
	  win.y = y
	  win.height = height
	  win.width = width
	}
  }

  class ManualOrOwner(
	val x: Double? = null,
	val y: Double? = null,
	val width: Double? = null,
	val height: Double? = null
  ): WinGeom() {
	override fun applyTo(win: Stage) {
	  require(win.owner != null) { "use initOwner first" }
	  win.x = x ?: win.owner.x
	  win.y = y ?: win.owner.y
	  win.height = height ?: win.owner.height
	  win.width = width ?: win.owner.width
	}
  }

  class Centered(
	val width: Double = 400.0,
	val height: Double = 400.0
  ): WinGeom() {
	override fun applyTo(win: Stage) {
	  require(win.owner != null) { "use initOwner first" }
	  win.width = width
	  win.height = height
	  win.bindXYToOwnerCenter()
	}
  }

  object CenteredMinWrapContent: WinGeom() {
	override fun applyTo(win: Stage) {
	  require(win.owner != null) { "use initOwner first" }

	  win.bindXYToOwnerCenter()
	}
  }

  class MatchOwner: WinGeom() {
	override fun applyTo(win: Stage) {
	  require(win.owner != null) { "use initOwner first" }
	  win.bindXYToOwnerCenter()
	  win.bindHWToOwner()
	}
  }

  abstract fun applyTo(win: Stage)
}

sealed class WinOwn {
  object None: WinOwn() {
	override fun applyTo(win: Stage) {
	  /*do nothing*/
	}
  }

  class Owner(val owner: Window): WinOwn() {
	override fun applyTo(win: Stage) {
	  win.initOwner(owner)
	}
  }

  object Auto: WinOwn() {
	override fun applyTo(win: Stage) {
	  win.initOwner(Window.getWindows().firstOrNull())
	}
  }

  abstract fun applyTo(win: Stage)
}

fun Parent.openInNewWindow(
  wait: Boolean = false,
  Wclosable: WMode = NOTHING,
  EscClosable: Boolean = false,
  EnterClosable: Boolean = false,
  own: WinOwn = WinOwn.Auto,
  geom: WinGeom = WinGeom.Centered(),
  mScene: Boolean = true,
  beforeShowing: Stage.()->Unit = {},
  border: Boolean = true
): MStage {
  return MStage(wMode = Wclosable, EscClosable = EscClosable, EnterClosable = EnterClosable).apply {
	scene = if (mScene) MScene(this@openInNewWindow) else Scene(this@openInNewWindow)
	own.applyTo(this)
	geom.applyTo(this)
	if (border) {
	  (this@openInNewWindow as Region).borderFill = Color.DARKBLUE
	}
	beforeShowing()
	if (wait) {
	  showAndWait()
	} else {
	  show()
	}
  }
}

fun File.openImageInWindow() {
  AnchorPane(ImageView(this@openImageInWindow.toURI().toString()).apply {
	isPreserveRatio = true
	runLater {
	  fitHeightProperty().bind(scene.window.heightProperty())
	  fitWidthProperty().bind(scene.window.widthProperty())
	  this.onDoubleClick { (scene.window as Stage).close() }
	}
  }).openInNewWindow()
}

fun ImageView.doubleClickToOpenInWindow() {
  this.onDoubleClick { File(URI(this.image.url)).openImageInWindow() }
}

fun showMTextInputDialog(
  promptAndDefault: String,
  stage: Stage?
): Optional<String> {
  return TextInputDialog(promptAndDefault).apply {
	initOwner(stage)
	this.initStyle(StageStyle.UTILITY)
  }.showAndWait()
}

