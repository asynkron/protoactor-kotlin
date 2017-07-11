package proto.actor

import java.time.Duration

object Supervision {
    val defaultStrategy = OneForOneStrategy({ _, _ -> SupervisorDirective.Restart }, 10, Duration.ofSeconds(10))
}


