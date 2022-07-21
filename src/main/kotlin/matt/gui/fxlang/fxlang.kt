package matt.gui.fxlang

import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.paint.Paint
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.wrapper.ListViewWrapper
import matt.hurricanefx.wrapper.RegionWrapper
import matt.hurricanefx.wrapper.TableViewWrapper
import matt.hurricanefx.wrapper.TreeLikeWrapper
import matt.klib.lang.err

var RegionWrapper.backgroundFill: Paint?
  set(value) {
	if (value == null) {
	  this.background = null
	} else {
	  background = backgroundColor(value)
	}

  }
  get() {
	err("no getter yet")
  }

fun backgroundColor(c: Paint) = Background(BackgroundFill(c, null, null))

fun <T> ListViewWrapper<T>.onSelect(op: (T?)->Unit) {
  selectionModel.selectedItemProperty().onChange {
	op(it)
  }
}

fun <T> TreeLikeWrapper<*, T>.onSelect(op: (T?)->Unit) {
  selectionModel.selectedItemProperty().onChange {
	op(it?.value)
  }
}

fun <T> TableViewWrapper<T>.onSelect(op: (T?)->Unit) {
  selectionModel.selectedItemProperty().onChange {
	op(it)
  }
}