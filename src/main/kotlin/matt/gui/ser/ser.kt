package matt.gui.ser

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import matt.exec.app.myDataFolder
import matt.kjlib.map.lazyMap
import matt.klib.commons.get
import java.io.File
import kotlin.reflect.KClass


annotation class SaveFile(val path: String)

val saveFiles = lazyMap<KClass<*>, File> {
  myDataFolder[it::class.annotations.filterIsInstance<SaveFile>().firstOrNull()?.path
	?: ("o" + File.separator + it.simpleName + ".json")]
}

