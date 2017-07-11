package proto.actor

class MessageHeader {
    val map: MutableMap<String, String> = mutableMapOf()

    fun getOrDefault(key: String, default: String): String {
        if (map.containsKey(key)) {
            return map[key]!!
        }
        return default
    }

    fun set(key: String, value: String) {
        map[key] = value
    }
}

