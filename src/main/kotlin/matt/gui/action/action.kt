package matt.gui.action

import matt.fx.graphics.wrapper.pane.grid.GridPaneWrapper
import matt.fx.graphics.wrapper.text.text
import matt.gui.action.button.actionButton
import matt.lang.go
import matt.model.idea.UserActionIdea
import matt.model.obsmod.run.Proceeding
import matt.obs.bind.binding
import matt.obs.bindings.bool.ObsB
import matt.obs.bindings.str.ObsS
import matt.obs.prop.VarProp

class ActionOnProceeding(
  val action: GuiAction,
  val proceeding: Proceeding?
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


fun GridPaneWrapper<*>.actionOnProceedingRow(ap: ActionOnProceeding) {
  row {
	actionButton(ap.action)
	ap.proceeding?.go { p ->
	  text(p.status.binding { it.name })
	  text(p.message)
	}
  }
}