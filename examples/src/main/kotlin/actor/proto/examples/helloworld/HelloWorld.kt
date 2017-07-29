package actor.proto.examples.helloworld

import actor.proto.*

fun main(args: Array<String>) {
    val prop = fromFunc {
        when (message) {
            is Started -> println("Started")
            is String -> {
                println("Hello " + message)
                stop(self)
            }
            is Stopping -> println("Stopping")
            is Stopped -> println("Stopped")
            else -> println("unknown " + message.toString())
        }
    }

    val pid = spawn(prop)
    send(pid, "Proto.Actor")
    readLine()

    val clazz : Receiver = ReceiverClass()
    val d = Data()
}

class Data {
    fun doStuff(){}
}

interface Receiver {
    fun Data.receive()
}

class ReceiverClass : Receiver {
    fun x(){
        val d = Data()
        d.receive()
    }
    override fun Data.receive() {
        doStuff()
        bar()
    }

    private fun Data.bar(){
        doStuff()
    }
}