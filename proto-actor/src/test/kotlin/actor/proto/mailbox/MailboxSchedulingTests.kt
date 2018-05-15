package actor.proto.mailbox

import actor.proto.fixture.TestMailboxHandler
import actor.proto.fixture.TestMessage
import actor.proto.fixture.TestSystemMessage
import org.junit.Test
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

fun unboundedQueue(): MailboxQueue = ConcurrentLinkedQueue<Any>()
private fun MailboxQueue.hasMessages(): Boolean = !this.isEmpty()

open class MailboxSchedulingTests {
    /*
    @Test
    fun givenNonCompletedUserMessage_ShouldHaltProcessingUntilCompletion () {
        newUnboundedMailbox()
        val mailboxHandler = TestMailboxHandler()
        val userMailbox = unboundedQueue()
        val systemMessages = unboundedQueue()

        val msg1 = TestMessage()
        val msg2 = TestMessage()

        async {
            val mailbox = DefaultMailbox(systemMessages, userMailbox)
            mailbox.registerHandlers(mailboxHandler, mailboxHandler)

            mailbox.postUserMessage(msg1)
            mailbox.postUserMessage(msg2)
        }.

        runBlocking {
            delay(1000L)
        }

        assertTrue(userMailbox.hasMessages(), "Mailbox should not have processed msg2 because processing of msg1 is not completed.")
        msg1.taskCompletionSource.complete(0)
        msg1.taskCompletionSource.complete(1)

        assertFalse(userMailbox.hasMessages(), "Mailbox should have processed msg2 because processing of msg1 is completed.")
    }
    */

    @Test
    fun givenCompletedUserMessage_ShouldContinueProcessing() {
        val mailboxHandler = TestMailboxHandler()
        val userMailbox = unboundedQueue()
        val systemMessages = unboundedQueue()
        val mailbox = DefaultMailbox(systemMessages, userMailbox)
        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
        val msg1 = TestMessage()
        val msg2 = TestMessage()
        mailbox.postUserMessage(msg1)
        mailbox.postUserMessage(msg2)

        assertEquals(1, msg1.taskCompletionSource.get())
        assertEquals(1, msg2.taskCompletionSource.get())
        assertFalse(userMailbox.hasMessages(), "Mailbox should have processed both messages because they were already completed.")
    }

    /*
        @Test fun givenNonCompletedSystemMessage_ShouldHaltProcessingUntilCompletion () {
            val mailboxHandler = TestMailboxHandler()
            val userMailbox = unboundedQueue()
            val systemMessages = unboundedQueue()
            val mailbox = DefaultMailbox(systemMessages, userMailbox)
            mailbox.registerHandlers(mailboxHandler, mailboxHandler)
            val msg1 = TestMessage()
            val msg2 = TestMessage()
            mailbox.postSystemMessage(msg1)
            mailbox.postSystemMessage(msg2)
            assertTrue(systemMessages.hasMessages, "Mailbox should not have processed msg2 because processing of msg1 is not completed.")
            msg1.taskCompletionSource.setResult(0)

            assertFalse(systemMessages.hasMessages, "Mailbox should have processed msg2 because processing of msg1 is completed.")
        }
    */
    @Test
    fun givenCompletedSystemMessage_ShouldContinueProcessing() {
        val mailboxHandler = TestMailboxHandler()
        val userMailbox = unboundedQueue()
        val systemMessages = unboundedQueue()
        val mailbox = DefaultMailbox(systemMessages, userMailbox)
        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
        val msg1 = TestSystemMessage()
        val msg2 = TestSystemMessage()
        mailbox.postSystemMessage(msg1)
        mailbox.postSystemMessage(msg2)

        assertEquals(1, msg1.taskCompletionSource.get())
        assertEquals(1, msg2.taskCompletionSource.get())
        assertFalse(systemMessages.hasMessages(), "Mailbox should have processed both messages because they were already completed.")
    }

    @Test
    fun givenNonCompletedUserMessage_ShouldSetMailboxToIdleAfterCompletion() {
        val mailboxHandler = TestMailboxHandler()
        val userMailbox = unboundedQueue()
        val systemMessages = unboundedQueue()
        val mailbox = DefaultMailbox(systemMessages, userMailbox)
        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
        val msg1 = TestMessage()

        mailbox.postUserMessage(msg1)

        assertEquals(1, msg1.taskCompletionSource.get())
        assertTrue(mailbox.status() == MailboxStatus.IDLE, "Mailbox should be set back to IDLE after completion of message.")
    }
}
