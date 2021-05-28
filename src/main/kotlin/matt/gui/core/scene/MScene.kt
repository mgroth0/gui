package matt.gui.core.scene

import javafx.event.Event
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.input.ContextMenuEvent
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import matt.auto.openInIntelliJ
import matt.gui.core.context.mcontextmenu
import matt.gui.core.context.showMContextMenu
import matt.gui.fxlang.actionitem
import matt.gui.hotkeys.addDefaultHotkeys
import matt.gui.ican.ICON_HEIGHT
import matt.gui.ican.ICON_WIDTH
import matt.gui.ican.Icon
import matt.gui.ican.IconFolder
import matt.gui.lang.onDoubleClickConsume
import matt.gui.mag.reversed_displays
import matt.gui.style.CUSTOM_CSS
import matt.gui.style.DARK_MODENA_CSS
import matt.gui.style.borderFill
import matt.gui.style.styleInfo
import matt.gui.win.interact.WinGeom
import matt.gui.win.interact.WinOwn
import matt.gui.win.interact.openInNewWindow
import matt.gui.win.winfun.noDocking
import matt.hurricanefx.tornadofx.async.runLater
import matt.hurricanefx.tornadofx.menu.item
import matt.hurricanefx.tornadofx.menu.menu
import matt.kjlib.MemReport
import matt.kjlib.file.get
import matt.kjlib.recurse.recurse
import matt.kjlib.stream.notContainedIn
import java.io.File
import java.net.URL
import kotlin.reflect.KClass

open class MScene(
  root: Parent,
  val icon: File
): Scene(root) {
  constructor(
	root: Parent,
	icon: String
  ): this(root, IconFolder["white/$icon.png"])

  constructor(
	root: Parent
  ): this(root, "chunk")

  init {
	addDefaultHotkeys()
	stylesheets.add(DARK_MODENA_CSS)
	stylesheets.add(CUSTOM_CSS)


	//     ensure that even while the screen is loading it is black. So not white flashes or flickering while refreshing
	fill = Color.BLACK
	//        maybe also possible by styleing .root in css, but if I remember correctly that also affects other nodes


	mcontextmenu {
	  menu("style") {
		actionitem("reload style") {
		  stylesheets.setAll(DARK_MODENA_CSS, CUSTOM_CSS)
		}
		actionitem("open darkModena.css") {
		  File(URL(DARK_MODENA_CSS).file).openInIntelliJ()
		}
		actionitem("open custom.css") {
		  File(URL(CUSTOM_CSS).file).openInIntelliJ()
		}
		actionitem("print style info samples") {
		  val classesPrinted = mutableListOf<KClass<*>>()
		  (root as Node).recurse {
			(it as? Parent)?.childrenUnmodifiable ?: listOf()
		  }.forEach {
			if (it::class.notContainedIn(classesPrinted)) {
			  println(it.styleInfo())
			  classesPrinted += it::class
			}
		  }
		}
		/*need this*/
		this.menu("set border") {
		  /*specify this here explicitly at least once
		  * or else it will use the `actionitem` above without import*/
		  this.actionitem("none") {
			(root as? Region)?.borderFill = null
		  }
		  actionitem("yellow") {
			(root as? Region)?.borderFill = Color.YELLOW
		  }
		  actionitem("blue") {
			(root as? Region)?.borderFill = Color.BLUE
		  }
		  actionitem("red") {
			(root as? Region)?.borderFill = Color.RED
		  }
		  actionitem("green") {
			(root as? Region)?.borderFill = Color.GREEN
		  }
		  actionitem("orange") {
			(root as? Region)?.borderFill = Color.ORANGE
		  }
		  actionitem("purple") {
			(root as? Region)?.borderFill = Color.PURPLE
		  }
		  actionitem("white") {
			(root as? Region)?.borderFill = Color.WHITE
		  }
		}
	  }

	  actionitem("reverse displays") {
		reversed_displays = !reversed_displays
	  }

	  actionitem("test exception") {
		throw Exception("test exception")
	  }

	  actionitem("iconify", ::iconify)

	  onRequest {
		val mreport = MemReport()
		menu("MemReport") {
		  /*need one this to enforce THIS*/
		  this.item("total:${mreport.total}") {}
		  item("max:${mreport.max}") {}
		  item("free:${mreport.free}") {}
		}
	  }
	}
	fun handleContextMenuReq(e: Event) {
	  if (e is ContextMenuEvent) {
		(e.target as? Node)?.let { showMContextMenu(it, e.screenX to e.screenY) }
		e.consume()
	  }

	  /*this doesnt work. lets try insets for right clicking*/
	  //	  else if (matt.kjlib.jmath.e is MouseEvent) {
	  //		(matt.kjlib.jmath.e.target as? Node)?.let { showMContextMenu(it, matt.kjlib.jmath.e.screenX to matt.kjlib.jmath.e.screenY) }
	  //		/*dont consume. maybe if I'm lucky I'll get both my context menu and the web one*/
	  //	  }

	}
	addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED) { e ->
	  handleContextMenuReq(e)
	}

	/*this doesnt work.  lets try insets for right clicking*/
	//	/*for web view. plese work*/
	//	addEventFilter(MouseEvent.MOUSE_CLICKED) { matt.kjlib.jmath.e ->
	//	  if (matt.kjlib.jmath.e.isSecondaryButtonDown) {
	//		handleContextMenuReq(matt.kjlib.jmath.e)
	//	  }

	//	}
  }


  fun iconify() {
	var iconWindow: Stage? = null
	VBox(Icon(icon)).apply {
	  var xOffset: Double? = null
	  var yOffset: Double? = null
	  setOnMousePressed { e ->
		iconWindow?.let {
		  xOffset = it.x - e.screenX
		  yOffset = it.y - e.screenY
		}
	  }
	  setOnMouseDragged {
		iconWindow?.x = it.screenX + (xOffset ?: 0.0)
		iconWindow?.y = it.screenY + (yOffset ?: 0.0)
	  }
	  onDoubleClickConsume {
		runLater {
		  (this@MScene.window as Stage).show()
		  (scene.window as Stage).close()
		}
	  }
	}.openInNewWindow(
	  own = WinOwn.None,
	  geom = WinGeom.ManualOr0(
		width = ICON_WIDTH,
		height = ICON_HEIGHT,
		x = this@MScene.window.x + (this@MScene.window.width/2) - (ICON_WIDTH/2),
		y = this@MScene.window.y + (this@MScene.window.height/2) - (ICON_HEIGHT/2),
	  ),
	  mScene = false,
	  border = false,
	  beforeShowing = {
		scene.stylesheets.add(DARK_MODENA_CSS)
		scene.stylesheets.add(CUSTOM_CSS)
	  }
	).apply {
	  iconWindow = this
	  isAlwaysOnTop = true
	  noDocking()
	}
	window.hide()
  }


}