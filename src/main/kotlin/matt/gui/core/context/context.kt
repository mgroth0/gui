package matt.gui.core.context

import javafx.beans.property.BooleanProperty
import javafx.event.EventTarget
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.ContextMenu
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.layout.Region
import javafx.scene.shape.Shape
import matt.auto.IntelliJNavAction
import matt.gui.hotkey.filters
import matt.gui.hotkey.handlers
import matt.kjlib.commons.FLOW_FOLDER
import matt.kjlib.lang.NEVER
import matt.kjlib.reflect.jumpToKotlinSourceString
import matt.kjlib.str.tab
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
	  isMnemonicParsing = false
	  setOnAction {
		op()
		it.consume()
	  }
	})
  }

  fun item(s: String, g: Node? = null, op: MenuItem.()->Unit = {}) {
	add(MenuItem(s, g).apply {
	  isMnemonicParsing = false
	  op()
	})
  }


  infix fun String.toggles(b: BooleanProperty) = checkitem(this, b)
  fun checkitem(s: String, b: BooleanProperty, op: CheckMenuItem.()->Unit = {}) {
	add(CheckMenuItem(s).apply {
	  isMnemonicParsing = false
	  selectedProperty().bindBidirectional(b)
	  op()
	})
  }

  fun menu(s: String, op: Menu.()->Unit) {
	add(Menu(s).apply {
	  isMnemonicParsing = false
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
				  FLOW_FOLDER!!,
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
		is Scene            -> node.window
		else                -> break
	  }
	}
	if (items.isNotEmpty()) items += SeparatorMenuItem()
	items += MenuItem("Print Hotkey Info").apply {
	  setOnAction {
		println("\nHOTKEY INFO\n")
		var hknode: Node? = target
		while (hknode != null) {
		  println(hknode)
		  tab("HANDLERS")
		  handlers[hknode]?.let { h ->
			tab("\tqp=${h.quickPassForNormalTyping}")
			h.hotkeys.forEach {
			  tab("\t${it.getHotkeys().joinToString { it.toString() }}")
			}
		  }
		  tab("FILTERS")
		  filters[hknode]?.let { h ->
			tab("\tqp=${h.quickPassForNormalTyping}")
			h.hotkeys.forEach {
			  tab("\t${it.getHotkeys().joinToString { it.toString() }}")
			}
		  }
		  hknode = hknode.parent
		}
		println("\n")
	  }
	}
  }.show(target, xy.first, xy.second)
}

