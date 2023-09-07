package matt.gui.settings

import matt.pref.obs.ObsPrefNode
import matt.ui.prefs.SettingsDomains


object MattGeneralSettingsNode : ObsPrefNode(
    key = SettingsDomains().Settings
) {
    val reversedDisplays by bool(defaultValue = true)
}

object MattGeneralStateNode : ObsPrefNode(
    key = SettingsDomains().State
)

