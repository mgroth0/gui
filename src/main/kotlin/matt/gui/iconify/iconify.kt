package matt.gui.iconify

import matt.fx.graphics.icon.ICON_SIZE
import matt.fx.graphics.icon.Icon
import matt.fx.graphics.style.DarkModeController
import matt.fx.graphics.style.reloadStyle
import matt.fx.graphics.win.winfun.noDocking
import matt.fx.graphics.wrapper.node.setOnDoubleClick
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.fx.graphics.wrapper.scene.SceneWrapper
import matt.fx.graphics.wrapper.stage.StageWrapper
import matt.gui.interact.WinGeom
import matt.gui.interact.WinOwn
import matt.gui.interact.openInNewWindow
import matt.lang.model.file.FsFile

fun SceneWrapper<*>.iconify(icon: FsFile) {
    var iconWindow: StageWrapper? = null
    println("making icon with $icon")
    VBoxWrapperImpl(Icon(icon)).apply {
        var xOffset: Double? = null
        var yOffset: Double? = null
        setOnMousePressed { e ->
            iconWindow?.let {
                xOffset = it.x - e.screenX
                yOffset = it.y - e.screenY
            }
        }
        setOnMouseDragged {
            iconWindow?.x = it.screenX + (xOffset ?: 0.0)
            iconWindow?.y = it.screenY + (yOffset ?: 0.0)
        }
        setOnDoubleClick {
            (this@iconify.window as StageWrapper).show()
            (scene!!.window as StageWrapper).close()
        }
    }.openInNewWindow(own = WinOwn.None, geom = WinGeom.ManualOr0(
        size = ICON_SIZE,
        x = this@iconify.window!!.x + (this@iconify.window!!.width / 2) - (ICON_SIZE.width / 2),
        y = this@iconify.window!!.y + (this@iconify.window!!.height / 2) - (ICON_SIZE.height / 2),
    ), mScene = false, border = false, beforeShowing = {
        scene!!.reloadStyle(DarkModeController.darkModeProp.value)
        DarkModeController.darkModeProp.onChangeWithWeak(this) { _, _ ->
            scene!!.reloadStyle(DarkModeController.darkModeProp.value)
        }
    }).apply {
        iconWindow = this
        isAlwaysOnTop = true
        noDocking()
    }
    window!!.hide()
}

