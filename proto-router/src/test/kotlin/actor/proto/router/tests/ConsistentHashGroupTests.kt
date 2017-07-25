//package proto.router.tests
//
//open class ConsistentHashGroupTests {
//    companion object {
//        private val MyActorProps : Props = Actor.fromProducer{ MyTestActor() }.withMailbox{ TestMailbox() }
//
//        private object SuperIntelligentDeterministicHash {
//            fun hash (hashKey : String) : UInt32 {
//                if (hashKey.endsWith("routee1"))
//                    return 10
//
//                if (hashKey.endsWith("routee2"))
//                    return 20
//
//                if (hashKey.endsWith("routee3"))
//                    return 30
//
//                if (hashKey.endsWith("routee4"))
//                    return 40
//
//                if (hashKey.endsWith("message1"))
//                    return 9
//
//                if (hashKey.endsWith("message2"))
//                    return 19
//
//                if (hashKey.endsWith("message3"))
//                    return 29
//
//                if (hashKey.endsWith("message4"))
//                    return 39
//
//                return 0
//            }
//        }
//    }
//    private val _timeout : Duration = Duration.ofMillis(1000)
//    fun consistentHashGroupRouter_MessageWithSameHashAlwaysGoesToSameRoutee () {
//        var (router, routee1, routee2, routee3) = createRouterWith3Routees()
//        router.send(Message("message1"))
//        router.send(Message("message1"))
//        router.send(Message("message1"))
//        assertEquals(3, routee1.requestAwait("received?", _timeout))
//        assertEquals(0, routee2.requestAwait("received?", _timeout))
//        assertEquals(0, routee3.requestAwait("received?", _timeout))
//    }
//    fun consistentHashGroupRouter_MessagesWithDifferentHashesGoToDifferentRoutees () {
//        var (router, routee1, routee2, routee3) = createRouterWith3Routees()
//        router.send(Message("message1"))
//        router.send(Message("message2"))
//        router.send(Message("message3"))
//        assertEquals(1, routee1.requestAwait("received?", _timeout))
//        assertEquals(1, routee2.requestAwait("received?", _timeout))
//        assertEquals(1, routee3.requestAwait("received?", _timeout))
//    }
//    fun consistentHashGroupRouter_MessageWithSameHashAlwaysGoesToSameRoutee_EvenWhenNewRouteeAdded () {
//        var (router, routee1, routee2, routee3) = createRouterWith3Routees()
//        router.send(Message("message1"))
//        val routee4 = Actor.spawn(MyActorProps)
//        router.send(RouterAddRoutee)
//        router.send(Message("message1"))
//        assertEquals(2, routee1.requestAwait("received?", _timeout))
//        assertEquals(0, routee2.requestAwait("received?", _timeout))
//        assertEquals(0, routee3.requestAwait("received?", _timeout))
//    }
//    fun consistentHashGroupRouter_RouteesCanBeRemoved () {
//        var (router, routee1, routee2, routee3) = createRouterWith3Routees()
//        router.send(RouterRemoveRoutee)
//        val routees = router.requestAwait(RouterGetRoutees(), _timeout)
//        assertFalse (routees.pIDs.contains(routee1))
//        assertTrue (routees.pIDs.contains(routee2))
//        assertTrue (routees.pIDs.contains(routee3))
//    }
//    fun consistentHashGroupRouter_RouteesCanBeAdded () {
//        var (router, routee1, routee2, routee3) = createRouterWith3Routees()
//        val routee4 = Actor.spawn(MyActorProps)
//        router.send(RouterAddRoutee)
//        val routees = router.requestAwait(RouterGetRoutees(), _timeout)
//        assertTrue (routees.pIDs.contains(routee1))
//        assertTrue (routees.pIDs.contains(routee2))
//        assertTrue (routees.pIDs.contains(routee3))
//        assertTrue (routees.pIDs.contains(routee4))
//    }
//    fun consistentHashGroupRouter_RemovedRouteesNoLongerReceiveMessages () {
//        var (router, routee1, _, _) = createRouterWith3Routees()
//        router.send(RouterRemoveRoutee)
//        router.send(Message("message1"))
//        assertEquals(0, routee1.requestAwait("received?", _timeout))
//    }
//    fun consistentHashGroupRouter_AddedRouteesReceiveMessages () {
//        var (router, _, _, _) = createRouterWith3Routees()
//        val routee4 = Actor.spawn(MyActorProps)
//        router.send(RouterAddRoutee)
//        router.send(Message("message4"))
//        assertEquals(1, routee4.requestAwait("received?", _timeout))
//    }
//    fun consistentHashGroupRouter_MessageIsReassignedWhenRouteeRemoved () {
//        var (router, routee1, routee2, _) = createRouterWith3Routees()
//        router.send(Message("message1"))
//        assertEquals(1, routee1.requestAwait("received?", _timeout))
//        router.send(RouterRemoveRoutee)
//        router.send(Message("message1"))
//        assertEquals(1, routee2.requestAwait("received?", _timeout))
//    }
//    fun consistentHashGroupRouter_AllRouteesReceiveRouterBroadcastMessages () {
//        var (router, routee1, routee2, routee3) = createRouterWith3Routees()
//        router.send(RouterBroadcastMessage)
//        assertEquals(1, routee1.requestAwait("received?", _timeout))
//        assertEquals(1, routee2.requestAwait("received?", _timeout))
//        assertEquals(1, routee3.requestAwait("received?", _timeout))
//    }
//    private fun createRouterWith3Routees () :  {
//        val routee1 = Actor.spawnNamed(MyActorProps, Guid.newGuid() + "routee1")
//        val routee2 = Actor.spawnNamed(MyActorProps, Guid.newGuid() + "routee2")
//        val routee3 = Actor.spawnNamed(MyActorProps, Guid.newGuid() + "routee3")
//        val props = Router.newConsistentHashGroup(SuperIntelligentDeterministicHash.hash, 1, routee1, routee2, routee3).withMailbox{ TestMailbox() }
//        val router = Actor.spawn(props)
//        return NonSupportedTuple4(router, routee1, routee2, routee3)
//    }
//
//    open internal class Message : Hashable {
//        private  _value : String
//        constructor(value : String)  {
//            _value = value
//        }
//        override fun hashBy () : String {
//            return _value
//        }
//        fun toString () : String {
//            return _value
//        }
//    }
//
//    open internal class MyTestActor : Actor {
//        private val _receivedMessages : MutableList<String> = mutableListOf()
//        suspend override fun receiveAsync (context : Context) {
//            val tmp = context.message
//            when (tmp) {
//                is String -> {
//                    val msg = tmp
//                    context.sender.send(_receivedMessages.count())
//                }
//                is Message -> {
//                    val msg = tmp
//                    _receivedMessages.add(msg.toString())
//                }
//            }
//            return Actor.Done
//        }
//    }
//}
//
