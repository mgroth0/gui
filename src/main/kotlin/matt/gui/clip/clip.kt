package matt.gui.clip

import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DataFormat
import java.io.File

fun String.copyToClipboard() {
  val clipboard = Clipboard.getSystemClipboard()
  val content = ClipboardContent()
  content.putString(this)
  clipboard.setContent(content)
}

fun File.copyToClipboard() {
  val clipboard = Clipboard.getSystemClipboard()
  val content = ClipboardContent()
  content.putFiles(listOf(this))
  clipboard.setContent(content)
}


fun clipboardString(): String? =
	Clipboard
		.getSystemClipboard()
		.getContent(DataFormat.PLAIN_TEXT) as? String

