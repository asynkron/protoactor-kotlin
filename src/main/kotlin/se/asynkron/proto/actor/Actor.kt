package proto.actor

fun fromProducer(producer: () -> IActor): Props = Props().withProducer(producer)
fun fromFunc(receive: suspend (IContext) -> Unit): Props = fromProducer { -> FunActor(receive) }
fun spawn(props: Props): PID {
    val name: String = ProcessRegistry.nextId()
    return spawnNamed(props, name)
}

fun spawnPrefix(props: Props, prefix: String): PID {
    val name: String = prefix + ProcessRegistry.nextId()
    return spawnNamed(props, name)
}

fun spawnNamed(props: Props, name: String): PID {
    return props.spawn(name, null)
}
