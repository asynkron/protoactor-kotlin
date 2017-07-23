package actor.proto.remote

data class RemoteConfig(
        val endpointWriterBatchSize: Int = 1000,
        val advertisedHostname: String? = null,
        val advertisedPort: Int? = null
)
