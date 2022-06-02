package matt.gui.ican

import javafx.scene.image.Image
import javafx.scene.image.ImageView
import matt.hurricanefx.tornadofx.nodes.add
import matt.klib.commons.RootProject.flow
import matt.klib.commons.get
import matt.klib.file.MFile

val IconFolder by lazy { flow.folder!!["icon"] }

fun IconImage(file: MFile): Image = Image(file.toPath().toUri().toURL().toString())
fun IconImage(file: String) = IconImage(IconFolder[file])
fun Icon(file: MFile): ImageView = Icon(IconImage(file))

const val ICON_WIDTH = 20.0
const val ICON_HEIGHT = 20.0

fun Icon(image: Image) = ImageView(image).apply {
  isPreserveRatio = false
  fitWidth = ICON_WIDTH
  fitHeight = ICON_HEIGHT
}

fun Icon(file: String) = Icon(IconFolder[file])


fun javafx.scene.Node.icon(file: MFile) {
  add(Icon(file))
}

fun javafx.scene.Node.icon(image: Image) {
  add(Icon(image))
}

fun javafx.scene.Node.icon(file: String) {
  add(Icon(file))
}

