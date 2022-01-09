package matt.gui.mag.apple

import javafx.geometry.Rectangle2D
import javafx.stage.Screen
import javafx.stage.Stage
import matt.auto.applescript
import matt.auto.interactiveOsascript
import matt.gui.mag.left
import matt.hurricanefx.tornadofx.async.runLater
import matt.kjlib.date.tic
import kotlin.concurrent.thread

fun moveFrontmostWindowByApplescript(x: Number, y: Number, width: Number, height: Number) {
  applescript(
	"""
	tell application "System Events"
		set frontmostProcess to first application process where it is frontmost
		tell frontmostProcess
			tell (1st window whose value of attribute "AXMain" is true)
				set windowTitle to value of attribute "AXTitle"
				set position to {${x.toInt()}, ${y.toInt()}}
				set size to {${width.toInt()}, ${height.toInt()}}
			end tell
		end tell
	end tell
  """.trimIndent()
  )
}

fun moveAppWindowByApplescript(app: String, x: Number, y: Number, width: Number, height: Number) {
  applescript(
	"""
	tell application "System Events"
		set frontmostProcess to first application process whose name is "$app"
		tell frontmostProcess
			tell (1st window whose value of attribute "AXMain" is true)
				set windowTitle to value of attribute "AXTitle"
				set position to {${x.toInt()}, ${y.toInt()}}
				set size to {${width.toInt()}, ${height.toInt()}}
			end tell
		end tell
	end tell
  """.trimIndent()
  )
}

fun getFrontmostWindowPositionAndSizeByApplescript(): Rectangle2D {
  var s = applescript(
	"""
	tell application "System Events"
		set frontmostProcess to first application process where it is frontmost
		tell frontmostProcess
			tell (1st window whose value of attribute "AXMain" is true)
				return {position, size}
			end tell
		end tell
	end tell
  """.trimIndent()
  )
  val x = s.substringBefore(",").trim().toInt()
  s = s.substringAfter(",")
  val y = s.substringBefore(",").trim().toInt()
  s = s.substringAfter(",")
  val width = s.substringBefore(",").trim().toInt()
  s = s.substringAfter(",")
  val height = s.trim().toInt().toString()
  return Rectangle2D(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
}

fun getAppWindowPositionAndSizeByApplescript(app: String): Rectangle2D {
  var s = applescript(
	"""
	tell application "System Events"
		set frontmostProcess to first application process whose name is "$app"
		tell frontmostProcess
			tell (1st window whose value of attribute "AXMain" is true)
				return {position, size}
			end tell
		end tell
	end tell
  """.trimIndent()
  )
  val x = s.substringBefore(",").trim().toInt()
  s = s.substringAfter(",")
  val y = s.substringBefore(",").trim().toInt()
  s = s.substringAfter(",")
  val width = s.substringBefore(",").trim().toInt()
  s = s.substringAfter(",")
  val height = s.trim().toInt().toString()
  return Rectangle2D(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
}

fun getNameOfFrontmostProcessFromApplescript(): String {
  return applescript(
	"""
	tell application "System Events"
		set frontmostProcess to first application process where it is frontmost
		return name of frontmostProcess
	end tell
  """.trimIndent()
  )
}

fun sdtInTest() {
  val p = interactiveOsascript(
	"""
on run argv
     set stdin to do shell script "cat 0<&3"
     return "hello, " & stdin
end run
  """.trimIndent()
  )
  val reader = p.inputStream.bufferedReader()
  val writer = p.outputStream.bufferedWriter()
  thread {
	reader.forEachLine {
	  println("READ:$it")
	}
  }
  writer.write("HELLO1")
  writer.write("HELLO2")
  writer.close()
  println("CODE=${p.waitFor()}")
}

fun appleLeft() {
  sdtInTest()
  val t = tic()
  t.toc("toc1")
  t.toc("toc2")
  println("LEFT!")
  val appName = getNameOfFrontmostProcessFromApplescript()
  t.toc("toc3")
  println("appName=${appName}")
  if (appName != "java") {
	val bounds = getFrontmostWindowPositionAndSizeByApplescript()
	t.toc("toc4")
	applescript("1+1")
	t.toc("toc4.5")
	println("bounds=${bounds}")
	runLater {
	  t.toc("toc5")
	  val screen =
		Screen.getScreensForRectangle(bounds.minX, bounds.minY, bounds.width, bounds.height).firstOrNull()!!
	  t.toc("toc6")
	  moveFrontmostWindowByApplescript(
		screen.bounds.minX,
		screen.bounds.minY,
		screen.bounds.width/2,
		screen.bounds.height
	  )
	  t.toc("toc7")
	}
	t.toc("toc8")
  }
  if (false) {
	runLater {
	  val fakeStage = Stage()
	  fakeStage.left()
	}
  }
  /*matt.gui.mag.apple.moveFrontmostWindowByApplescript()*/
  //aaaaaaaa
}