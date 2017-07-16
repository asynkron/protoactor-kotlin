@file:Suppress("MemberVisibilityCanPrivate")

package actor.proto.remote

import com.google.protobuf.ByteString
import com.google.protobuf.Descriptors
import com.google.protobuf.Message
import com.google.protobuf.Parser


object Serialization {
    var defaultSerializerId: Int = 0
    internal val TypeLookup: HashMap<String, Parser<Message>> = HashMap()
    private val Serializers: MutableList<Serializer> = mutableListOf()
    private val ProtobufSerializer: ProtobufSerializer = ProtobufSerializer()
    private val JsonSerializer: JsonSerializer = JsonSerializer()

    init {
        registerFileDescriptor(actor.proto.Protos.getDescriptor())
        registerFileDescriptor(actor.proto.remote.RemoteProtos.getDescriptor())
        registerSerializer(ProtobufSerializer, true)
        registerSerializer(JsonSerializer, false)
    }


    fun registerSerializer(serializer: Serializer, makeDefault: Boolean) {
        Serializers.add(serializer)
        if (makeDefault) {
            defaultSerializerId = Serializers.count() - 1
        }
    }

    fun registerFileDescriptor(fd: Descriptors.FileDescriptor) {
        for (msg in fd.messageTypes) {
            val name: String = fd.`package` + "." + msg.name
            //     TypeLookup.put(name, msg.???)
        }
    }

    fun serialize(message: Any, serializerId: Int): ByteString = Serializers[serializerId].serialize(message)
    fun getTypeName(message: Any, serializerId: Int): String = Serializers[serializerId].getTypeName(message)
    fun deserialize(typeName: String, bytes: ByteString, serializerId: Int): Any = Serializers[serializerId].deserialize(bytes, typeName)
}

