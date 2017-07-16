package proto.remote

object ProtosReflection {
    val descriptor : FileDescriptor
    private var descriptor : FileDescriptor? = null
    constructor()  {
        val descriptorData : Array<Byte> = globalSystem.Convert.fromBase64String(.concat("CgxQcm90b3MucHJvdG8SBnJlbW90ZRoYUHJvdG8uQWN0b3IvcHJvdG9zLnBy", "b3RvImQKDE1lc3NhZ2VCYXRjaBISCgp0eXBlX25hbWVzGAEgAygJEhQKDHRh", "cmdldF9uYW1lcxgCIAMoCRIqCgllbnZlbG9wZXMYAyADKAsyFy5yZW1vdGUu", "TWVzc2FnZUVudmVsb3BlInsKD01lc3NhZ2VFbnZlbG9wZRIPCgd0eXBlX2lk", "GAEgASgFEhQKDG1lc3NhZ2VfZGF0YRgCIAEoDBIOCgZ0YXJnZXQYAyABKAUS", "GgoGc2VuZGVyGAQgASgLMgouYWN0b3IuUElEEhUKDXNlcmlhbGl6ZXJfaWQY", "BSABKAUiLQoPQWN0b3JQaWRSZXF1ZXN0EgwKBG5hbWUYASABKAkSDAoEa2lu", "ZBgCIAEoCSIrChBBY3RvclBpZFJlc3BvbnNlEhcKA3BpZBgBIAEoCzIKLmFj", "dG9yLlBJRCIGCgRVbml0IhAKDkNvbm5lY3RSZXF1ZXN0IjAKD0Nvbm5lY3RS", "ZXNwb25zZRIdChVkZWZhdWx0X3NlcmlhbGl6ZXJfaWQYASABKAUyfQoIUmVt", "b3RpbmcSPAoHQ29ubmVjdBIWLnJlbW90ZS5Db25uZWN0UmVxdWVzdBoXLnJl", "bW90ZS5Db25uZWN0UmVzcG9uc2UiABIzCgdSZWNlaXZlEhQucmVtb3RlLk1l", "c3NhZ2VCYXRjaBoMLnJlbW90ZS5Vbml0IgAoATABQg+qAgxQcm90by5SZW1v", "dGViBnByb3RvMw=="))
        descriptor = pbrFileDescriptor.fromGeneratedCode(descriptorData, Array<FileDescriptor>(), GeneratedClrTypeInfo(null, Array<GeneratedClrTypeInfo>()))
    }
}


