package actor.proto.mailbox

import actor.proto.fixture.TestMailboxHandler
import actor.proto.fixture.TestMessage
import actor.proto.fixture.TestSystemMessage
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import java.util.concurrent.ExecutionException
import kotlin.test.assertEquals

open class EscalateFailureTests {
    fun Iterable<Exception>.singleExecutionException(): ExecutionException {
        return this.filterIsInstance<java.util.concurrent.ExecutionException>()
                .single()
    }

    @Test
    fun `Should escalate failure when a User message is completed exceptionally`() {
        val mailboxHandler = TestMailboxHandler()
        val mailbox = newUnboundedMailbox()
        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
        val message = TestMessage()
        val taskException = Exception("cannot complete future")
        message.taskCompletionSource.completeExceptionally(taskException)

        mailbox.postUserMessage(message)

        val escalatedFailure = mailboxHandler.escalatedFailures.singleExecutionException()
        assertEquals(taskException, escalatedFailure.cause)
    }

    @Test
    fun `Should escalate failure when a System message is completed exceptionally`() {
        val mailboxHandler = TestMailboxHandler()
        val mailbox = newUnboundedMailbox()
        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
        val systemMessage = TestSystemMessage()
        val taskException = Exception()

        systemMessage.taskCompletionSource.completeExceptionally(taskException)
        mailbox.postSystemMessage(systemMessage)

        val escalatedFailure = mailboxHandler.escalatedFailures.singleExecutionException()
        assertEquals(taskException, escalatedFailure.cause)
    }

    @Test
    fun `Should escalate failure when waiting for a User message to be completed exceptionally`() {
        val mailboxHandler = TestMailboxHandler()
        val mailbox = newUnboundedMailbox()
        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
        val message = TestMessage()
        val taskException = Exception()

        runBlocking {
            val job = async(CommonPool) {
                mailbox.postUserMessage(message)
            }

            message.taskCompletionSource.completeExceptionally(taskException)
            job.await()
        }

        val escalatedFailure = mailboxHandler.escalatedFailures.singleExecutionException()
        assertEquals(taskException, escalatedFailure.cause)
    }

    @Test
    fun `Should escalate failure when waiting for a System message to be completed exceptionally`() {
        val mailboxHandler = TestMailboxHandler()
        val mailbox = newUnboundedMailbox()
        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
        val systemMessage = TestSystemMessage()
        val taskException = Exception()

        runBlocking {
            val job = async(CommonPool) {
                mailbox.postSystemMessage(systemMessage)
            }

            systemMessage.taskCompletionSource.completeExceptionally(taskException)
            job.await()
        }

        val escalatedFailure = mailboxHandler.escalatedFailures.singleExecutionException()
        assertEquals(taskException, escalatedFailure.cause)
    }
}
