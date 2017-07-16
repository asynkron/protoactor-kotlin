package actor.proto.remote

import com.google.protobuf.ByteString
import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import java.io.NotSerializableException

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
        val parser = JsonFormat.parser()
        val builder = Serialization.getMessageBuilder(typeName)
        parser.merge(json, builder)
        return Any() //TODO: fix
    }

    override fun getTypeName(message: Any): String = when (message) {
        is JsonMessage -> message.typeName
        is Message -> message.descriptorForType.fullName
        else -> throw NotSerializableException()
    }
}