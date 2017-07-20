package proto.tests

import actor.proto.PID
import actor.proto.Props
import actor.proto.fixture.EmptyReceive
import actor.proto.fromFunc
import actor.proto.spawn
import org.junit.Assert
import org.junit.Test

open class SpawnTests {
    @Test fun given_PropsWithSpawner_SpawnShouldReturnPidCreatedBySpawner () {
        val spawnedPid : PID = PID("test", "test")
        val props : Props = fromFunc(EmptyReceive).withSpawner{ _, _, _ -> spawnedPid}
        val pid : PID = spawn(props)
        Assert.assertSame(spawnedPid, pid)
    }
}

