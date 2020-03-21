package actor.proto.tests

import actor.proto.*
import actor.proto.fixture.DoNothingActor
import actor.proto.fixture.TestMailbox
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MiddlewareTests {
    @Test
    fun `given ReceiveMiddleware should call middleware in order then actor receive`() {
        val logs: MutableList<String> = mutableListOf()
        val testMailbox = TestMailbox()
        val props: Props = fromFunc { msg ->
            if (msg is String) {
                logs.add("actor")
            }
        }.withReceiveMiddleware({ next ->
            { c ->
                if (c.message is String)
                    logs.add("middleware 1")

                next(c)
            }
        }, { next ->
            { c ->
                if (c.message is String)
                    logs.add("middleware 2")

                next(c)
            }
        }).withMailbox { testMailbox }
        val pid = spawn(props)

        send(pid, "")

        assertEquals(listOf("middleware 1", "middleware 2", "actor"), logs)
    }

    @Test
    fun `given SenderMiddleware should call middleware in order`() {
        val logs: MutableList<String> = mutableListOf()
        val pid1 = spawn(fromProducer { DoNothingActor() })
        val props: Props = fromFunc { msg ->
            if (msg is String) {
                send(pid1, "hey")
            }
        }.withSenderMiddleware({ next ->
            { c, t, e ->
                if (c.message is String)
                    logs.add("middleware 1")

                next(c, t, e)
            }
        }, { next ->
            { c, t, e ->
                if (c.message is String)
                    logs.add("middleware 2")

                next(c, t, e)
            }
        }).withMailbox { TestMailbox() }
        val sender = spawn(props)

        send(sender, "")

        assertEquals(listOf("middleware 1", "middleware 2"), logs)
    }
}
