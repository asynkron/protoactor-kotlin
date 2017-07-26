package actor.proto

internal object ContextHelper {
    suspend internal fun defaultReceive(context: Context) {
        return when (context.message) {
            is PoisonPill -> context.self.stop()
            else -> context.actor.receive(context)
        }
    }

    internal fun defaultSender(@Suppress("UNUSED_PARAMETER") ctx: SenderContext, target: PID, envelope: MessageEnvelope) {
        target.cachedProcess()?.sendUserMessage(target, envelope)
    }
}