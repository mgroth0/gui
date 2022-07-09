package matt.gui.fxlang

import javafx.scene.control.ListView
import javafx.scene.control.TableView
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.paint.Paint
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.wrapper.NodeWrapper
import matt.hurricanefx.wrapper.RegionWrapper
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

fun <T> ListView<T>.onSelect(op: (T?) -> Unit) {
    selectionModel.selectedItemProperty().onChange {
        op(it)
    }
}
fun <T> NodeWrapper<ListView<T>>.onSelect(op: (T?) -> Unit) = node.onSelect(op)

fun <T> TreeLikeWrapper<*,T>.onSelect(op: (T?) -> Unit) {
    selectionModel.selectedItemProperty().onChange {
        op(it?.value)
    }
}

fun <T> TableView<T>.onSelect(op: (T?) -> Unit) {
    selectionModel.selectedItemProperty().onChange {
        op(it)
    }
}

@JvmName("onSelectTable") fun <T> NodeWrapper<TableView<T>>.onSelect(op: (T?) -> Unit) = node.onSelect(op)
