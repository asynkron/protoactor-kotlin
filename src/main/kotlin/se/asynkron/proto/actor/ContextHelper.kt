package proto.actor

internal object ContextHelper {
    suspend internal fun defaultReceive(context: Context) {
        when (context.message) {
            is PoisonPill -> context.self.stop()
            else -> context.actor.receiveAsync(context)
        }
    }

    internal fun defaultSender(ctx: SenderContext, target: PID, envelope: MessageEnvelope) {
        target.cachedProcess()?.sendUserMessage(target, envelope)
    }
}