package proto.tests

import actor.proto.*
import actor.proto.fixture.TestProcess
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test
import java.util.*
import kotlin.test.*

class ProcessRegistryTests {
    @Test fun `given pid does not exist, add should add local pid`() {
        val id: String = UUID.randomUUID().toString()
        val p: TestProcess = TestProcess()
        val reg: ProcessRegistry = ProcessRegistry
        val pid = reg.add(id, p)
        assertEquals(reg.address, pid.address)
    }

    @Test fun `given pid exists, add should not add local pid`() {
        val id: String = UUID.randomUUID().toString()
        val p: TestProcess = TestProcess()
        val reg: ProcessRegistry = ProcessRegistry
        reg.add(id, p)

        assertFailsWith<ProcessNameExistException> {
            val pid = reg.add(id, p)
        }
    }

    @Test fun `given pid exists, get should return it`() {
        val id: String = UUID.randomUUID().toString()
        val p: TestProcess = TestProcess()
        val reg: ProcessRegistry = ProcessRegistry
        val pid = reg.add(id, p)
        val p2: Process = reg.get(pid)
        assertSame(p, p2)
    }

    @Test fun `given pid was removed, get should return deadLetter process`() {
        val id: String = UUID.randomUUID().toString()
        val p: TestProcess = TestProcess()
        val reg: ProcessRegistry = ProcessRegistry
        val pid = reg.add(id, p)
        reg.remove(pid)
        val p2: Process = reg.get(pid)
        assertSame(DeadLetterProcess, p2)
    }

    @Test fun `given pid exists in host resolver, get should return it`() {
        val pid: PID = PID("abc", "def")
        val p: TestProcess = TestProcess()
        val reg: ProcessRegistry = ProcessRegistry
        reg.registerHostResolver { _ -> p }
        val p2: Process = reg.get(pid)
        assertSame(p, p2)
    }
}

