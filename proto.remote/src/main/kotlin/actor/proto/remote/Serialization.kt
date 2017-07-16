package proto.remote

import actor.proto.remote.JsonMessage

interface Serializer {
    fun serialize (obj : Any) : ByteString
    fun deserialize (bytes : ByteString, typeName : String) : Any
    fun getTypeName (message : Any) : String
}


open class JsonSerializer : Serializer {
    override fun serialize (obj : Any) : ByteString {
        if (obj is JsonMessage /* jsonMessage  */) {
            return ByteString.copyFromUtf8(jsonMessage.json)
        }
        val message : Message = obj as IMessage
        val json : String = JsonFormatter.default.format(message)
        return ByteString.copyFromUtf8(json)
    }
    override fun deserialize (bytes : ByteString, typeName : String) : Any {
        val json : String = bytes.toStringUtf8()
        val parser : MessageParser = Serialization.TypeLookup[typeName]
        val o : Message = parser.parseJson(json)
        return o
    }
    override fun getTypeName (obj : Any) : String {
        if (obj is JsonMessage /* jsonMessage  */) {
            return jsonMessage.typeName
        }
        val message : Message = obj as IMessage
        if (message == null) {
            throw IllegalArgumentException("obj must be of type IMessage", nameof(obj))
        }
        return message.descriptor.file.package + "." + message.descriptor.name
    }
}


open class ProtobufSerializer : Serializer {
    override fun serialize (obj : Any) : ByteString {
        val message : Message = obj as IMessage
        return message.toByteString()
    }
    override fun deserialize (bytes : ByteString, typeName : String) : Any {
        val parser : MessageParser = Serialization.TypeLookup[typeName]
        val o : Message = parser.parseFrom(bytes)
        return o
    }
    override fun getTypeName (obj : Any) : String {
        val message : Message = obj as IMessage
        if (message == null) {
            throw IllegalArgumentException("obj must be of type IMessage", nameof(obj))
        }
        return message.descriptor.file.package + "." + message.descriptor.name
    }
}


object Serialization {
    internal val TypeLookup : Dictionary<String, MessageParser> = Dictionary<String, MessageParser>()
    private val Serializers : MutableList<Serializer> = mutableListOf()
    private val ProtobufSerializer : ProtobufSerializer = ProtobufSerializer()
    private val JsonSerializer : JsonSerializer = JsonSerializer()
    constructor()  {
        registerFileDescriptor(Proto.ProtosReflection.descriptor)
        registerFileDescriptor(ProtosReflection.descriptor)
        registerSerializer(ProtobufSerializer, true)
        registerSerializer(JsonSerializer)
    }
    var defaultSerializerId : Int
    fun registerSerializer (serializer : Serializer, makeDefault : Boolean) {
        Serializers.add(serializer)
        if (makeDefault) {
            defaultSerializerId = Serializers.count - 1
        }
    }
    fun registerFileDescriptor (fd : FileDescriptor) {
        for(msg in fd.messageTypes) {
            val name : String = fd.package + "." + msg.name
            TypeLookup.add(name, msg.parser)
        }
    }
    fun serialize (message : Any, serializerId : Int) : ByteString = Serializers[serializerId].serialize(message)
    fun getTypeName (message : Any, serializerId : Int) : String = Serializers[serializerId].getTypeName(message)
    fun deserialize (typeName : String, bytes : ByteString, serializerId : Int) : Any = Serializers[serializerId].deserialize(bytes, typeName)
}

