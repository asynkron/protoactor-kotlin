package actor.proto.remote

data class RemoteConfig(
        val endpointWriterBatchSize: Int = 1000,
        val channelCredentials : ChannelCredentials = ChannelCredentials.insecure,
        val serverCredentials : ServerCredentials = ServerCredentials.insecure,
        val advertisedHostname : String? = null,
        val advertisedPort : Int? = null
)
