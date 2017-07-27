package actor.proto.java

import java.util.concurrent.CompletableFuture

interface FutureActor {
    fun receive(context: JavaContext): CompletableFuture<*>
}