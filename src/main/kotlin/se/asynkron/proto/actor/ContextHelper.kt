package proto.actor

internal object ContextHelper {
    suspend internal fun defaultReceive(context: Context) {
        val c: ActorContext = context as ActorContext
        when (c.message) {
            is PoisonPill -> c.self.stop()
            else -> c.actor.receiveAsync(context)
        }
    }

    internal fun defaultSender(ctx: SenderContext, target: PID, envelope: MessageEnvelope) {
        target.cachedProcess()?.sendUserMessage(target, envelope)
    }
}