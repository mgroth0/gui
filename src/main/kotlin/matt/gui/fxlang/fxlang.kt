package matt.gui.fxlang

import javafx.scene.control.ContextMenu
import javafx.scene.control.ListView
import javafx.scene.control.Menu
import javafx.scene.control.TableView
import javafx.scene.control.TreeTableView
import javafx.scene.control.TreeView
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.Region
import javafx.scene.paint.Paint
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.tornadofx.menu.item
import matt.kjlib.log.err

var Region.backgroundFill: Paint?
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

fun <T> TreeTableView<T>.onSelect(op: (T?) -> Unit) {
    selectionModel.selectedItemProperty().onChange {
        op(it?.value)
    }
}

fun <T> TreeView<T>.onSelect(op: (T?) -> Unit) {
    selectionModel.selectedItemProperty().onChange {
        op(it?.value)
    }
}

fun <T> TableView<T>.onSelect(op: (T?) -> Unit) {
    selectionModel.selectedItemProperty().onChange {
        op(it)
    }
}

fun Menu.actionitem(s: String, op: () -> Unit) {
    item(s) {
        setOnAction {
            op()
        }
    }
}

fun ContextMenu.actionitem(s: String, op: () -> Unit) {
    item(s) {
        setOnAction {
            op()
        }
    }
}