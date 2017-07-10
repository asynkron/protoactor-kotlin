package proto.actor

import java.util.*

class Behavior(receive: (IContext) -> Task) {
    private val _behaviors : Stack<(IContext) -> Task> = Stack()
    fun become (receive : (IContext) -> Task) {
        _behaviors.clear()
        _behaviors.push(receive)
    }
    fun becomeStacked (receive : (IContext) -> Task) {
        _behaviors.push(receive)
    }
    fun unbecomeStacked () {
        _behaviors.pop()
    }
    fun receiveAsync (context : IContext) : Task {
        val behavior : (IContext) -> Task = _behaviors.peek()
        return behavior(context)
    }

    init {
        become(receive)
    }
}

