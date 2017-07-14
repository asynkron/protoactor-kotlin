package proto.actor

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.time.Duration
import java.util.concurrent.TimeUnit

class AsyncTimer(private val callback: () -> Unit, private val tick: Duration) {
    private var job : Job? = null
    fun start() {
        job = launch(CommonPool) {
            while (true) {
                delay(tick.toNanos(), TimeUnit.NANOSECONDS)
                callback()
            }
        }
    }
    fun stop() {
        job?.cancel()
    }

    fun reset() {
        stop()
        start()
    }
}