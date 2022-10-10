package matt.gui.action

import matt.model.idea.UserActionIdea
import matt.obs.bindings.bool.ObsB
import matt.obs.bindings.str.ObsS
import matt.obs.prop.VarProp


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
