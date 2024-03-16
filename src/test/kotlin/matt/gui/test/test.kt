package matt.gui.test


import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.pane.vbox.VBoxW
import matt.gui.action.dsl.action
import matt.gui.app.threadinspectordaemon.ThreadInspectorDaemon
import matt.gui.resize.DragResizer
import matt.gui.settings.MattGeneralSettingsNode
import matt.gui.settings.MattGeneralStateNode
import matt.test.assertions.JupiterTestAssertions.assertRunsInOneMinute
import matt.ui.winloc.GeometryNode
import matt.ui.winloc.MattGeneralStateWindowsNode
import kotlin.test.Test

class GuiTests {
    @Test
    fun initObjects() =
        assertRunsInOneMinute {
            ThreadInspectorDaemon
            MattGeneralSettingsNode
            MattGeneralStateNode
            MattGeneralStateWindowsNode
        }

    @Test
    fun constructObjects() =
        assertRunsInOneMinute {
            GeometryNode("abc")
            action("abc") {
            }

            DragResizer.makeResizable(
                VBoxW(childClass = NodeWrapper::class).apply {
                }.node
            ) {}
        }
}
