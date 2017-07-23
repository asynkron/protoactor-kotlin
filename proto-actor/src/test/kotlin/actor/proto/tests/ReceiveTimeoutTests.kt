package actor.proto.tests

import actor.proto.*
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

class ReceiveTimeoutTests {
    @Test fun `receive timeout received within expected time`() {
        var timeoutReceived: Boolean = false
        val props: Props = fromFunc {
            val tmp = message
            when (tmp) {
                is Started -> {
                    setReceiveTimeout(Duration.ofMillis(150))
                }
                is ReceiveTimeout -> {
                    timeoutReceived = true
                }
            }

        }

        spawn(props)
        Thread.sleep(1500)
        assertTrue(timeoutReceived)
    }

    @Test fun `receive timeout not received within expected_time`() {
        var timeoutReceived: Boolean = false
        val props: Props = fromFunc {
            val tmp = message
            when (tmp) {
                is Started -> {
                    setReceiveTimeout(Duration.ofMillis(1500))
                }
                is ReceiveTimeout -> {
                    timeoutReceived = true
                }
            }

        }

        spawn(props)
        Thread.sleep(1500)
        assertFalse(timeoutReceived)
    }

    @Test fun `can cancel receive timeout`() {
        var timeoutReceived: Boolean = false
        val props: Props = fromFunc {
            val tmp = message
            when (tmp) {
                is Started -> {
                    setReceiveTimeout(Duration.ofMillis(150))
                    cancelReceiveTimeout()
                }
                is ReceiveTimeout -> {
                    timeoutReceived = true
                }
            }

        }

        spawn(props)
        Thread.sleep(1500)
        assertFalse(timeoutReceived)
    }

    @Test fun `can still set receive timeout after cancelling`() {
        var timeoutReceived: Boolean = false
        val props: Props = fromFunc {
            val tmp = message
            when (tmp) {
                is Started -> {
                    setReceiveTimeout(Duration.ofMillis(150))
                    cancelReceiveTimeout()
                    setReceiveTimeout(Duration.ofMillis(150))
                }
                is ReceiveTimeout -> {
                    timeoutReceived = true
                }
            }

        }

        spawn(props)
        Thread.sleep(1500)
        assertTrue(timeoutReceived)
    }
}

