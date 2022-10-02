package matt.gui.setview

import javafx.application.Platform.runLater
import javafx.scene.control.ListView
import javafx.scene.control.TableView
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableView
import javafx.scene.control.TreeView
import javafx.scene.text.Text
import matt.fx.control.wrapper.control.table.TableViewWrapper
import matt.fx.control.wrapper.control.treetable.TreeTableViewWrapper
import matt.stream.recurse.recurse
import matt.stream.recurse.recurseToFlat
import matt.stream.recurse.recursionDepth


val String.fxWidth: Double
  get() = Text(this).layoutBounds.width

//  call the method after inserting the data into table
fun <T> TableViewWrapper<T>.autoResizeColumns() {
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

// this one is different! it will apply a special width for the first matt.hurricanefx.tableview.coolColumn (which it assumes is for arrows)
fun <T> TreeTableViewWrapper<T>.autoResizeColumns() {
  columnResizePolicy = TreeTableView.UNCONSTRAINED_RESIZE_POLICY

  columns.forEachIndexed { index, column ->
	if (index == 0) {
	  column.prefWidth =
		root.recursionDepth { it.children }*15.0 // guess. works with depth=2. maybe can be smaller.
	} else {
	  column.setPrefWidth(
		((root.recurseToFlat({ it.children }).map {
		  column.getCellData(it)
		}.map {
		  "$it".fxWidth
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
	  else      -> selectionModel.clearSelection()
	}
  }
}

fun <T> TableView<T>.select(o: T?) {
  runLater {
	when {
	  o != null -> selectionModel.select(o)
	  else      -> selectionModel.clearSelection()
	}
  }
}

fun <T> TreeView<T>.select(o: T?) {
  runLater {
	when {
	  o != null -> {
		selectionModel.select(items().firstOrNull { it == o })
	  }

	  else      -> selectionModel.clearSelection()
	}
  }
}

fun <T> TreeTableView<T>.select(o: T?) {
  runLater {
	when {
	  o != null -> {
		selectionModel.select(items().firstOrNull { it == o })
	  }

	  else      -> selectionModel.clearSelection()
	}
  }
}

fun <T> TreeView<T>.items(): Sequence<TreeItem<T>> = root.recurse { it.children }
fun <T> TreeTableView<T>.items(): Sequence<TreeItem<T>> = root.recurse { it.children }