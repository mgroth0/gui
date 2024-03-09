package matt.gui.action.gen.procaction

import matt.gui.action.GuiAction
import matt.gui.action.GuiActionImpl
import matt.gui.action.or.OrAction
import matt.model.obsmod.proceeding.Proceeding
import matt.model.obsmod.proceeding.Proceeding.Status.OFF
import matt.model.obsmod.proceeding.Proceeding.Status.RUNNING
import matt.model.obsmod.proceeding.man.thread.ThreadProceeding
import matt.model.obsmod.proceeding.stop.StoppableProceeding
import matt.obs.bindings.bool.and
import matt.obs.bindings.comp.eq

fun Proceeding.startProceedingAction(): GuiAction =
    GuiActionImpl(
        buttonLabel = startButtonLabel,
        op = {
            sendStartSignal()
        },
        allowed = (status.eq(OFF) and canStart)
    )

fun StoppableProceeding.stopProceedingAction(): GuiAction =
    GuiActionImpl(
        buttonLabel = stopButtonLabel,
        op = {
            sendStopSignal()
        },
        allowed = status.eq(RUNNING) and canStop
    )

fun ThreadProceeding.forceStopProceedingAction(): GuiAction =
    GuiActionImpl(
        buttonLabel = "force stop $name",
        op = {
            forceStop()
        },
        allowed = status.eq(RUNNING)
    )

fun StoppableProceeding.startOrStopAction() =
    OrAction(
        startProceedingAction(),
        stopProceedingAction()
    )

fun ThreadProceeding.startOrForceStopAction() =
    OrAction(
        startProceedingAction(),
        forceStopProceedingAction()
    )
