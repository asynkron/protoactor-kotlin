package proto.tests

import actor.proto.*
import actor.proto.fixture.EmptyReceive
import actor.proto.fixture.TestMailbox
import actor.proto.fixture.TestProcess
import org.junit.Assert
import org.junit.Test
import java.util.*

class PIDTests {
    @Test fun given_ActorNotDead_Ref_ShouldReturnIt() {
        val pid: PID = spawn(fromFunc(EmptyReceive))
        val p = pid.cachedProcess()
        Assert.assertNotNull(p)
    }

    @Test fun given_ActorDied_Ref_ShouldNotReturnIt() {
        val pid: PID = spawn(fromFunc(EmptyReceive).withMailbox { TestMailbox() })
        pid.stop()
        val p = pid.cachedProcess()
        Assert.assertNotNull(p)
    }

    @Test fun given_OtherProcess_Ref_ShouldReturnIt() {
        val id = UUID.randomUUID().toString()
        val p: TestProcess = TestProcess()
        val pid = ProcessRegistry.add(id, p)
        val p2 = pid.cachedProcess()
        Assert.assertSame(p, p2)
    }
}

