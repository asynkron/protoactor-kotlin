package proto.actor

import proto.mailbox.Dispatchers

object EventStream : EventStreamImpl<Any>() {
    init {
        subscribe({
            if (it is DeadLetterEvent) {
//                _logger.logInformation("[DeadLetter] '{0}' got '{1}:{2}' from '{3}'", letter.pid.toShortString(), letter.message.getType().name, letter.message, letter.sender?.toShortString())
            }
        }, Dispatchers.SYNCHRONOUS_DISPATCHER)
    }
}


