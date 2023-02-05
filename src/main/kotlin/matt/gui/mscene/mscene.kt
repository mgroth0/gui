package matt.gui.mscene

import javafx.event.Event
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.input.ContextMenuEvent
import javafx.scene.paint.Color
import matt.collect.itr.recurse.recurse
import matt.file.MFile
import matt.file.commons.ICON_FOLDER
import matt.fx.control.menu.context.mcontextmenu
import matt.fx.control.menu.context.showMContextMenu
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.core.scene.NEED_REVERSED_DISPLAYS_FEATURE
import matt.fx.graphics.core.scene.reloadStyle
import matt.fx.graphics.style.DarkModeController
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.fx.graphics.style.border.FXBorder
import matt.fx.graphics.wrapper.scene.SceneWrapper
import matt.fx.graphics.wrapper.style.StyleableWrapper
import matt.fx.graphics.wrapper.style.findName
import matt.log.profile.stopwatch.tic
import kotlin.reflect.KClass
import matt.fx.control.iconify.iconify
import matt.gui.hotkeys.addDefaultHotkeys
import matt.gui.settings.MattGeneralSettingsNode

open class MScene<R: ParentWrapper<*>>(
  root: R, val icon: MFile, userWidth: Double = -1.0, userHeight: Double = -1.0
): SceneWrapper<R>(root, userWidth, userHeight) {
  constructor(
	root: R, icon: String, userWidth: Double = -1.0, userHeight: Double = -1.0
  ): this(root, ICON_FOLDER["white/$icon.png"], userWidth = userWidth, userHeight = userHeight)

  constructor(
	root: R, userWidth: Double = -1.0, userHeight: Double = -1.0
  ): this(root, "chunk", userWidth = userWidth, userHeight = userHeight)


  private fun handleContextMenuReq(e: Event) {
	//	println("context menu requested from e=${e.hashCode()}")
	//	tab("target=${e.target}")
	//	tab("source=${e.source}")
	if (e is ContextMenuEvent) {
	  (e.target as? Node)?.let {
		showMContextMenu(it, e.screenX to e.screenY)
	  }
	  e.consume()
	}
  }


  init {
	val t = tic("creating mscene", enabled = false)
	addDefaultHotkeys()
	t.toc("added default hotkeys")


	val dark = DarkModeController.darkModeProp.value
	t.toc("finished DarkModeController 0")
	reloadStyle(dark)
	t.toc("finished DarkModeController 1")
	t.toc("finished DarkModeController 2")
	DarkModeController.darkModeProp.onChangeWithWeakAndOld(this) { scene, old, new ->
	  if (new != old) {
		scene.reloadStyle(DarkModeController.darkModeProp.value)
	  }
	}
	t.toc("finished DarkModeController 3")







	mcontextmenu {


	  menu("style") {
		actionitem("reload style") {
		  this@MScene.reloadStyle(DarkModeController.darkModeProp.value)
		}

		/*this is controlled from the OS from now on*/        /*matt.fx.graphics.menu.actionitem("toggle darkMode") {
		  darkMode = !darkMode
		  if (darkMode) {
			stylesheets.matt.lang.setall.setAll(DARK_MODENA_CSS, CUSTOM_CSS)
		  } else {
			stylesheets.matt.lang.setall.setAll()
		  }
		  darkModeListeners.forEach { it() }
		}*/






		actionitem("print style info samples") {
		  val classesPrinted = mutableListOf<KClass<*>>()
		  /*  (root.node as Node).recurse {
			  (it as? Parent)?.childrenUnmodifiable ?: listOf()
			}.forEach {
			  if (it::class !in classesPrinted) {
				println(it.matt.hurricanefx.eye.wrapper.matt.hurricanefx.eye.wrapper.obs.collect.wrapped().styleInfo())
				classesPrinted += it::class
			  }
			}*/
		  (root.node as Node).recurse {
			(it as? Parent)?.childrenUnmodifiable ?: listOf()
		  }.forEach {
			if (it::class !in classesPrinted) {
			  println((it.wrapped() as StyleableWrapper).styleInfo())
			  classesPrinted += it::class
			}
		  }
		}
		/*need this*/
		menu("set border") {        /*specify this here explicitly at least once
		  * or else it will use the `matt.fx.graphics.menu.actionitem` above without import*/
		  this.actionitem("none") {
			(root as RegionWrapper<*>).border = null
			/*(root.node as? Region)?.matt.hurricanefx.eye.wrapper.matt.hurricanefx.eye.wrapper.obs.collect.wrapped()?.borderFill = null*/
		  }
		  listOf(Color.YELLOW, Color.BLUE, Color.RED, Color.GREEN, Color.ORANGE, Color.PURPLE, Color.WHITE).forEach {
			actionitem(it.findName()) {
			  (root as RegionWrapper<*>).border = FXBorder.solid(it)
			  /*(root.node as? Region)?.matt.hurricanefx.eye.wrapper.matt.hurricanefx.eye.wrapper.obs.collect.wrapped()?.borderFill = it*/
			}
		  }
		}
	  }


	  if (NEED_REVERSED_DISPLAYS_FEATURE) actionitem("reverse displays") {
		MattGeneralSettingsNode.reversedDisplays.value = !MattGeneralSettingsNode.reversedDisplays.value!!
	  }

	}
	t.toc("finished main mcontextmenu")
	addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED) { e ->
	  handleContextMenuReq(e)
	}
	t.toc("finished mscene init")
  }


}



fun MScene<*>.iconify() = iconify(icon)