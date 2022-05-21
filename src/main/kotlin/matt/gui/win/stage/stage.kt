package matt.gui.win.stage

import javafx.stage.Stage
import javafx.stage.StageStyle
import matt.gui.core.scene.MScene
import matt.gui.hotkey.hotkeys
import matt.gui.win.stage.WMode.CLOSE
import matt.gui.win.stage.WMode.ICONIFY
import matt.gui.win.stage.WMode.NOTHING
import matt.gui.win.winfun.pullBackWhenOffscreen
import matt.kbuild.Machine
import matt.kbuild.thisMachine

enum class WMode {
    CLOSE,
    NOTHING,
    ICONIFY
}

open class MStage(
    wMode: WMode = NOTHING,
    EscClosable: Boolean = false,
    EnterClosable: Boolean = false,
    decorated: Boolean = false
) : Stage(if (decorated) StageStyle.DECORATED else StageStyle.UNDECORATED) {
    init {
        pullBackWhenOffscreen()
        hotkeys {
            if (thisMachine == Machine.WINDOWS) {
                Q.opt op ::close // on Mac, meta-Q quits program. this an OS feature.
            }
            (if (thisMachine == Machine.WINDOWS) {
                W.opt
            } else W.meta) op when (wMode) {
                CLOSE -> ::close
                NOTHING -> {
                    {}
                }
                ICONIFY -> {
                    {
                        (this@MStage.scene as MScene).iconify()
                    }
                }
            }
            if (EscClosable) ESCAPE op ::close
            if (EnterClosable) ENTER op ::close
        }
    }
}

