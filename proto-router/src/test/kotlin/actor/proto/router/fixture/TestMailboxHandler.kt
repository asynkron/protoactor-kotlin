package actor.proto.router.fixture

import actor.proto.mailbox.Dispatcher
import actor.proto.mailbox.Mailbox
import actor.proto.mailbox.MessageInvoker
import actor.proto.mailbox.SystemMessage
import kotlinx.coroutines.runBlocking


class TestMailboxHandler : MessageInvoker, Dispatcher {
    private var escalatedFailures: MutableList<Exception> = mutableListOf()
    override suspend fun invokeSystemMessage(msg: SystemMessage) {
        return //(TestMessagemsg).taskCompletionSource.task
    }

    override suspend fun invokeUserMessage(msg: Any) {
        return //(msg as TestMessage).taskCompletionSource.
    }

    override suspend fun escalateFailure(reason: Exception, message: Any) {
        escalatedFailures.add(reason)
    }

    override var throughput: Int = 10
    override fun schedule(mailbox:Mailbox) {
        runBlocking { mailbox.run() }
    }
}

