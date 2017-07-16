package actor.proto.remote

import com.google.protobuf.ByteString
import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat

open class JsonSerializer : Serializer {
    override fun serialize(obj: Any): ByteString {
        when (obj) {
            is JsonMessage -> return ByteString.copyFromUtf8(obj.json)
            else -> {
                val message = obj as Message
                val json: String = JsonFormat.printer().print(message)
                return ByteString.copyFromUtf8(json)
            }
        }
    }

    override fun deserialize(bytes: ByteString, typeName: String): Any {
        val json: String = bytes.toStringUtf8()
//        val parser : Parser<Message> = Serialization.TypeLookup[typeName]!!
        val parser = JsonFormat.parser()
        val builder = Serialization.TypeLookup[typeName]!!
        //  val o = parser.merge(json, builder)
        return Any()
    }

    override fun getTypeName(obj: Any): String {
        when (obj) {
            is JsonMessage -> return obj.typeName
            else -> {
                val message = obj as Message
                return message.descriptorForType.file.`package` + "." + message.descriptorForType.name
            }
        }
    }
}