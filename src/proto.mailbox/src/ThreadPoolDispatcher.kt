package proto.mailbox


class ThreadPoolDispatcher : IDispatcher {
    override fun schedule (runner : () -> Task)  {}
    override var throughput : Int = 300
}

