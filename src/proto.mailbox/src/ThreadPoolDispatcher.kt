package proto.mailbox


class ThreadPoolDispatcher : IDispatcher {
    override fun schedule (runner : () -> Unit)  {}
    override var throughput : Int = 300
}

