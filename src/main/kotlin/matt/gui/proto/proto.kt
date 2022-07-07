package matt.gui.proto

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.event.EventTarget
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
import javafx.scene.shape.Circle
import matt.fx.graphics.layout.minBind
import matt.hurricanefx.Scrolls
import matt.hurricanefx.exactHeightProperty
import matt.hurricanefx.exactWidthProperty
import matt.hurricanefx.eye.prop.div
import matt.hurricanefx.eye.prop.minus
import matt.hurricanefx.eye.prop.objectBinding
import matt.hurricanefx.eye.prop.times
import matt.hurricanefx.tornadofx.fx.opcr
import matt.hurricanefx.tornadofx.tab.staticTab
import matt.hurricanefx.wrapper.RegionWrapper
import matt.hurricanefx.wrapper.wrapped
import matt.klib.lang.applyIt

infix fun TextField.withPrompt(s: String): TextField {
  promptText = s
  return this
}

fun TabPane.vtab(s: String = "", op: VBox.()->Unit = {}): Tab {
  return staticTab(s, VBox()) {
	op()
  }
}


infix fun RegionWrapper.wrappedIn(sp: ScrollPane): ScrollPane {
  this minBind sp.wrapped()
  sp.backgroundProperty().bindBidirectional(backgroundProperty)
  return sp.apply {
	content = this@wrappedIn.node
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
		wrapped().exactWidthProperty().bind(sp.widthProperty().minus(woffset*2))

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
	  initialScale = scale.toDouble()
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
  val initialScale: Double = 1.0
): Region() {
  constructor(hw: Number, scale: Double): this(height = hw.toDouble(), width = hw.toDouble(), initialScale = scale)

  val awesomeScaleProperty = SimpleDoubleProperty(initialScale)
  /*val extraH = (height.toDouble()*initialScale - height.toDouble())/2
  val extraW = (width.toDouble()*initialScale - width.toDouble())/2*/

  val canvas = Canvas(
	width.toDouble(),
	height.toDouble()
  ).apply {
	layoutXProperty().bind((widthProperty()*awesomeScaleProperty - widthProperty())/2)
	layoutYProperty().bind((heightProperty()*awesomeScaleProperty - heightProperty())/2)
	scaleXProperty().bind(awesomeScaleProperty)
	scaleYProperty().bind(awesomeScaleProperty)
	children.add(this)
  }


  init {
	wrapped().exactHeightProperty().bind(awesomeScaleProperty*height)
	wrapped().exactWidthProperty().bind(awesomeScaleProperty*width)
  }

  val gc by lazy { canvas.graphicsContext2D }
  private val pw by lazy { gc.pixelWriter }
  operator fun set(x: Int, y: Int, c: Color) = pw.setColor(x, y, c)
}

fun indicatorCircle(booleanProperty: BooleanProperty) = Circle(8.0).apply {
  fillProperty().bind(booleanProperty.objectBinding {
	if (it == true) Color.LIGHTGREEN else Color.DARKRED
  })
}