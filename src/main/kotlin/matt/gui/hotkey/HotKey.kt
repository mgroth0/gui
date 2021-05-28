package matt.gui.hotkey

import javafx.application.Platform.runLater
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import matt.hurricanefx.eye.lang.BProp
import matt.hurricanefx.toggle
import matt.kjlib.log.NEVER
import matt.kjlib.log.err
import matt.klibexport.klibexport.DSL
import matt.klibexport.klibexport.allUnique
import matt.klibexport.klibexport.go
import matt.klibexport.lang.applyEach
import java.lang.System.currentTimeMillis
import java.util.WeakHashMap
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract

sealed class HotKeyContainer {
  abstract fun getHotkeys(): List<HotKey>

}

data class HotKey(
  val code: KeyCode,
  var isMeta: Boolean = false,
  var isOpt: Boolean = false,
  var isCtrl: Boolean = false,
  var isShift: Boolean = false,

  var previous: HotKey? = null

): HotKeyContainer() {

  override fun getHotkeys() = listOf(this)


  var theOp: (()->Unit)? = null
  var theHandler: ((KeyEvent)->Unit)? = null
  var blocksFXorOSdefault = false

  val meta
	get() = apply { isMeta = true }
  val opt
	get() = apply { isOpt = true }
  val ctrl
	get() = apply { isCtrl = true }
  val shift
	get() = apply { isShift = true }
  val bare get() = this

  var isIgnoreFix = false

  val ignoreFix
	get() = apply { isIgnoreFix = true }

  fun wrapOp(wrapper: (()->Unit)->Unit) {
	require(theOp != null)
	val oldOp = theOp
	theOp = {
	  wrapper(oldOp!!)
	}
  }

  fun wraphandler(wrapper: ((KeyEvent)->Unit)->Unit) {
	require(theHandler != null)
	val oldHandle = theHandler
	theHandler = {
	  wrapper(oldHandle!!)
	}
  }


  fun blockFXorOSdefault() = apply {
	blocksFXorOSdefault = true
  }

}


operator fun KeyCode.plus(other: KeyCode) = HotKeySet(HotKey(this), HotKey(other))
operator fun HotKey.plus(other: HotKey) = HotKeySet(this, other)
operator fun HotKey.plus(other: KeyCode) = HotKeySet(this, HotKey(other))
operator fun KeyCode.plus(other: HotKey) = HotKeySet(HotKey(this), other)


private val KeyCode.meta
  get() = HotKey(this, isMeta = true)
private val KeyCode.opt
  get() = HotKey(this, isOpt = true)
private val KeyCode.ctrl
  get() = HotKey(this, isCtrl = true)
private val KeyCode.shift
  get() = HotKey(this, isShift = true)
private val KeyCode.bare
  get() = HotKey(this)


class HotKeySet(vararg keys: HotKey): HotKeyContainer() {
  val keys = keys.toList()
  override fun getHotkeys() = keys
  val meta
	get() = apply {
	  keys.applyEach { isMeta = true }
	}
  val opt
	get() = apply { keys.applyEach { isOpt = true } }
  val ctrl
	get() = apply { keys.applyEach { isCtrl = true } }
  val shift
	get() = apply { keys.applyEach { isShift = true } }

  val ignoreFix
	get() = apply { keys.applyEach { isIgnoreFix = true } }


  fun blockFXorOSdefault() = apply {
	keys.applyEach {
	  blocksFXorOSdefault = true
	}
  }

}


infix fun KeyEvent.matches(h: HotKey) =
	code == h.code && isMetaDown == h.isMeta && isAltDown == h.isOpt && isControlDown == h.isCtrl && isShiftDown == h.isShift

infix fun HotKey.matches(h: HotKey) =
	code == h.code && isMeta == h.isMeta && isOpt == h.isOpt && isCtrl == h.isCtrl && isShift == h.isShift

infix fun KeyEvent.matches(h: KeyEvent) =
	code == h.code && isMetaDown == h.isMetaDown && isAltDown == h.isAltDown && isControlDown == h.isControlDown && isShiftDown == h.isShiftDown


