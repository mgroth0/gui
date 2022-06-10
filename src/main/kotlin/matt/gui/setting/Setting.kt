package matt.gui.setting

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.CheckMenuItem
import kotlinx.serialization.json.jsonObject
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.bind.toBinding
import matt.json.custom.bool
import matt.json.custom.jsonObj
import matt.json.parseJson
import matt.json.prim.writeJson
import matt.klib.file.MFile

import kotlin.contracts.ExperimentalContracts
import kotlin.reflect.KProperty


@ExperimentalContracts
open class HasSettings(private val jsonFile: MFile) {


  inner class Setting(val name: String, defaultValue: Boolean) {
	val fxProp = SimpleBooleanProperty(
	  loaded?.get(name) ?: defaultValue
	)

	init {
	  fxProp.onChange { save() }
	  registeredSettings += this
	}

	fun set(v: Boolean) = fxProp.set(v)

	fun get() = fxProp.get()

	fun CheckItem() = CheckMenuItem(name).apply {
	  selectedProperty().bindBidirectional(fxProp)
	}

	fun weakBinding() = fxProp.toBinding()

  }

  inner class SettingDelegate(val defaultValue: Boolean) {
	private var setting: Setting? = null
	operator fun provideDelegate(
	  thisRef: HasSettings,
	  prop: KProperty<*>
	): SettingDelegate {
	  setting = Setting(prop.name, defaultValue)
	  return this
	}

	operator fun getValue(
	  thisRef: Any?,
	  property: KProperty<*>
	) = setting!!.get()


	operator fun setValue(
	  thisRef: Any?,
	  property: KProperty<*>,
	  value: Boolean
	) = setting!!.set(value)
  }

  val registeredSettings = mutableListOf<Setting>()

  val loaded: Map<String, Boolean>? by lazy {
	jsonFile
	  .takeIf { it.exists() }
	  ?.parseJson()
	  ?.jsonObject
	  ?.entries
	  ?.associate { it.key to it.value.bool }
  }

  fun save() {
	jsonFile.writeJson(
	  jsonObj(
		registeredSettings
		  .associate { it.name to it.get() }
	  )
	)
  }

  fun checkMenuItems() = registeredSettings
	.map { it.CheckItem() }
}

