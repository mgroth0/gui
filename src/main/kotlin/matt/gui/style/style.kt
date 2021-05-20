package matt.gui.style

import javafx.css.Styleable
import javafx.scene.layout.Border
import javafx.scene.layout.BorderStroke
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.layout.Region
import javafx.scene.paint.Paint
import matt.kjlib.commons.ROOT_FOLDER
import matt.kjlib.file.get
import matt.kjlib.log.NEVER
import matt.kjlib.str.LineAppender
import kotlin.reflect.KProperty

val DARK_MODENA_CSS = ROOT_FOLDER["style"]["darkModena.css"].toURI().toURL().toString()
val CUSTOM_CSS = ROOT_FOLDER["style"]["custom.css"].toURI().toURL().toString()


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
  get() = NEVER
  set(value) {
	border = if (value == null) null
	else {
	  Border(BorderStroke(value, BorderStrokeStyle.SOLID, null, null))
	}

  }
var Region.borderDashFill: Paint?
  get() = NEVER
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
  val whiteText by StyleClass()
  val greenBackground by StyleClass()
  val nodeTextField by StyleClass()
  val presentationMode by StyleClass()
}

fun Styleable.sty(op: StyleClassDSL.()->Unit) {
  StyleClassDSL(this).apply(op)
}

