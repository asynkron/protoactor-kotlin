package actor.proto.fixture

import java.util.concurrent.CompletableFuture

data class TestMessage(var message: String) {
    var taskCompletionSource = CompletableFuture<Int>()
}

