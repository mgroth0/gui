package matt.gui.proto

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED
import javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER
import javafx.scene.image.PixelWriter
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import matt.hurricanefx.Scrolls
import matt.hurricanefx.eye.prop.math.div
import matt.hurricanefx.eye.prop.math.minus
import matt.hurricanefx.eye.prop.math.times
import matt.hurricanefx.eye.prop.objectBinding
import matt.hurricanefx.wrapper.canvas.CanvasWrapper
import matt.hurricanefx.wrapper.control.text.field.TextFieldWrapper
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.hurricanefx.wrapper.node.attach
import matt.hurricanefx.wrapper.pane.PaneWrapperImpl
import matt.hurricanefx.wrapper.pane.scroll.ScrollPaneWrapper
import matt.hurricanefx.wrapper.pane.vbox.VBoxWrapper
import matt.hurricanefx.wrapper.region.RegionWrapper
import matt.hurricanefx.wrapper.shape.circle.CircleWrapper
import matt.hurricanefx.wrapper.target.EventTargetWrapper
import matt.klib.lang.applyIt

infix fun TextFieldWrapper.withPrompt(s: String): TextFieldWrapper {
  promptText = s
  return this
}


infix fun <C: RegionWrapper> C.wrappedIn(sp: ScrollPaneWrapper<C>): ScrollPaneWrapper<C> {
  this minBind sp
  sp.backgroundProperty.bindBidirectional(backgroundProperty)
  return sp.apply {
	content = this@wrappedIn
  }
}

fun <C: NodeWrapper> ScrollPaneNoBars(content: C? = null): ScrollPaneWrapper<C> {
  return (content?.let { ScrollPaneWrapper(it) } ?: ScrollPaneWrapper()).apply {
	vbarPolicy = NEVER
	hbarPolicy = NEVER
  }
}


abstract class ScrollVBox(
  scrollpane: ScrollPaneWrapper<VBoxWrapper> = ScrollPaneWrapper(),
  val vbox: VBoxWrapper = VBoxWrapper()
): PaneWrapperImpl<Pane>(Pane()), Scrolls { //Refreshable
  override val scrollPane = scrollpane

  init {
	children.add(scrollPane.applyIt { sp ->
	  /*If I want to configure, make into constructor params?*/
	  vbarPolicy = AS_NEEDED
	  hbarPolicy = NEVER
	  isFitToWidth = true

	  prefWidthProperty.bind(this@ScrollVBox.widthProperty)
	  prefHeightProperty.bind(this@ScrollVBox.heightProperty)
	  val woffset = 25.0
	  layoutX = woffset
	  layoutY = 0.0

	  content = this@ScrollVBox.vbox.apply {
		/*matt.hurricanefx.tornadofx.vector.matt.hurricanefx.eye.prop.math.minus 10 here is so everything looks nicer*/
		/*also neccesary to prevent buggy javafx bug where fitToWidth doesnt work and it trys to hscroll.*/
		/*needs to be exact or content will flow out of scrollpane (doesnt obey fitToWidth)*/
		exactWidthProperty().bind(sp.widthProperty.minus(woffset*2))

		/*reason: this causes stupid buggy fx vertical scroll bar to properly hide when not needed*/
		minHeightProperty.bind(sp.heightProperty.minus(50.0))
	  }
	}.node)
  }

  //  abstract fun VBox.refreshContent()
  //
  //  final override fun refresh() {
  //	vbox.refreshContent()
  //  }
}


fun EventTargetWrapper.scaledCanvas(
  width: Number,
  height: Number,
  scale: Number = 1.0,
  op: ScaledCanvas.()->Unit = {}
) =
  attach(
	ScaledCanvas(
	  width = width,
	  height = height,
	  initialScale = scale.toDouble()
	), op
  )

fun EventTargetWrapper.scaledCanvas(
  hw: Number,
  scale: Number = 1.0,
  op: ScaledCanvas.()->Unit = {}
) = scaledCanvas(height = hw, width = hw, scale = scale, op = op)

class ScaledCanvas(
  height: Number,
  width: Number,
  val initialScale: Double = 1.0
): PaneWrapperImpl<Pane>(Pane()) {
  constructor(hw: Number, scale: Double): this(height = hw.toDouble(), width = hw.toDouble(), initialScale = scale)

  val awesomeScaleProperty = SimpleDoubleProperty(initialScale)
  /*val extraH = (height.toDouble()*initialScale - height.toDouble())/2
  val extraW = (width.toDouble()*initialScale - width.toDouble())/2*/

  val canvas = CanvasWrapper(
	width.toDouble(),
	height.toDouble()
  ).apply {
	layoutXProperty().bind((widthProperty*this@ScaledCanvas.awesomeScaleProperty - widthProperty)/2)
	layoutYProperty().bind((heightProperty*this@ScaledCanvas.awesomeScaleProperty - heightProperty)/2)
	scaleXProperty().bind(this@ScaledCanvas.awesomeScaleProperty)
	scaleYProperty().bind(this@ScaledCanvas.awesomeScaleProperty)
	this@ScaledCanvas.children.add(this.node)
  }


  init {
	exactHeightProperty().bind(awesomeScaleProperty*height)
	exactWidthProperty().bind(awesomeScaleProperty*width)
  }

  val gc by lazy { canvas.graphicsContext2D }
  private val pw: PixelWriter by lazy { gc.pixelWriter }
  operator fun set(x: Int, y: Int, c: Color) = pw.setColor(x, y, c)
}

fun indicatorCircle(booleanProperty: BooleanProperty) = CircleWrapper(8.0).apply {
  fillProperty().bind(booleanProperty.objectBinding {
	val colo = if (it == true) Color.LIGHTGREEN else Color.DARKRED
	//	val colo = if (it == true) null else Color.DARKRED
	//	println("colo=$colo")
	colo
  })
}