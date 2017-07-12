package proto.router.routers

open internal class RandomPoolRouterConfig : PoolRouterConfig {
    private val _seed : Int
    constructor(poolSize : Int, seed : Int) : super(poolSize) {
        _seed = seed
    }
    constructor(poolSize : Int) : super(poolSize) {
        _seed = 0
    }
    override fun createRouterState () : RouterState {
        return RandomRouterState(_seed)
    }
}

