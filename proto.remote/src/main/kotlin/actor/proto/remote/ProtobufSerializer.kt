package actor.proto.remote

import com.google.protobuf.ByteString
import com.google.protobuf.Message
import com.google.protobuf.Parser
import java.io.NotSerializableException

open class ProtobufSerializer : Serializer {
    override fun serialize(obj: Any): ByteString {
        val message = obj as Message
        return message.toByteString()
    }

    override fun deserialize(bytes: ByteString, typeName: String): Any {
        val parser: Parser<Message> = Serialization.getMessageParser(typeName)
        val o = parser.parseFrom(bytes)
        return o
    }

    override fun getTypeName(message: Any): String = when (message) {
        is Message -> message.descriptorForType.fullName
        else -> throw NotSerializableException()
    }
}