package matt.gui.resize

import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Region

/**
 * [DragResizer] can be used to add mouse listeners to a [Region]
 * and make it resizable by the user by clicking and dragging the border in the
 * same way as a window.
 *
 *
 * Only height resizing is currently implemented. Usage: <pre>DragResizer.makeResizable(myAnchorPane);</pre>
 *
 * @author atill (modified by matt)
 */
class DragResizer private constructor(private val region: Region) {
  private var y = 0.0
  private var initMinHeight = false
  private var dragging = false
  protected fun mouseReleased(event: MouseEvent?) {
	dragging = false
	region.cursor = Cursor.DEFAULT
  }

  protected fun mouseOver(event: MouseEvent) {
	if (isInDraggableZone(event) || dragging) {
	  region.cursor = Cursor.S_RESIZE
	} else {
	  region.cursor = Cursor.DEFAULT
	}
  }

  protected fun isInDraggableZone(event: MouseEvent): Boolean {
	return event.y > region.height - RESIZE_MARGIN
  }

  protected fun mouseDragged(event: MouseEvent) {
	if (!dragging) {
	  return
	}
	val mousey = event.y
	val newHeight = region.minHeight + (mousey - y)
	region.minHeight = newHeight
	y = mousey
  }

  protected fun mousePressed(event: MouseEvent) {

	// ignore clicks outside of the draggable margin
	if (!isInDraggableZone(event)) {
	  return
	}
	dragging = true

	// make sure that the minimum height is set to the current height once,
	// setting a min height that is smaller than the current height will
	// have no effect
	if (!initMinHeight) {
	  region.minHeight = region.height
	  initMinHeight = true
	}
	y = event.y
  }

  companion object {
	/**
	 * The margin around the control that a user can click in to start resizing
	 * the region.
	 */
	const val RESIZE_MARGIN = 5
	fun makeResizable(region: Region) {
	  val resizer = DragResizer(region)
	  region.onMousePressed = EventHandler { event -> resizer.mousePressed(event) }
	  region.onMouseDragged = EventHandler { event -> resizer.mouseDragged(event) }
	  region.onMouseMoved = EventHandler { event -> resizer.mouseOver(event) }
	  region.onMouseReleased = EventHandler { event -> resizer.mouseReleased(event) }
	}
  }
}