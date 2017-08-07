package actor.proto.fixture

import actor.proto.mailbox.SystemMessage
import java.util.concurrent.CompletableFuture

data class TestMessage(var message: String = "") {
    var taskCompletionSource = CompletableFuture<Int>()
}

data class TestSystemMessage(var message: String = "") : SystemMessage {
    var taskCompletionSource = CompletableFuture<Int>()
}
