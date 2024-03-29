package matt.gui.action

import matt.fx.control.wrapper.control.choice.choicebox
import matt.fx.graphics.hotkey.FXHotkeyDSL
import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.fx.graphics.wrapper.pane.grid.GridPaneWrapper
import matt.fx.graphics.wrapper.pane.hbox.h
import matt.fx.graphics.wrapper.text.text
import matt.gui.action.button.actionButton
import matt.gui.menu.context.mcontextmenu
import matt.hotkey.KeyStroke
import matt.model.code.idea.UserActionIdea
import matt.model.obsmod.proceeding.Proceeding
import matt.obs.bind.binding
import matt.obs.bind.deepBinding
import matt.obs.bindings.bool.ObsB
import matt.obs.bindings.str.ObsS
import matt.obs.prop.ObsVal
import matt.obs.prop.writable.BindableProperty
import matt.obs.prop.writable.VarProp
import matt.obs.prop.writable.toVarProp

fun EventTargetWrapper.contextMenuAction(action: GuiAction) {
    mcontextmenu {
        item(action.buttonLabel.value) {
            textProperty.bind(action.buttonLabel)
            enableWhen { action.allowed }
            setOnAction {
                action.invoke()
            }
        }
    }
}

fun FXHotkeyDSL.action(
    keystroke: KeyStroke,
    action: GuiAction
) {

    keystroke op { if (action.allowed.value) action.invoke() }
}

open class ActionOnProceeding(
    val action: GuiAction,
    val proceeding: Proceeding?
) {
    final override fun toString(): String = action.buttonLabel.value + " [${proceeding?.name}]"
}

interface GuiAction : UserActionIdea {
    val buttonLabel: ObsS
    operator fun invoke()
    val allowed: ObsB
}

class GuiActionImpl(
    buttonLabel: String,
    override val allowed: ObsB,
    val op: () -> Unit
) : GuiAction {
    override val buttonLabel by lazy { VarProp(buttonLabel) }
    override fun invoke() {
        op()
    }
}


fun GridPaneWrapper<*>.actionOnProceedingRow(ap: ActionOnProceeding) = actionOnProceedingRow(ap.toVarProp())


fun GridPaneWrapper<*>.actionOnProceedingRow(ap: ObsVal<out ActionOnProceeding>) {
    row {
        actionButton(ap.binding { it.action })

        text(ap.deepBinding { it.proceeding?.status?.binding { it.name } ?: "".toVarProp() })

        text(ap.deepBinding { it.proceeding?.message ?: "".toVarProp() })
    }
}

inline fun <reified A : ActionOnProceeding> GridPaneWrapper<*>.actionOnProceedingRow(
    ap: BindableProperty<A>,
    choices: List<A>
) {
    row {

        h {
            choicebox(values = choices, property = ap)
            actionButton(ap.binding { it.action })
        }


        text(ap.deepBinding { it.proceeding?.status?.binding { it.name } ?: "".toVarProp() })

        text(ap.deepBinding { it.proceeding?.message ?: "".toVarProp() })
    }
}
