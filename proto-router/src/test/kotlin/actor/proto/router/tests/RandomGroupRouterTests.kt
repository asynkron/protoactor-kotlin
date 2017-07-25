//package proto.router.tests
//
//open class RandomGroupRouterTests {
//    companion object {
//        private val MyActorProps : Props = Actor.fromProducer{ MyTestActor() }
//    }
//    private val _timeout : Duration = Duration.ofMillis(1000)
//    fun randomGroupRouter_RouteesReceiveMessagesInRandomOrder () {
//        var (router, routee1, routee2, routee3) = createRouterWith3Routees()
//        router.send("1")
//        router.send("2")
//        router.send("3")
//        assertEquals("2", routee1.requestAwait("received?", _timeout))
//        assertEquals("3", routee2.requestAwait("received?", _timeout))
//        assertEquals("1", routee3.requestAwait("received?", _timeout))
//    }
//    fun randomGroupRouter_NewlyAddedRouteesReceiveMessages () {
//        var (router, routee1, routee2, routee3) = createRouterWith3Routees()
//        val routee4 = Actor.spawn(MyActorProps)
//        router.send(RouterAddRoutee)
//        router.send("1")
//        router.send("2")
//        router.send("3")
//        router.send("4")
//        assertEquals("2", routee1.requestAwait("received?", _timeout))
//        assertEquals(null, routee2.requestAwait("received?", _timeout))
//        assertEquals("3", routee3.requestAwait("received?", _timeout))
//        assertEquals("4", routee4.requestAwait("received?", _timeout))
//    }
//    fun randomGroupRouter_RemovedRouteesDoNotReceiveMessages () {
//        var (router, routee1, _, _) = createRouterWith3Routees()
//        router.send(RouterRemoveRoutee)
//        for (i in 0 until 100) {
//            router.send(i.toString())
//        }
//        assertEquals(null, routee1.requestAwait("received?", _timeout))
//    }
//    fun randomGroupRouter_RouteesCanBeRemoved () {
//        var (router, routee1, routee2, routee3) = createRouterWith3Routees()
//        router.send(RouterRemoveRoutee)
//        val routees = router.requestAwait(RouterGetRoutees(), _timeout)
//        assertFalse (routees.pIDs.contains(routee1))
//        assertTrue (routees.pIDs.contains(routee2))
//        assertTrue (routees.pIDs.contains(routee3))
//    }
//    fun randomGroupRouter_RouteesCanBeAdded () {
//        var (router, routee1, routee2, routee3) = createRouterWith3Routees()
//        val routee4 = Actor.spawn(MyActorProps)
//        router.send(RouterAddRoutee)
//        val routees = router.requestAwait(RouterGetRoutees(), _timeout)
//        assertTrue (routees.pIDs.contains(routee1))
//        assertTrue (routees.pIDs.contains(routee2))
//        assertTrue (routees.pIDs.contains(routee3))
//        assertTrue (routees.pIDs.contains(routee4))
//    }
//    fun randomGroupRouter_AllRouteesReceiveRouterBroadcastMessages () {
//        var (router, routee1, routee2, routee3) = createRouterWith3Routees()
//        router.send(RouterBroadcastMessage)
//        assertEquals("hello", routee1.requestAwait("received?", _timeout))
//        assertEquals("hello", routee2.requestAwait("received?", _timeout))
//        assertEquals("hello", routee3.requestAwait("received?", _timeout))
//    }
//    private fun createRouterWith3Routees () :  {
//        val routee1 = Actor.spawn(MyActorProps)
//        val routee2 = Actor.spawn(MyActorProps)
//        val routee3 = Actor.spawn(MyActorProps)
//        val props = Router.newRandomGroup(10000, routee1, routee2, routee3).withMailbox{ TestMailbox() }
//        val router = Actor.spawn(props)
//        return NonSupportedTuple4(router, routee1, routee2, routee3)
//    }
//
//    open internal class MyTestActor : Actor {
//        private lateinit var _received : String
//        suspend override fun receiveAsync (context : Context) {
//            val tmp = context.message
//            when (tmp) {
//                is String -> {
//                    val msg = tmp
//                    context.sender.send(_received)
//                }
//                is String -> {
//                    val msg = tmp
//                    _received = msg
//                }
//            }
//            return Actor.Done
//        }
//    }
//}
//
