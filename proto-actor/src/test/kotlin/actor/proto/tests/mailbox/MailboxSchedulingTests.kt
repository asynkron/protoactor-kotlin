//package proto.mailbox.tests
//
//import actor.proto.fixture.TestMailboxHandler
//import actor.proto.fixture.TestMessage
//import actor.proto.mailbox.DefaultMailbox
//import actor.proto.mailbox.MailboxStatus
//import actor.proto.mailbox.UnboundedMailboxQueue
//import kotlin.test.assertFalse
//import kotlin.test.assertTrue
//
//open class MailboxSchedulingTests {
//    fun givenNonCompletedUserMessage_ShouldHaltProcessingUntilCompletion () {
//        val mailboxHandler = TestMailboxHandler()
//        val userMailbox = UnboundedMailboxQueue()
//        val systemMessages = UnboundedMailboxQueue()
//        val mailbox = DefaultMailbox(systemMessages, userMailbox)
//        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
//        val msg1 = TestMessage()
//        val msg2 = TestMessage()
//        mailbox.postUserMessage(msg1)
//        mailbox.postUserMessage(msg2)
//        assertTrue(userMailbox.hasMessages, "Mailbox should not have processed msg2 because processing of msg1 is not completed.")
//        msg1.taskCompletionSource.setResult(0)
//
//        assertFalse(userMailbox.hasMessages, "Mailbox should have processed msg2 because processing of msg1 is completed.")
//    }
//    fun givenCompletedUserMessage_ShouldContinueProcessing () {
//        val mailboxHandler = TestMailboxHandler()
//        val userMailbox = UnboundedMailboxQueue()
//        val systemMessages = UnboundedMailboxQueue()
//        val mailbox = DefaultMailbox(systemMessages, userMailbox)
//        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
//        val msg1 = TestMessage()
//        val msg2 = TestMessage()
//        msg1.taskCompletionSource.setResult(0)
//        msg2.taskCompletionSource.setResult(0)
//        mailbox.postUserMessage(msg1)
//        mailbox.postUserMessage(msg2)
//        assertFalse(userMailbox.hasMessages, "Mailbox should have processed both messages because they were already completed.")
//    }
//    fun givenNonCompletedSystemMessage_ShouldHaltProcessingUntilCompletion () {
//        val mailboxHandler = TestMailboxHandler()
//        val userMailbox = UnboundedMailboxQueue()
//        val systemMessages = UnboundedMailboxQueue()
//        val mailbox = DefaultMailbox(systemMessages, userMailbox)
//        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
//        val msg1 = TestMessage()
//        val msg2 = TestMessage()
//        mailbox.postSystemMessage(msg1)
//        mailbox.postSystemMessage(msg2)
//        assertTrue(systemMessages.hasMessages, "Mailbox should not have processed msg2 because processing of msg1 is not completed.")
//        msg1.taskCompletionSource.setResult(0)
//
//        assertFalse(systemMessages.hasMessages, "Mailbox should have processed msg2 because processing of msg1 is completed.")
//    }
//    fun givenCompletedSystemMessage_ShouldContinueProcessing () {
//        val mailboxHandler = TestMailboxHandler()
//        val userMailbox = UnboundedMailboxQueue()
//        val systemMessages = UnboundedMailboxQueue()
//        val mailbox = DefaultMailbox(systemMessages, userMailbox)
//        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
//        val msg1 = TestMessage()
//        val msg2 = TestMessage()
//        msg1.taskCompletionSource.setResult(0)
//        msg2.taskCompletionSource.setResult(0)
//        mailbox.postSystemMessage(msg1)
//        mailbox.postSystemMessage(msg2)
//        assertFalse(systemMessages.hasMessages, "Mailbox should have processed both messages because they were already completed.")
//    }
//    fun givenNonCompletedUserMessage_ShouldSetMailboxToIdleAfterCompletion () {
//        val mailboxHandler = TestMailboxHandler()
//        val userMailbox = UnboundedMailboxQueue()
//        val systemMessages = UnboundedMailboxQueue()
//        val mailbox = DefaultMailbox(systemMessages, userMailbox)
//        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
//        val msg1 = TestMessage()
//        mailbox.postUserMessage(msg1)
//        msg1.taskCompletionSource.setResult(0)
//        Thread.sleep(500)
//        assertTrue(mailbox.status.get() == MailboxStatus.IDLE, "Mailbox should be set back to IDLE after completion of message.")
//    }
//}
//
