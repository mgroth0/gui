package matt.gui.win.stage

import javafx.stage.Stage
import javafx.stage.StageStyle
import matt.gui.hotkey.hotkeys
import matt.gui.win.winfun.pullBackWhenOffscreen

open class MStage(
  Wclosable: Boolean = false,
  EscClosable: Boolean = false,
  EnterClosable: Boolean = false
): Stage(StageStyle.UNDECORATED) {
  init {
	pullBackWhenOffscreen()
	hotkeys {
	  if (Wclosable) W op ::close
	  if (EscClosable) ESCAPE op ::close
	  if (EnterClosable) ENTER op ::close
	}
  }
}

