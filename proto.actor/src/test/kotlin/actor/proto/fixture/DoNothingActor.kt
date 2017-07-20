package actor.proto.fixture

import actor.proto.Actor
import actor.proto.Context

open class DoNothingActor : Actor {
    suspend override fun receive (context : Context) {}
}

