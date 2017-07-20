//package proto.mailbox.tests
//
//import actor.proto.fixture.TestMailboxHandler
//import actor.proto.fixture.TestMailboxStatistics
//import actor.proto.fixture.TestMessage
//import actor.proto.mailbox.DefaultMailbox
//import actor.proto.mailbox.UnboundedMailboxQueue
//import kotlin.test.assertFalse
//import kotlin.test.assertTrue
//
//open class MailboxStatisticsTests {
//    fun givenMailboxStarted_ShouldInvokeMailboxStarted () {
//        val mailboxHandler = TestMailboxHandler()
//        val userMailbox = UnboundedMailboxQueue()
//        val systemMessages = UnboundedMailboxQueue()
//        val mailboxStatistics = TestMailboxStatistics()
//        val mailbox = DefaultMailbox(systemMessages, userMailbox, arrayOf(mailboxStatistics))
//        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
//        mailbox.start()
//        assertTrue (mailboxStatistics.stats.contains("Started"))
//    }
//    fun givenUserMessage_ShouldInvokeMessagePosted () {
//        val mailboxHandler = TestMailboxHandler()
//        val userMailbox = UnboundedMailboxQueue()
//        val systemMessages = UnboundedMailboxQueue()
//        val mailboxStatistics = TestMailboxStatistics()
//        val mailbox = DefaultMailbox(systemMessages, userMailbox, arrayOf(mailboxStatistics))
//        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
//        val msg1 = TestMessage()
//        mailbox.postUserMessage(msg1)
//        assertTrue (mailboxStatistics.posted.contains(msg1))
//    }
//    fun givenSystemMessage_ShouldInvokeMessagePosted () {
//        val mailboxHandler = TestMailboxHandler()
//        val userMailbox = UnboundedMailboxQueue()
//        val systemMessages = UnboundedMailboxQueue()
//        val mailboxStatistics = TestMailboxStatistics()
//        val mailbox = DefaultMailbox(systemMessages, userMailbox, arrayOf(mailboxStatistics))
//        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
//        val msg1 = TestMessage()
//        mailbox.postSystemMessage(msg1)
//        assertTrue (mailboxStatistics.posted.contains(msg1))
//    }
//    fun givenNonCompletedUserMessage_ShouldInvokeMessageReceivedAfterCompletion () {
//        val mailboxHandler = TestMailboxHandler()
//        val userMailbox = UnboundedMailboxQueue()
//        val systemMessages = UnboundedMailboxQueue()
//        val mailboxStatistics = TestMailboxStatistics()
//        val mailbox = DefaultMailbox(systemMessages, userMailbox, arrayOf(mailboxStatistics))
//        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
//        val msg1 = TestMessage()
//        mailbox.postUserMessage(msg1)
//        assertFalse (mailboxStatistics.received.contains(msg1))
//        //msg1.taskCompletionSource.setResult(0)
//        //Thread.sleep(10)
//        assertTrue (mailboxStatistics.posted.contains(msg1))
//    }
//    fun givenCompletedUserMessage_ShouldInvokeMessageReceivedImmediately () {
//        val mailboxHandler = TestMailboxHandler()
//        val userMailbox = UnboundedMailboxQueue()
//        val systemMessages = UnboundedMailboxQueue()
//        val mailboxStatistics = TestMailboxStatistics()
//        val mailbox = DefaultMailbox(systemMessages, userMailbox, arrayOf(mailboxStatistics))
//        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
//        val msg1 = TestMessage()
//        msg1.taskCompletionSource.setResult(0)
//        mailbox.postUserMessage(msg1)
//        assertTrue (mailboxStatistics.posted.contains(msg1))
//    }
//    fun givenNonCompletedUserMessageThrewException_ShouldNotInvokeMessageReceived () {
//        val mailboxHandler = TestMailboxHandler()
//        val userMailbox = UnboundedMailboxQueue()
//        val systemMessages = UnboundedMailboxQueue()
//        val mailboxStatistics = TestMailboxStatistics()
//        val mailbox = DefaultMailbox(systemMessages, userMailbox, arrayOf(mailboxStatistics))
//        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
//        val msg1 = TestMessage()
//        mailbox.postUserMessage(msg1)
//        msg1.taskCompletionSource.setException(Exception())
//        Thread.sleep(10)
//        assertFalse (mailboxStatistics.received.contains(msg1))
//    }
//    fun givenCompletedUserMessageThrewException_ShouldNotInvokeMessageReceived () {
//        val mailboxHandler = TestMailboxHandler()
//        val userMailbox = UnboundedMailboxQueue()
//        val systemMessages = UnboundedMailboxQueue()
//        val mailboxStatistics = TestMailboxStatistics()
//        val mailbox = DefaultMailbox(systemMessages, userMailbox, arrayOf(mailboxStatistics))
//        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
//        val msg1 = TestMessage()
//        msg1.taskCompletionSource.setException(Exception())
//        mailbox.postUserMessage(msg1)
//        assertFalse (mailboxStatistics.received.contains(msg1))
//    }
//    fun givenNonCompletedSystemMessageThrewException_ShouldNotInvokeMessageReceived () {
//        val mailboxHandler = TestMailboxHandler()
//        val userMailbox = UnboundedMailboxQueue()
//        val systemMessages = UnboundedMailboxQueue()
//        val mailboxStatistics = TestMailboxStatistics()
//        val mailbox = DefaultMailbox(systemMessages, userMailbox, arrayOf(mailboxStatistics))
//        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
//        val msg1 = TestMessage()
//        mailbox.postSystemMessage(msg1)
//        msg1.taskCompletionSource.setException(Exception())
//        Thread.sleep(10)
//        assertFalse (mailboxStatistics.received.contains(msg1))
//    }
//    fun givenCompletedSystemMessageThrewException_ShouldNotInvokeMessageReceived () {
//        val mailboxHandler = TestMailboxHandler()
//        val userMailbox = UnboundedMailboxQueue()
//        val systemMessages = UnboundedMailboxQueue()
//        val mailboxStatistics = TestMailboxStatistics()
//        val mailbox = DefaultMailbox(systemMessages, userMailbox, arrayOf(mailboxStatistics))
//        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
//        val msg1 = TestMessage()
//        msg1.taskCompletionSource.setException(Exception())
//        mailbox.postSystemMessage(msg1)
//        assertFalse (mailboxStatistics.received.contains(msg1))
//    }
//}
//
