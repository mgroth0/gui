@file:OptIn(ExperimentalSerializationApi::class)

package matt.gui.ser

import javafx.beans.property.Property
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import matt.exec.app.myDataFolder
import matt.hurricanefx.eye.delegate.createFxProp
import matt.hurricanefx.eye.lib.onActualChange
import matt.kjlib.file.writeToFile
import matt.kjlib.map.lazyMap
import matt.kjlib.weak.bag.WeakBag
import matt.klib.commons.get
import matt.klib.lang.KotlinPrimitive
import matt.klib.lang.NEVER
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible


annotation class SaveFile(val path: String)
annotation class Savable


val savableAwareJson = Json {
  serializersModule = SerializersModule {
	contextual(Any::class, AnySerializer)
  }
  serializersModule
  useArrayPolymorphism
  coerceInputValues
  allowStructuredMapKeys
}

object AnySerializer:
  KSerializer<Any> {
  override val descriptor = object: SerialDescriptor {
	override val elementsCount = 1
	override val kind = StructureKind.LIST
	override val serialName = Any::class.qualifiedName!!
	override fun getElementAnnotations(index: Int): List<Annotation> {
	  require(index >= 0) { "Illegal index $index, $serialName expects only non-negative indices" }
	  return emptyList()
	}

	override fun getElementDescriptor(index: Int): SerialDescriptor {
	  require(index >= 0) { "Illegal index $index, $serialName expects only non-negative indices" }
	  return when (index) {
		0    -> String.serializer().descriptor
		1    -> NEVER
		2    -> NEVER
		else -> NEVER
	  }
	}

	override fun getElementIndex(name: String): Int =
	  name.toIntOrNull() ?: throw IllegalArgumentException("$name is not a valid list index")

	override fun getElementName(index: Int): String = index.toString()

	override fun isElementOptional(index: Int): Boolean {
	  require(index >= 0) { "Illegal index $index, $serialName expects only non-negative indices" }
	  return false
	}


  }

  override fun deserialize(decoder: Decoder): Any {
	var theAny: Any? = null

	decoder.decodeStructure(descriptor) {
	  var nextIndex = this.decodeElementIndex(descriptor)
	  val qname = this.decodeStringElement(descriptor, 0)
	  nextIndex = this.decodeElementIndex(descriptor)
	  theAny = this.decodeSerializableElement(descriptor, 2, findSerializerFor(qname))
	}
	return theAny!!
  }

  override fun serialize(encoder: Encoder, value: Any) {
	val qname = value::class.qualifiedName!!
	encoder.encodeStructure(descriptor) {
	  encodeStringElement(descriptor, 0, qname)
	  @Suppress("UNCHECKED_CAST")
	  encodeSerializableElement(descriptor, 1, findSerializerFor(value) as SerializationStrategy<Any>, value)
	}

  }
}

val saveFiles = lazyMap<KClass<*>, File> {
  myDataFolder[it::class.annotations.filterIsInstance<SaveFile>().firstOrNull()?.path
	?: ("o" + File.separator + it.simpleName + ".json")]
}

@Serializable data class SerializableWithSavables<T>(
  val ser: T, val savs: Map<String, @Contextual Any>

)

object SerializableWithSavablesSerializer:
  SerializationStrategy<SerializableWithSavables<*>> {
  override val descriptor = object: SerialDescriptor {
	override val elementsCount = 1
	override val kind = StructureKind.CLASS
	override val serialName = SerializableWithSavables::class.qualifiedName!!
	override fun getElementAnnotations(index: Int): List<Annotation> {
	  require(index >= 0) { "Illegal index $index, $serialName expects only non-negative indices" }
	  return emptyList()
	}

	override fun getElementDescriptor(index: Int): SerialDescriptor {
	  require(index >= 0) { "Illegal index $index, $serialName expects only non-negative indices" }
	  return when (index) {
		0    -> this
		else -> NEVER
	  }
	}

	override fun getElementIndex(name: String): Int =
	  name.toIntOrNull() ?: throw IllegalArgumentException("$name is not a valid list index")

	override fun getElementName(index: Int): String = when (index) {
	  0    -> "ser"
	  1    -> "savs"
	  else -> NEVER
	}

	override fun isElementOptional(index: Int): Boolean {
	  require(index >= 0) { "Illegal index $index, $serialName expects only non-negative indices" }
	  return false
	}
  }

  override fun serialize(
	encoder: Encoder, value: SerializableWithSavables<*>
  ) {
	encoder.encodeStructure(descriptor) {
	  @Suppress("UNCHECKED_CAST")
	  encodeSerializableElement(descriptor, 0, findSerializerFor(value.ser) as SerializationStrategy<Any>, value.ser!!)
	  encodeSerializableElement(
		descriptor, 1, MapSerializer(
		  keySerializer = String.serializer(), valueSerializer = AnySerializer
		), value.savs
	  )
	}
  }
}


