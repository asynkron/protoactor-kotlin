package proto.actor

class MessageEnvelope(val message: Any, val sender: PID, var header: MessageHeader) {
    companion object {
        fun unwrap (message : Any) : Triple<Any, PID?, MessageHeader?> {
            if (message is MessageEnvelope) {
                return Triple(message.message,message.sender,message.header)
            }
            return Triple(message,null,null)
        }
    }

    fun getHeader (key : String, default : String) : String {
        return header.getOrDefault(key,default)
    }
    fun setHeader (key : String, value : String) {
        header.set(key,value)
    }
}

