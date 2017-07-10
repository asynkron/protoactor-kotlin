package proto

interface ISenderContext {
    val message : Any
    val headers : MessageHeader
}

