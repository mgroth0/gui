package matt.gui.setting

import javafx.beans.property.BooleanPropertyBase
import kotlinx.serialization.json.jsonObject
import matt.file.MFile
import matt.hurricanefx.eye.lib.onChange
import matt.json.custom.bool
import matt.json.custom.jsonObj
import matt.json.parseJson
import matt.json.prim.writeJson
import matt.klib.lang.NEVER
import kotlin.contracts.ExperimentalContracts
import kotlin.reflect.KProperty


@ExperimentalContracts
open class HasSettings(val jsonFile: MFile) {


  inner class Setting(private val settingName: String, defaultValue: Boolean): BooleanPropertyBase(loaded?.get(settingName) ?: defaultValue) {


	init {
	  /*fxProp.*/onChange { save() }
	  registeredSettings += this
	}

	override fun getBean() = NEVER

	override fun getName() = settingName

	//	fun set(v: Boolean) = fxProp.set(v)
//
//	fun get() = fxProp.get()

//	fun CheckItem() = CheckMenuItem(name).apply {
//	  selectedProperty().bindBidirectional(fxProp)
//	}

//	fun weakBinding() = fxProp.toBinding()

  }

  inner class SettingDelegate(private val defaultValue: Boolean) {
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
	  ),
	  pretty = true
	)
  }
}

