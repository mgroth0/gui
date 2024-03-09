package matt.gui.service

import javafx.application.Platform
import javafx.scene.control.Alert.AlertType.CONFIRMATION
import javafx.scene.control.ButtonType
import matt.fx.control.tfx.dialog.alert
import matt.fx.control.tfx.dialog.asyncAlert
import matt.fx.graphics.fxthread.ensureInFXThreadInPlace
import matt.fx.graphics.fxthread.runLaterReturn
import matt.fx.graphics.wrapper.stage.StageWrapper
import matt.gui.interact.popupTextInput
import matt.lang.assertions.require.requireNot
import matt.lang.common.NEVER
import matt.model.data.dir.YesOrNo
import matt.model.data.dir.YesOrNo.NO
import matt.model.data.dir.YesOrNo.YES
import matt.service.action.ActionAbilitiesService

object FXActionAbilitiesService: ActionAbilitiesService {
    override fun confirm(s: String): Boolean =
        ensureInFXThreadInPlace {
            var ok = false
            alert(CONFIRMATION, s) {
                ok = it == ButtonType.OK
            }
            ok
        }

    override fun yesOrNo(s: String): YesOrNo =
        ensureInFXThreadInPlace {
            var ok: YesOrNo? = null
            alert(
                CONFIRMATION,
                s
            ) {
                ok =
                    when (it) {
                        ButtonType.YES -> YES
                        ButtonType.NO  -> NO
                        else           -> NEVER
                    }
            }
            ok!!
        }

    override fun <T> yesOrNoAllowAllCommands(prompts: Map<T, String>): Map<T, YesOrNo> {
        TODO()
    }

    override fun <E: Enum<E>> enum(enumOptions: List<E>, prompt: String): E {
        TODO()
    }

    override fun input(prompt: String) = popupTextInput(prompt) ?: NEVER

    override fun openFile(prompt: String): String? =
        ensureInFXThreadInPlace {
            matt.fx.graphics.dialog.openFile {
                title = prompt
            }?.abspath
        }
}


class AsyncFXActionAbilitiesService(
    private val stage: StageWrapper
): ActionAbilitiesService {
    override fun confirm(s: String): Boolean {
        requireNot(Platform.isFxApplicationThread())
        val response =
            runLaterReturn {
                asyncAlert(
                    CONFIRMATION,
                    "Confirmation",
                    s,
                    closeOnEscape = false,
                    owner = stage
                ) {
                    Platform.runLater {
                        x = stage.x + (stage.width / 2.0) - (width / 2.0)
                        y = stage.y - height
                    }
                }
            }

        return response.join {
            it == ButtonType.OK
        }
    }

    override fun openFile(prompt: String): String? {
        requireNot(Platform.isFxApplicationThread())
        return ensureInFXThreadInPlace {
            FXActionAbilitiesService.openFile(prompt)
        }
    }

    override fun yesOrNo(s: String): YesOrNo {
        requireNot(Platform.isFxApplicationThread())
        val response =
            ensureInFXThreadInPlace {
                asyncAlert(
                    CONFIRMATION,
                    "Yes or No?",
                    s,
                    buttons = arrayOf(ButtonType.NO, ButtonType.YES),
                    closeOnEscape = false,
                    owner = stage
                ) {
                    Platform.runLater {
                        x = stage.x + (stage.width / 2.0) - (width / 2.0)
                        y = stage.y - height
                    }
                }
            }
        return response.join {
            when (it) {
                ButtonType.YES -> YES
                ButtonType.NO  -> NO
                else           -> NEVER
            }
        }
    }

    override fun <T> yesOrNoAllowAllCommands(prompts: Map<T, String>): Map<T, YesOrNo> {
        TODO()
    }

    override fun <E: Enum<E>> enum(enumOptions: List<E>, prompt: String): E {
        TODO()
    }

    override fun input(prompt: String) = popupTextInput(prompt) ?: NEVER
}