const val DOUBLE_HOTKEY_WINDOW_MS = 500L
var lastHotKey: Pair<HotKey, Long>? = null


fun KeyEvent.runAgainst(
  hotkeys: Iterable<HotKeyContainer>,
  last: KeyEvent? = null, fixer: HotKeyEventHandler
) {

  val pressTime = currentTimeMillis()

  if (this.code.isModifierKey) return consume()
  //  val debug = !this.code.isModifierKey
  //  if (debug) {
  //  println("${this} is running against")
  //  hotkeys.forEach {
  //	println(it)
  //  }
  //    }


  /*https://stackoverflow.com/questions/47797980/javafx-keyevent-triggers-twice*/
  /*the solution apparently is to use key released*/
  /*but that feels less responsive to me. heres my custom solution... here goes...*/
  /*potential issue: if I'm typing and I need to type same key multiple times and hjavafx is laggy, it might not clear this and my keys might not go through. So I'm only doing this with keys that are actual hotkeys*/
  //  println("here1 ${this.hashCode()}")
  if (last != null) {
	//	println("here2 ${this.hashCode()}")
	if (this matches last) {
	  //	  println("here3 ${this.hashCode()}")
	  if (this.target == last.target) { //this one never passes
		//		println("here4")

		/*

		here I assume there will be at most one duplicate...

		but what if there is no duplicate?

		I guess my best option is to assume there will be a duplicate always... if there isnt at least
		i can use that to start tracking down the root cause



		*/
		fixer.last = null


		return consume()

	  }
	} else {
	  //	  println("no match")
	  //	  println("me: ${this}")
	  //	  println("last: ${last}")
	}
  }
  //  runLater { fixer.last = null }
  /*only possible race condition now is with:
  * 1. javafx lag
  * 2. same hotkey twice really fast, faster than this runLater can run
  * 3. or not even so fast, of there's lots of lag (but who cares then there shouldnt be lots of lag)
  * more concerned with if there's little lag but I just am trying to do lots of things fast...
  * guess there's little I can do for now. Would need to file a bug report with a minimal working example...
  * */

  /*UGH! I CANNOT AVOID THIS PROBLEM!!!! I must use eventHandlers wherever this occurs. Its fine, they are the proper HANDLERS anyway (filters are FILTERS)
  *
  * UPDATE: EVEN HANDLERS ARENT WORKING
  * */


  var ensureConsume = false
  hotkeys.asSequence()
	  .flatMap { it.getHotkeys() }
	  .filter { h ->
		this matches h && (h.previous == null || (lastHotKey?.let {
		  h.previous!!.matches(it.first) && (pressTime - it.second) <= DOUBLE_HOTKEY_WINDOW_MS
		} ?: false))
	  }
	  .onEach {
		ensureConsume = ensureConsume || it.blocksFXorOSdefault
	  }.forEach { h ->
		lastHotKey = h to currentTimeMillis()
		if (!h.isIgnoreFix) {
		  fixer.last = this
		}
		if (isConsumed) return
		h.theOp?.go {
		  it()
		  runLater { fixer.last = null } /*... finally got it. not to early not too late. wow.*/
		  return consume()
		}
		h.theHandler?.invoke(this)
	  }
  if (ensureConsume && !isConsumed) consume()
}

class HotKeyEventHandler(
  hks: Iterable<HotKeyContainer>,
  var quickPassForNormalTyping: Boolean
): EventHandler<KeyEvent> {

  val hotkeys = hks.toMutableList()

  init {
	hotkeys.flatMap { it.getHotkeys() }.forEach {
	  if (it.code == KeyCode.H && it.isMeta && !it.isCtrl && !it.isOpt && !it.isShift) {
		err("meta H is blocked by KM to prevent OS window hiding")
	  }
	  if (it.isOpt && !it.code.isArrowKey && !it.isMeta && !it.isCtrl && !it.isShift) {
		err("I think MacOS has problems with opt where it sends an extra key typed event. stupid. also seen in intellij")
	  }
	}
  }

  var last: KeyEvent? = null
  override fun handle(event: KeyEvent) {
	if (quickPassForNormalTyping && (event.code.isDigitKey || event.code.isLetterKey) && !event.isMetaDown && !event.isControlDown && !event.isAltDown) {
	  return
	}
	event.runAgainst(hotkeys, last = last, fixer = this)
  }
}

