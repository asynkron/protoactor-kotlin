package actor.proto

import java.util.*

class Behavior(receive: suspend (Context) -> Unit) {
    private val behaviors: Stack<suspend (Context) -> Unit> = Stack()
    fun become(receive: suspend (Context) -> Unit) {
        behaviors.clear()
        behaviors.push(receive)
    }

    fun becomeStacked(receive: suspend (Context) -> Unit) {
        behaviors.push(receive)
    }

    fun unbecomeStacked() {
        behaviors.pop()
    }

    suspend fun receive(context: Context): Unit {
        val behavior = behaviors.peek()
        return behavior(context)
    }

    init {
        become(receive)
    }
}

