package matt.gui.refresh

import matt.async.schedule.MyTimerTask
import matt.async.schedule.every
import matt.fx.graphics.wrapper.node.NodeWrapper
import kotlin.time.Duration

fun <T: NodeWrapper> T.refreshWhileInSceneEvery(
  refreshRate: Duration,
  op: MyTimerTask.(T)->Unit
) {
  val thisNode: T = this
  sceneProperty.onChange {
	if (it != null) {
	  every(refreshRate, ownTimer = true) {
		if (thisNode.node.scene == null) {
		  cancel()
		}
		op(thisNode)
	  }
	}
  }
}


