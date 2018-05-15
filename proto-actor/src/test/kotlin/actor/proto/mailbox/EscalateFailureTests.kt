package actor.proto.mailbox

import actor.proto.fixture.ExceptionalMessage
import actor.proto.fixture.ExceptionalSystemMessage
import actor.proto.fixture.TestMailboxHandler
import org.junit.Test
import kotlin.test.assertEquals

open class EscalateFailureTests {
    private inline fun <reified T> Iterable<Exception>.singleExceptionOf(): T {
        return this.filterIsInstance<T>()
                .single()
    }

    class MessageHandlerTestException : Exception("Handler Exception")

    @Test
    fun `Should escalate failure when a User message is completed exceptionally`() {
        val mailboxHandler = TestMailboxHandler()
        val mailbox = newUnboundedMailbox()
        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
        val taskException = MessageHandlerTestException()
        val message = ExceptionalMessage(taskException)

        mailbox.postUserMessage(message)

        val escalatedFailure = mailboxHandler.escalatedFailures
                .singleExceptionOf<MessageHandlerTestException>()
        assertEquals(taskException, escalatedFailure)
    }

    @Test
    fun `Should escalate failure when a System message is completed exceptionally`() {
        val mailboxHandler = TestMailboxHandler()
        val mailbox = newUnboundedMailbox()
        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
        val taskException = MessageHandlerTestException()
        val systemMessage = ExceptionalSystemMessage(taskException)

        mailbox.postSystemMessage(systemMessage)

        val escalatedFailure = mailboxHandler.escalatedFailures
                .singleExceptionOf<MessageHandlerTestException>()
        assertEquals(taskException, escalatedFailure)
    }

    @Test
    fun `Should escalate failure when waiting for a User message to be completed exceptionally`() {
        val mailboxHandler = TestMailboxHandler()
        val mailbox = newUnboundedMailbox()
        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
        val taskException = MessageHandlerTestException()

        val message = ExceptionalMessage(taskException)
        mailbox.postUserMessage(message)

        val escalatedFailures = mailboxHandler.escalatedFailures
        val escalatedFailure = escalatedFailures
                .singleExceptionOf<MessageHandlerTestException>()
        assertEquals(taskException, escalatedFailure)
    }

    @Test
    fun `Should escalate failure when waiting for a System message to be completed exceptionally`() {
        val mailboxHandler = TestMailboxHandler()
        val mailbox = newUnboundedMailbox()
        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
        val taskException = MessageHandlerTestException()
        val systemMessage = ExceptionalSystemMessage(taskException)

        mailbox.postSystemMessage(systemMessage)

        val escalatedFailure = mailboxHandler.escalatedFailures
                .singleExceptionOf<MessageHandlerTestException>()
        assertEquals(taskException, escalatedFailure)
    }
}
