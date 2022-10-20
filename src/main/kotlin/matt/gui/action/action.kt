package matt.gui.action

import matt.fx.control.menu.context.mcontextmenu
import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.fx.graphics.wrapper.pane.grid.GridPaneWrapper
import matt.fx.graphics.wrapper.text.text
import matt.gui.action.button.actionButton
import matt.model.idea.UserActionIdea
import matt.model.obsmod.run.Proceeding
import matt.obs.bind.binding
import matt.obs.bind.deepBinding
import matt.obs.bindings.bool.ObsB
import matt.obs.bindings.str.ObsS
import matt.obs.prop.ObsVal
import matt.obs.prop.VarProp
import matt.obs.prop.toVarProp

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

class ActionOnProceeding(
  val action: GuiAction, val proceeding: Proceeding?
)

interface GuiAction: UserActionIdea {
  val buttonLabel: ObsS
  operator fun invoke()
  val allowed: ObsB
}

class GuiActionImpl(
  buttonLabel: String, override val allowed: ObsB, val op: ()->Unit
): GuiAction {
  override val buttonLabel by lazy { VarProp(buttonLabel) }
  override fun invoke() {
	op()
  }
}


fun GridPaneWrapper<*>.actionOnProceedingRow(ap: ActionOnProceeding) = actionOnProceedingRow(ap.toVarProp())
fun GridPaneWrapper<*>.actionOnProceedingRow(ap: ObsVal<ActionOnProceeding>) {
  row {
	actionButton(ap.binding { it.action })

	text(ap.deepBinding { it.proceeding?.status?.binding { it.name } ?: "".toVarProp() })

	text(ap.deepBinding { it.proceeding?.message ?: "".toVarProp() })
  }
}