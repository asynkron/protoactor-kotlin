package proto.actor

internal object ContextHelper {
    suspend internal fun defaultReceive(context: IContext) {
        val c: Context = context as Context
        when (c.message) {
            is PoisonPill -> c.self.stop()
            else -> c.actor.receiveAsync(context)
        }
    }

    internal fun defaultSender(ctx: SenderContext, target: PID, envelope: MessageEnvelope) {
        target.ref()?.sendUserMessage(target, envelope)
    }
}