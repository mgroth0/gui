package matt.gui.actiontext

import matt.fx.control.wrapper.label.label
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.onLeftClick
import matt.fx.graphics.wrapper.text.text
import matt.fx.graphics.wrapper.text.textlike.highlightOnHover

fun ET.actionText(text: String, action: ()->Unit) = text(text) {
    highlightOnHover()
    onLeftClick {
        action()
    }
}


fun ET.actionLabel(text: String, action: ()->Unit) = label(text) {
    highlightOnHover()
    onLeftClick {
        action()
    }
}
