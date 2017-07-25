//package proto.mailbox.tests
//
//import actor.proto.mailbox.MailboxQueue
//import kotlin.test.assertEquals
//import kotlin.test.assertFalse
//import kotlin.test.assertTrue
//
//open class MailboxQueuesTests {
//
//    enum class MailboxQueueKind {
//        Bounded, Unbounded
//    }
//    private fun getMailboxQueue (kind : MailboxQueueKind) : MailboxQueue {
//        val tmp = kind
//        when (tmp) {
//            MailboxQueueKind.Bounded -> {
//                return BoundedMailboxQueue(4)
//            }
//            MailboxQueueKind.Unbounded -> {
//                return UnboundedMailboxQueue()
//            }
//            else -> {
//                throw ArgumentOutOfRangeException(nameof(kind), kind, null)
//            }
//        }
//    }
//    fun given_MailboxQueue_When_push_pop_Then_HasMessages_relate_the_queue_status (kind : MailboxQueueKind) {
//        val sut = getMailboxQueue(kind)
//        assertFalse(sut.hasMessages)
//        sut.add(1)
//        assertTrue(sut.hasMessages)
//        sut.add(2)
//        assertTrue(sut.hasMessages)
//        assertEquals(1, sut.poll())
//        assertTrue(sut.hasMessages)
//        assertEquals(2, sut.poll())
//        assertFalse(sut.hasMessages)
//    }
//    fun given_MailboxQueue_when_enqueue_and_dequeue_in_different_threads_Then_we_get_the_elements_in_the_FIFO_order (kind : MailboxQueueKind) {
//        val msgCount = 1000
//        val cancelSource = CancellationTokenSource()
//        val sut = getMailboxQueue(kind)
//        val producer = Thread{_ ->
//            for (i in 0 until msgCount) {
//                if (cancelSource.isCancellationRequested)
//                    return
//
//                sut.add(i)
//            }
//        }
//
//        val consumerList = mutableListOf<Int>()
//        val consumer = Thread{l ->
//            val list = l
//            for (i in 0 until msgCount) {
//                var popped = sut.poll()
//                while (popped == null) {
//                    if (cancelSource.isCancellationRequested)
//                        return
//
//                    Thread.sleep(1)
//                    popped = sut.poll()
//                }
//                list.add(popped)
//            }
//        }
//
//        producer.start()
//        consumer.start(consumerList)
//        producer.join(1000)
//        consumer.join(1000)
//        cancelSource.cancel()
//        assertEquals(msgCount, consumerList.count())
//        for (i in 0 until msgCount) {
//            assertEquals(i, consumerList[i])
//        }
//    }
//}
//
