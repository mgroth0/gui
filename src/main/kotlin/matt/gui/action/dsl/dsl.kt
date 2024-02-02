package matt.gui.action.dsl

import matt.gui.action.GuiActionImpl
import matt.lang.delegation.provider
import matt.lang.delegation.valProp
import matt.model.code.idea.UIIdea
import matt.obs.prop.ValProp

abstract class UI: UIIdea {

    fun action(
        buttonLabel: String? = null, op: ()->Unit
    ) = provider {
        valProp {
            GuiActionImpl(
                buttonLabel = buttonLabel ?: it, op = op, allowed = ValProp(true)
            )
        }
    }

}

fun action(
    buttonLabel: String, op: ()->Unit
) = GuiActionImpl(
    buttonLabel = buttonLabel,
    op = op,
    allowed = ValProp(true)
)
