//package actor.proto.mailbox
//
//import kotlinx.coroutines.experimental.CoroutineDispatcher
//import net.openhft.affinity.AffinityLock
//import net.openhft.affinity.AffinityStrategies
//import net.openhft.affinity.AffinityThreadFactory
//import org.jctools.queues.MpscUnboundedArrayQueue
//import java.util.*
//import java.util.concurrent.ConcurrentLinkedQueue
//import java.util.concurrent.locks.LockSupport
//import kotlin.coroutines.experimental.CoroutineContext
//import kotlin.coroutines.experimental.startCoroutine
//
//class AffinityDispatcher(override var throughput: Int) : Dispatcher{
//    private val dispatchers : Array<ThreadDispatcher> = (0 until Runtime.getRuntime().availableProcessors()).map { i -> ThreadDispatcher(i) }.toTypedArray()
//    private val continuations = dispatchers.map {EmptyContinuation(it)}.toTypedArray()
//    override fun schedule(mailbox: Mailbox) {
//        val hash = mailbox.hashCode()
//        val index = hash % continuations.count()
//        val continuation = continuations[index]
//
//        val runner: suspend () -> Unit = {mailbox.run()}
//        runner.startCoroutine(continuation)
//    }
//
//    fun stop(){
//        dispatchers.forEach { it.stop() }
//    }
//}
//val f = AffinityThreadFactory("threads",true,AffinityStrategies.SAME_CORE)
//class ThreadDispatcher(index : Int) : CoroutineDispatcher() {
//    private val queue : Queue<Runnable> = MpscUnboundedArrayQueue(1000)
//    private val thread : Thread =Thread(this::run)
//    @Volatile private var running = true
//    init{
//        thread.isDaemon = true
//        thread.priority = Thread.MAX_PRIORITY
//        thread.start()
//    }
//
//    fun stop(){
//        running = false
//
//    }
//    private fun run() {
//        while(running) {
//            spin@for (i in 0..100) {
//                while(true) {
//                    val w = queue.poll() ?: break@spin
//                    w.run()
//                }
//            }
//            LockSupport.parkNanos(1000000)
//        }
//    }
//    override fun dispatch(context: CoroutineContext, block: Runnable) {
//        queue.add(block)
//    }
//}