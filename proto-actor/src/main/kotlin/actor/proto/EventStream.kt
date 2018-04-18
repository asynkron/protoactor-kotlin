package actor.proto

import actor.proto.mailbox.Dispatchers
import mu.KotlinLogging


private val logger = KotlinLogging.logger {}
object EventStream : EventStreamImpl() {
    init {
        subscribe({
            when (it) {
                is DeadLetterEvent -> logger.warn("[DeadLetter] '${it.pid.toShortString()}' got '${it.message.javaClass.name}:${it.message}' from '${it.sender?.toShortString()}'")
            }
        }, Dispatchers.SYNCHRONOUS_DISPATCHER)
    }
}


