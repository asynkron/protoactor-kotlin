package proto.remote

open class RemoteConfig(var endpointWriterBatchSize: Int = 1000) {
    var channelCredentials : ChannelCredentials = ChannelCredentials.insecure
    var serverCredentials : ServerCredentials = ServerCredentials.insecure
    var advertisedHostname : String
    var advertisedPort : Int?
}