class MessageBatch : Message {
    companion object {
        private val _parser : MessageParser<MessageBatch> = MessageParser<MessageBatch>{ -> MessageBatch()}
        val parser : MessageParser<MessageBatch>
        val descriptor : MessageDescriptor
        val TypeNamesFieldNumber : Int = 1
        private val _repeated_typeNames_codec : FieldCodec<String> = pbFieldCodec.forString(10)
        val TargetNamesFieldNumber : Int = 2
        private val _repeated_targetNames_codec : FieldCodec<String> = pbFieldCodec.forString(18)
        val EnvelopesFieldNumber : Int = 3
        private val _repeated_envelopes_codec : FieldCodec<MessageEnvelope> = pbFieldCodec.forMessage(26, globalProto.Remote.MessageEnvelope.parser)
    }
    override val descriptor : MessageDescriptor
    constructor()  {
        onConstruction()
    }
    fun onConstruction ()
    constructor(other : MessageBatch)  {
        typeNames_ = other.typeNames_.clone()
        targetNames_ = other.targetNames_.clone()
        envelopes_ = other.envelopes_.clone()
    }
    override fun clone () : MessageBatch {
        return MessageBatch(this)
    }
    private val typeNames_ : RepeatedField<String> = RepeatedField<String>()
    val typeNames : RepeatedField<String>
    private val targetNames_ : RepeatedField<String> = RepeatedField<String>()
    val targetNames : RepeatedField<String>
    private val envelopes_ : RepeatedField<MessageEnvelope> = RepeatedField<MessageEnvelope>()
    val envelopes : RepeatedField<MessageEnvelope>
    fun equals (other : Any) : Boolean {
        return equals(other as MessageBatch)
    }
    override fun equals (other : MessageBatch) : Boolean {
        if (referenceEquals(other, null)) {
            return false
        }
        if (referenceEquals(other, this)) {
            return true
        }
        if (!typeNames_.equals(other.typeNames_))
            return false

        if (!targetNames_.equals(other.targetNames_))
            return false

        if (!envelopes_.equals(other.envelopes_))
            return false

        return true
    }
    fun getHashCode () : Int {
        var hash : Int = 1
        hash ^= typeNames_.getHashCode()
        hash ^= targetNames_.getHashCode()
        hash ^= envelopes_.getHashCode()
        return hash
    }
    fun toString () : String {
        return pbJsonFormatter.toDiagnosticString(this)
    }
    override fun writeTo (output : CodedOutputStream) {
        typeNames_.writeTo(output, _repeated_typeNames_codec)
        targetNames_.writeTo(output, _repeated_targetNames_codec)
        envelopes_.writeTo(output, _repeated_envelopes_codec)
    }
    override fun calculateSize () : Int {
        var size : Int = 0
        size += typeNames_.calculateSize(_repeated_typeNames_codec)
        size += targetNames_.calculateSize(_repeated_targetNames_codec)
        size += envelopes_.calculateSize(_repeated_envelopes_codec)
        return size
    }
    override fun mergeFrom (other : MessageBatch) {
        if (other == null) {
            return 
        }
        typeNames_.add(other.typeNames_)
        targetNames_.add(other.targetNames_)
        envelopes_.add(other.envelopes_)
    }
    override fun mergeFrom (input : CodedInputStream) {
        var tag : UInt32 = 
        while ((tag = input.readTag()) != 0) {
            val tmp = tag
            when (tmp) {
                else -> {
                    input.skipLastField()
                }
                10 -> {
                    typeNames_.addEntriesFrom(input, _repeated_typeNames_codec)
                }
                18 -> {
                    targetNames_.addEntriesFrom(input, _repeated_targetNames_codec)
                }
                26 -> {
                    envelopes_.addEntriesFrom(input, _repeated_envelopes_codec)
                }
            }
        }
    }
}


