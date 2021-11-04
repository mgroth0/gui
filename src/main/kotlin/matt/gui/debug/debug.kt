package matt.gui.debug

import javafx.beans.property.BooleanProperty
import javafx.scene.layout.Pane
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import matt.gui.loop.runLater
import matt.hurricanefx.tornadofx.nodes.add
import matt.hurricanefx.tornadofx.nodes.onDoubleClick
import matt.kjlib.log.err
import matt.kjlib.str.taball



fun Pane.debugProp(prop: BooleanProperty) {
  onDoubleClick {
	prop.set(prop.value.not())
  }
}

fun TextFlow.forceRefreshLater() = runLater("forceRefreshLater") {
  add(Text("")) // DEBUG FORCE UPDATE
}

