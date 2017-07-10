package proto.mailbox

class SynchronousDispatcher : IDispatcher {
    override fun schedule(runner: () -> Unit) {
        runner()
    }

    override var throughput: Int = 300
}