class MessageEnvelope : Message {
    companion object {
        private val _parser : MessageParser<MessageEnvelope> = MessageParser<MessageEnvelope>{ -> MessageEnvelope()}
        val parser : MessageParser<MessageEnvelope>
        val descriptor : MessageDescriptor
        val TypeIdFieldNumber : Int = 1
        val MessageDataFieldNumber : Int = 2
        val TargetFieldNumber : Int = 3
        val SenderFieldNumber : Int = 4
        val SerializerIdFieldNumber : Int = 5
    }
    override val descriptor : MessageDescriptor
    constructor()  {
        onConstruction()
    }
    fun onConstruction ()
    constructor(other : MessageEnvelope)  {
        typeId_ = other.typeId_
        messageData_ = other.messageData_
        target_ = other.target_
        sender = if (other.sender_ != null) other.sender.clone() else null
        serializerId_ = other.serializerId_
    }
    override fun clone () : MessageEnvelope {
        return MessageEnvelope(this)
    }
    private var typeId_ : Int = 0
    var typeId : Int
    private var messageData_ : ByteString = pbByteString.empty
    var messageData : ByteString
    private var target_ : Int = 0
    var target : Int
    private var sender_ : PID? = null
    var sender : PID
    private var serializerId_ : Int = 0
    var serializerId : Int
    fun equals (other : Any) : Boolean {
        return equals(other as MessageEnvelope)
    }
    override fun equals (other : MessageEnvelope) : Boolean {
        if (referenceEquals(other, null)) {
            return false
        }
        if (referenceEquals(other, this)) {
            return true
        }
        if (typeId != other.typeId)
            return false

        if (messageData != other.messageData)
            return false

        if (target != other.target)
            return false

        if (!.equals(sender, other.sender))
            return false

        if (serializerId != other.serializerId)
            return false

        return true
    }
    fun getHashCode () : Int {
        var hash : Int = 1
        if (typeId != 0)
            hash ^= typeId.getHashCode()

        if (messageData.length != 0)
            hash ^= messageData.getHashCode()

        if (target != 0)
            hash ^= target.getHashCode()

        if (sender_ != null)
            hash ^= sender.getHashCode()

        if (serializerId != 0)
            hash ^= serializerId.getHashCode()

        return hash
    }
    fun toString () : String {
        return pbJsonFormatter.toDiagnosticString(this)
    }
    override fun writeTo (output : CodedOutputStream) {
        if (typeId != 0) {
            output.writeRawTag(8)
            output.writeInt32(typeId)
        }
        if (messageData.length != 0) {
            output.writeRawTag(18)
            output.writeBytes(messageData)
        }
        if (target != 0) {
            output.writeRawTag(24)
            output.writeInt32(target)
        }
        if (sender_ != null) {
            output.writeRawTag(34)
            output.writeMessage(sender)
        }
        if (serializerId != 0) {
            output.writeRawTag(40)
            output.writeInt32(serializerId)
        }
    }
    override fun calculateSize () : Int {
        var size : Int = 0
        if (typeId != 0) {
            size += 1 + pbCodedOutputStream.computeInt32Size(typeId)
        }
        if (messageData.length != 0) {
            size += 1 + pbCodedOutputStream.computeBytesSize(messageData)
        }
        if (target != 0) {
            size += 1 + pbCodedOutputStream.computeInt32Size(target)
        }
        if (sender_ != null) {
            size += 1 + pbCodedOutputStream.computeMessageSize(sender)
        }
        if (serializerId != 0) {
            size += 1 + pbCodedOutputStream.computeInt32Size(serializerId)
        }
        return size
    }
    override fun mergeFrom (other : MessageEnvelope) {
        if (other == null) {
            return 
        }
        if (other.typeId != 0) {
            typeId = other.typeId
        }
        if (other.messageData.length != 0) {
            messageData = other.messageData
        }
        if (other.target != 0) {
            target = other.target
        }
        if (other.sender_ != null) {
            if (sender_ == null) {
                sender_ = PID()
            }
            sender.mergeFrom(other.sender)
        }
        if (other.serializerId != 0) {
            serializerId = other.serializerId
        }
    }
    override fun mergeFrom (input : CodedInputStream) {
        var tag : UInt32 = 
        while ((tag = input.readTag()) != 0) {
            val tmp = tag
            when (tmp) {
                else -> {
                    input.skipLastField()
                }
                8 -> {
                    typeId = input.readInt32()
                }
                18 -> {
                    messageData = input.readBytes()
                }
                24 -> {
                    target = input.readInt32()
                }
                34 -> {
                    if (sender_ == null) {
                        sender_ = PID()
                    }
                    input.readMessage(sender_)
                }
                40 -> {
                    serializerId = input.readInt32()
                }
            }
        }
    }
}


class ActorPidRequest : Message {
    companion object {
        private val _parser : MessageParser<ActorPidRequest> = MessageParser<ActorPidRequest>{ -> ActorPidRequest()}
        val parser : MessageParser<ActorPidRequest>
        val descriptor : MessageDescriptor
        val NameFieldNumber : Int = 1
        val KindFieldNumber : Int = 2
    }
    override val descriptor : MessageDescriptor
    constructor()  {
        onConstruction()
    }
    fun onConstruction ()
    constructor(other : ActorPidRequest)  {
        name_ = other.name_
        kind_ = other.kind_
    }
    override fun clone () : ActorPidRequest {
        return ActorPidRequest(this)
    }
    private var name_ : String = ""
    var name : String
    private var kind_ : String = ""
    var kind : String
    fun equals (other : Any) : Boolean {
        return equals(other as ActorPidRequest)
    }
    override fun equals (other : ActorPidRequest) : Boolean {
        if (referenceEquals(other, null)) {
            return false
        }
        if (referenceEquals(other, this)) {
            return true
        }
        if (name != other.name)
            return false

        if (kind != other.kind)
            return false

        return true
    }
    fun getHashCode () : Int {
        var hash : Int = 1
        if (name.length != 0)
            hash ^= name.getHashCode()

        if (kind.length != 0)
            hash ^= kind.getHashCode()

        return hash
    }
    fun toString () : String {
        return pbJsonFormatter.toDiagnosticString(this)
    }
    override fun writeTo (output : CodedOutputStream) {
        if (name.length != 0) {
            output.writeRawTag(10)
            output.writeString(name)
        }
        if (kind.length != 0) {
            output.writeRawTag(18)
            output.writeString(kind)
        }
    }
    override fun calculateSize () : Int {
        var size : Int = 0
        if (name.length != 0) {
            size += 1 + pbCodedOutputStream.computeStringSize(name)
        }
        if (kind.length != 0) {
            size += 1 + pbCodedOutputStream.computeStringSize(kind)
        }
        return size
    }
    override fun mergeFrom (other : ActorPidRequest) {
        if (other == null) {
            return 
        }
        if (other.name.length != 0) {
            name = other.name
        }
        if (other.kind.length != 0) {
            kind = other.kind
        }
    }
    override fun mergeFrom (input : CodedInputStream) {
        var tag : UInt32 = 
        while ((tag = input.readTag()) != 0) {
            val tmp = tag
            when (tmp) {
                else -> {
                    input.skipLastField()
                }
                10 -> {
                    name = input.readString()
                }
                18 -> {
                    kind = input.readString()
                }
            }
        }
    }
}


