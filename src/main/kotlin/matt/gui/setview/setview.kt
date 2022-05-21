package matt.gui.setview

import javafx.scene.Node
import javafx.scene.control.ComboBox
import javafx.scene.control.IndexedCell
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeTableCell
import javafx.scene.control.TreeTableColumn
import javafx.scene.control.TreeTableView
import javafx.scene.control.TreeView
import javafx.scene.text.Text
import matt.hurricanefx.tornadofx.async.runLater
import matt.kbuild.recurse
import matt.kjlib.recurse.recurseToFlat
import matt.kjlib.recurse.recursionDepth

interface SimpleCell<T> {
    fun updateItemExists(item: T)
}

@Suppress("UNCHECKED_CAST")
private fun <T> IndexedCell<T>.simpleUpdateLogic(item: T, empty: Boolean) {
    if (empty || item == null) {
        text = null
        graphic = null
    } else {
        (this as SimpleCell<T>).updateItemExists(item)
    }
}


abstract class SimpleListCell<T> : ListCell<T>(), SimpleCell<T> {
    final override fun updateItem(item: T, empty: Boolean) {
        super.updateItem(item, empty)
        simpleUpdateLogic(item, empty)
    }

}

fun <T> ListView<T>.simpleCellFactory(op: (T) -> Pair<String?, Node?>) {
    setCellFactory {
        object : SimpleListCell<T>() {
            override fun updateItemExists(item: T) {
                op(item).let {
                    text = it.first
                    graphic = it.second
                }
            }
        }
    }
}

fun <T> ComboBox<T>.simpleCellFactory(op: (T) -> Pair<String?, Node?>) {
    setCellFactory {
        object : SimpleListCell<T>() {
            override fun updateItemExists(item: T) {
                op(item).let {
                    text = it.first
                    graphic = it.second
                }
            }
        }
    }
}



abstract class SimpleTreeTableCell<S, T> : TreeTableCell<S, T>(), SimpleCell<T> {
    final override fun updateItem(item: T, empty: Boolean) {
        super.updateItem(item, empty)
        simpleUpdateLogic(item, empty)
    }
}

fun <S, T> TreeTableColumn<S, T>.simpleCellFactory(op: (T) -> Pair<String?, Node?>) {
    setCellFactory {
        object : SimpleTreeTableCell<S, T>() {
            override fun updateItemExists(item: T) {
                op(item).let {
                    text = it.first
                    graphic = it.second
                }
            }
        }
    }
}


abstract class SimpleTableCell<S, T> : TableCell<S, T>(), SimpleCell<T> {
    final override fun updateItem(item: T, empty: Boolean) {
        super.updateItem(item, empty)
        simpleUpdateLogic(item, empty)
    }
}

fun <S, T> TableColumn<S, T>.simpleCellFactory(op: (T) -> Pair<String?, Node?>) {
    setCellFactory {
        object : SimpleTableCell<S, T>() {
            override fun updateItemExists(item: T) {
                op(item).let {
                    text = it.first
                    graphic = it.second
                }
            }
        }
    }
}


abstract class SimpleTreeCell<T> : TreeCell<T>(), SimpleCell<T> {
    final override fun updateItem(item: T, empty: Boolean) {
        super.updateItem(item, empty)
        simpleUpdateLogic(item, empty)
    }
}

fun <T> TreeView<T>.simpleCellFactory(op: (T) -> Pair<String?, Node?>) {
    setCellFactory {
        object : SimpleTreeCell<T>() {
            override fun updateItemExists(item: T) {
                op(item).let {
                    text = it.first
                    graphic = it.second
                }
            }
        }
    }
}


val String.fxWidth: Double
    get() = Text(this).layoutBounds.width

//  call the method after inserting the data into table
fun <T> TableView<T>.autoResizeColumns() {
    columnResizePolicy = TableView.UNCONSTRAINED_RESIZE_POLICY
    columns.forEach { column ->
        column.setPrefWidth(
            (((0 until items.size).mapNotNull {
                column.getCellData(it)
            }.map {
                it.toString().fxWidth
            }.toMutableList() + listOf(
                column.text.fxWidth
            )).maxOrNull() ?: 0.0) + 10.0
        )
    }
}

// this one is different! it will apply a special width for the first column (which it assumes is for arrows)
fun <T> TreeTableView<T>.autoResizeColumns() {
    columnResizePolicy = TreeTableView.UNCONSTRAINED_RESIZE_POLICY

    columns.forEachIndexed { index, column ->
        if (index == 0) {
            column.prefWidth =
                root.recursionDepth { it.children } * 15.0 // guess. works with depth=2. maybe can be smaller.
        } else {
            column.setPrefWidth(
                ((root.recurseToFlat({ it.children }).map {
                    column.getCellData(it)
                }.map {
                    it.toString().fxWidth
                }.toMutableList() + listOf(
                    column.text.fxWidth
                )).maxOrNull() ?: 0.0) + 10.0
            )
        }
    }
}


fun <T> ListView<T>.select(o: T?) {
    runLater {
        when {
            o != null -> selectionModel.select(o)
            else -> selectionModel.clearSelection()
        }
    }
}

fun <T> TableView<T>.select(o: T?) {
    runLater {
        when {
            o != null -> selectionModel.select(o)
            else -> selectionModel.clearSelection()
        }
    }
}

fun <T> TreeView<T>.select(o: T?) {
    runLater {
        when {
            o != null -> {
                selectionModel.select(items().firstOrNull { it == o })
            }
            else -> selectionModel.clearSelection()
        }
    }
}

fun <T> TreeTableView<T>.select(o: T?) {
    runLater {
        when {
            o != null -> {
                selectionModel.select(items().firstOrNull { it == o })
            }
            else -> selectionModel.clearSelection()
        }
    }
}

fun <T> TreeView<T>.items() = root.recurse { it.children }
fun <T> TreeTableView<T>.items() = root.recurse { it.children }