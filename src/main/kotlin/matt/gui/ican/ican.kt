package matt.gui.ican

import javafx.scene.image.Image
import javafx.scene.image.ImageView
import matt.hurricanefx.tornadofx.nodes.add
import matt.kbuild.FLOW_FOLDER
import matt.kjlib.file.get

val IconFolder by lazy { FLOW_FOLDER["icon"] }

fun IconImage(file: java.io.File): Image = Image(file.toPath().toUri().toURL().toString())
fun IconImage(file: String) = IconImage(IconFolder[file])
fun Icon(file: java.io.File): ImageView = Icon(IconImage(file))

const val ICON_WIDTH = 20.0
const val ICON_HEIGHT = 20.0

fun Icon(image: Image) = ImageView(image).apply {
  isPreserveRatio = false
  fitWidth = ICON_WIDTH
  fitHeight = ICON_HEIGHT
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

