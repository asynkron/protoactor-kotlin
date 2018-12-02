package actor.proto

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration

class AsyncTimer(private val callback: () -> Unit, private val tick: Duration) {
    private var job: Job? = null
    fun start() {
        job = GlobalScope.launch {
            while (true) {
                delay(tick.toMillis())
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
