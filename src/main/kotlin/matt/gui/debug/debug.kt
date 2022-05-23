package matt.gui.debug

import javafx.beans.property.BooleanProperty
import javafx.scene.layout.Pane
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import matt.hurricanefx.tornadofx.async.runLater
import matt.hurricanefx.tornadofx.nodes.add
import matt.hurricanefx.tornadofx.nodes.onDoubleClick


fun Pane.debugProp(prop: BooleanProperty) {
  onDoubleClick {
	prop.set(prop.value.not())
  }
}

fun TextFlow.forceRefreshLater() = runLater {
  add(Text("")) // DEBUG FORCE UPDATE
}

