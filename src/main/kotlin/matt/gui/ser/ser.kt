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
import matt.kjlib.file.get
import matt.kjlib.lang.NEVER
import matt.kjlib.map.lazyMap
import matt.kjlib.str.writeToFile
import matt.kjlib.weak.bag.WeakBag
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible


annotation class SaveFile(val path: String)
annotation class Savable


val savableAwareJson = Json {
  serializersModule = SerializersModule {
	contextual(Any::class, AnySerializer)


	//	polymorphic(Any::class) {
	//	  subclass(SerializableWithSavables::class)
	//	}
  }
  serializersModule
  useArrayPolymorphism
  coerceInputValues
  allowStructuredMapKeys
}

@OptIn(InternalSerializationApi::class) @Suppress("OPT_IN_OVERRIDE", "OPT_IN_USAGE") object AnySerializer:
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
	println("deserialize 1")
	var theAny: Any? = null    //	decoder.decodeSerializableValue()

	decoder.decodeStructure(descriptor) {

	  /*println("decodeSequentially=${this.decodeSequentially()}")*/    //	  this.decodeElementIndex()


	  /*ALL I HAD TO DO WAS CALL THIS. DONT EVEN HAVE TO USE RESULT? THATS HOW YOU SKIP OVER COMMAS I THINK.*/
	  var nextIndex = this.decodeElementIndex(descriptor)




	  println("nextIndex2=${nextIndex}")
	  println("deserialize 2")
	  val qname = this.decodeStringElement(descriptor, 0)
	  println("deserialize 3: $qname")


	  /*ALL I HAD TO DO WAS CALL THIS. DONT EVEN HAVE TO USE RESULT? THATS HOW YOU SKIP OVER COMMAS I THINK.*/
	  nextIndex = this.decodeElementIndex(descriptor)




	  println("nextIndex4=${nextIndex}")
	  theAny = this.decodeSerializableElement(descriptor, 2, findSerializerFor(qname))
	  println("deserialize 4: $theAny")
	}
	return theAny!!
  }

  override fun serialize(encoder: Encoder, value: Any) {
	println("serialize1")
	val qname = value::class.qualifiedName!!
	println("qname=${qname}")

	//	val holder = OutHolder(value)
	//	val theOutAny = value as out Any


	encoder.encodeStructure(descriptor) {
	  encodeStringElement(descriptor, 0, qname)
	  println("wrote qname")
	  encodeSerializableElement(descriptor, 1, findSerializerFor(value) as SerializationStrategy<Any>, value)
	  println("probably not getting here")
	}

  }
}

class OutHolder<out T: Any>(val o: T) {

}


val saveFiles = lazyMap<KClass<*>, File> {
  myDataFolder[it::class.annotations.filterIsInstance<SaveFile>().firstOrNull()?.path
	?: ("o" + File.separator + it.simpleName + ".json")]
}

@Serializable data class SerializableWithSavables<T>(
  val ser: T, val savs: Map<String, @Contextual Any>

)

@OptIn(ExperimentalSerializationApi::class) object SerializableWithSavablesSerializer:
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

  @OptIn(InternalSerializationApi::class) override fun serialize(
	encoder: Encoder, value: SerializableWithSavables<*>
  ) {    //	value.savs::class.serializer()

	println("SerializableWithSavablesSerializer.serialize 1")

	encoder.encodeStructure(descriptor) {
	  println("SerializableWithSavablesSerializer.serialize 2")

	  encodeSerializableElement(descriptor, 0, findSerializerFor(value.ser) as SerializationStrategy<Any>, value.ser!!)
	  println("SerializableWithSavablesSerializer.serialize 3")    //	  encoder.encodeStructure(descriptor) {
	  //
	  //	  }


	  /*encodeSerializableElement(descriptor, 1, String.serializer(), "hey json")*/

	  encodeSerializableElement(
		descriptor, 1, MapSerializer(
		  keySerializer = String.serializer(), valueSerializer = AnySerializer
		), value.savs
	  )


	  println("SerializableWithSavablesSerializer.serialize 4")    //	  encodeStringElement(descriptor, 0, qname)
	  //	  encodeSerializableElement(descriptor, 1, theSerializer as SerializationStrategy<Any>, value)
	}

	/*encoder.encodeDouble(1.2)*/

  }
}

