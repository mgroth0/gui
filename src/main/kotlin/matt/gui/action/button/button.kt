package matt.gui.action.button

import matt.fx.control.wrapper.control.button.button
import matt.fx.graphics.fxthread.ts.nonBlockingFXWatcher
import matt.fx.graphics.wrapper.node.NW
import matt.gui.action.GuiAction

fun NW.actionButton(a: GuiAction) = button(a.buttonLabel.nonBlockingFXWatcher()) {
  enableProperty.bind(a.allowed.nonBlockingFXWatcher())
  setOnAction {
	a()
  }
}