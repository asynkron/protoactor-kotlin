//package proto.mailbox.tests
//
//import kotlin.test.assertEquals
//import kotlin.test.assertNull
//
//open class NonBlockingBoundedMailboxTests {
//    fun whenMailboxOverflows_OverflowActionCalledWithMessage () {
//        var overflowMessage = null
//        val mailbox = NonBlockingBoundedMailbox(1, {msg -> overflowMessage = msg }, Duration.ofSeconds(1))
//        mailbox.add("first message")
//        assertNull(overflowMessage)
//        val secondMessage = "second message"
//        mailbox.add(secondMessage)
//        assertEquals(overflowMessage, secondMessage)
//    }
//    fun whenMailboxOverflows_OverflowActionCalledOnAllSubsequentMessages () {
//        val overflowActionCallCount = 0
//        val mailbox = NonBlockingBoundedMailbox(1, {msg -> overflowActionCallCount++ }, Duration.ofSeconds(1))
//        mailbox.add("first message")
//        for (i in 0 until 10) {
//            mailbox.add(i)
//        }
//        assertEquals(overflowActionCallCount, 10)
//    }
//    fun whenMailboxOverflows_CurrentMessagesRemainInMailbox () {
//        var overflowMessage = null
//        val mailbox = NonBlockingBoundedMailbox(1, {msg -> overflowMessage = msg }, Duration.ofSeconds(1))
//        mailbox.add("first message")
//        mailbox.add("second message")
//        assertEquals("first message", mailbox.poll())
//    }
//}
//
