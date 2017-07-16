package actor.proto.remote

import java.net.URI
import java.net.URISyntaxException

data class Address(val host:String,val port : Int)

fun parseAddress(address:String) :Address {
    // WORKAROUND: add any scheme to make the resulting URI valid.
    val uri = URI("my://" + address); // may throw URISyntaxException
    if (uri.host == null || uri.port == -1) {
        throw URISyntaxException(uri.toString(), "URI must have host and port parts")
    }
    return Address(uri.host, uri.port)
}
