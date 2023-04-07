package matt.gui.menu.context

import javafx.application.Platform.runLater
import javafx.collections.ListChangeListener.Change
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.shape.Shape
import matt.collect.itr.recurse.chain
import matt.collect.map.dmap.withStoringDefault
import matt.collect.map.lazyMap
import matt.collect.map.sync.synchronized
import matt.fx.control.wrapper.contextmenu.ContextMenuWrapper
import matt.fx.control.wrapper.menu.MenuWrapper
import matt.fx.control.wrapper.menu.checkitem.CheckMenuItemWrapper
import matt.fx.control.wrapper.menu.item.MenuItemWrapper
import matt.fx.control.wrapper.menu.item.SimpleMenuItem
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.control.wrapper.wrapped.wrapper
import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl
import matt.fx.graphics.wrapper.node.parent.parent
import matt.fx.graphics.wrapper.scene.SceneWrapper
import matt.gui.interact.WinOwn
import matt.gui.interact.openInNewWindow
import matt.gui.menu.context.EventHandlerType.Filter
import matt.gui.menu.context.EventHandlerType.Handler
import matt.gui.menu.context.debug.SceneDebugger
import matt.log.tab
import matt.log.warn.warn
import matt.obs.prop.BindableProperty
import matt.obs.prop.Var
import java.lang.Thread.sleep
import java.util.*
import kotlin.concurrent.thread
import kotlin.reflect.KClass


fun EventTargetWrapper.mcontextmenu(op: MContextMenuBuilder.() -> Unit) = MContextMenuBuilder(this.node).apply(op)

class MContextMenuBuilder(
    val node: EventTarget,
    private val isGen: Boolean = false
) {


    val genList = mutableListOf<MenuItemWrapper<*>>()

    infix fun String.does(op: () -> Unit) = actionitem(this, op)
    infix fun String.doesInThread(op: () -> Unit) = actionitem(this) {
        thread {
            op()
        }
    }

    fun actionitem(s: String, op: () -> Unit) = SimpleMenuItem(s).apply {
        isMnemonicParsing = false
        setOnAction {
            op()
            it.consume()
        }
    }.also { add(it) }

    fun item(s: String = "", g: NW? = null, op: MenuItemWrapper<*>.() -> Unit = {}) =
        SimpleMenuItem(s, g?.node).apply {
            isMnemonicParsing = false
            op()
        }.also { add(it) }


    infix fun String.toggles(b: BindableProperty<Boolean>) = checkitem(this, b)

    fun checkitem(s: String, b: Var<Boolean>, op: CheckMenuItemWrapper.() -> Unit = {}) =
        CheckMenuItemWrapper(s).apply {
            isMnemonicParsing = false
            selectedProperty.bindBidirectional(b)
            op()
        }.also { add(it) }

    fun menu(s: String, op: MenuWrapper.() -> Unit) = MenuWrapper(s).apply {
        isMnemonicParsing = false
        op()
    }.also { add(it) }

    fun add(item: MenuItemWrapper<*>) {
        if (isGen) {
            genList.add(item)
        } else {
            contextMenuItems[node]!!.add(item)
        }
    }


    fun onRequest(op: MContextMenuBuilder.() -> Unit) {
        require(!isGen)
        contextMenuItemGens[node]!!.add(op)

    }
    //
    //  fun <T: Any?> onRequest(keyGetter: ()->T, op: MContextMenuBuilder.(T)->Unit) {
    //	require(!isGen)
    //	contextMenuItemsByKey[node]!![key]!!.add(op(keyGetter()))
    //  }


}

private fun getCMItems(node: EventTarget): List<MenuItemWrapper<*>>? {
    val normal = contextMenuItems[node]!!
    val gen = contextMenuItemGens[node]!!.flatMap {
        MContextMenuBuilder(node, isGen = true).apply(it).genList
    }
    //  val keyGen = contextMenuItemsByKey[node]!!
    return (normal + gen).takeIf { it.isNotEmpty() }
}


abstract class RunOnce {
    companion object {
        var ranOnce = mutableSetOf<KClass<out RunOnce>>()
    }


    protected abstract fun run()

    init {
        if (this::class !in ranOnce) {
            run()
            ranOnce += this::class
        }
    }

}


