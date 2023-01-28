package matt.gui.option

import matt.fx.control.toggle.mech.ToggleMechanism
import matt.lang.delegation.fullProvider
import matt.model.flowlogic.recursionblocker.RecursionBlocker
import matt.obs.hold.ObservableHolderImpl
import matt.obs.prop.Var
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


sealed class Setting<T>(val prop: Var<T>, val label: String, val tooltip: String, val default: T) {
  init {
	prop.observe {
	  println("Setting.prop changed...")
	}
  }

  fun resetToDefault() {
	prop.value = default
  }
}

class EnumSetting<E: Enum<E>>(val cls: KClass<E>, prop: Var<E>, label: String, tooltip: String, default: E):
	Setting<E>(prop, label = label, tooltip = tooltip, default = default) {
  fun createBoundToggleMechanism() = ToggleMechanism<E>().apply {
	val rBlocker = RecursionBlocker()
	selectValue(prop.value)
	selectedValue.onChange {
	  if (it != null) {
		rBlocker.with {
		  prop.value = it
		}
	  }
	}
	prop.onChange {
	  rBlocker.with {
		selectValue(it)
	  }
	}
  }



}

class IntSetting(prop: Var<Int>, label: String, tooltip: String, val min: Int, val max: Int, default: Int):
	Setting<Int>(prop, label = label, tooltip = tooltip, default = default)
class DoubleSetting(prop: Var<Double>, label: String, tooltip: String, val min: Double, val max: Double, default: Double):
	Setting<Double>(prop, label = label, tooltip = tooltip, default = default)
class BoolSetting(prop: Var<Boolean>, label: String, tooltip: String, default: Boolean):
	Setting<Boolean>(prop, label = label, tooltip = tooltip, default = default)


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
	  mSettings += EnumSetting(E::class, it.getValue(tr, p), label = label, tooltip = tooltip, default = defaultValue)
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
	  mSettings += BoolSetting(it.getValue(thisRef, prop), label = label, tooltip = tooltip, defaultValue)
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
	  mSettings += IntSetting(
		it.getValue(thisRef, prop),
		label = label,
		tooltip = tooltip,
		min = min,
		max = max,
		default = defaultValue
	  )
	}
  }

  protected inner class DoubleSettingProv(
	private val defaultValue: Double,
	private val label: String,
	private val tooltip: String,
	private val min: Double,
	private val max: Double
  ) {
	operator fun provideDelegate(
	  thisRef: ObservableHolderImpl,
	  prop: KProperty<*>,
	) = registeredProp(defaultValue).provideDelegate(thisRef, prop).also {
	  mSettings += DoubleSetting(
		it.getValue(thisRef, prop),
		label = label,
		tooltip = tooltip,
		min = min,
		max = max,
		default = defaultValue
	  )
	}
  }

}