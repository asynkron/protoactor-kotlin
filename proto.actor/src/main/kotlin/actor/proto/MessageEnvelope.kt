package actor.proto

class MessageEnvelope(val message: Any, val sender: PID?, var header: MessageHeader?) {
    fun getHeader(key: String, default: String): String {
        when (header) {
            null -> return default
            else -> return header!!.getOrDefault(key, default)
        }
    }

    fun setHeader(key: String, value: String) = ensureHeader().set(key, value)

    private fun ensureHeader(): MessageHeader {
        header = header ?: MessageHeader()
        return header!!
    }
}

