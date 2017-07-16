package actor.proto.remote

import com.google.protobuf.ByteString

interface Serializer {
    fun serialize(obj: Any): ByteString
    fun deserialize(bytes: ByteString, typeName: String): Any
    fun getTypeName(message: Any): String
}