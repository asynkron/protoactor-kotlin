package proto.actor

fun fromProducer(producer: () -> Actor): Props = Props().withProducer(producer)
fun fromFunc(receive: suspend Context.() -> Unit): Props = fromProducer { -> FunActor(receive) }
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
