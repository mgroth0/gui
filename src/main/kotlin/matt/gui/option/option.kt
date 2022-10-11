package matt.gui.option

import matt.lang.delegation.fullProvider
import matt.obs.hold.ObservableHolderImpl
import matt.obs.prop.Var
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


sealed class Setting<T>(val prop: Var<T>, val label: String, val tooltip: String) {
  init {
	prop.observe {
	  println("Setting.prop changed...")
	}
  }
}

class EnumSetting<E: Enum<E>>(val cls: KClass<E>, prop: Var<E>, label: String, tooltip: String):
  Setting<E>(prop, label = label, tooltip = tooltip)

class IntSetting(prop: Var<Int>, label: String, tooltip: String, val min: Int, val max: Int):
  Setting<Int>(prop, label = label, tooltip = tooltip)

class BoolSetting(prop: Var<Boolean>, label: String, tooltip: String):
  Setting<Boolean>(prop, label = label, tooltip = tooltip)


abstract class SettingsData: ObservableHolderImpl() {
  @PublishedApi
  internal val mSettings = mutableListOf<Setting<*>>()
  val settings: List<Setting<*>> = mSettings

  protected inline fun <reified E: Enum<E>> enumSettingProv(
	defaultValue: E,
	label: String,
	tooltip: String
  ) = fullProvider { tr, p ->
	registeredProp(defaultValue).provideDelegate(this, p).also {
	  mSettings += EnumSetting(E::class, it.getValue(tr, p), label = label, tooltip = tooltip)
	}
  }


  protected inner class BoolSettingProv(
	private val defaultValue: Boolean,
	private val label: String,
	private val tooltip: String
  ) {
	operator fun provideDelegate(
	  thisRef: ObservableHolderImpl,
	  prop: KProperty<*>,
	) = registeredProp(defaultValue).provideDelegate(thisRef, prop).also {
	  mSettings += BoolSetting(it.getValue(thisRef, prop), label = label, tooltip = tooltip)
	}
  }

  protected inner class IntSettingProv(
	private val defaultValue: Int,
	private val label: String,
	private val tooltip: String,
	private val min: Int,
	private val max: Int
  ) {
	operator fun provideDelegate(
	  thisRef: ObservableHolderImpl,
	  prop: KProperty<*>,
	) = registeredProp(defaultValue).provideDelegate(thisRef, prop).also {
	  mSettings += IntSetting(it.getValue(thisRef, prop), label = label, tooltip = tooltip, min = min, max = max)
	}
  }
}