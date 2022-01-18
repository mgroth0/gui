package matt.gui.mag

import javafx.stage.Screen
import javafx.stage.Window
import matt.json.prim.parseJsonObj
import matt.json.prim.set
import matt.json.prim.toGson
import matt.kjlib.commons.VAR_JSON

val Window.screen: Screen?
  get() = Screen.getScreensForRectangle(x, y, 1.0, 1.0).firstOrNull()

data class RectSize(
  val width: Number,
  val height: Number
)


fun Window.hhalf() {
  screen?.let {
	width = it.bounds.width/2
	height = it.bounds.height
  }
}

fun Window.vhalf() {
  screen?.let {
	width = it.bounds.width
	height = it.bounds.height/2
  }
}

fun Window.corner() {
  screen?.let {
	width = it.bounds.width/2
	height = it.bounds.height/2
  }
}

fun Window.eigth() {
  screen?.let {
	width = it.bounds.width/4
	height = it.bounds.height/2
  }
}

fun Window.resetPosition() {
  Screen.getPrimary().let {
	x = it.bounds.minX
	y = it.bounds.minY
	width = it.bounds.width
	height = it.bounds.height
  }
}

fun Window.left() {
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.minY
	hhalf()
  }
}


fun Window.right() {
  screen?.let {
	x = it.bounds.maxX - (it.bounds.width/2)
	y = it.bounds.minY
	hhalf()
  }
}

fun Window.top() {
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.minY
	vhalf()
  }
}

fun Window.bottom() {
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.maxY - (it.bounds.height/2)
	vhalf()
  }
}


fun Window.topleft() {
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.minY
	corner()
  }
}

fun Window.topright() {
  screen?.let {
	x = it.bounds.maxX - (it.bounds.width/2)
	y = it.bounds.minY
	corner()
  }
}

fun Window.bottomleft() {
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.maxY - (it.bounds.height/2)
	corner()
  }
}

fun Window.bottomright() {
  screen?.let {
	x = it.bounds.maxX - (it.bounds.width/2)
	y = it.bounds.maxY - (it.bounds.height/2)
	corner()
  }
}

fun Window.max() {
  // isMaximized isnt working for undecorated
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.minY
	width = it.bounds.width
	height = it.bounds.height
  }
}

fun Window.nextdisplay(reversed: Boolean = false) {
  val myscreen = screen
  val screens = Screen.getScreens().let { if (reversed) it.reversed() else it }
  if (screens.size > 1) {
	var next = screens[0]
	var found = false
	for (s in screens) {
	  if (found) {
		next = s
		break
	  }
	  if (s == myscreen) {
		found = true
	  }
	}
	x = next.bounds.minX
	y = next.bounds.minY
	width = next.bounds.width/2
	height = next.bounds.height/2
  }
}

fun Window.lastdisplay() {
  return nextdisplay(reversed = true)
}


fun Window.eighth1() {
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.minY
	eigth()
  }
}

fun Window.eighth2() {
  screen?.let {
	x = it.bounds.minX + (it.bounds.width/4)
	y = it.bounds.minY
	eigth()
  }
}

fun Window.eighth3() {
  screen?.let {
	x = it.bounds.minX + (it.bounds.width/2)
	y = it.bounds.minY
	eigth()
  }
}

fun Window.eighth4() {
  screen?.let {
	x = it.bounds.maxX - (it.bounds.width/4)
	y = it.bounds.minY
	eigth()
  }
}

fun Window.eighth5() {
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.minY + (it.bounds.height/2)
	eigth()
  }
}

fun Window.eighth6() {
  screen?.let {
	x = it.bounds.minX + (it.bounds.width/4)
	y = it.bounds.minY + (it.bounds.height/2)
	eigth()
  }
}

fun Window.eighth7() {
  screen?.let {
	x = it.bounds.minX + (it.bounds.width/2)
	y = it.bounds.minY + (it.bounds.height/2)
	eigth()
  }
}

fun Window.eighth8() {
  screen?.let {
	x = it.bounds.maxX - (it.bounds.width/4)
	y = it.bounds.minY + (it.bounds.height/2)
	eigth()
  }
}


var reversed_displays
  get() = VAR_JSON.takeIf { it.exists() }?.parseJsonObj()?.get("reversed_displays")?.asBoolean ?: true
  set(b) {
	if (VAR_JSON.exists()) {
	  VAR_JSON.writeText(VAR_JSON.parseJsonObj().apply {
		this["reversed_displays"] = b
	  }.toGson())
	}
  }