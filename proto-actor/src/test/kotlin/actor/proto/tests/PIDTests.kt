package actor.proto.tests

import actor.proto.*
import actor.proto.fixture.EmptyReceive
import actor.proto.fixture.TestMailbox
import actor.proto.fixture.TestProcess
import org.junit.Assert.assertSame
import org.junit.Test
import java.util.*
import kotlin.test.assertNotNull

class PIDTests {
    @Test
    fun `given actor not dead, cachedProcess should return it`() {
        val pid: PID = spawn(fromFunc(EmptyReceive))
        val p = pid.cachedProcess()
        assertNotNull(p)
    }

    @Test
    fun `given actor died, cachedProcess should not return it`() {
        val pid: PID = spawn(fromFunc(EmptyReceive).withMailbox { TestMailbox() })
        stop(pid)
        val p = pid.cachedProcess()
        assertNotNull(p)
    }

    @Test
    fun `given other process, cachedProcess should return it`() {
        val id = UUID.randomUUID().toString()
        val p = TestProcess()
        val pid = ProcessRegistry.put(id, p)
        val p2 = pid.cachedProcess()
        assertSame(p, p2)
    }
}

