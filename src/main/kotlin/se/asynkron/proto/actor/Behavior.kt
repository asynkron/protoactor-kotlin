package proto.actor

import java.util.*

class Behavior(receive: suspend (IContext) -> Unit) {
    private val behaviors: Stack<suspend (IContext) -> Unit> = Stack()
    fun become(receive: suspend (IContext) -> Unit) {
        behaviors.clear()
        behaviors.push(receive)
    }

    fun becomeStacked(receive: suspend (IContext) -> Unit) {
        behaviors.push(receive)
    }

    fun unbecomeStacked() {
        behaviors.pop()
    }

    suspend fun receiveAsync(context: IContext): Unit {
        val behavior: suspend (IContext) -> Unit = behaviors.peek()
        return behavior(context)
    }

    init {
        become(receive)
    }
}

