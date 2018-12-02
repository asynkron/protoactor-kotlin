package actor.proto.fixture

import actor.proto.mailbox.Dispatcher
import actor.proto.mailbox.Mailbox
import actor.proto.mailbox.MessageInvoker
import actor.proto.mailbox.SystemMessage
import kotlinx.coroutines.runBlocking

class TestMailboxHandler : MessageInvoker, Dispatcher {
    var escalatedFailures: MutableList<Exception> = mutableListOf()

    override suspend fun invokeSystemMessage(msg: SystemMessage) {
        when (msg) {
            is TestSystemMessage -> msg.taskCompletionSource.complete(1)
            is ExceptionalSystemMessage -> throw msg.exception
        }
    }

    override suspend fun invokeUserMessage(msg: Any) {
        when (msg) {
            is TestMessage -> msg.taskCompletionSource.complete(1)
            is ExceptionalMessage -> throw msg.exception
        }
    }

    override suspend fun escalateFailure(reason: Exception, message: Any) {
        escalatedFailures.add(reason)
    }

    override var throughput: Int = 10
    override fun schedule(mailbox: Mailbox) {
        runBlocking { mailbox.run() }
    }
}

