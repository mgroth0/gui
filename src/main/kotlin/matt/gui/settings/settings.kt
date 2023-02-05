package matt.gui.settings

import matt.pref.obs.ObsPrefNode

object MattGeneralSettingsNode: ObsPrefNode(
  key = "matt.general.settings"
) {
  val reversedDisplays by bool(defaultValue = true)
}