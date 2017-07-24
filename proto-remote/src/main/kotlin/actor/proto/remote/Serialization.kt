@file:Suppress("MemberVisibilityCanPrivate")

package actor.proto.remote

import com.google.protobuf.ByteString
import com.google.protobuf.Descriptors
import com.google.protobuf.Message
import com.google.protobuf.Parser
import java.lang.reflect.Field


object Serialization {
    var defaultSerializerId: Int = 0
    private val parserLookup: HashMap<String, Parser<Message>> = HashMap()
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
        //TODO: make this not horrible
        for (msg in fd.messageTypes) {
            val outer = if (fd.options.javaOuterClassname == "") fd.name.split('.')[0] else fd.options.javaOuterClassname
            val className = "${fd.options.javaPackage}.$outer$${msg.name}"
            val clazz = Thread.currentThread().contextClassLoader.loadClass(className)
            val parserField: Field = clazz.getDeclaredField("PARSER")
            parserField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val parser = parserField.get(null) as Parser<Message>
            parserLookup.put(msg.fullName, parser)
        }
    }

    fun serialize(message: Any, serializerId: Int): ByteString = Serializers[serializerId].serialize(message)
    fun getTypeName(message: Any, serializerId: Int): String = Serializers[serializerId].getTypeName(message)
    fun deserialize(typeName: String, bytes: ByteString, serializerId: Int): Any = Serializers[serializerId].deserialize(bytes, typeName)
    @Suppress("UNUSED_PARAMETER")
    fun getMessageBuilder(typeName: Any): Message.Builder? {
        return null
    }

    fun getMessageParser(typeName: String): Parser<Message> {
        return parserLookup[typeName]!!
    }
}