inline fun <reified T: Any> T.save() {
  val savablePropertiesMap = this::class.memberProperties.filter {
	it.hasAnnotation<Savable>()
  }.associate {
	it.name to it.call(this) as Any
  }
  val whole = SerializableWithSavables(this, savablePropertiesMap)
  val serializedWhole = savableAwareJson.encodeToString(SerializableWithSavablesSerializer, whole)
  serializedWhole.writeToFile(saveFiles[this::class]!!)
}


inline fun <reified R> File.load(): R? = takeIf { it.exists() }?.readText()?.takeIf { it.isNotBlank() }?.let { json ->


  val almost = savableAwareJson.decodeFromString<SerializableWithSavables<R>>(json)

  blockAutoSavingOfThese += almost.ser!!

  almost.savs.forEach { (k, v) ->
	((almost.ser!!::class as KClass<*>)
	  .memberProperties
	  .first { it.hasAnnotation<Savable>() && it.name == k } as KMutableProperty<*>).setter.call(almost.ser, v)
  }

  blockAutoSavingOfThese -= almost.ser!!

  almost.ser
}

inline fun <reified R> KClass<*>.load() = saveFiles[this]!!.load<R>()


inline fun <reified T: Any> fx(default: T? = null, autosave: Boolean = false) =
  FXPropProvider(default, T::class, autosave = autosave)

inline fun <reified T: Any> autoFX(default: T? = null) = FXPropProvider(default, T::class, autosave = true)

val blockAutoSavingOfThese = WeakBag<Any>()

class FXPropProvider<T: Any>(val default: T?, val vCls: KClass<T>, val autosave: Boolean = false) {
  operator fun provideDelegate(
	thisRef: Any, prop: KProperty<*>
  ): FXProp<T> {
	return FXProp(
	  default, vCls = vCls,    //	  tCls = thisRef::class,
	  autosave = autosave, thisRef
	)/*.also { savableFXPropsM[prop.name] = it.fxProp }*/
  }
}

class FXProp<V>(
  default: V?, vCls: KClass<*>, //  tCls: KClass<T>,
  autosave: Boolean, thisRef: Any
) {
  @Suppress("UNCHECKED_CAST") val fxProp: Property<V> = (vCls.createFxProp() as Property<V>).also {
	if (default != null) it.value = default
	if (autosave) it.onActualChange {
	  if (thisRef !in blockAutoSavingOfThese) thisRef.save()
	}
  }

  operator fun getValue(
	thisRef: Any?,
	property: KProperty<*>,
  ): V = fxProp.value

  operator fun setValue(
	thisRef: Any?, property: KProperty<*>, value: V
  ) {
	fxProp.value = value
  }

}

@Suppress("UNCHECKED_CAST") val <V> KProperty0<V>.fx
  get() = (this.apply {    /*
	*
	* Caused by: kotlin.reflect.full.IllegalPropertyDelegateAccessException: Cannot obtain the delegate of a non-accessible property. Use "isAccessible = true" to make the property accessible
	*
	* */
	isAccessible = true
  }.getDelegate() as FXProp<V>).fxProp


@Suppress("UNCHECKED_CAST") fun <T, V> KProperty1<T, V>.fx(t: T) = (this.apply {/*
  *
  * Caused by: kotlin.reflect.full.IllegalPropertyDelegateAccessException: Cannot obtain the delegate of a non-accessible property. Use "isAccessible = true" to make the property accessible
  *
  * */
  isAccessible = true
}.getDelegate(t) as FXProp<V>).fxProp


@OptIn(InternalSerializationApi::class)
fun findSerializerFor(o: Any?): KSerializer<out Any> {
  return when (o) {
	is Boolean -> Boolean.serializer()
	is Char    -> Char.serializer()
	is String  -> String.serializer()
	is Int     -> Int.serializer()
	is Long    -> Long.serializer()
	is Float   -> Float.serializer()
	is Double  -> Double.serializer()
	else       -> o!!::class.serializer()
  }
}

@OptIn(InternalSerializationApi::class)
fun findSerializerFor(qname: String): KSerializer<out Any> {
  KotlinPrimitive::class.sealedSubclasses.forEach {
	if (qname == it.qualifiedName) return it.serializer()
  }
  return when (qname) {
	Boolean::class.qualifiedName -> Boolean.serializer() /*Class.forName is not working for kotlin primitives?*/
	else                         -> Class.forName(qname).kotlin.serializer()
  }
}