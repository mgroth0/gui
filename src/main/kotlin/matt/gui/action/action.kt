package matt.gui.action

import matt.fx.control.wrapper.control.button.button
import matt.fx.graphics.wrapper.node.NW
import matt.model.idea.UIIdea
import matt.model.idea.UserActionIdea
import matt.model.obsmod.run.Proceeding
import matt.model.obsmod.run.StoppableProceeding
import matt.obs.bind.MyBinding
import matt.obs.bind.deepBinding
import matt.obs.bindings.bool.ObsB
import matt.obs.bindings.bool.and
import matt.obs.bindings.bool.not
import matt.obs.bindings.str.ObsS
import matt.obs.prop.VarProp
import kotlin.reflect.KProperty

fun NW.actionButton(a: GuiAction) {
  button(a.label) {
	enableProperty.bind(a.allowed)
	setOnAction {
	  a()
	}
  }
}


abstract class UI: UIIdea {
  fun action(label: String? = null, op: ()->Unit): PropGuiActionProvider {
	return PropGuiActionProvider(
	  label = label, op = op
	)
  }

  fun startProceeding(proceeding: Proceeding, label: String): PropGuiActionProvider {
	return PropGuiActionProvider(
	  label = label, op = {
		proceeding.startIfNotRunning()
	  }, allowed = proceeding.running.not().and(proceeding.canStart)
	)
  }

  fun stopProceedingAndJoin(proceeding: StoppableProceeding, label: String): PropGuiActionProvider {
	return PropGuiActionProvider(
	  label = label, op = {
		proceeding.stopAndJoin()
	  }, allowed = proceeding.running.and(proceeding.canStop)
	)
  }
}


class PropGuiActionProvider(
  private val label: String?, private val allowed: ObsB = VarProp(true), private val op: ()->Unit
) {

  operator fun provideDelegate(
	thisRef: Any, prop: KProperty<*>
  ): PropGuiActionDelegate {
	return PropGuiActionDelegate(
	  label = label ?: prop.name, op = op, allowed = allowed
	)
  }


}

class PropGuiActionDelegate(
  label: String, allowed: ObsB, op: ()->Unit
) {

  private val theAction by lazy {
	PropGuiAction(label, allowed = allowed, op = op)
  }

  operator fun getValue(thisRef: Any, property: KProperty<*>): PropGuiAction {
	return theAction
  }

}

interface GuiAction: UserActionIdea {
  val label: ObsS
  operator fun invoke()
  val allowed: ObsB
}

class PropGuiAction(
  label: String, allowed: ObsB, override val op: ()->Unit
): GuiActionBase(
  allowed = allowed
) {
  override val label by lazy { VarProp(label) }
}

abstract class GlobalGuiAction: GuiAction {
  override val label: ObsS by lazy { VarProp(this::class.simpleName!!) }
}

abstract class GuiActionBase(
  override val allowed: ObsB
): GuiAction {
  abstract override val label: ObsS
  abstract val op: ()->Unit
  override operator fun invoke() = op()
}

class OrAction(action: GuiAction, vararg actions: GuiAction): GuiAction {

  private val allActions = listOf(action, *actions)

  private val currentAction = MyBinding(*allActions.map { it.allowed }.toTypedArray()) {
	allActions.firstOrNull { it.allowed.value } ?: action
  }


  override val label = currentAction.deepBinding {
	it.label
  }

  override fun invoke() {
	currentAction.value()
  }

  override val allowed = currentAction.deepBinding { it.allowed }

}


