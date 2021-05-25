package matt.gui.hotkeys

import javafx.application.Platform.runLater
import javafx.scene.layout.Border
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.stage.Stage
import matt.gui.core.scene.MScene
import matt.gui.hotkey.HotKey
import matt.gui.hotkey.hotkeys
import matt.gui.mag.bottom
import matt.gui.mag.bottomleft
import matt.gui.mag.bottomright
import matt.gui.mag.eighth1
import matt.gui.mag.eighth2
import matt.gui.mag.eighth3
import matt.gui.mag.eighth4
import matt.gui.mag.eighth5
import matt.gui.mag.eighth6
import matt.gui.mag.eighth7
import matt.gui.mag.eighth8
import matt.gui.mag.lastdisplay
import matt.gui.mag.left
import matt.gui.mag.max
import matt.gui.mag.nextdisplay
import matt.gui.mag.resetPosition
import matt.gui.mag.reversed_displays
import matt.gui.mag.right
import matt.gui.mag.top
import matt.gui.mag.topleft
import matt.gui.mag.topright
import matt.gui.style.borderFill
import matt.klib.dmap.withStoringDefault
import matt.klibexport.klibexport.go
import java.lang.Thread.sleep
import java.util.WeakHashMap
import kotlin.concurrent.thread
import kotlin.contracts.ExperimentalContracts


@ExperimentalContracts
fun MScene.addDefaultHotkeys() {
  val scene = this

  hotkeys(quickPassForNormalTyping = true) {


	LEFT.ctrl.opt { window?.left() }
	RIGHT.ctrl.opt { window?.right() }
	UP.ctrl.opt { window?.top() }
	DOWN.ctrl.opt { window?.bottom() }

	LEFT.ctrl.opt.meta { window?.bottomleft() }
	RIGHT.ctrl.opt.meta { window?.topright() }
	UP.ctrl.opt.meta { window?.topleft() }
	DOWN.ctrl.opt.meta { window?.bottomright() }

	LEFT.ctrl.opt.shift {
	  window?.apply {
		if (!reversed_displays) lastdisplay()
		else nextdisplay()
	  }
	}
	RIGHT.ctrl.opt.shift {
	  window?.apply {
		if (!reversed_displays) nextdisplay()
		else lastdisplay()
	  }
	}

	ENTER.ctrl.opt.shift { (window as? Stage?)?.max() }
	ENTER.ctrl.opt.shift.meta { window?.resetPosition() }
	I.ctrl.opt.shift(::iconify)

	DIGIT1.ctrl.opt.shift { window.eighth1() }
	DIGIT2.ctrl.opt.shift { window.eighth2() }
	DIGIT3.ctrl.opt.shift { window.eighth3() }
	DIGIT4.ctrl.opt.shift { window.eighth4() }
	DIGIT5.ctrl.opt.shift { window.eighth5() }
	DIGIT6.ctrl.opt.shift { window.eighth6() }
	DIGIT7.ctrl.opt.shift { window.eighth7() }
	DIGIT8.ctrl.opt.shift { window.eighth8() }

	hotkeys.map { it as HotKey }.forEach {
	  it.wrapOp {
		val reg = (scene.root as? Region)
		val old = regs[reg]
		reg?.borderFill = Color.YELLOW
		it()
		reg?.go {
		  thread {
			sleep(750)
			runLater {
			  it.border = old
			}
		  }
		}
	  }
	}
  }

}


val regs = WeakHashMap<Region, Border>().withStoringDefault { it.border ?: Border.EMPTY }