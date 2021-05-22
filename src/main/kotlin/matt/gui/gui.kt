package matt.gui

import matt.gui.proto.ScaledCanvas
import matt.hurricanefx.drags
import matt.hurricanefx.fileIcons
import matt.hurricanefx.intColorToFXColor
import java.awt.image.BufferedImage
import java.io.File


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