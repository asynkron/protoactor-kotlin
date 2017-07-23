//package proto.mailbox.tests
//
//import actor.proto.fixture.TestMailboxHandler
//import actor.proto.fixture.TestMessage
//import kotlin.test.assertEquals
//import kotlin.test.assertTrue
//
//open class EscalateFailureTests {
//    fun givenCompletedUserMessageTaskThrewException_ShouldEscalateFailure () {
//        val mailboxHandler = TestMailboxHandler()
//        val mailbox = UnboundedMailbox.create()
//        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
//        val msg1 = TestMessage()
//        val taskException = Exception()
//        msg1.taskCompletionSource.setException(taskException)
//        mailbox.postUserMessage(msg1)
//        assertEquals(1, mailboxHandler.escalatedFailures.count())
//        val e = assertTrue (mailboxHandler.escalatedFailures[0] is AggregateException)
//        assertEquals(taskException, e.innerException)
//    }
//    fun givenCompletedSystemMessageTaskThrewException_ShouldEscalateFailure () {
//        val mailboxHandler = TestMailboxHandler()
//        val mailbox = UnboundedMailbox.create()
//        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
//        val msg1 = TestMessage()
//        val taskException = Exception()
//        msg1.taskCompletionSource.setException(taskException)
//        mailbox.postSystemMessage(msg1)
//        assertEquals(1, mailboxHandler.escalatedFailures.count())
//        val e = assertTrue (mailboxHandler.escalatedFailures[0] is AggregateException)
//        assertEquals(taskException, e.innerException)
//    }
//    fun givenNonCompletedUserMessageTaskThrewException_ShouldEscalateFailure () {
//        val mailboxHandler = TestMailboxHandler()
//        val mailbox = UnboundedMailbox.create()
//        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
//        val msg1 = TestMessage()
//        mailbox.postUserMessage(msg1)
//        val taskException = Exception()
//        msg1.taskCompletionSource.setException(taskException)
//        Thread.sleep(500)
//        assertEquals(1, mailboxHandler.escalatedFailures.count())
//        val e = assertTrue (mailboxHandler.escalatedFailures[0] is AggregateException)
//        assertEquals(taskException, e.innerException)
//    }
//    fun givenNonCompletedSystemMessageTaskThrewException_ShouldEscalateFailure () {
//        val mailboxHandler = TestMailboxHandler()
//        val mailbox = UnboundedMailbox.create()
//        mailbox.registerHandlers(mailboxHandler, mailboxHandler)
//        val msg1 = TestMessage()
//        mailbox.postSystemMessage(msg1)
//        val taskException = Exception()
//        msg1.taskCompletionSource.setException(taskException)
//        Thread.sleep(500)
//        assertEquals(1, mailboxHandler.escalatedFailures.count())
//        val e = assertTrue (mailboxHandler.escalatedFailures[0] is AggregateException)
//        assertEquals(taskException, e.innerException)
//    }
//}
//
