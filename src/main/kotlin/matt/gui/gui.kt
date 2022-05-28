package matt.gui

import javafx.animation.Interpolator
import javafx.beans.property.DoubleProperty
import javafx.geometry.Pos.TOP_CENTER
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.paint.Color.SKYBLUE
import javafx.stage.Screen
import matt.async.date.sec
import matt.gui.core.scene.MScene
import matt.gui.fxlang.backgroundFill
import matt.gui.proto.ScaledCanvas
import matt.gui.win.stage.MStage
import matt.hurricanefx.drags
import matt.hurricanefx.exactHeight
import matt.hurricanefx.exactWidth
import matt.hurricanefx.eye.lang.DProp
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.fileIcons
import matt.hurricanefx.intColorToFXColor
import matt.hurricanefx.toFXDuration
import matt.hurricanefx.tornadofx.animation.keyframe
import matt.hurricanefx.tornadofx.animation.timeline
import matt.hurricanefx.tornadofx.async.runLater
import matt.hurricanefx.tornadofx.control.text
import java.awt.image.BufferedImage
import java.io.File
import java.lang.Thread.sleep
import java.util.WeakHashMap
import kotlin.concurrent.thread


interface Refreshable {
  fun refresh()
}

fun BufferedImage.toFXCanvas(): ScaledCanvas {

  val canv = ScaledCanvas(width = width, height = height)
  (0 until width).forEach { x ->
	(0 until height).forEach { y ->
	  canv[x, y] = intColorToFXColor(getRGB(x, y))
	}
  }
  return canv
}

fun File.draggableIcon() = fileIcons[this].toFXCanvas().apply {
  drags(this@draggableIcon)
}

const val NOTIFICATION_WIDTH = 200.0
const val NOTIFICATION_HEIGHT = 100.0
const val INTER_NOTIFICATION_SPACE = 20.0
const val Y_MOVE_AMOUNT = NOTIFICATION_HEIGHT + INTER_NOTIFICATION_SPACE
val openNotifications = mutableListOf<MStage>()
val notificationYs = WeakHashMap<MStage, Double>()
val fakeYProps = WeakHashMap<MStage, DoubleProperty>()

fun notification(
  text: String
) {

  runLater {
	val stage = MStage(
	  pullBackWhenOffScreen = false
	).apply {
	  isAlwaysOnTop = true
	}
	stage.scene = MScene(
	  VBox().apply {
		this.alignment = TOP_CENTER
		runLater {
		  backgroundFill = SKYBLUE
		}
		exactHeight = 100.00
		exactWidth = NOTIFICATION_WIDTH
		this.text(text) {
		  runLater {
			fill = Color.YELLOW
		  }
		}
		setOnMousePressed {
		  stage.close()
		}
	  }
	).apply {
	  this.fill = SKYBLUE
	}
	val screen = Screen.getScreens().minByOrNull { it.bounds.minX }!!
	//	stage.x = screen.bounds.minX - 110.0
	//	stage.y = screen.bounds.minY + 50.0
	//	println("screen.bounds.minX=${ screen.bounds.minX}")
	//	println("stage.x1=${stage.x}")

	stage.x = screen.bounds.minX - NOTIFICATION_WIDTH - 10.0
	stage.y = screen.bounds.minY + 50.0 + Y_MOVE_AMOUNT*openNotifications.size
	notificationYs[stage] = stage.y

	val fakeXProp = DProp(stage.x).apply {
	  onChange {
		stage.x = it
	  }
	}
	val fakeYProp = DProp(stage.y).apply {
	  onChange {
		stage.y = it
	  }
	}
	fakeYProps[stage] = fakeYProp

	stage.showingProperty().onChange {
	  if (!it) {
		openNotifications -= stage
		timeline {
		  openNotifications.forEach {
			keyframe(0.2.sec.toFXDuration()) {
			  keyvalue(fakeYProps[it]!!, notificationYs[it]!! - Y_MOVE_AMOUNT, Interpolator.EASE_BOTH)
			}
			notificationYs[it] = notificationYs[it]!! - Y_MOVE_AMOUNT
		  }
		}

	  }
	}
	stage.show()
	openNotifications += stage


	thread {
	  sleep(1000)
	  runLater {
		timeline {
		  keyframe(0.75.sec.toFXDuration()) {
			keyvalue(fakeXProp, screen.bounds.minX + 30.0, Interpolator.EASE_BOTH)
		  }
		}
	  }
	}
  }
}