fun <K, V> Map<K, V>.invert(): Map<V, K> {
  require(values.allUnique())
  return this.map { it.value to it.key }.associate { it.first to it.second }
}

val handlers = WeakHashMap<EventTarget, HotKeyEventHandler>()
val filters = WeakHashMap<EventTarget, HotKeyEventHandler>()

fun EventTarget.register(
  hotkeys: Iterable<HotKeyContainer>,
  quickPassForNormalTyping: Boolean = false,
) {
  val oldHandler = handlers[this]
  if (oldHandler != null) {
	oldHandler.hotkeys.addAll(hotkeys.toList())
	if (quickPassForNormalTyping) {
	  oldHandler.quickPassForNormalTyping = true
	}
  } else {
	val handler = HotKeyEventHandler(hotkeys, quickPassForNormalTyping)
	handlers[this] = handler
	when (this) {
	  is Node  -> addEventHandler(KeyEvent.KEY_PRESSED, handler)
	  is Scene -> addEventHandler(KeyEvent.KEY_PRESSED, handler)
	  is Stage -> addEventHandler(KeyEvent.KEY_PRESSED, handler)
	  else     -> NEVER
	}
  }

}

fun EventTarget.registerInFilter(
  hotkeys: Iterable<HotKeyContainer>,
  quickPassForNormalTyping: Boolean = false,
) {

  val oldHandler = filters[this]
  if (oldHandler != null) {
	oldHandler.hotkeys.addAll(hotkeys.toList())
	if (quickPassForNormalTyping) {
	  oldHandler.quickPassForNormalTyping = true
	}
  } else {
	val handler = HotKeyEventHandler(hotkeys, quickPassForNormalTyping)
	filters[this] = handler
	when (this) {
	  is Node  -> addEventFilter(KeyEvent.KEY_PRESSED, handler)
	  is Scene -> addEventFilter(KeyEvent.KEY_PRESSED, handler)
	  is Stage -> addEventFilter(KeyEvent.KEY_PRESSED, handler)
	  else     -> NEVER
	}
  }
}

@Suppress("PropertyName")
class HotkeyDSL(): DSL {


  val hotkeys = mutableListOf<HotKeyContainer>()

  val A get() = KeyCode.A.bare
  val B get() = KeyCode.B.bare
  val C get() = KeyCode.C.bare
  val D get() = KeyCode.D.bare
  val E get() = KeyCode.E.bare
  val F get() = KeyCode.F.bare
  val G get() = KeyCode.G.bare
  val H get() = KeyCode.H.bare
  val I get() = KeyCode.I.bare
  val J get() = KeyCode.J.bare
  val K get() = KeyCode.K.bare
  val L get() = KeyCode.L.bare
  val M get() = KeyCode.M.bare
  val N get() = KeyCode.N.bare
  val O get() = KeyCode.O.bare
  val P get() = KeyCode.P.bare
  val Q get() = KeyCode.Q.bare
  val R get() = KeyCode.R.bare
  val S get() = KeyCode.S.bare
  val T get() = KeyCode.T.bare
  val U get() = KeyCode.U.bare
  val V get() = KeyCode.V.bare
  val W get() = KeyCode.W.bare
  val X get() = KeyCode.X.bare
  val Y get() = KeyCode.Y.bare
  val Z get() = KeyCode.Z.bare
  val ENTER get() = KeyCode.ENTER.bare
  val DELETE get() = KeyCode.DELETE.bare
  val BACK_SPACE get() = KeyCode.BACK_SPACE.bare
  val CLOSE_BRACKET get() = KeyCode.CLOSE_BRACKET.bare
  val OPEN_BRACKET get() = KeyCode.OPEN_BRACKET.bare
  val ESCAPE get() = KeyCode.ESCAPE.bare
  val TAB get() = KeyCode.TAB.bare
  val COMMA get() = KeyCode.COMMA.bare

