package actor.proto.fixture

import actor.proto.mailbox.SystemMessage
import java.util.concurrent.CompletableFuture

data class TestMessage(var message: String = "") {
    var taskCompletionSource = CompletableFuture<Int>()
}

data class ExceptionalMessage(val exception: Exception)
data class ExceptionalSystemMessage(val exception: Exception) : SystemMessage

data class TestSystemMessage(var message: String = "") : SystemMessage {
    var taskCompletionSource = CompletableFuture<Int>()
}