class ActorPidResponse : Message {
    companion object {
        private val _parser : MessageParser<ActorPidResponse> = MessageParser<ActorPidResponse>{ -> ActorPidResponse()}
        val parser : MessageParser<ActorPidResponse>
        val descriptor : MessageDescriptor
        val PidFieldNumber : Int = 1
    }
    override val descriptor : MessageDescriptor
    constructor()  {
        onConstruction()
    }
    fun onConstruction ()
    constructor(other : ActorPidResponse)  {
        pid = if (other.pid_ != null) other.pid.clone() else null
    }
    override fun clone () : ActorPidResponse {
        return ActorPidResponse(this)
    }
    private var pid_ : PID? = null
    var pid : PID
    fun equals (other : Any) : Boolean {
        return equals(other as ActorPidResponse)
    }
    override fun equals (other : ActorPidResponse) : Boolean {
        if (referenceEquals(other, null)) {
            return false
        }
        if (referenceEquals(other, this)) {
            return true
        }
        if (!.equals(pid, other.pid))
            return false

        return true
    }
    fun getHashCode () : Int {
        var hash : Int = 1
        if (pid_ != null)
            hash ^= pid.getHashCode()

        return hash
    }
    fun toString () : String {
        return pbJsonFormatter.toDiagnosticString(this)
    }
    override fun writeTo (output : CodedOutputStream) {
        if (pid_ != null) {
            output.writeRawTag(10)
            output.writeMessage(pid)
        }
    }
    override fun calculateSize () : Int {
        var size : Int = 0
        if (pid_ != null) {
            size += 1 + pbCodedOutputStream.computeMessageSize(pid)
        }
        return size
    }
    override fun mergeFrom (other : ActorPidResponse) {
        if (other == null) {
            return 
        }
        if (other.pid_ != null) {
            if (pid_ == null) {
                pid_ = PID()
            }
            pid.mergeFrom(other.pid)
        }
    }
    override fun mergeFrom (input : CodedInputStream) {
        var tag : UInt32 = 
        while ((tag = input.readTag()) != 0) {
            val tmp = tag
            when (tmp) {
                else -> {
                    input.skipLastField()
                }
                10 -> {
                    if (pid_ == null) {
                        pid_ = PID()
                    }
                    input.readMessage(pid_)
                }
            }
        }
    }
}


class Unit : Message {
    companion object {
        private val _parser : MessageParser<Unit> = MessageParser<Unit>{ -> Unit()}
        val parser : MessageParser<Unit>
        val descriptor : MessageDescriptor
    }
    override val descriptor : MessageDescriptor
    constructor()  {
        onConstruction()
    }
    fun onConstruction ()
    constructor(other : Unit)  {
    }
    override fun clone () {
        return Unit(this)
    }
    fun equals (other : Any) : Boolean {
        return equals(other as Unit)
    }
    override fun equals (other : Unit) : Boolean {
        if (referenceEquals(other, null)) {
            return false
        }
        if (referenceEquals(other, this)) {
            return true
        }
        return true
    }
    fun getHashCode () : Int {
        val hash : Int = 1
        return hash
    }
    fun toString () : String {
        return pbJsonFormatter.toDiagnosticString(this)
    }
    override fun writeTo (output : CodedOutputStream) {
    }
    override fun calculateSize () : Int {
        val size : Int = 0
        return size
    }
    override fun mergeFrom (other : Unit) {
        if (other == null) {
            return 
        }
    }
    override fun mergeFrom (input : CodedInputStream) {
        var tag : UInt32 = 
        while ((tag = input.readTag()) != 0) {
            val tmp = tag
            when (tmp) {
                else -> {
                    input.skipLastField()
                }
            }
        }
    }
}