  val LEFT get() = KeyCode.LEFT.bare
  val RIGHT get() = KeyCode.RIGHT.bare
  val UP get() = KeyCode.UP.bare
  val DOWN get() = KeyCode.DOWN.bare

  val DIGIT1 get() = KeyCode.DIGIT1.bare
  val DIGIT2 get() = KeyCode.DIGIT2.bare
  val DIGIT3 get() = KeyCode.DIGIT3.bare
  val DIGIT4 get() = KeyCode.DIGIT4.bare
  val DIGIT5 get() = KeyCode.DIGIT5.bare
  val DIGIT6 get() = KeyCode.DIGIT6.bare
  val DIGIT7 get() = KeyCode.DIGIT7.bare
  val DIGIT8 get() = KeyCode.DIGIT8.bare
  val DIGIT9 get() = KeyCode.DIGIT9.bare
  val DIGIT0 get() = KeyCode.DIGIT0.bare

  val PLUS get() = KeyCode.PLUS.bare
  val EQUALS get() = KeyCode.EQUALS.bare
  val MINUS get() = KeyCode.MINUS.bare

  fun HotKey.meta(h: ()->Unit) = hotkeys.add(this.meta op { h() })
  fun HotKey.opt(h: ()->Unit) = hotkeys.add(this.opt op { h() })
  fun HotKey.ctrl(h: ()->Unit) = hotkeys.add(this.ctrl op { h() })
  fun HotKey.shift(h: ()->Unit) = hotkeys.add(this.shift op { h() })
  fun HotKey.bare(h: ()->Unit) = hotkeys.add(this op { h() })


  fun HotKeySet.meta(h: ()->Unit) = hotkeys.add(this.meta op { h() })
  fun HotKeySet.opt(h: ()->Unit) = hotkeys.add(this.opt op { h() })
  fun HotKeySet.ctrl(h: ()->Unit) = hotkeys.add(this.ctrl op { h() })
  fun HotKeySet.shift(h: ()->Unit) = hotkeys.add(this.shift op { h() })
  fun HotKeySet.bare(h: ()->Unit) = hotkeys.add(this op { h() })


  infix fun HotKey.op(setOp: ()->Unit) = apply {
	require(theHandler == null)
	theOp = setOp
	hotkeys.add(this)
  }


  infix fun HotKey.toggles(b: BProp) = op { b.toggle() }

  infix fun HotKey.handle(setHandler: (KeyEvent)->Unit) = apply {
	require(theOp == null)
	theHandler = setHandler
	hotkeys.add(this)
  }

  infix fun HotKeySet.op(setOp: ()->Unit) = apply {
	keys.applyEach {
	  require(theHandler == null)
	  theOp = setOp
	}
	hotkeys.add(this)
  }

  infix fun HotKeySet.toggles(b: BProp) = op { b.toggle() }

  infix fun HotKeySet.handle(setHandler: (KeyEvent)->Unit) = apply {
	keys.applyEach {
	  require(theOp == null)
	  theHandler = setHandler
	}
	hotkeys.add(this)
  }

  infix fun HotKey.then(h: HotKey) {
	hotkeys.add(this)
	hotkeys.add(h.apply { previous = this@then })
  }

}

inline fun EventTarget.hotkeys(
  filter: Boolean = false,
  quickPassForNormalTyping: Boolean = false,
  op: HotkeyDSL.()->Unit,
) {
  contract {
	callsInPlace(op, EXACTLY_ONCE)
  }
  HotkeyDSL().apply(op).hotkeys.go {
	if (filter) this.registerInFilter(it, quickPassForNormalTyping)
	else this.register(it, quickPassForNormalTyping)
  }
}
