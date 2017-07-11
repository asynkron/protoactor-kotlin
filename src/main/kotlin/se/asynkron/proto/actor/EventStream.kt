package proto.actor

import proto.mailbox.Dispatchers

class EventStream : EventStreamImpl<Any>() {
    companion object {
        val Instance: EventStream = EventStream()
    }

    init {
        subscribe({
            if (it is DeadLetterEvent) {
//                _logger.logInformation("[DeadLetter] '{0}' got '{1}:{2}' from '{3}'", letter.pid.toShortString(), letter.message.getType().name, letter.message, letter.sender?.toShortString())
            }
        }, Dispatchers.synchronousDispatcher)
    }
}


