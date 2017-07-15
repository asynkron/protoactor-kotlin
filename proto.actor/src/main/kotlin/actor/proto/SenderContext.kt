package actor.proto

interface SenderContext {
    val message: Any?
    val headers: MessageHeader?
}

