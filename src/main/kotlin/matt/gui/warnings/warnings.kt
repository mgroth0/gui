package matt.gui.warnings

import com.sun.javafx.application.PlatformImpl.runLater
import matt.fx.control.wrapper.control.button.ButtonWrapper
import matt.fx.control.wrapper.control.list.listview
import matt.fx.graphics.fxthread.ensureInFXThreadInPlace
import matt.fx.graphics.fxthread.ensureInFXThreadOrRunLater
import matt.fx.graphics.wrapper.pane.vbox.VBoxW
import matt.gui.interact.openInNewWindow
import matt.model.obj.ui.UserInterface
import matt.obs.col.olist.basicMutableObservableListOf
import matt.obs.col.olist.readOnly
import matt.obs.prop.toVarProp

open class UiWithWarnings : UserInterface {

    private val warningsM = basicMutableObservableListOf<Warning>()
    private val warnings = warningsM.readOnly()

    final override fun warn(s: String) {
        ensureInFXThreadOrRunLater {
            warningsM += Warning(s)
        }
    }

    private fun removeWarning(warning: Warning) {
        ensureInFXThreadInPlace {
            warningsM.remove(warning)
        }
    }

    private fun clearWarnings() {
        ensureInFXThreadInPlace {
            warningsM.clear()
        }
    }

    fun initWarningsWindow() {

        VBoxW().apply {

            listview(warnings) {
                simpleCellFactoryFromProps { warning ->
                    warning.message.toVarProp() to ButtonWrapper().apply {
                        setOnAction {
                            removeWarning(warning)
                        }
                    }.toVarProp()
                }
            }

        }.openInNewWindow {

            runLater {
                isAlwaysOnTop = false
                hide()
                fun update() {
                    if (warnings.isEmpty()) {
                        isAlwaysOnTop = false
                        hide()
                    } else {
                        show()
                        isAlwaysOnTop = true
                    }
                }
                runLater {
                    warnings.onChange {
                        update()
                    }
                    update()
                }

            }
        }
    }

}

class Warning(val message: String) {
    override fun toString(): String = message
}
