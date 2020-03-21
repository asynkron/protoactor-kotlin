package actor.proto.tests

import actor.proto.DeadLetterProcess
import actor.proto.PID
import actor.proto.ProcessNameExistException
import actor.proto.ProcessRegistry
import actor.proto.fixture.TestProcess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class ProcessRegistryTests {
    @Test
    fun `given pid does not exist, add should add local pid`() {
        val id = UUID.randomUUID().toString()
        val p = TestProcess()
        val reg = ProcessRegistry
        val pid = reg.put(id, p)
        assertEquals(reg.address, pid.address)
    }

    @Test
    fun `given pid exists, add should not add local pid`() {
        val id = UUID.randomUUID().toString()
        val p = TestProcess()
        val reg = ProcessRegistry
        reg.put(id, p)

        assertThrows<ProcessNameExistException> ("Should throw an exception") {
            reg.put(id, p)
        }
    }

    @Test
    fun `given pid exists, get should return it`() {
        val id = UUID.randomUUID().toString()
        val p = TestProcess()
        val reg = ProcessRegistry
        val pid = reg.put(id, p)
        val p2 = reg.get(pid)
        assertSame(p, p2)
    }

    @Test
    fun `given pid was removed, get should return deadLetter process`() {
        val id = UUID.randomUUID().toString()
        val p = TestProcess()
        val reg = ProcessRegistry
        val pid = reg.put(id, p)
        reg.remove(pid)
        val p2 = reg.get(pid)
        assertSame(DeadLetterProcess, p2)
    }

    @Test
    fun `given pid exists in host resolver, get should return it`() {
        val pid = PID("abc", "def")
        val p = TestProcess()
        val reg = ProcessRegistry
        reg.registerHostResolver { _ -> p }
        val p2 = reg.get(pid)
        assertSame(p, p2)
    }
}

