package matt.gui.ican

import javafx.scene.image.Image
import javafx.scene.image.ImageView
import matt.hurricanefx.tornadofx.nodes.add
import matt.kjlib.commons.ROOT_FOLDER
import matt.kjlib.file.get

val IconFolder = ROOT_FOLDER["icon"]

fun Icon(file: java.io.File): ImageView = Icon(Image(file.toPath().toUri().toURL().toString()))

fun Icon(image: Image) = ImageView(image).apply {
  isPreserveRatio = true
  fitWidth = 25.0
}

fun Icon(file: String) = Icon(IconFolder[file])

fun javafx.scene.Node.icon(file: java.io.File) {
  add(Icon(file))
}

fun javafx.scene.Node.icon(image: Image) {
  add(Icon(image))
}

fun javafx.scene.Node.icon(file: String) {
  add(Icon(file))
}

