package actor.proto.mailbox

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import org.awaitility.Awaitility
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class DefaultDispatcherTest {

    @Test
    fun coroutineScopeTest() {
        val mailbox1 = TestMailbox(100, true)
        val mailbox2 = TestMailbox(300, false)
        val mailbox3 = TestMailbox(100, false)

        val dispatcher1 = DefaultDispatcher()
        val threadPool = Executors.newWorkStealingPool().asCoroutineDispatcher()
        val dispatcher2 = DefaultDispatcher(threadPool, 300)

        dispatcher1.schedule(mailbox1)
        dispatcher1.schedule(mailbox2)
        dispatcher2.schedule(mailbox3)


        Awaitility.ignoreExceptionsByDefault()
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            assertTrue(mailbox1.completedRunning)
            //if the exception in mailbox1 causes all child coroutines to cancel, then this will fail
            assertTrue(mailbox2.completedRunning)
            //mailboxes using same dispatcher should have same coroutine dispatcher
            assertSame(mailbox1.runInContext?.get(ContinuationInterceptor.Key), mailbox2.runInContext?.get(ContinuationInterceptor.Key))
            //but mailboxes with a different dispatcher have a different coroutine dispatcher
            assertNotSame(mailbox1.runInContext?.get(ContinuationInterceptor.Key), mailbox3.runInContext?.get(ContinuationInterceptor.Key))
        }
    }
}


class TestMailbox(private val millis: Long, private val exception: Boolean) : Mailbox {

    var runInContext: CoroutineContext? = null
    var completedRunning : Boolean = false

    override fun postSystemMessage(msg: Any) {

    }

    override fun registerHandlers(invoker: MessageInvoker, dispatcher: Dispatcher) {

    }

    override fun start() {

    }

    override suspend fun run() {
        runInContext = coroutineContext
        delay(millis)
        completedRunning = true
        if (exception) throw Exception()
    }

    override fun postUserMessage(msg: Any) {

    }

}