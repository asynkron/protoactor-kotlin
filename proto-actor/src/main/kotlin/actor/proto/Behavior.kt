package actor.proto

import java.util.*

private typealias Receive2 = suspend Context.(msg: Any) -> Unit

class Behavior(receive: Receive2 = {}) {
    private val behaviors: Stack<Receive2> = Stack()
    fun become(receive: Receive2) {
        behaviors.clear()
        behaviors.push(receive)
    }

    fun becomeStacked(receive: Receive2) {
        behaviors.push(receive)
    }

    fun unbecomeStacked() {
        behaviors.pop()
    }

    suspend fun receive(ctx: Context, msg: Any) {
        val behavior = behaviors.peek()
        behavior(ctx, msg)
    }

    init {
        become(receive)
    }
}

