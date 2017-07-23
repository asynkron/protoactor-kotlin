//package proto.mailbox.tests
//
//import kotlin.test.assertEquals
//import kotlin.test.assertNull
//
//open class NonBlockingBoundedMailboxTests {
//    fun whenMailboxOverflows_OverflowActionCalledWithMessage () {
//        var overflowMessage = null
//        val mailbox = NonBlockingBoundedMailbox(1, {msg -> overflowMessage = msg }, Duration.ofSeconds(1))
//        mailbox.push("first message")
//        assertNull(overflowMessage)
//        val secondMessage = "second message"
//        mailbox.push(secondMessage)
//        assertEquals(overflowMessage, secondMessage)
//    }
//    fun whenMailboxOverflows_OverflowActionCalledOnAllSubsequentMessages () {
//        val overflowActionCallCount = 0
//        val mailbox = NonBlockingBoundedMailbox(1, {msg -> overflowActionCallCount++ }, Duration.ofSeconds(1))
//        mailbox.push("first message")
//        for (i in 0 until 10) {
//            mailbox.push(i)
//        }
//        assertEquals(overflowActionCallCount, 10)
//    }
//    fun whenMailboxOverflows_CurrentMessagesRemainInMailbox () {
//        var overflowMessage = null
//        val mailbox = NonBlockingBoundedMailbox(1, {msg -> overflowMessage = msg }, Duration.ofSeconds(1))
//        mailbox.push("first message")
//        mailbox.push("second message")
//        assertEquals("first message", mailbox.pop())
//    }
//}
//
