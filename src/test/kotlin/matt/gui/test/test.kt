package matt.gui.test


import matt.fx.graphics.wrapper.pane.vbox.VBoxW
import matt.gui.action.dsl.action
import matt.gui.app.threadinspectordaemon.ThreadInspectorDaemon
import matt.gui.gui.YesIUseGui
import matt.gui.resize.DragResizer
import matt.gui.settings.MattGeneralSettingsNode
import matt.gui.settings.MattGeneralStateNode
import matt.test.JupiterTestAssertions.assertRunsInOneMinute
import matt.ui.winloc.GeometryNode
import matt.ui.winloc.MattGeneralStateWindowsNode
import kotlin.test.Test

class GuiTests {
    @Test
    fun initObjects() = assertRunsInOneMinute {
        ThreadInspectorDaemon
        MattGeneralSettingsNode
        MattGeneralStateNode
        MattGeneralStateWindowsNode
        YesIUseGui
    }

    @Test
    fun constructObjects() = assertRunsInOneMinute {
        GeometryNode("abc")
        action("abc") {

        }

        DragResizer.makeResizable(VBoxW().apply {

        }.node, {})
    }
}