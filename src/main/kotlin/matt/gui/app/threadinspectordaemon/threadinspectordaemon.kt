package matt.gui.app.threadinspectordaemon

import matt.async.thread.aliveNonDaemonThreads
import matt.log.taball
import kotlin.time.Duration.Companion.seconds

internal object ThreadInspectorDaemon : Thread() {
    init {
        isDaemon = true
        name = "Thread Inspector Daemon"
    }

    override fun run() {
        matt.time.dur.sleep(5.seconds)
        while (true) {
            taball("aliveNonDaemonThreads", aliveNonDaemonThreads())
            matt.time.dur.sleep(3.seconds)
        }
    }
}
