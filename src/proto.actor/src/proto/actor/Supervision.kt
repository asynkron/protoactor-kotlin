package proto.actor

import java.time.Duration

class Supervision {
    companion object {
        val defaultStrategy: ISupervisorStrategy = OneForOneStrategy({ _, _ -> SupervisorDirective.Restart }, 10, Duration.ofSeconds(10))
    }
}


