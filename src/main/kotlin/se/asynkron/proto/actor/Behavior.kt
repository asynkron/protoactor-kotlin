package proto.actor

import java.util.*

class Behavior(receive: suspend (IContext) -> Unit) {
    private val _behaviors: Stack<suspend (IContext) -> Unit> = Stack()
    fun become(receive: suspend (IContext) -> Unit) {
        _behaviors.clear()
        _behaviors.push(receive)
    }

    fun becomeStacked(receive: suspend (IContext) -> Unit) {
        _behaviors.push(receive)
    }

    fun unbecomeStacked() {
        _behaviors.pop()
    }

    suspend fun receiveAsync(context: IContext): Unit {
        val behavior: suspend (IContext) -> Unit = _behaviors.peek()
        return behavior(context)
    }

    init {
        become(receive)
    }
}

