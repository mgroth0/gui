package matt.gui.app.warmup

/*the purpose of this is to have consistent performance and profiling expectations. It can be omitted but its best to keep so that I can easily distinguish actionable bottlenecks and not worry about un-actionable internal jvm warmup matt.math.op.matt.obs.math.op.matt.obs.math.double.op.times*/
fun warmupJvmThreading() {
  Thread {

  }.apply {
	isDaemon = true
	start()
  }
}

