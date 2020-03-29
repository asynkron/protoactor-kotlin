package actor.proto.mailbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DefaultMailboxTest {

    @Test
    fun `check that the messages are delivered in the same order with async dispatcher`() {

        val mailbox = newUnboundedMailbox()
        assertEquals(MailboxStatus.IDLE, (mailbox as DefaultMailbox).status())
        val invoker = TestMailboxInvoker()
        mailbox.registerHandlers(invoker, Dispatchers.DEFAULT_DISPATCHER)
        mailbox.postUserMessage("TestUserMessage1")
        mailbox.postUserMessage("TestUserMessage2")
        mailbox.postSystemMessage(SomeSystemMessage(17))
        mailbox.postSystemMessage(SomeSystemMessage(18))
        mailbox.postUserMessage("TestUserMessage3")
        mailbox.postSystemMessage(SomeSystemMessage(19))
        Thread.sleep(500) // let the DefaultDispatcher to call mailbox.run()
        assertEquals("TestUserMessage1", invoker.userMessages[0])
        assertEquals("TestUserMessage2", invoker.userMessages[1])
        assertEquals("TestUserMessage3", invoker.userMessages[2])
        Thread.sleep(100) // let the DefaultDispatcher to call mailbox.run()
        assertEquals(SomeSystemMessage(17), invoker.systemMessages[0])
        assertEquals(SomeSystemMessage(18), invoker.systemMessages[1])
        assertEquals(SomeSystemMessage(19), invoker.systemMessages[2])
    }

    @Test
    fun `check that the messages are delivered in the same order with sync dispatcher`() {
        val mailbox = newUnboundedMailbox()
        assertEquals(MailboxStatus.IDLE, (mailbox as DefaultMailbox).status())
        val invoker = TestMailboxInvoker()
        mailbox.registerHandlers(invoker, Dispatchers.SYNCHRONOUS_DISPATCHER)
        mailbox.postUserMessage("TestUserMessage1")
        mailbox.postUserMessage("TestUserMessage2")
        mailbox.postSystemMessage(SomeSystemMessage(17))
        mailbox.postSystemMessage(SomeSystemMessage(18))
        mailbox.postUserMessage("TestUserMessage3")
        mailbox.postSystemMessage(SomeSystemMessage(19))

        assertEquals("TestUserMessage1", invoker.userMessages[0])
        assertEquals("TestUserMessage2", invoker.userMessages[1])
        assertEquals("TestUserMessage3", invoker.userMessages[2])

        assertEquals(SomeSystemMessage(17), invoker.systemMessages[0])
        assertEquals(SomeSystemMessage(18), invoker.systemMessages[1])
        assertEquals(SomeSystemMessage(19), invoker.systemMessages[2])
    }

    @Test
    fun `check suspension and resumption of user messages`() {
        val mailbox = newUnboundedMailbox()
        assertEquals(MailboxStatus.IDLE, (mailbox as DefaultMailbox).status())
        val invoker = TestMailboxInvoker()
        mailbox.registerHandlers(invoker, Dispatchers.SYNCHRONOUS_DISPATCHER)
        mailbox.postUserMessage("TestUserMessage1")
        mailbox.postUserMessage("TestUserMessage2")
        mailbox.postSystemMessage(SomeSystemMessage(17))
        mailbox.postSystemMessage(SomeSystemMessage(18))
        mailbox.postSystemMessage(SuspendMailbox)
        mailbox.postUserMessage("TestUserMessage3")
        mailbox.postSystemMessage(SomeSystemMessage(19))
        // user messages after suspension should not be delivered
        assertEquals("TestUserMessage1", invoker.userMessages[0])
        assertEquals("TestUserMessage2", invoker.userMessages[1])
        assertEquals(2, invoker.userMessages.size)
        // system messages after suspension should be delivered anyway
        assertEquals(SomeSystemMessage(17), invoker.systemMessages[0])
        assertEquals(SomeSystemMessage(18), invoker.systemMessages[1])
        assertEquals(SuspendMailbox, invoker.systemMessages[2])
        assertEquals(SomeSystemMessage(19), invoker.systemMessages[3])
        assertEquals(4, invoker.systemMessages.size)
        // user messages after resumption should be delivered
        mailbox.postSystemMessage(ResumeMailbox)
        assertEquals("TestUserMessage3", invoker.userMessages[2])
        assertEquals(5, invoker.systemMessages.size)

    }
}

private class TestMailboxInvoker : MessageInvoker {
    var escalatedFailures: MutableList<Exception> = mutableListOf()
    var systemMessages: MutableList<SystemMessage> = mutableListOf()
    var userMessages: MutableList<Any> = mutableListOf()

    override suspend fun invokeSystemMessage(msg: SystemMessage) {
        systemMessages.add(msg)
    }

    override suspend fun invokeUserMessage(msg: Any) {
        userMessages.add(msg)
    }

    override suspend fun escalateFailure(reason: Exception, message: Any) {
        escalatedFailures.add(reason)
    }
}

private data class SomeSystemMessage(val id: Int) : SystemMessage