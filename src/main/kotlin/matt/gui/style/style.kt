package matt.gui.style

import com.jthemedetecor.OsThemeDetector
import javafx.css.Styleable
import javafx.scene.layout.Border
import javafx.scene.layout.BorderStroke
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.layout.Region
import javafx.scene.paint.Paint
import matt.hurricanefx.tornadofx.async.runLater
import matt.kbuild.FLOW_FOLDER
import matt.kjlib.file.get
import matt.kjlib.prop.BasicBooleanProperty
import matt.kjlib.str.LineAppender
import matt.klib.log.warn
import java.util.logging.Level
import kotlin.reflect.KProperty

object DarkModeController {

  private val detector = OsThemeDetector.getDetector()

  init {
	//	LoggerFactory.getLogger(MacOSThemeDetector::class.java)
	warn("asked for help on github...")
	java.util.logging.LogManager.getLogManager().loggerNames.asIterator().forEach {
	  println("logger:${it}")
	}
	java.util.logging.Logger.getLogger("com.jthemedetecor.MacOSThemeDetector").level = Level.OFF
	java.util.logging.Logger.getLogger("com.jthemedetecor").level = Level.OFF
  }


  private fun getIsDarkSafe(): Boolean? {
	warn("this is pointless. The error is caught and annoying logged inside the library")
	return try {
	  detector.isDark
	} catch (e: java.lang.NullPointerException) {
	  /*https://github.com/Dansoftowner/jSystemThemeDetector/issues/25*/
	  warn("caught the null jSystemThemeDetector bug again")
	  null
	}
  }

  val darkModeProp = BasicBooleanProperty(getIsDarkSafe() ?: true)

  init {
	detector.registerListener { isDark ->
	  if (isDark != null) {
		runLater {
		  darkModeProp.value = isDark
		}
	  } else {
		warn("isDark was null. Guess that thing is still happening")
	  }
	}
  }
}

val MODENA_CSS = FLOW_FOLDER["style"]["modena.css"].toURI().toURL().toString()
val DARK_MODENA_CSS = FLOW_FOLDER["style"]["darkModena.css"].toURI().toURL().toString()
val CUSTOM_CSS = FLOW_FOLDER["style"]["custom.css"].toURI().toURL().toString()


fun Styleable.styleInfo(): String {
  val r = LineAppender()
  r += ("${this::class}->${typeSelector}")

  r += ("\tstyleclasses")
  styleClass.forEach { sc ->
	r += ("\t\t$sc")
  }

  r += ("\tpseudo")
  pseudoClassStates.forEach { pc ->
	r += ("\t\t$pc->${pc.pseudoClassName}")
  }
  r += ("\tsample")
  r += ("\t\t${style}")
  if (false) {
	// string too big!
	r += ("\tmeta (${cssMetaData.size})")
	cssMetaData.forEach {
	  r += ("\t\t${it}")
	}
  }
  return r.toString()
}

var Region.borderFill: Paint?
  get() = border?.strokes?.firstOrNull()?.topStroke
  set(value) {
	border = if (value == null) null
	else {
	  Border(BorderStroke(value, BorderStrokeStyle.SOLID, null, null))
	}

  }
var Region.borderDashFill: Paint?
  get() = border?.strokes?.firstOrNull { it.topStyle == BorderStrokeStyle.DASHED }?.topStroke
  set(value) {
	border = if (value == null) null
	else {
	  Border(BorderStroke(value, BorderStrokeStyle.DASHED, null, null))
	}

  }

private class StyleClass {
  operator fun getValue(styleClassDSL: StyleClassDSL, property: KProperty<*>): Any? {
	styleClassDSL.s.styleClass += property.name
	return null
  }
}

class StyleClassDSL(val s: Styleable) {
  val yellowText by StyleClass()
  val blueText by StyleClass()
  val darkGreyText by StyleClass()
  val redText by StyleClass()
  val whiteText by StyleClass()
  val greenBackground by StyleClass()
  val nodeTextField by StyleClass()
  val presentationMode by StyleClass()
  val flowablePresentationMode by StyleClass()
}

fun Styleable.sty(op: StyleClassDSL.()->Unit) {
  StyleClassDSL(this).apply(op)
}

