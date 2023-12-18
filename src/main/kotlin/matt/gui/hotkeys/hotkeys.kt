package matt.gui.hotkeys

import javafx.application.Platform.runLater
import javafx.scene.paint.Color
import matt.async.thread.namedThread
import matt.fx.graphics.hotkey.hotkeys
import matt.fx.graphics.mag.bottom
import matt.fx.graphics.mag.bottomLeft
import matt.fx.graphics.mag.bottomRight
import matt.fx.graphics.mag.eighth1
import matt.fx.graphics.mag.eighth2
import matt.fx.graphics.mag.eighth3
import matt.fx.graphics.mag.eighth4
import matt.fx.graphics.mag.eighth5
import matt.fx.graphics.mag.eighth6
import matt.fx.graphics.mag.eighth7
import matt.fx.graphics.mag.eighth8
import matt.fx.graphics.mag.lastDisplay
import matt.fx.graphics.mag.left
import matt.fx.graphics.mag.max
import matt.fx.graphics.mag.nextDisplay
import matt.fx.graphics.mag.resetPosition
import matt.fx.graphics.mag.right
import matt.fx.graphics.mag.top
import matt.fx.graphics.mag.topLeft
import matt.fx.graphics.mag.topRight
import matt.fx.graphics.style.border.FXBorder
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.fx.graphics.wrapper.stage.StageWrapper
import matt.gui.iconify.iconify
import matt.gui.mscene.MScene
import matt.gui.settings.MattGeneralSettingsNode
import matt.lang.go
import matt.lang.scope
import matt.log.NOPLogger
import java.lang.Thread.sleep
import matt.hotkey.Key


fun MScene<*>.addDefaultHotkeys() = NOPLogger.scope {


    val scene = this@addDefaultHotkeys

    /*needed filter to be true here or for some reason LEFT.ctrl.opt.shift wasn't being captured in music app even though it was captured in all other apps (globalhotkeys, brainstorm, kjg)*/
    hotkeys(quickPassForNormalTyping = true, filter = true) {

        LEFT.ctrl.opt {
            println("window op 1")
            window!!.x -= window!!.width
        }
        RIGHT.ctrl.opt {
            println("window op 2")
            window!!.x += window!!.width
        }
        UP.ctrl.opt {
            println("window op 3")
            window!!.y -= window!!.height
        }
        DOWN.ctrl.opt {
            println("window op 4")
            window!!.y += window!!.height
        }

        val smallShift = 10.0

        LEFT.ctrl.shift {
            println("window op 5")
            window!!.x -= smallShift
        }
        RIGHT.ctrl.shift {
            println("window op 6")
            window!!.x += smallShift
        }
        UP.ctrl.shift {
            println("window op 7")
            window!!.y -= smallShift
        }
        DOWN.ctrl.shift {
            println("window op 8")
            window!!.y += smallShift
        }

        LEFT.ctrl.meta.opt {
            println("window op 9")
            window!!.width -= smallShift
        }
        RIGHT.ctrl.meta.opt {
            println("window op 10")
            window!!.width -= smallShift
            window!!.x += smallShift
        }
        UP.ctrl.meta.opt {
            println("window op 11")
            window!!.height -= smallShift
        }
        DOWN.ctrl.meta.opt {
            println("window op 12")
            window!!.height -= smallShift
            window!!.y += smallShift
        }

        LEFT.ctrl.meta {
            println("window op 13")
            window!!.width /= 2
        }
        RIGHT.ctrl.meta {
            println("window op 14")
            window!!.width /= 2
            window!!.x += window!!.width
        }
        UP.ctrl.meta {
            println("window op 15")
            window!!.height /= 2
        }
        DOWN.ctrl.meta {
            println("window op 16")
            window!!.height /= 2
            window!!.y += window!!.height
        }


        LEFT.ctrl.meta.shift.opt {
            println("window op 17")
            window!!.x -= smallShift
            window!!.width += smallShift
        }
        RIGHT.ctrl.meta.shift.opt {
            println("window op 18")
            window!!.width += smallShift
        }
        UP.ctrl.meta.shift.opt {
            println("window op 19")
            window!!.height += smallShift
            window!!.y -= smallShift
        }
        DOWN.ctrl.meta.shift.opt {
            println("window op 20")
            window!!.height += smallShift

        }

        LEFT.ctrl.meta.shift {
            println("window op 21")
            window!!.x -= window!!.width
            window!!.width *= 2
        }
        RIGHT.ctrl.meta.shift {
            println("window op 22")
            window!!.width *= 2
        }
        UP.ctrl.meta.shift {
            println("window op 23")
            window!!.y -= window!!.height
            window!!.height *= 2
        }
        DOWN.ctrl.meta.shift {
            println("window op 24")
            window!!.height *= 2
        }

        A.ctrl.opt {
            println("window op 25")
            window?.left()
        }
        D.ctrl.opt {
            println("window op 26")
            window?.right()
        }
        W.ctrl.opt {
            println("window op 27")
            window?.top()
        }
        Key.S.ctrl.opt { println("window op 28");window?.bottom() }

        Key.Z.ctrl.opt { println("window op 29"); window?.bottomLeft() }
        E.ctrl.opt { println("window op 30");window?.topRight() }
        Q.ctrl.opt { println("window op 31");window?.topLeft() }
        C.ctrl.opt { println("window op 32");window?.bottomRight() }

        Key.LEFT_BRACKET.ctrl.opt {
            println("window op 33");
            window?.apply {
                if (!MattGeneralSettingsNode.reversedDisplays.value!!) lastDisplay()
                else nextDisplay()
            }
        }
        Key.RIGHT_BRACKET.ctrl.opt {
            println("window op 34");
            window?.apply {
                if (!MattGeneralSettingsNode.reversedDisplays.value!!) nextDisplay()
                else lastDisplay()
            }
        }

        Key.F.ctrl.opt {
            println("window op 35");(window as? StageWrapper?)?.isFullScreen = !((window as StageWrapper).isFullScreen)
        }
        Key.TAB.ctrl.opt { println("window op 36");(window as? StageWrapper?)?.max() }
        Key.RETURN.ctrl.opt { println("window op 37");window?.resetPosition() }
        Key.X.ctrl.opt { println("window op 38");iconify(icon) }

        Key.DIGIT_1.ctrl.opt { println("window op 39");window?.eighth1() }
        Key.DIGIT_2.ctrl.opt { println("window op 41"); window?.eighth2() }
        Key.DIGIT_3.ctrl.opt { println("window op 42");window?.eighth3() }
        Key.DIGIT_4.ctrl.opt { println("window op 43");window?.eighth4() }
        Key.DIGIT_5.ctrl.opt { println("window op 44");window?.eighth5() }
        Key.DIGIT_6.ctrl.opt { println("window op 45");window?.eighth6() }
        Key.DIGIT_7.ctrl.opt { println("window op 46"); window?.eighth7() }
        Key.DIGIT_8.ctrl.opt { println("window op 47");window?.eighth8() }


        decorateAllOps {
            val reg = (scene.root as? RegionWrapper)
            reg?.border = FXBorder.solid(Color.YELLOW)
            val r = it()
            (reg as? RegionWrapperImpl<*, *>)?.go {
                namedThread(name = "addDefaultHotkeys Thread") {
                    sleep(750)
                    runLater {
                        it.border = it.defaultBorder
                    }
                }
            }
            r
        }


    }
}

