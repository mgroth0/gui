package matt.gui.proto

import javafx.beans.property.BooleanProperty
import javafx.scene.Node
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
import matt.gui.layout.minBind
import matt.hurricanefx.Scrolls
import matt.hurricanefx.exactWidthProperty
import matt.hurricanefx.eye.prop.minus
import matt.hurricanefx.eye.prop.objectBinding
import matt.hurricanefx.tornadofx.layout.vbox
import matt.hurricanefx.tornadofx.tab.tab
import matt.klibexport.klibexport.applyIt

infix fun TextField.withPrompt(s: String): TextField {
  promptText = s
  return this
}

fun TabPane.vtab(s: String = "", op: VBox.()->Unit = {}): Tab {
  return tab(s) {
	isClosable = false
	vbox {
	  op()
	}
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


fun indicatorCircle(booleanProperty: BooleanProperty) = Circle(8.0).apply {
  fillProperty().bind(booleanProperty.objectBinding {
	if (it == true) Color.LIGHTGREEN else Color.DARKRED
  })
}