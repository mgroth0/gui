package matt.gui.loop

import matt.kjlib.date.Duration
import java.lang.Thread.sleep
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

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

fun runMuchLater(d: Duration, op: ()->Unit) {
  thread {
	sleep(d.inMilliseconds.toLong())
	runLater {
	  op()
	}
  }
}

fun runMuchLaterReturn(d: Duration, op: ()->Unit) {
  sleep(d.inMilliseconds.toLong())
  runLaterReturn {
	op()
  }
}

fun <T> runLaterReturn(s: String, op: ()->T): T {
  val r = runLaterReturn { op() }
  return r
}