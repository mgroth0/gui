package matt.gui.lang

import javafx.beans.property.StringProperty
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ContextMenu
import javafx.scene.control.Control
import javafx.scene.control.MenuItem
import javafx.scene.input.KeyCombination
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.op
import matt.hurricanefx.tornadofx.menu.item
import matt.hurricanefx.tornadofx.nodes.add
import matt.klib.lang.err

fun Node.setOnFocusLost(op: ()->Unit) {
  focusedProperty().onChange {it: Boolean? ->
	if (it == null) err("here it is")
	if (!it) {
	  op()
	}
  }
}

fun Node.setOnFocusGained(op: ()->Unit) {
  focusedProperty().onChange {it: Boolean? ->
	if (it == null) err("here it is")
	if (it) {
	  op()
	}
  }
}

fun ActionButton(text: String, graphic: Node? = null, action: (ActionEvent)->Unit) = Button(text, graphic).apply {
  setOnAction {
	action(it)
	it.consume()
  }
}

// Buttons
fun EventTarget.actionbutton(text: String = "", graphic: Node? = null, action: (ActionEvent)->Unit): Button =
  ActionButton(text, graphic, action).also {
	add(it)
  }
//
//fun EventTarget.actionbutton(text: String = "", graphic: Node? = null, action: ()->Unit): Button =
//  actionbutton(text = text, graphic = graphic) { a: ActionEvent -> action() }
//
//fun EventTarget.actionbutton(text: String = "", action: ()->Unit): Button =
//  actionbutton(text = text, graphic = null) { a: ActionEvent -> action() }


infix fun Button.withAction(newOp: ()->Unit) = this.apply { op = newOp }

fun EventTarget.removecontextmenu() {
  if (this is Control) {
	contextMenu = null
  } else (this as? Node)?.apply {
	setOnContextMenuRequested {
	}
  }
}

// because when in listcells, "item" is taken
@Suppress("unused")
fun ContextMenu.menuitem(
  name: String, keyCombination: KeyCombination? = null, graphic: Node? = null, op: MenuItem.()->Unit = {}
) = item(name, keyCombination, graphic, op)


@Suppress("unused")
fun StringProperty.appendln(s: String) {
  append("\n" + s)
}

fun StringProperty.append(s: String) {
  set(get() + s)
}

@Suppress("unused")
fun StringProperty.append(c: Char) {
  set(get() + c)
}


fun <T> ObservableList<T>.setToSublist(start: Int, Stop: Int) {
  setAll(subList(start, Stop).toList())
}

fun <T> ObservableList<T>.removeAllButLastN(num: Int) {
  val siz = size
  setToSublist(siz - num, siz)
}

fun Node.onDoubleClickConsume(action: ()->Unit) {
  setOnMouseClicked {
	if (it.clickCount == 2) {
	  action()
	  it.consume()
	}
  }
}

@Suppress("unused")
fun Scene.onDoubleClickConsume(action: ()->Unit) {
  setOnMouseClicked {
	if (it.clickCount == 2) {
	  action()
	  it.consume()
	}
  }
}
