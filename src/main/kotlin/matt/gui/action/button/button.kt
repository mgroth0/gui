package matt.gui.action.button

import matt.fx.control.wrapper.control.button.ButtonWrapper
import matt.fx.control.wrapper.control.button.button
import matt.fx.graphics.fxthread.ts.nonBlockingFXWatcher
import matt.fx.graphics.wrapper.node.NW
import matt.gui.action.GuiAction
import matt.obs.bind.deepBinding
import matt.obs.prop.ObsVal
import matt.obs.prop.writable.toVarProp

fun NW.actionButton(a: GuiAction): ButtonWrapper = actionButton(a.toVarProp())

fun NW.actionButton(a: ObsVal<GuiAction>) =
    button(a.deepBinding { it.buttonLabel.nonBlockingFXWatcher() }) {
        enableProperty.bind(a.deepBinding { it.allowed.nonBlockingFXWatcher() })
        setOnAction {
            a.value()
        }
    }
