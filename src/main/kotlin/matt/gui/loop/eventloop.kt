package matt.gui.loop

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
  javafx.application.Platform.runLater {
	op()
  }
}

fun <T> runLaterReturn(s: String, op: ()->T): T {
  val r = runLaterReturn { op() }
  return r
}