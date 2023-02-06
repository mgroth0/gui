package matt.gui.actiontext

import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.onLeftClick
import matt.fx.graphics.wrapper.text.text

fun ET.actionText(text: String, action: ()->Unit) = text(text) {
  highlightOnHover()
  onLeftClick {
	action()
  }
}
