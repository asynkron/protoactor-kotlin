package proto.actor

interface ISenderContext {
    val message: Any?
    val headers: MessageHeader?
}