@OptIn(ExperimentalSerializationApi::class) class MyMapSerializer<K, V>(map: Map<K, V>):
  SerializationStrategy<Map<K, V>> {
  override val descriptor = object: SerialDescriptor {
	override val elementsCount = 1
	override val kind = StructureKind.MAP
	override val serialName = MyMapSerializer::class.qualifiedName!!
	override fun getElementAnnotations(index: Int): List<Annotation> {
	  require(index >= 0) { "Illegal index $index, $serialName expects only non-negative indices" }
	  return emptyList()
	}

	override fun getElementDescriptor(index: Int): SerialDescriptor {
	  require(index >= 0) { "Illegal index $index, $serialName expects only non-negative indices" }
	  if (index < map.size) {
		return this
	  }
	  NEVER

	  //	  LinkedHashMapSerializer

	}

	override fun getElementIndex(name: String): Int = map.entries.indexOfFirst { it.key.toString() == name }

	override fun getElementName(index: Int): String = map.entries.toList()[index].key.toString()

	override fun isElementOptional(index: Int): Boolean {
	  require(index >= 0) { "Illegal index $index, $serialName expects only non-negative indices" }
	  return false
	}
  }

  @OptIn(InternalSerializationApi::class) override fun serialize(encoder: Encoder, value: Map<K, V>) {
	var index = 0
	println("MyMapSerializer.serialize 1")
	encoder.encodeStructure(descriptor) {
	  println("MyMapSerializer.serialize 2")
	  value.forEach { (k, v) ->
		println("MyMapSerializer.serialize 3")
		encodeSerializableElement(descriptor, index, findSerializerFor(v) as SerializationStrategy<Any>, v!!)
		println("MyMapSerializer.serialize 5")
		index++
	  }
	}    //	encoder.encodeStructure(descriptor) {
	//	  encodeSerializableElement(descriptor, 0, theSerializer as SerializationStrategy<Any>, value.ser)
	//
	//	  //	  encoder.encodeStructure(descriptor) {
	//	  //
	//	  //	  }
	//
	//
	//	  encodeSerializableElement(
	//		descriptor, 1, ContextualSerializer(Map::class) as SerializationStrategy<Any>, value.savs
	//	  )
	//	  encodeStringElement(descriptor, 0, qname)
	//	  encodeSerializableElement(descriptor, 1, theSerializer as SerializationStrategy<Any>, value)
  }

}


inline fun <reified T: Any> T.save() {
  println("save1")
  val savablePropertiesMap = this::class.memberProperties.filter {
	it.hasAnnotation<Savable>()
  }.associate {
	it.name to it.call(this) as Any
  }
  println("save2")
  val whole = SerializableWithSavables(this, savablePropertiesMap)
  println("save3")
  val serializedWhole = savableAwareJson.encodeToString(SerializableWithSavablesSerializer, whole)
  println("save4")
  serializedWhole.writeToFile(saveFiles[this::class]!!)
} // ///*needed for reifying with autosave*/
//internal inline fun <reified T: Any> T.save(cls: KClass<T>) {
//  println("save1")
//  val savablePropertiesMap = this::class.memberProperties.filter {
//	it.hasAnnotation<Savable>()
//  }.associate {
//	it.name to it.call(this) as Any
//  }
//  println("save2")
//  val whole = SerializableWithSavables(this, savablePropertiesMap)
//  println("save3")
//  val serializedWhole = savableAwareJson.encodeToString(whole)
//  println("save4")
//  serializedWhole.writeToFile(saveFiles[this::class]!!)
//}


inline fun <reified R> File.load(): R? = takeIf { it.exists() }?.readText()?.takeIf { it.isNotBlank() }?.let { json ->


  val almost = savableAwareJson.decodeFromString<SerializableWithSavables<R>>(json)

  blockAutoSavingOfThese += almost.ser!!

  println("got almost1: $almost")
  almost.savs.forEach { (k, v) ->
	((almost.ser!!::class as KClass<*>)
	  .memberProperties
	  .first { it.hasAnnotation<Savable>() && it.name == k } as KMutableProperty<*>).setter.call(almost.ser, v)
	//	  .forEach {
	//		(it as KMutableProperty<*>).setter.call(almost.ser, v)        //		it.call()
	//		println("got almost1.5: $almost")
	//	  }
  }

  println("got almost2: $almost")
  blockAutoSavingOfThese -= almost.ser!!

  almost.ser
}

inline fun <reified R> KClass<*>.load() = saveFiles[this]!!.load<R>()


