package matt.gui.menu.context.debug

import javafx.scene.layout.GridPane
import matt.fx.control.proto.actiontext.actionText
import matt.fx.control.wrapper.checkbox.checkbox
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.parent.parent
import matt.fx.graphics.wrapper.pane.grid.GridPaneWrapper
import matt.fx.graphics.wrapper.pane.vbox.VBoxW
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.fx.graphics.wrapper.text.text
import matt.lang.go


class SceneDebugger(): VBoxW() {

  var lastNav: NodeDebugger? = null

  fun navTo(nw: NodeWrapper) = navTo(NodeDebugger(this, nw))
  fun navTo(nd: NodeDebugger) {
	(lastNav?.debugNode as? RegionWrapper<*>)?.border = null
	lastNav = nd
	(nd.debugNode as? RegionWrapper<*>)?.yellow()
	clear()
	val s = nd.debugNode.scene
	text("scene: $s")
	val r = s?.root
	if (r == null) {
	  text("root: $r")
	} else {
	  actionText("root: $r") {
		navTo(r)
	  }
	}
	+nd
  }

}

class NodeDebugger(
  debugger: SceneDebugger,
  val debugNode: NodeWrapper
): VBoxW() {
  init {

	val p = debugNode.parent
	actionText("parent: $p") {
	  if (p != null) {
		debugger.navTo(p)
	  }
	}
	val sibs = (p as? RegionWrapper<*>)?.children
	val previousSib = sibs?.getOrNull(sibs.indexOf(debugNode) - 1)
	val nextSib = sibs?.getOrNull(sibs.indexOf(debugNode) + 1)
	actionText("previous sibling: $previousSib") {
	  previousSib?.go {
		debugger.navTo(previousSib)
	  }
	}
	actionText("next sibling: $nextSib") {
	  nextSib?.go {
		debugger.navTo(nextSib)
	  }
	}
	actionText("node: $debugNode") {
	  debugger.navTo(debugNode)
	}
	if (debugNode is RegionWrapper<*>) {
	  val n = debugNode
	  text("grid row: ${GridPane.getRowIndex(debugNode.node)}")
	  text("grid col: ${GridPane.getColumnIndex(debugNode.node)}")
	  text("height: ${debugNode.height}")
	  text("min height: ${debugNode.minHeight}")
	  text("pref height: ${debugNode.prefHeight}")
	  text("max height: ${debugNode.maxHeight}")

	  text("width: ${debugNode.width}")
	  text("min width: ${debugNode.minWidth}")
	  text("pref width: ${debugNode.prefWidth}")
	  text("max width: ${debugNode.maxWidth}")

	  text("layoutX: ${debugNode.layoutX}")
	  text("layoutY: ${debugNode.layoutY}")

	  text("visible: ${debugNode.isVisible}")
	  text("managed: ${debugNode.isManaged}")
	  checkbox("blue") {
		selectedProperty.onChange {
		  if (it) n.blue() else {
			n.border = null
		  }
		}
	  }
	  if (debugNode is GridPaneWrapper<*>) {
		checkbox("gridLines", debugNode.gridLinesVisibleProp)
	  }
	  debugNode.children.takeIf { it.isNotEmpty() }?.go { it ->
		text("children:")
		it.forEach {
		  actionText("\t${it}") {
			debugger.navTo(it)
		  }
		}
	  }
	}

  }
}