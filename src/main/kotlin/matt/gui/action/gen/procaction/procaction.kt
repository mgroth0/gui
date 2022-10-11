package matt.gui.action.gen.procaction

import matt.gui.action.GuiAction
import matt.gui.action.GuiActionImpl
import matt.gui.action.or.OrAction
import matt.model.obsmod.run.Proceeding
import matt.model.obsmod.run.Proceeding.Status.OFF
import matt.model.obsmod.run.Proceeding.Status.RUNNING
import matt.model.obsmod.run.StoppableProceeding
import matt.obs.bindings.bool.and
import matt.obs.bindings.comp.eq

fun Proceeding.startProceedingAction(): GuiAction {
  return GuiActionImpl(
	buttonLabel = startButtonLabel,
	op = {
	  sendStartSignal()
	},
	allowed = status.eq(OFF) and canStart
  )
}

fun StoppableProceeding.stopProceedingAction(): GuiAction {
  return GuiActionImpl(
	buttonLabel = stopButtonLabel,
	op = {
	  sendStopSignal()
	},
	allowed = status.eq(RUNNING) and canStop
  )
}

fun StoppableProceeding.startOrStopAction() = OrAction(
  startProceedingAction(),
  stopProceedingAction()
)