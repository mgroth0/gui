package matt.gui.core.refresh

import javafx.scene.Node
import matt.hurricanefx.eye.lib.onChange
import matt.kjlib.async.MyTimerTask
import matt.kjlib.async.every
import matt.kjlib.date.Duration

fun <T : Node> T.refreshWhileInSceneEvery(
    refresh_rate: Duration,
    op: MyTimerTask.(T) -> Unit
) {
    val thisNode: T = this
    sceneProperty().onChange {
        if (it != null) {
            every(refresh_rate, ownTimer = true) {
                if (thisNode.scene == null) {
                    cancel()
                }
                op(thisNode)
            }
        }
    }
}