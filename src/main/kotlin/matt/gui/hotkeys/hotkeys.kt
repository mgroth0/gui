package matt.gui.hotkeys

import javafx.application.Platform.runLater
import javafx.scene.paint.Color
import matt.fx.control.iconify.iconify
import matt.fx.graphics.hotkey.HotKey
import matt.fx.graphics.hotkey.hotkeys
import matt.fx.graphics.mag.bottom
import matt.fx.graphics.mag.bottomleft
import matt.fx.graphics.mag.bottomright
import matt.fx.graphics.mag.eighth1
import matt.fx.graphics.mag.eighth2
import matt.fx.graphics.mag.eighth3
import matt.fx.graphics.mag.eighth4
import matt.fx.graphics.mag.eighth5
import matt.fx.graphics.mag.eighth6
import matt.fx.graphics.mag.eighth7
import matt.fx.graphics.mag.eighth8
import matt.fx.graphics.mag.lastdisplay
import matt.fx.graphics.mag.left
import matt.fx.graphics.mag.max
import matt.fx.graphics.mag.nextdisplay
import matt.fx.graphics.mag.resetPosition
import matt.fx.graphics.mag.right
import matt.fx.graphics.mag.top
import matt.fx.graphics.mag.topleft
import matt.fx.graphics.mag.topright
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.fx.graphics.style.border.FXBorder
import matt.fx.graphics.wrapper.stage.StageWrapper
import matt.gui.mscene.MScene
import matt.gui.settings.MattGeneralSettingsNode
import matt.lang.go
import java.lang.Thread.sleep
import kotlin.concurrent.thread



fun MScene<*>.addDefaultHotkeys() {
  val scene = this

  /*needed filter to be true here or for some reason LEFT.ctrl.opt.shift wasn't being captured in music app even though it was captured in all other apps (globalhotkeys, brainstorm, kjg)*/
  hotkeys(quickPassForNormalTyping = true, filter = true) {

	LEFT.ctrl.opt { window!!.x -= window!!.width }
	RIGHT.ctrl.opt { window!!.x += window!!.width }
	UP.ctrl.opt { window!!.y -= window!!.height }
	DOWN.ctrl.opt { window!!.y += window!!.height }

	val smallShift = 10.0

	LEFT.ctrl.shift { window!!.x -= smallShift }
	RIGHT.ctrl.shift { window!!.x += smallShift }
	UP.ctrl.shift { window!!.y -= smallShift }
	DOWN.ctrl.shift { window!!.y += smallShift }

	LEFT.ctrl.meta.opt {
	  window!!.width -= smallShift
	}
	RIGHT.ctrl.meta.opt {
	  window!!.width -= smallShift
	  window!!.x += smallShift
	}
	UP.ctrl.meta.opt {
	  window!!.height -= smallShift
	}
	DOWN.ctrl.meta.opt {
	  window!!.height -= smallShift
	  window!!.y += smallShift
	}

	LEFT.ctrl.meta {
	  window!!.width /= 2
	}
	RIGHT.ctrl.meta {
	  window!!.width /= 2
	  window!!.x += window!!.width
	}
	UP.ctrl.meta {
	  window!!.height /= 2
	}
	DOWN.ctrl.meta {
	  window!!.height /= 2
	  window!!.y += window!!.height
	}


	LEFT.ctrl.meta.shift.opt {
	  window!!.x -= smallShift
	  window!!.width += smallShift
	}
	RIGHT.ctrl.meta.shift.opt {
	  window!!.width += smallShift
	}
	UP.ctrl.meta.shift.opt {
	  window!!.height += smallShift
	  window!!.y -= smallShift
	}
	DOWN.ctrl.meta.shift.opt {
	  window!!.height += smallShift

	}

	LEFT.ctrl.meta.shift {
	  window!!.x -= window!!.width
	  window!!.width *= 2
	}
	RIGHT.ctrl.meta.shift {
	  window!!.width *= 2
	}
	UP.ctrl.meta.shift {
	  window!!.y -= window!!.height
	  window!!.height *= 2
	}
	DOWN.ctrl.meta.shift {
	  window!!.height *= 2
	}

	A.ctrl.opt { window?.left() }
	D.ctrl.opt { window?.right() }
	W.ctrl.opt { window?.top() }
	S.ctrl.opt { window?.bottom() }

	Z.ctrl.opt { window?.bottomleft() }
	E.ctrl.opt { window?.topright() }
	Q.ctrl.opt { window?.topleft() }
	C.ctrl.opt { window?.bottomright() }

	LEFT_BRACKET.ctrl.opt {
	  window?.apply {
		if (!MattGeneralSettingsNode.reversedDisplays) lastdisplay()
		else nextdisplay()
	  }
	}
	RIGHT_BRACKET.ctrl.opt {
	  window?.apply {
		if (!MattGeneralSettingsNode.reversedDisplays) nextdisplay()
		else lastdisplay()
	  }
	}

	F.ctrl.opt { (window as? StageWrapper?)?.isFullScreen = !((window as StageWrapper).isFullScreen) }
	TAB.ctrl.opt { (window as? StageWrapper?)?.max() }
	ENTER.ctrl.opt { window?.resetPosition() }
	X.ctrl.opt { iconify(icon) }

	DIGIT1.ctrl.opt { window?.eighth1() }
	DIGIT2.ctrl.opt { window?.eighth2() }
	DIGIT3.ctrl.opt { window?.eighth3() }
	DIGIT4.ctrl.opt { window?.eighth4() }
	DIGIT5.ctrl.opt { window?.eighth5() }
	DIGIT6.ctrl.opt { window?.eighth6() }
	DIGIT7.ctrl.opt { window?.eighth7() }
	DIGIT8.ctrl.opt { window?.eighth8() }

	hotkeys.map { it as HotKey }.forEach {
	  it.wrapOp {
		val reg = (scene.root as? RegionWrapper)
		reg?.border = FXBorder.solid(Color.YELLOW)
		it()
		(reg as? RegionWrapperImpl<*, *>)?.go {
		  thread {
			sleep(750)
			runLater {
			  it.border = it.defaultBorder
			}
		  }
		}
	  }
	}
  }
}

