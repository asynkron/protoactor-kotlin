package proto.tests

import actor.proto.*
import org.junit.Assert
import org.junit.Test
import java.time.Duration

open class ReceiveTimeoutTests {
    @Test fun receive_timeout_received_within_expected_time () {
        var timeoutReceived : Boolean = false
        val props : Props = fromFunc{ 
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
        Assert.assertTrue(timeoutReceived)
    }
    @Test fun receive_timeout_not_received_within_expected_time () {
        var timeoutReceived : Boolean = false
        val props : Props = fromFunc{
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
        Assert.assertFalse(timeoutReceived)
    }
    @Test fun can_cancel_receive_timeout () {
        var timeoutReceived : Boolean = false
        val props : Props = fromFunc{
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
        Assert.assertFalse(timeoutReceived)
    }
    @Test  fun can_still_set_receive_timeout_after_cancelling () {
        var timeoutReceived : Boolean = false
        val props : Props = fromFunc{
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
        Assert.assertTrue(timeoutReceived)
    }
}

