package matt.gui.menu.context.debug

import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import matt.fx.control.lang.actionbutton
import matt.fx.control.wrapper.checkbox.checkbox
import matt.fx.control.wrapper.control.button.button
import matt.fx.control.wrapper.control.text.area.textarea
import matt.fx.graphics.wrapper.imageview.ImageViewWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.parent.parent
import matt.fx.graphics.wrapper.pane.grid.GridPaneWrapper
import matt.fx.graphics.wrapper.pane.vbox.VBoxW
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.fx.graphics.wrapper.stage.StageWrapper
import matt.fx.graphics.wrapper.text.text
import matt.fx.graphics.wrapper.text.textlike.ColoredText
import matt.fx.graphics.wrapper.window.HasScene
import matt.fx.graphics.wrapper.window.WindowWrapper
import matt.gui.actiontext.actionText
import matt.lang.common.go
import matt.obs.bind.binding
import matt.obs.bindings.str.mybuildobs.obsString
import matt.obs.prop.ObsVal
import matt.prim.str.joinWithCommas
import matt.prim.str.mybuild.api.string


class SceneDebugger : VBoxW() {

    companion object {
        const val TITLE = "Scene Debugger"
    }

    private var lastNav: FXObjectDebugger? = null


    fun navTo(nw: NodeWrapper) = navTo(NodeDebugger(this, nw))

    private fun navTo(nd: FXObjectDebugger) {
        (lastNav?.debugNode as? RegionWrapper<*>)?.border = null
        lastNav = nd
        (nd.debugNode as? RegionWrapper<*>)?.yellow()
        clear()
        val s = nd.debugNode.scene
        val window = s?.window
        val windowString = "window: $window"
        if (window == null) {
            text(windowString)
        } else {
            actionText(windowString) {
                navTo(WindowDebugger(window))
            }
        }
        text("scene: $s")
        val r = s?.root
        val string = "root: $r"
        if (r == null) {
            text(string)
        } else {
            actionText(string) {
                navTo(r)
            }
        }
        +nd
    }
}

abstract class FXObjectDebugger : VBoxW() {
    abstract val debugNode: HasScene

    protected fun staticProp(
        label: String,
        value: Any?
    ) = text(
        string {
            +"$label: "
            +value
        }
    )

    protected fun dynamicProp(
        label: String,
        value: ObsVal<*>
    ) = text(
        obsString {
            appendStatic("$label: ")
            +value
        }
    ) {
        fill = Color.LIGHTBLUE
    }
}

class WindowDebugger(override val debugNode: WindowWrapper<*>) : FXObjectDebugger() {
    init {
        if (debugNode is StageWrapper) {
            dynamicProp("title", debugNode.titleProperty)
        }
    }
}

class NodeDebugger(
    debugger: SceneDebugger,
    override val debugNode: NodeWrapper
) : FXObjectDebugger() {
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

        dynamicProp("visible", debugNode.visibleProperty)
        dynamicProp("managed", debugNode.managedProperty)
        dynamicProp("style", debugNode.styleProperty)
        dynamicProp(
            "style class",
            debugNode.styleClass.binding {
                it.tempDebugCollectionDelegate().joinWithCommas()
            }
        )
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
                    actionText("\t$it") {
                        debugger.navTo(it)
                    }
                }
            }
        }

        if (debugNode is ImageViewWrapper) {
            dynamicProp("fitWidth", debugNode.fitWidthProperty)
            dynamicProp("fitHeight", debugNode.fitHeightProperty)
            dynamicProp("isPreserveRatio", debugNode.preserveRatioProperty)
            dynamicProp("image", debugNode.imageProperty)
            dynamicProp("image->url", debugNode.imageProperty.binding { it?.url })
            dynamicProp("image->width", debugNode.imageProperty.binding { it?.width })
            dynamicProp("image->height", debugNode.imageProperty.binding { it?.height })
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

class DebugAction(
    val name: String,
    val op: () -> String
)
