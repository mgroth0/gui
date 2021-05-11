package matt.gui.core.context

import javafx.beans.property.BooleanProperty
import javafx.event.EventTarget
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.canvas.Canvas
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.ContextMenu
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.layout.Region
import javafx.scene.shape.Shape
import matt.auto.IntelliJNavAction
import matt.kjlib.commons.ROOT_FOLDER
import matt.kjlib.log.NEVER
import matt.kjlib.reflect.jumpToKotlinSourceString
import java.util.WeakHashMap
import kotlin.collections.set
import kotlin.concurrent.thread
import kotlin.contracts.ExperimentalContracts

val contextMenuItems = WeakHashMap<EventTarget, MutableList<MenuItem>>()
val contextMenuItemGens = WeakHashMap<EventTarget, MutableList<MContextMenuBuilder.()->Unit>>()

fun EventTarget.mcontextmenu(op: MContextMenuBuilder.()->Unit) {
  MContextMenuBuilder(this).apply(op)
}

class MContextMenuBuilder(
  val node: EventTarget,
  private val isGen: Boolean = false
) {
  val genList = mutableListOf<MenuItem>()

  init {
	if (!isGen) {
	  if (contextMenuItems[node] == null) {
		contextMenuItems[node] = mutableListOf()
	  }
	}
  }

  infix fun String.does(op: ()->Unit) = actionitem(this, op)
  infix fun String.doesInThread(op: ()->Unit) = actionitem(this) {
	thread {
	  op()
	}
  }

  fun actionitem(s: String, op: ()->Unit) {
	add(MenuItem(s).apply {
	  setOnAction {
		op()
		it.consume()
	  }
	})
  }

  fun item(s: String, op: MenuItem.()->Unit) {
	add(MenuItem(s).apply {
	  op()
	})
  }

  infix fun String.toggles(b: BooleanProperty) = checkitem(this, b)
  fun checkitem(s: String, b: BooleanProperty, op: CheckMenuItem.()->Unit = {}) {
	add(CheckMenuItem(s).apply {
	  selectedProperty().bindBidirectional(b)
	  op()
	})
  }

  fun menu(s: String, op: Menu.()->Unit) {
	add(Menu(s).apply {
	  op()
	})
  }

  fun add(item: MenuItem) {
	if (isGen) {
	  genList.add(item)
	} else {
	  contextMenuItems[node]!!.add(item)
	}
  }


  fun onRequest(op: MContextMenuBuilder.()->Unit) {
	if (isGen) NEVER
	if (contextMenuItemGens[node] == null) {
	  contextMenuItemGens[node] = mutableListOf()
	}
	contextMenuItemGens[node]!!.add(op)
  }


}

private fun getCMItems(node: EventTarget): List<MenuItem>? {
  val normal = contextMenuItems[node]
  val gen = contextMenuItemGens[node]?.flatMap {
	MContextMenuBuilder(node, isGen = true).apply(it).genList
  }
  return ((normal ?: listOf()) + (gen ?: listOf())).takeIf { it.isNotEmpty() }
}

@ExperimentalContracts
fun showMContextMenu(
  target: Node,
  xy: Pair<Double, Double>
) {
  ContextMenu().apply {
	isAutoHide = true; isAutoFix = true
	var node: EventTarget = target
	val added = mutableListOf<String>()
	while (true) {
	  var addedHere = false
	  getCMItems(node)?.let { newitems ->
		if (items.isNotEmpty()) items += SeparatorMenuItem()
		addedHere = true
		newitems.forEach { items += it }
	  }
	  val qname = node::class.qualifiedName

	  qname?.let {
		val pack = node::class.java.`package`.name
		val thisnode = node
		if ("matt" in it && it !in added) {
		  if (items.isNotEmpty() && !addedHere) items += SeparatorMenuItem()
		  items += MenuItem("Reflect: ${thisnode::class.simpleName!!}").apply {
			setOnAction {
			  thread {
				jumpToKotlinSourceString(
				  ROOT_FOLDER,
				  thisnode::class.simpleName!!,
				  packageFilter = pack
				)?.let { fl ->
				  IntelliJNavAction(fl.first.absolutePath, fl.second).start()
				}
			  }
			}
		  }
		  added += it
		}
	  }
	  node = when (node) {
		is Region, is Group -> when ((node as Parent).parent) {
		  null -> node.scene
		  else -> node.parent
		}
		is Shape            -> node.parent
		is Canvas           -> node.parent
		else                -> break
	  }
	}
  }.show(target, xy.first, xy.second)
}

