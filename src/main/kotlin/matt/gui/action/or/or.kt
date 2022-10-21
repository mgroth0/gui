package matt.gui.action.or

import matt.gui.action.GuiAction
import matt.model.debug.DebugLogger
import matt.obs.bind.MyBinding
import matt.obs.bind.deepBinding

class OrAction(action: GuiAction, vararg actions: GuiAction): GuiAction {

  private val allActions = listOf(action, *actions)

  private val currentAction = (MyBinding(*allActions.map { it.allowed }.toTypedArray()) {
	allActions.firstOrNull { it.allowed.value } ?: action
  }).apply {
    debugger = DebugLogger("currentAction")
  }

  override val buttonLabel = currentAction.deepBinding { it.buttonLabel }

  override fun invoke() = currentAction.value()

  override val allowed = (currentAction.deepBinding { it.allowed }).apply {
    debugger = DebugLogger("OrAction.allowed")
  }

}


