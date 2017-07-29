package actor.proto

internal object ContextHelper {

    internal fun defaultSender(@Suppress("UNUSED_PARAMETER") ctx: SenderContext, target: PID, envelope: MessageEnvelope) {
        target.cachedProcess()?.sendUserMessage(target, envelope)
    }
}