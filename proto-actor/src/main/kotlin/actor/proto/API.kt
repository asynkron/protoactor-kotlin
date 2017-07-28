@file:JvmName("Actors")
@file:JvmMultifileClass
package actor.proto

@JvmSynthetic fun fromProducer(producer: () -> Actor): Props = Props().withProducer(producer)
@JvmSynthetic fun fromFunc(receive: suspend Context.() -> Unit): Props = fromProducer {
    object : Actor {
        suspend override fun receive(context: Context) = receive(context)
    }
}
fun spawn(props: Props): PID {
    val name = ProcessRegistry.nextId()
    return spawnNamed(props, name)
}

fun spawnPrefix(props: Props, prefix: String): PID {
    val name = prefix + ProcessRegistry.nextId()
    return spawnNamed(props, name)
}

fun spawnNamed(props: Props, name: String): PID {
    return props.spawn(name, null)
}

