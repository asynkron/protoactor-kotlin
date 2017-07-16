package actor.proto

import actor.proto.mailbox.Dispatchers


object EventStream : EventStreamImpl() {
    init {
        subscribe({
            when (it) {
                is DeadLetterEvent -> Logger.logInformation("[DeadLetter] '${it.pid.toShortString()}' got '${it.message.javaClass.name}:${it.message}' from '${it.sender?.toShortString()}'")
            }
        }, Dispatchers.SYNCHRONOUS_DISPATCHER)
    }
}


