package matt.gui.settings

import matt.collect.map.lazyMap
import matt.math.geom.Geometry
import matt.pref.obs.ObsPrefNode

object MattGeneralSettingsNode: ObsPrefNode(
  key = "matt.general.settings"
) {
  val reversedDisplays by bool(defaultValue = true)
}

object MattGeneralStateNode: ObsPrefNode(
  key = "matt.general.state"
)

object MattGeneralStateWindowsNode: ObsPrefNode(
  key = "matt.general.state.window"
) {
  val nodeByWindowKey = lazyMap<String, GeometryNode> {
	GeometryNode(it)
  }
}

class GeometryNode(key: String): ObsPrefNode(key = "matt.general.state.window.$key") {
  val geometry by obj<Geometry>(silent=true)
}