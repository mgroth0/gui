package matt.gui.loop

import matt.klib.log.debug
import java.util.concurrent.Semaphore

fun <T> runLaterReturn(op: ()->T): T {
  var r: T? = null
  val sem = Semaphore(0)
  try {
	runLater {
	  r = op()
	  sem.release()
	}
  } catch (e: Exception) {
	sem.release()
	e.printStackTrace()
  }
  sem.acquire()
  return r!!
}

fun runLater(s: String = "NO MESSAGE", op: ()->Unit) {
  debug("matt.hurricanefx.runLater:${s}:scheduling")
  javafx.application.Platform.runLater {
	debug("matt.hurricanefx.runLater:${s}:running")
	op()
	debug("matt.hurricanefx.runLater:${s}:finished")
  }
  debug("matt.hurricanefx.runLater:${s}:scheduled")
}

fun <T> runLaterReturn(s: String, op: ()->T): T {
  debug("runLaterReturn:${s}:blocking")
  val r = runLaterReturn {
	debug("runLaterReturn:${s}:running")
	debug("runLaterReturn:${s}:finished")
	op()
  }
  debug("runLaterReturn:${s}:returning")
  return r
}