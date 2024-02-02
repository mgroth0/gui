package matt.gui.mscene

import javafx.event.Event
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.input.ContextMenuEvent
import javafx.scene.paint.Color
import matt.collect.itr.recurse.recurse
import matt.file.commons.ICON_FOLDER
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.core.scene.NEED_REVERSED_DISPLAYS_FEATURE
import matt.fx.graphics.style.DarkModeController
import matt.fx.graphics.style.border.FXBorder
import matt.fx.graphics.style.reloadStyle
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.fx.graphics.wrapper.scene.SceneWrapper
import matt.fx.graphics.wrapper.style.StyleableWrapper
import matt.fx.graphics.wrapper.style.findName
import matt.gui.hotkeys.addDefaultHotkeys
import matt.gui.iconify.iconify
import matt.gui.menu.context.mcontextmenu
import matt.gui.menu.context.showMContextMenu
import matt.gui.settings.MattGeneralSettingsNode
import matt.lang.model.file.FsFile
import kotlin.reflect.KClass

open class MScene<R : ParentWrapper<*>>(
    root: R,
    val icon: FsFile,
    userWidth: Double = -1.0,
    userHeight: Double = -1.0
) : SceneWrapper<R>(root, userWidth, userHeight) {
    constructor(
        root: R,
        icon: String,
        userWidth: Double = -1.0,
        userHeight: Double = -1.0
    ) : this(root, ICON_FOLDER["white/$icon.png"], userWidth = userWidth, userHeight = userHeight)

    constructor(
        root: R,
        userWidth: Double = -1.0,
        userHeight: Double = -1.0
    ) : this(root, "chunk", userWidth = userWidth, userHeight = userHeight)


    private fun handleContextMenuReq(e: Event) {
        if (e is ContextMenuEvent) {
            (e.target as? Node)?.let {
                showMContextMenu(it, e.screenX to e.screenY)
            }
            e.consume()
        }
    }


    init {
        addDefaultHotkeys()
        val dark = DarkModeController.darkModeProp.value
        reloadStyle(dark)
        DarkModeController.darkModeProp.onChangeWithWeakAndOld(this) { scene, old, new ->
            if (new != old) {
                scene.reloadStyle(DarkModeController.darkModeProp.value)
            }
        }
        addStyleContextMenu()
        addDisplaysContextMenu()
        addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED) { e ->
            handleContextMenuReq(e)
        }
    }


}


fun MScene<*>.iconify() = iconify(icon)


private fun SceneWrapper<*>.addStyleContextMenu() {
    mcontextmenu {
        menu("style") {
            actionitem("reload style") {
                this@addStyleContextMenu.reloadStyle(DarkModeController.darkModeProp.value)
            }

            actionitem("print style info samples") {
                val classesPrinted = mutableListOf<KClass<*>>()
                (this@addStyleContextMenu.root.node as Node).recurse {
                    (it as? Parent)?.childrenUnmodifiable ?: listOf()
                }.forEach {
                    if (it::class !in classesPrinted) {
                        println((it.wrapped() as StyleableWrapper).styleInfo())
                        classesPrinted += it::class
                    }
                }
            }

            menu("set border") {
                actionitem("none") {
                    (this@addStyleContextMenu.root as RegionWrapper<*>).border = null
                }
                listOf(
                    Color.YELLOW, Color.BLUE, Color.RED, Color.GREEN, Color.ORANGE, Color.PURPLE, Color.WHITE
                ).forEach {
                    actionitem(it.findName()) {
                        (this@addStyleContextMenu.root as RegionWrapper<*>).border = FXBorder.solid(it)
                    }
                }
            }
        }

    }
}

private fun SceneWrapper<*>.addDisplaysContextMenu() {
    mcontextmenu {
        if (NEED_REVERSED_DISPLAYS_FEATURE) actionitem("reverse displays") {
            MattGeneralSettingsNode.reversedDisplays.value = !MattGeneralSettingsNode.reversedDisplays.value!!
        }
    }
}
