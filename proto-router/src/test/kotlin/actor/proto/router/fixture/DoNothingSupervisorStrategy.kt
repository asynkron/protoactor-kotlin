package actor.proto.router.fixture

import actor.proto.PID
import actor.proto.RestartStatistics
import actor.proto.Supervisor
import actor.proto.SupervisorStrategy

class DoNothingSupervisorStrategy : SupervisorStrategy {
    override fun handleFailure(supervisor: Supervisor, child: PID, rs: RestartStatistics, reason: Exception) {
    }
}

