package matt.gui.win.winfun

import javafx.stage.Stage
import matt.gui.loop.runLaterReturn
import matt.gui.mag.screen
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.tornadofx.async.runLater
import matt.kjlib.async.daemon
import matt.klib.log.warn

fun Stage.pullBackWhenOffscreen() {
  setOnShowing {
	daemon {
	  while (isShowing) {
		runLaterReturn { /*this runLaterReturn is essential. It fixed a bug where new windows were getting reset.*/
		  if (screen == null) {
			warn("resetting offscreen window")
			x = 0.0
			y = 0.0
			width = 500.0
			height = 500.0
		  }
		}
		Thread.sleep(5000)
	  }
	}
  }
}

fun Stage.noDocking(
  ifCondition: ()->Boolean = { true }
) {
  iconifiedProperty().onChange {
	if (it && ifCondition()) {
	  runLater {
		show()
		isMaximized = true
		toFront()
	  }
	}
  }
}