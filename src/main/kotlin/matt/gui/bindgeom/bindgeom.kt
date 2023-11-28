package matt.gui.bindgeom

import matt.fx.graphics.wrapper.stage.StageWrapper
import matt.lang.go
import matt.model.data.rect.DoubleBox
import matt.ui.winloc.MattGeneralStateWindowsNode


fun StageWrapper.bindGeometry(
    key: String,
    defaultWidth: Double = 400.0,
    defaultHeight: Double = 600.0
) {

    MattGeneralStateWindowsNode.nodeByWindowKey[key].geometry.value?.go {
        loadGeometry(it)
    } ?: run {
        width = defaultWidth
        height = defaultHeight
    }
    listOf(
        xProperty, yProperty, widthProperty, heightProperty
    ).forEach { it.onChange { saveGeometryIfValid(key) } }
}

private fun StageWrapper.saveGeometryIfValid(key: String) {
    if (!isShowing || isIconified || isFullScreen || x.isNaN() || y.isNaN() || width.isNaN() || height.isNaN()) {
        return
    }
    MattGeneralStateWindowsNode.nodeByWindowKey[key].geometry.value = DoubleBox(
        x,
        y,
        width,
        height
    )
}

private fun StageWrapper.loadGeometry(key: String) = apply {
    val stageGeometry = MattGeneralStateWindowsNode.nodeByWindowKey[key].geometry.value
    stageGeometry?.let {
        loadGeometry(it)
    }
}

private fun StageWrapper.loadGeometry(geom: DoubleBox) = apply {
    x = geom.x
    y = geom.y
    width = geom.width
    height = geom.height
}


