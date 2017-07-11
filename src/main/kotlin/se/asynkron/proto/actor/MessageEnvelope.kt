package proto.actor

class MessageEnvelope(val message: Any, val sender: PID?, var header: MessageHeader?) {
    companion object {
        fun unwrap(message: Any): Triple<Any, PID?, MessageHeader?> {
            when (message) {
                is MessageEnvelope -> return Triple(message.message, message.sender, message.header)
                else -> return Triple(message, null, null)
            }
        }
    }

    fun getHeader(key: String, default: String): String {
        when (header) {
            null -> return default
            else -> return header!!.getOrDefault(key, default)
        }
    }

    fun setHeader(key: String, value: String) = ensureHeader().set(key,value)

    private fun ensureHeader() : MessageHeader {
        header = header ?: MessageHeader()
        return header!!
    }
}

