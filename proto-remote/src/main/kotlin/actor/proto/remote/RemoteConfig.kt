package actor.proto.remote

data class RemoteConfig(
        val endpointWriterBatchSize: Int = 1000,
        val advertisedHostname: String? = null,
        val advertisedPort: Int? = null,
        val idleTimeout: Long? = null,
        val keepAliveTime: Long? = null,
        val keepAliveTimout: Long? = null,
        val keepAliveWithoutCalls : Boolean? = null,
        val usePlainText : Boolean? = true
)