class ConnectRequest : Message {
    companion object {
        private val _parser : MessageParser<ConnectRequest> = MessageParser<ConnectRequest>{ -> ConnectRequest()}
        val parser : MessageParser<ConnectRequest>
        val descriptor : MessageDescriptor
    }
    override val descriptor : MessageDescriptor
    constructor()  {
        onConstruction()
    }
    fun onConstruction ()
    constructor(other : ConnectRequest)  {
    }
    override fun clone () : ConnectRequest {
        return ConnectRequest(this)
    }
    fun equals (other : Any) : Boolean {
        return equals(other as ConnectRequest)
    }
    override fun equals (other : ConnectRequest) : Boolean {
        if (referenceEquals(other, null)) {
            return false
        }
        if (referenceEquals(other, this)) {
            return true
        }
        return true
    }
    fun getHashCode () : Int {
        val hash : Int = 1
        return hash
    }
    fun toString () : String {
        return pbJsonFormatter.toDiagnosticString(this)
    }
    override fun writeTo (output : CodedOutputStream) {
    }
    override fun calculateSize () : Int {
        val size : Int = 0
        return size
    }
    override fun mergeFrom (other : ConnectRequest) {
        if (other == null) {
            return 
        }
    }
    override fun mergeFrom (input : CodedInputStream) {
        var tag : UInt32 = 
        while ((tag = input.readTag()) != 0) {
            val tmp = tag
            when (tmp) {
                else -> {
                    input.skipLastField()
                }
            }
        }
    }
}


class ConnectResponse : Message {
    companion object {
        private val _parser : MessageParser<ConnectResponse> = MessageParser<ConnectResponse>{ -> ConnectResponse()}
        val parser : MessageParser<ConnectResponse>
        val descriptor : MessageDescriptor
        val DefaultSerializerIdFieldNumber : Int = 1
    }
    override val descriptor : MessageDescriptor
    constructor()  {
        onConstruction()
    }
    fun onConstruction ()
    constructor(other : ConnectResponse)  {
        defaultSerializerId_ = other.defaultSerializerId_
    }
    override fun clone () : ConnectResponse {
        return ConnectResponse(this)
    }
    private var defaultSerializerId_ : Int = 0
    var defaultSerializerId : Int
    fun equals (other : Any) : Boolean {
        return equals(other as ConnectResponse)
    }
    override fun equals (other : ConnectResponse) : Boolean {
        if (referenceEquals(other, null)) {
            return false
        }
        if (referenceEquals(other, this)) {
            return true
        }
        if (defaultSerializerId != other.defaultSerializerId)
            return false

        return true
    }
    fun getHashCode () : Int {
        var hash : Int = 1
        if (defaultSerializerId != 0)
            hash ^= defaultSerializerId.getHashCode()

        return hash
    }
    fun toString () : String {
        return pbJsonFormatter.toDiagnosticString(this)
    }
    override fun writeTo (output : CodedOutputStream) {
        if (defaultSerializerId != 0) {
            output.writeRawTag(8)
            output.writeInt32(defaultSerializerId)
        }
    }
    override fun calculateSize () : Int {
        var size : Int = 0
        if (defaultSerializerId != 0) {
            size += 1 + pbCodedOutputStream.computeInt32Size(defaultSerializerId)
        }
        return size
    }
    override fun mergeFrom (other : ConnectResponse) {
        if (other == null) {
            return 
        }
        if (other.defaultSerializerId != 0) {
            defaultSerializerId = other.defaultSerializerId
        }
    }
    override fun mergeFrom (input : CodedInputStream) {
        var tag : UInt32 = 
        while ((tag = input.readTag()) != 0) {
            val tmp = tag
            when (tmp) {
                else -> {
                    input.skipLastField()
                }
                8 -> {
                    defaultSerializerId = input.readInt32()
                }
            }
        }
    }
}

