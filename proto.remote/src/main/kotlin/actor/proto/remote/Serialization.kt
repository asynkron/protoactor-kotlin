@file:Suppress("MemberVisibilityCanPrivate")

package actor.proto.remote

import com.google.protobuf.*
import com.google.protobuf.util.JsonFormat
import kotlin.collections.HashMap

interface Serializer {
    fun serialize (obj : Any) : ByteString
    fun deserialize (bytes : ByteString, typeName : String) : Any
    fun getTypeName (message : Any) : String
}


open class JsonSerializer : Serializer {
    override fun serialize (obj : Any) : ByteString {
        when (obj) {
            is JsonMessage -> return ByteString.copyFromUtf8(obj.json)
            else -> {
                val message = obj as Message
                val json: String = JsonFormat.printer().print(message)
                return ByteString.copyFromUtf8(json)
            }
        }
    }
    override fun deserialize (bytes : ByteString, typeName : String) : Any {
        val json : String = bytes.toStringUtf8()
//        val parser : Parser<Message> = Serialization.TypeLookup[typeName]!!
        val parser = JsonFormat.parser()
        val builder = Serialization.TypeLookup[typeName]!!
      //  val o = parser.merge(json, builder)
        return Any()
    }
    override fun getTypeName (obj : Any) : String {
        when (obj) {
            is JsonMessage -> return obj.typeName
            else -> {
                val message = obj as Message
                return message.descriptorForType.file.`package` + "." + message.descriptorForType.name
            }
        }
    }
}


open class ProtobufSerializer : Serializer {
    override fun serialize (obj : Any) : ByteString {
        val message = obj as Message
        return message.toByteString()
    }
    override fun deserialize (bytes : ByteString, typeName : String) : Any {
        val parser : Parser<Message> = Serialization.TypeLookup[typeName]!!
        val o = parser.parseFrom(bytes)
        return o
    }
    override fun getTypeName (obj : Any) : String {
        val message = obj as Message
        return message.descriptorForType.file.`package` + "." + message.descriptorForType.name
    }
}


object Serialization {
    var defaultSerializerId : Int = 0
    internal val TypeLookup : HashMap<String,  Parser<Message>> = HashMap()
    private val Serializers : MutableList<Serializer> = mutableListOf()
    private val ProtobufSerializer : ProtobufSerializer = ProtobufSerializer()
    private val JsonSerializer : JsonSerializer = JsonSerializer()
    init  {
        registerFileDescriptor(actor.proto.Protos.getDescriptor())
        registerFileDescriptor(actor.proto.remote.RemoteProtos.getDescriptor())
        registerSerializer(ProtobufSerializer, true)
        registerSerializer(JsonSerializer,false)
    }



    fun registerSerializer (serializer : Serializer, makeDefault : Boolean) {
        Serializers.add(serializer)
        if (makeDefault) {
            defaultSerializerId = Serializers.count() - 1
        }
    }
    fun registerFileDescriptor (fd : Descriptors.FileDescriptor) {
        for(msg in fd.messageTypes) {
            val name : String = fd.`package` + "." + msg.name
       //     TypeLookup.put(name, msg.???)
        }
    }
    fun serialize (message : Any, serializerId : Int) : ByteString = Serializers[serializerId].serialize(message)
    fun getTypeName (message : Any, serializerId : Int) : String = Serializers[serializerId].getTypeName(message)
    fun deserialize (typeName : String, bytes : ByteString, serializerId : Int) : Any = Serializers[serializerId].deserialize(bytes, typeName)
}

