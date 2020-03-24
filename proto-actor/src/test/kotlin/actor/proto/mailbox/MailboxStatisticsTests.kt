package actor.proto.mailbox

import actor.proto.fixture.ExceptionalMessage
import actor.proto.fixture.ExceptionalSystemMessage
import actor.proto.fixture.TestMailboxHandler
import actor.proto.fixture.TestMailboxStatistics
import actor.proto.fixture.TestMessage
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

open class MailboxStatisticsTests {
    @Test
    fun givenMailboxStarted_ShouldInvokeMailboxStarted() {
        val mailboxHandler = TestMailboxHandler()
        val userMailbox = unboundedQueue()
        val systemMessages = unboundedQueue()
        val mailboxStatistics = TestMailboxStatistics()
        val mailbox = DefaultMailbox(systemMessages, userMailbox, arrayOf(mailboxStatistics))
        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
        mailbox.start()
        assertTrue(mailboxStatistics.stats.contains("Started"))
    }

    @Test
    fun givenUserMessage_ShouldInvokeMessagePosted() {
        val mailboxHandler = TestMailboxHandler()
        val userMailbox = unboundedQueue()
        val systemMessages = unboundedQueue()
        val mailboxStatistics = TestMailboxStatistics()
        val mailbox = DefaultMailbox(systemMessages, userMailbox, arrayOf(mailboxStatistics))
        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
        val msg1 = TestMessage()
        mailbox.postUserMessage(msg1)
        assertTrue(mailboxStatistics.posted.contains(msg1))
    }

    @Test
    fun givenSystemMessage_ShouldInvokeMessagePosted() {
        val mailboxHandler = TestMailboxHandler()
        val userMailbox = unboundedQueue()
        val systemMessages = unboundedQueue()
        val mailboxStatistics = TestMailboxStatistics()
        val mailbox = DefaultMailbox(systemMessages, userMailbox, arrayOf(mailboxStatistics))
        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
        val msg1 = TestMessage()
        mailbox.postSystemMessage(msg1)
        assertTrue(mailboxStatistics.posted.contains(msg1))
    }

    @Test
    fun givenNonCompletedUserMessage_ShouldInvokeMessageReceivedAfterCompletion() {
        val mailboxHandler = TestMailboxHandler()
        val userMailbox = unboundedQueue()
        val systemMessages = unboundedQueue()
        val mailboxStatistics = TestMailboxStatistics()
        val mailbox = DefaultMailbox(systemMessages, userMailbox, arrayOf(mailboxStatistics))
        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
        val msg1 = TestMessage()
        mailbox.postUserMessage(msg1)
        assertTrue(mailboxStatistics.received.contains(msg1))
        assertTrue(mailboxStatistics.posted.contains(msg1))
    }

    @Test
    fun givenCompletedUserMessage_ShouldInvokeMessageReceivedImmediately() {
        val mailboxHandler = TestMailboxHandler()
        val userMailbox = unboundedQueue()
        val systemMessages = unboundedQueue()
        val mailboxStatistics = TestMailboxStatistics()
        val mailbox = DefaultMailbox(systemMessages, userMailbox, arrayOf(mailboxStatistics))
        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
        val msg1 = TestMessage()
        mailbox.postUserMessage(msg1)
        assertTrue(mailboxStatistics.posted.contains(msg1))
    }

    @Test
    fun givenNonCompletedUserMessageThrewException_ShouldNotInvokeMessageReceived() {
        val mailboxHandler = TestMailboxHandler()
        val userMailbox = unboundedQueue()
        val systemMessages = unboundedQueue()
        val mailboxStatistics = TestMailboxStatistics()
        val mailbox = DefaultMailbox(systemMessages, userMailbox, arrayOf(mailboxStatistics))
        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
        val msg1 = ExceptionalMessage(Exception())
        mailbox.postUserMessage(msg1)
        Thread.sleep(10)
        assertFalse(mailboxStatistics.received.contains(msg1))
    }

    @Test
    fun givenCompletedUserMessageThrewException_ShouldNotInvokeMessageReceived() {
        val mailboxHandler = TestMailboxHandler()
        val userMailbox = unboundedQueue()
        val systemMessages = unboundedQueue()
        val mailboxStatistics = TestMailboxStatistics()
        val mailbox = DefaultMailbox(systemMessages, userMailbox, arrayOf(mailboxStatistics))
        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
        val msg1 = ExceptionalMessage(Exception());
        mailbox.postUserMessage(msg1)
        assertFalse(mailboxStatistics.received.contains(msg1))
    }

    @Test
    fun givenNonCompletedSystemMessageThrewException_ShouldNotInvokeMessageReceived() {
        val mailboxHandler = TestMailboxHandler()
        val userMailbox = unboundedQueue()
        val systemMessages = unboundedQueue()
        val mailboxStatistics = TestMailboxStatistics()
        val mailbox = DefaultMailbox(systemMessages, userMailbox, arrayOf(mailboxStatistics))
        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
        val msg1 = ExceptionalSystemMessage(Exception())
        mailbox.postSystemMessage(msg1)
        Thread.sleep(10)
        assertFalse(mailboxStatistics.received.contains(msg1))
    }

    @Test
    fun givenCompletedSystemMessageThrewException_ShouldNotInvokeMessageReceived() {
        val mailboxHandler = TestMailboxHandler()
        val userMailbox = unboundedQueue()
        val systemMessages = unboundedQueue()
        val mailboxStatistics = TestMailboxStatistics()
        val mailbox = DefaultMailbox(systemMessages, userMailbox, arrayOf(mailboxStatistics))
        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
        val msg1 = ExceptionalSystemMessage(Exception())
        mailbox.postSystemMessage(msg1)
        assertFalse(mailboxStatistics.received.contains(msg1))
    }
}
