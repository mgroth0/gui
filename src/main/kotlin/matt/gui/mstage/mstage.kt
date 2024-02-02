package matt.gui.mstage

import javafx.stage.StageStyle
import matt.file.thismachine.thisMachine
import matt.fx.graphics.hotkey.hotkeys
import matt.fx.graphics.wrapper.stage.StageWrapper
import matt.gui.interact.WindowConfig
import matt.gui.mscene.MScene
import matt.gui.mscene.iconify
import matt.gui.mstage.WMode.CLOSE
import matt.gui.mstage.WMode.HIDE
import matt.gui.mstage.WMode.ICONIFY
import matt.gui.mstage.WMode.NOTHING
import matt.model.code.sys.WindowsLaptop

enum class WMode {
    CLOSE,
    HIDE,
    NOTHING,
    ICONIFY
}

enum class ShowMode {
    SHOW,
    SHOW_AND_WAIT,
    DO_NOT_SHOW,
}

open class MStage(
    var wMode: WMode = WindowConfig.DEFAULT.wMode,
    EscClosable: Boolean = WindowConfig.DEFAULT.EscClosable,
    EnterClosable: Boolean = WindowConfig.DEFAULT.EnterClosable,
    decorated: Boolean = WindowConfig.DEFAULT.decorated,
): StageWrapper(if (decorated) StageStyle.DECORATED else StageStyle.UNDECORATED) {

    var decorated = decorated
        @Synchronized set(value) {
            if (value != field && decorated) StageStyle.DECORATED else StageStyle.UNDECORATED
            field = value
        }


    init {
        hotkeys {
            if (thisMachine == WindowsLaptop) {
                Q.opt op ::close // on Mac, meta-Q quits program. this an OS feature.
            }
            (if (thisMachine == WindowsLaptop) {
                W.opt
            } else W.meta) op when (wMode) {
                CLOSE   -> ::close
                HIDE    -> ::hide
                NOTHING -> {
                    {}
                }

                ICONIFY -> {
                    {
                        (this@MStage.scene as? MScene<*>)?.iconify()
                        Unit
                    }
                }

            }
            if (EnterClosable) RETURN op ::close
        }
        hotkeys(filter = true) {
            if (EscClosable) ESCAPE op ::close
        }
    }


    var EnterClosable = EnterClosable
        @Synchronized set(value) {
            if (value != field) {
                if (value) {
                    hotkeys {
                        RETURN op ::close
                    }
                } else {
                    TODO()
                }
            }
            field = value
        }


    var EscClosable = EscClosable
        @Synchronized set(value) {
            if (value != field) {
                if (value) {
                    hotkeys(filter = true) {
                        ESCAPE op ::close
                    }
                } else {
                    TODO()
                }
            }
            field = value
        }

}