/*	.takeIf {
	println("checking if exists")
	it.exists()
  }
	?.readText()
	?.takeIf {
	  println("checking if not blank")
	  it.isNotBlank()
	}?.let {
	 *//* println("load1")
	  val r = savableAwareJson.decodeFromString<R>(it)
	  println("load2:${r}")
	  r
*//*
	  val almost = savableAwareJson.decodeFromString<SerializableWithSavables<R>>(it)
	  almost.savs.forEach { (k, v) ->
		(almost.ser!!::class as KClass<*>)
		  .memberProperties
		  .filter { it.hasAnnotation<Savable>() }
		  .forEach {
			it.call(almost.ser, v)
		  }
	  }
	  almost.ser


	}*/


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
	if (autosave && thisRef !in blockAutoSavingOfThese) it.onActualChange {
	  thisRef.save()
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

//@Suppress("UNCHECKED_CAST") fun <T, V> KProperty1<T, V>.fx(t: T) = (this.apply {/*
//  *
//  * Caused by: kotlin.reflect.full.IllegalPropertyDelegateAccessException: Cannot obtain the delegate of a non-accessible property. Use "isAccessible = true" to make the property accessible
//  *
//  * */
//  isAccessible = true
//}.getDelegate(t) as FXProp<V>).fxProp

@Suppress("UNCHECKED_CAST") fun <T, V> KProperty1<T, V>.fx(t: T) = (this.apply {/*
  *
  * Caused by: kotlin.reflect.full.IllegalPropertyDelegateAccessException: Cannot obtain the delegate of a non-accessible property. Use "isAccessible = true" to make the property accessible
  *
  * */
  isAccessible = true
}.getDelegate(t) as FXProp<V>).fxProp

/*@Suppress("UNCHECKED_CAST") fun <R> KCallable<R>.fx(t: Any) = (this.apply {

   *//*Caused by: kotlin.reflect.full.IllegalPropertyDelegateAccessException: Cannot obtain the delegate of a non-accessible property. Use "isAccessible = true" to make the property accessible*//*


  isAccessible = true

//  t::class.java.

}.getDelegate(t) as FXProp<V>).fxProp*/

//@Suppress("UNCHECKED_CAST") fun <T, V> kotlin.reflect.jvm.internal.KProperty1Impl<T, V>.fx(t: T) = (this.apply {/*
//  *
//  * Caused by: kotlin.reflect.full.IllegalPropertyDelegateAccessException: Cannot obtain the delegate of a non-accessible property. Use "isAccessible = true" to make the property accessible
//  *
//  * */
//  isAccessible = true
//}.getDelegate(t) as FXProp<V>).fxProp


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

sealed interface KotlinPrimitive {
  val kls: KClass<*>
}


enum class Ints(override val kls: KClass<*>): KotlinPrimitive {
  Byte(kotlin.Byte::class),
  Short(kotlin.Short::class),
  Int(kotlin.Int::class),
  Long(kotlin.Long::class),
}

enum class IntArrays(override val kls: KClass<*>): KotlinPrimitive {
  ByteArray(kotlin.ByteArray::class),
  IntArray(kotlin.IntArray::class),
  ShortArray(kotlin.ShortArray::class),
  LongArray(kotlin.LongArray::class),
}

enum class FloatingPoints(override val kls: KClass<*>): KotlinPrimitive {
  Float(kotlin.Float::class),
  Double(kotlin.Double::class),
}

enum class FloatingPointArrays(override val kls: KClass<*>): KotlinPrimitive {
  FloatArray(kotlin.FloatArray::class),
  DoubleArray(kotlin.DoubleArray::class),
}

enum class UnsignedInts(override val kls: KClass<*>): KotlinPrimitive {
  UByte(kotlin.UByte::class),
  UShort(kotlin.UShort::class),
  UInt(kotlin.UInt::class),
  ULong(kotlin.ULong::class),
}

@OptIn(ExperimentalUnsignedTypes::class)
enum class UnsignedIntArrays(override val kls: KClass<*>): KotlinPrimitive {
  /*unsigned arrays and ranges*/
  UByteArray(kotlin.UByteArray::class),
  UShortArray(kotlin.UShortArray::class),
  UIntArray(kotlin.UIntArray::class),
  ULongArray(kotlin.ULongArray::class),
}

enum class Booleans(override val kls: KClass<*>): KotlinPrimitive {
  Boolean(kotlin.Boolean::class),
}

enum class BooleanArrays(override val kls: KClass<*>): KotlinPrimitive {
  BooleanArray(kotlin.BooleanArray::class),
}

enum class Chars(override val kls: KClass<*>): KotlinPrimitive {
  Char(kotlin.Char::class),
}

enum class CharArrays(override val kls: KClass<*>): KotlinPrimitive {
  CharArray(kotlin.CharArray::class),
}

enum class Strings(override val kls: KClass<*>): KotlinPrimitive {
  String(kotlin.String::class),
}

enum class Arrays(override val kls: KClass<*>): KotlinPrimitive {
  Array(kotlin.Array::class),
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