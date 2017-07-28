package actor.proto.java

import java.util.concurrent.CompletableFuture

interface Actor {
    fun receive(context: Context): CompletableFuture<*>
}