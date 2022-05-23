@file:Suppress("unused", "UNUSED_PARAMETER", "MemberVisibilityCanBePrivate")

package matt.gui.resize

import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Region

class DragResizer private constructor(
  private val region: Region,
  val op: (Double)->Unit
) {
  private var y = 0.0
  private var initMinHeight = false
  private var dragging = false
  fun mouseReleased(event: MouseEvent?) {
	dragging = false
	region.cursor = Cursor.DEFAULT
  }

  fun mouseOver(event: MouseEvent) {
	if (isInDraggableZone(event) || dragging) {
	  region.cursor = Cursor.S_RESIZE
	} else {
	  region.cursor = Cursor.DEFAULT
	}
  }

  fun isInDraggableZone(event: MouseEvent): Boolean {
	return event.y > region.height - RESIZE_MARGIN
  }

  fun mouseDragged(event: MouseEvent) {
	if (!dragging) {
	  return
	}
	val mouseY = event.y
	@Suppress("UNUSED_VARIABLE") val newHeight = region.minHeight + (mouseY - y)
	op(mouseY - y)
	/*region.minHeight = newHeight*/
	y = mouseY
  }

  fun mousePressed(event: MouseEvent) {
	if (!isInDraggableZone(event)) {
	  return
	}
	dragging = true
	if (!initMinHeight) {
	  region.minHeight = region.height
	  initMinHeight = true
	}
	y = event.y
  }

  companion object {
	const val RESIZE_MARGIN = 5
	fun makeResizable(
	  region: Region,
	  op: (Double)->Unit
	) {
	  val resizer = DragResizer(region, op)
	  region.onMousePressed = EventHandler { event -> resizer.mousePressed(event) }
	  region.onMouseDragged = EventHandler { event -> resizer.mouseDragged(event) }
	  region.onMouseMoved = EventHandler { event -> resizer.mouseOver(event) }
	  region.onMouseReleased = EventHandler { event -> resizer.mouseReleased(event) }
	}
  }
}