package matt.gui.win.bindgeom

import javafx.stage.Stage
import matt.hurricanefx.eye.lib.onChange
import matt.kbuild.gson
import matt.kjlib.commons.WINDOW_GEOMETRY_FOLDER
import matt.kjlib.file.get
import matt.klib.math.Geometry
import java.io.File

fun Stage.bindGeometry(key: String) = bindGeometry(WINDOW_GEOMETRY_FOLDER["$key.json"])
fun Stage.bindGeometry(file: File, defaultWidth: Double = 400.0, defaultHeight: Double = 600.0) {
  if (file.exists()) {
	loadGeometry(file)
  } else {
	file.parentFile.mkdirs()
	width = defaultWidth
	height = defaultHeight
  }
  listOf(
	xProperty(), yProperty(), widthProperty(), heightProperty()
  ).forEach { it.onChange { saveGeometryIfValid(file) } }
}

private fun Stage.saveGeometryIfValid(file: File) {
  if (!isShowing || isIconified || isFullScreen || x.isNaN() || y.isNaN() || width.isNaN() || height.isNaN()) {
	return
  }
  val r = gson.toJson(
	Geometry(
	  x,
	  y,
	  width,
	  height
	)
  )
  file.writeText(r)
}

private fun Stage.loadGeometry(file: File) {
  this.apply {
	val stageGeometry = gson.fromJson(file.readText(), Geometry::class.java)!!
	x = stageGeometry.x
	y = stageGeometry.y
	width = stageGeometry.width
	height = stageGeometry.height
  }
}

