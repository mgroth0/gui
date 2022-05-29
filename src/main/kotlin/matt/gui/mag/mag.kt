package matt.gui.mag

import javafx.stage.Screen
import javafx.stage.Window
import matt.gui.app.NEW_MAC_NOTCH_ESTIMATE
import matt.json.custom.bool
import matt.json.prim.parseJsonObj
import matt.json.prim.set
import matt.json.prim.writeJson
import matt.klib.commons.VAR_JSON

val Window.screen: Screen?
  get() = Screen.getScreensForRectangle(x, y, 1.0, 1.0).firstOrNull()

data class RectSize(
  val width: Number,
  val height: Number
)

/*val MENU_BAR_Y = if (isNewMac) NEW_MAC_NOTCH_ESTIMATE else 0.0*/


/*no idea if this will work*/
fun Screen.isPrimary() = bounds.minX == 0.0 && bounds.minY == 0.0

fun Window.extraMinY() = if (this.screen?.isPrimary() != false) NEW_MAC_NOTCH_ESTIMATE else 0.0

fun Window.maxsize() {
  screen?.let {
	width = it.bounds.width
	height = it.bounds.height - extraMinY()
  }
}

fun Window.hhalf() {
  println("hhalf")
  println("screen=${screen}")
  screen?.let {
	width = it.bounds.width/2
	height = it.bounds.height - extraMinY()
	println("width = ${width}")
	println("height=${height}")
  }
}

fun Window.vhalf() {
  screen?.let {
	width = it.bounds.width
	height = it.bounds.height/2 - (extraMinY()/2.0)
  }
}

fun Window.corner() {
  screen?.let {
	width = it.bounds.width/2
	height = it.bounds.height/2 - (extraMinY()/2.0)
  }
}

fun Window.eigth() {
  screen?.let {
	width = it.bounds.width/4
	height = it.bounds.height/2 - (extraMinY()/2.0)
  }
}

fun Window.resetPosition() {
  Screen.getPrimary().let {
	x = it.bounds.minX
	y = it.bounds.minY + extraMinY()
	width = it.bounds.width
	height = it.bounds.height - extraMinY()
  }
}

fun Window.myMax() {
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.minY + extraMinY()
	maxsize()
  }
}

fun Window.left() {
  println("left")
  println("screen = $screen")
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.minY + extraMinY()
	println("x=${x}")
	println("y=${y}")
	hhalf()
  }
}


fun Window.right() {
  println("right")
  println("screen = $screen")
  screen?.let {
	x = it.bounds.maxX - (it.bounds.width/2)
	y = it.bounds.minY + extraMinY()
	println("x=${x}")
	println("y=${y}")
	hhalf()
  }
}

fun Window.top() {
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.minY + extraMinY()
	vhalf()
  }
}

fun Window.bottom() {
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.maxY - (it.bounds.height/2) - (extraMinY()/2.0)
	vhalf()
  }
}


fun Window.topleft() {
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.minY + extraMinY()
	corner()
  }
}

fun Window.topright() {
  screen?.let {
	x = it.bounds.maxX - (it.bounds.width/2)
	y = it.bounds.minY + extraMinY()
	corner()
  }
}

fun Window.bottomleft() {
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.maxY - (it.bounds.height/2) - (extraMinY()/2.0)
	corner()
  }
}

fun Window.bottomright() {
  screen?.let {
	x = it.bounds.maxX - (it.bounds.width/2)
	y = it.bounds.maxY - (it.bounds.height/2) - (extraMinY()/2.0)
	corner()
  }
}

fun Window.max() {
  // isMaximized isnt working for undecorated
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.minY + extraMinY()
	width = it.bounds.width
	height = it.bounds.height - extraMinY()
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
	y = next.bounds.minY + extraMinY()
	width = next.bounds.width/2
	height = next.bounds.height/2 - (extraMinY()/2.0)
  }
}

fun Window.lastdisplay() {
  return nextdisplay(reversed = true)
}


fun Window.eighth1() {
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.minY + extraMinY()
	eigth()
  }
}

fun Window.eighth2() {
  screen?.let {
	x = it.bounds.minX + (it.bounds.width/4)
	y = it.bounds.minY + extraMinY()
	eigth()
  }
}

fun Window.eighth3() {
  screen?.let {
	x = it.bounds.minX + (it.bounds.width/2)
	y = it.bounds.minY + extraMinY()
	eigth()
  }
}

fun Window.eighth4() {
  screen?.let {
	x = it.bounds.maxX - (it.bounds.width/4)
	y = it.bounds.minY + extraMinY()
	eigth()
  }
}

fun Window.eighth5() {
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.minY + (it.bounds.height/2) - (extraMinY()/2.0)
	eigth()
  }
}

fun Window.eighth6() {
  screen?.let {
	x = it.bounds.minX + (it.bounds.width/4)
	y = it.bounds.minY + (it.bounds.height/2) - (extraMinY()/2.0)
	eigth()
  }
}

fun Window.eighth7() {
  screen?.let {
	x = it.bounds.minX + (it.bounds.width/2)
	y = it.bounds.minY + (it.bounds.height/2) - (extraMinY()/2.0)
	eigth()
  }
}

fun Window.eighth8() {
  screen?.let {
	x = it.bounds.maxX - (it.bounds.width/4)
	y = it.bounds.minY + (it.bounds.height/2) - (extraMinY()/2.0)
	eigth()
  }
}


var reversed_displays
  get() = VAR_JSON.takeIf { it.exists() }?.parseJsonObj()?.get("reversed_displays")?.bool ?: true
  set(b) {
	if (VAR_JSON.exists()) {

	  VAR_JSON.writeJson(


		VAR_JSON.parseJsonObj().apply {
		  this["reversed_displays"] = b
		}

	  )
	}
  }