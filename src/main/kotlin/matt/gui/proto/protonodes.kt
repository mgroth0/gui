package matt.gui.proto

<<<<<<< HEAD
import javafx.event.EventTarget
=======
import javafx.beans.property.BooleanProperty
>>>>>>> 4167628939cdec826782cefdde778062eb8fd901
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.control.ScrollPane
import javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED
import javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TextField
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
<<<<<<< HEAD
=======
import javafx.scene.shape.Circle
>>>>>>> 4167628939cdec826782cefdde778062eb8fd901
import matt.gui.layout.minBind
import matt.hurricanefx.Scrolls
import matt.hurricanefx.exactHeight
import matt.hurricanefx.exactWidth
import matt.hurricanefx.exactWidthProperty
import matt.hurricanefx.eye.prop.minus
<<<<<<< HEAD
import matt.hurricanefx.tornadofx.fx.opcr
import matt.hurricanefx.tornadofx.tab.staticTab
=======
import matt.hurricanefx.eye.prop.objectBinding
import matt.hurricanefx.tornadofx.layout.vbox
import matt.hurricanefx.tornadofx.tab.tab
>>>>>>> 4167628939cdec826782cefdde778062eb8fd901
import matt.klibexport.klibexport.applyIt

infix fun TextField.withPrompt(s: String): TextField {
  promptText = s
  return this
}

fun TabPane.vtab(s: String = "", op: VBox.()->Unit = {}): Tab {
  return staticTab(s, VBox()) {
	op()
  }
}


infix fun Region.wrappedIn(sp: ScrollPane): ScrollPane {
  this minBind sp
  sp.backgroundProperty().bindBidirectional(backgroundProperty())
  return sp.apply {
	content = this@wrappedIn
  }
}

fun ScrollPaneNoBars(content: Node? = null): ScrollPane {
  return ScrollPane(content).apply {
	vbarPolicy = NEVER
	hbarPolicy = NEVER
  }
}


abstract class ScrollVBox(
  scrollpane: ScrollPane = ScrollPane(),
  val vbox: VBox = VBox()
): Region(), Scrolls { //Refreshable
  override val scrollPane = scrollpane

  init {
	children.add(scrollpane.applyIt { sp ->
	  /*If I want to configure, make into constructor params?*/
	  vbarPolicy = AS_NEEDED
	  hbarPolicy = NEVER
	  isFitToWidth = true

	  prefWidthProperty().bind(this@ScrollVBox.widthProperty())
	  prefHeightProperty().bind(this@ScrollVBox.heightProperty())
	  val woffset = 25.0
	  layoutX = woffset
	  layoutY = 0.0

	  content = vbox.apply {
		/*matt.hurricanefx.tornadofx.vector.minus 10 here is so everything looks nicer*/
		/*also neccesary to prevent buggy javafx bug where fitToWidth doesnt work and it trys to hscroll.*/
		/*needs to be exact or content will flow out of scrollpane (doesnt obey fitToWidth)*/
		exactWidthProperty().bind(sp.widthProperty().minus(woffset*2))

		/*reason: this causes stupid buggy fx vertical scroll bar to properly hide when not needed*/
		minHeightProperty().bind(sp.heightProperty().minus(50.0))
	  }
	})
  }

  //  abstract fun VBox.refreshContent()
  //
  //  final override fun refresh() {
  //	vbox.refreshContent()
  //  }
}


<<<<<<< HEAD

fun EventTarget.scaledCanvas(
  width: Number,
  height: Number,
  scale: Number = 1.0,
  op: ScaledCanvas.()->Unit = {}
) =
	opcr(
	  this, ScaledCanvas(
		width = width,
		height = height,
		scale = scale.toDouble()
	  ), op
	)

fun EventTarget.scaledCanvas(
  hw: Number,
  scale: Number = 1.0,
  op: ScaledCanvas.()->Unit = {}
) = scaledCanvas(height = hw, width = hw, scale = scale, op = op)

class ScaledCanvas(
  height: Number,
  width: Number,
  val scale: Double
): Region() {
  val extraH = (height.toDouble()*scale - height.toDouble())/2
  val extraW = (width.toDouble()*scale - width.toDouble())/2
  val canvas = Canvas(
	width.toDouble(),
	height.toDouble()
  ).apply {
	layoutX = extraW
	layoutY = extraH
	scaleX = scale
	scaleY = scale
	children.add(this)
  }

  init {
	exactHeight = height.toDouble()*scale
	exactWidth = width.toDouble()*scale
  }

  private val pw by lazy { canvas.graphicsContext2D.pixelWriter }
  operator fun set(x: Int, y: Int, c: Color) = pw.setColor(x, y, c)
}
=======
fun indicatorCircle(booleanProperty: BooleanProperty) = Circle(8.0).apply {
  fillProperty().bind(booleanProperty.objectBinding {
	if (it == true) Color.LIGHTGREEN else Color.DARKRED
  })
}
>>>>>>> 4167628939cdec826782cefdde778062eb8fd901
