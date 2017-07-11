package proto.actor

interface SenderContext {
    val message: Any?
    val headers: MessageHeader?
}

