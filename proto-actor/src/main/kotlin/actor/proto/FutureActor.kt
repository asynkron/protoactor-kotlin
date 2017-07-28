package actor.proto

import java.util.concurrent.CompletableFuture

interface FutureActor {
    fun receive(context: FutureContext): CompletableFuture<*>
}