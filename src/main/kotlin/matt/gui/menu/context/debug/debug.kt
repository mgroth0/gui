package matt.gui.menu.context.debug

import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import matt.fx.control.lang.actionbutton
import matt.fx.control.wrapper.checkbox.checkbox
import matt.fx.control.wrapper.control.button.button
import matt.fx.control.wrapper.control.text.area.textarea
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.parent.parent
import matt.fx.graphics.wrapper.pane.grid.GridPaneWrapper
import matt.fx.graphics.wrapper.pane.vbox.VBoxW
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.fx.graphics.wrapper.text.text
import matt.fx.graphics.wrapper.text.textlike.ColoredText
import matt.gui.actiontext.actionText
import matt.lang.go
import matt.obs.bind.binding
import matt.obs.bindings.str.mybuildobs.obsString
import matt.obs.prop.ObsVal
import matt.prim.str.joinWithCommas
import matt.prim.str.mybuild.string


class SceneDebugger : VBoxW() {

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
) : VBoxW() {
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
        fun staticProp(
            label: String,
            value: Any?
        ) = text(string {
            +"$label: "
            +value
        })

        fun dynamicProp(
            label: String,
            value: ObsVal<*>
        ) = text(obsString {
            appendStatic("$label: ")
            +value
        }) {
            fill = Color.LIGHTBLUE
        }
        dynamicProp("visible", debugNode.visibleProperty)
        dynamicProp("managed", debugNode.managedProperty)
        dynamicProp("style", debugNode.styleProperty)
        dynamicProp("style class", debugNode.styleClass.binding { it.joinWithCommas() })
        dynamicProp("layout x", debugNode.layoutXProperty)
        dynamicProp("layout y", debugNode.layoutYProperty)
        staticProp("grid row", GridPane.getRowIndex(debugNode.node))
        staticProp("grid col", GridPane.getColumnIndex(debugNode.node))
        if (debugNode is ColoredText) {
            dynamicProp("text", debugNode.textProperty)
            dynamicProp("text fill", debugNode.textFillProperty)
        }
        if (debugNode is RegionWrapper<*>) {
            val n = debugNode

            dynamicProp("height", debugNode.heightProperty)
            dynamicProp("min height", debugNode.minHeightProperty)
            dynamicProp("pref height", debugNode.prefHeightProperty)
            dynamicProp("max height", debugNode.maxHeightProperty)

            dynamicProp("width", debugNode.widthProperty)

            dynamicProp("min width", debugNode.minWidthProperty)
            dynamicProp("pref width", debugNode.prefWidthProperty)
            dynamicProp("max width", debugNode.maxWidthProperty)





            checkbox("blue") {
                selectedProperty.onChange {
                    if (it) n.blue() else {
                        n.border = null
                    }
                }
            }
            actionbutton("green background") {
                n.backgroundFill = Color.GREEN
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

        (debugNode as? DebuggableNode)?.debugActions()?.forEach { action ->
            val btn = button(action.name)
            val ta = textarea()
            btn.setOnAction {
                ta.text = action.op()
            }
        }


    }
}

interface DebuggableNode : NodeWrapper {
    fun debugActions(): Iterable<DebugAction>
}

class DebugAction(val name: String, val op: () -> String)