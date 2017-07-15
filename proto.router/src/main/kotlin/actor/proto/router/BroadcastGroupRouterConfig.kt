package actor.proto.router


internal class BroadcastGroupRouterConfig(routees: Set<actor.proto.PID>) : GroupRouterConfig(routees) {
    override fun createRouterState(): RouterState = BroadcastRouterState()
}

