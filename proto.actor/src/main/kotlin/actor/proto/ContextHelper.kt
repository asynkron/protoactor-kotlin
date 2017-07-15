package actor.proto

internal object ContextHelper {
    suspend internal fun defaultReceive(context: Context) {
        when (context.message) {
            is PoisonPill -> context.self.stop()
            else -> context.actor.receiveAsync(context)
        }
    }

    @Suppress("unused")
    internal fun defaultSender(@Suppress("UNUSED_PARAMETER") ctx: SenderContext, target: Protos.PID, envelope: MessageEnvelope) {
        target.cachedProcess()?.sendUserMessage(target, envelope)
    }
}