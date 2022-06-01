package matt.gui.clip

import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DataFormat
import matt.klib.file.MFile


fun String.copyToClipboard() {
  val clipboard = Clipboard.getSystemClipboard()
  val content = ClipboardContent()
  content.putString(this)
  clipboard.setContent(content)
}

fun MFile.copyToClipboard() {
  val clipboard = Clipboard.getSystemClipboard()
  val content = ClipboardContent()
  content.putFiles(listOf(this))
  clipboard.setContent(content)
}


fun clipboardString(): String? =
  Clipboard
	.getSystemClipboard()
	.getContent(DataFormat.PLAIN_TEXT) as? String