class CmFix : RunOnce() {
    override fun run() {
        /*https://bugs.openjdk.org/browse/JDK-8198497*/
        ContextMenu.getWindows().addListener { change: Change<*> ->
            while (change.next()) {
                change.addedSubList.filterIsInstance<ContextMenu>().forEach { cm ->
                    cm.setOnShown {
                        /* I added the thread and runLater, since this still isn't working */
                        thread {
                            sleep(100)
                            runLater {
                                cm.sizeToScene()
                                /*IT FINALLY WORKS*/
                            }
                        }
                    }
                }
            }
        }
    }
}

val contextMenus = lazyMap<Scene, ContextMenuWrapper> {
    ContextMenuWrapper().apply {
        isAutoHide = true
        isAutoFix = true
    }
}


/**
 * see [here](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ContextMenu.html) for info on how to propertly use a context menu
 * KDoc test: [NodeWrapperImpl]
 * [inline markup](https://kotlinlang.org/docs/kotlin-doc.html#inline-markup)
 *
 * @param target must be a [Node] and not a [NodeWrapperImpl] because event targets dont carry a wrapper reference
 *
 */
fun SceneWrapper<*>.showMContextMenu(
    target: Node, /*cannot be [NodeWrapper] because event targets are not wrappers?*/
    xy: Pair<Double, Double>
) {
    sequenceOf(1, 2, 3)
    CmFix()
    val devMenu = MenuWrapper("dev")
    devMenu.actionitem("test exception") {
        throw Exception("test exception")
    }
    devMenu.actionitem("print nodes") {
        println("NODES:")
        tab("TARGET:\t${target::class.simpleName}")
        var parent: Node? = target.parent
        while (parent != null) {
            tab("PARENT:\t${parent::class.simpleName}")
            parent = parent.parent
        }
    }
    devMenu.actionitem("open FX Debugger") {
        SceneDebugger().apply {
            navTo(target.wrapper as NodeWrapper)
        }.openInNewWindow(
            decorated = true,
            alwaysOnTop = true,
            title = "FX Debugger",
            own = WinOwn.None
        ).apply {
            width = 1000.0
            height = 1500.0
        }
    }
    contextMenus[this.node].apply {
        items.clear()
        var node: EventTarget = target
        while (true) {
            getCMItems(node)?.let {
                if (items.isNotEmpty()) separator()
                items += it.map { it.node }
            }
            try {
                node = when (node) {
                    is Parent -> node.parent ?: node.scene
                    is Shape -> node.parent
                    is Canvas -> node.parent
                    is Scene -> node.window
                    else -> break
                }
            } catch (e: NullPointerException) {
                warn("got null parent in context menu generator again")
                System.err.println("here is the stack trace:")
                e.printStackTrace()
                items.add(MenuItem("Got weird null parent in context menu generator! see log for stack trace"))
                break
            }
        }
        if (items.isNotEmpty()) separator()
        items += target.wrapped().hotkeyInfoMenu().node
        items += devMenu.node
    }.node.show(target, xy.first, xy.second)
}

enum class EventHandlerType {
    Handler, Filter
}


private fun NodeWrapper.hotkeyInfoMenu() = MenuWrapper("Click For Hotkey Info").apply {
    val node = this@hotkeyInfoMenu
    fun addInfo(type: EventHandlerType) {
        menu(
            when (type) {
                Handler -> "handlers"; Filter -> "filters"
            }
        ) {


            (node.chain { it.parent } + node.scene + node.stage).forEach { subNode ->
                menu(subNode.toString()) {
                    val h = when (type) {
                        Handler -> subNode?.hotKeyHandler
                        Filter -> subNode?.hotKeyFilter
                    }
                    item("\tqp=${h?.quickPassForNormalTyping}")
                    subNode?.hotKeyHandler?.hotkeys?.forEach { hkc ->
                        item("\t${hkc.getHotkeys().joinToString { it.toString() }}")
                    }
                }
            }
        }
    }


    setOnAction {
        items.clear()
        addInfo(Handler)
        addInfo(Filter)


    }
    //  setOnMouseClicked {
    //
    //  }

}


private val contextMenuItems =
    WeakHashMap<EventTarget, MutableList<MenuItemWrapper<*>>>().withStoringDefault { mutableListOf() }
        .synchronized()

private val contextMenuItemGens =
    WeakHashMap<EventTarget, MutableList<MContextMenuBuilder.() -> Unit>>().withStoringDefault { mutableListOf() }
        .synchronized()
