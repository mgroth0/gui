package matt.gui.core.refresh

import javafx.scene.Node
import matt.async.MyTimerTask
import matt.async.date.Duration
import matt.async.every
import matt.hurricanefx.eye.lib.onChange

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