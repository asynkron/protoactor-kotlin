package actor.proto.mailbox

import kotlinx.coroutines.experimental.CoroutineDispatcher
import org.jctools.queues.MpscUnboundedArrayQueue
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.LockSupport
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.startCoroutine

class AffinityDispatcher(threads : Int, override var throughput: Int) : Dispatcher{
    private val dispatchers : Array<ThreadDispatcher> = (0 until threads).map { ThreadDispatcher() }.toTypedArray()
    private val continuations = dispatchers.map {EmptyContinuation(it)}.toTypedArray()
    override fun schedule(mailbox: Mailbox) {
        val hash = mailbox.hashCode()
        val index = hash % continuations.count()
        val continuation = continuations[index]

        val runner: suspend () -> Unit = {mailbox.run()}
        runner.startCoroutine(continuation)
    }

    fun stop(){
        dispatchers.forEach { it.stop() }
    }
}

class ThreadDispatcher : CoroutineDispatcher() {
    private val queue : Queue<Runnable> = MpscUnboundedArrayQueue(1000)
    private val thread : Thread = Thread(this::run)
    @Volatile private var running = true
    init{
        thread.start()
    }

    fun stop(){
        running = false
    }
    private fun run() {
        while(running) {
            spin@for (i in 0..100) {
                while(true) {
                    val w = queue.poll() ?: break@spin
                    w.run()
                }
            }
            LockSupport.parkNanos(1000000)
        }
    }
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        queue.add(block)
    }
}