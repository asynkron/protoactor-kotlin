package actor.proto.fixture

import actor.proto.mailbox.Dispatcher
import actor.proto.mailbox.Mailbox
import actor.proto.mailbox.MessageInvoker
import actor.proto.mailbox.SystemMessage
import kotlinx.coroutines.experimental.runBlocking

open class TestMailbox : Mailbox {
    private lateinit var _invoker: MessageInvoker
    val userMessages: MutableList<Any> = mutableListOf()
    val systemMessages: MutableList<Any> = mutableListOf()
    override fun postUserMessage(msg: Any) {
        userMessages.add(msg)
        runBlocking { _invoker.invokeUserMessage(msg) }
    }

    override fun postSystemMessage(msg: Any) {
        systemMessages.add(msg)
        runBlocking { _invoker.invokeSystemMessage(msg as SystemMessage) }
    }

    override fun registerHandlers(invoker: MessageInvoker, dispatcher: Dispatcher) {
        _invoker = invoker
    }

    override fun start() {}
}

