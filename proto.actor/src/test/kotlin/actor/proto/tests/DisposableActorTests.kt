//package actor.proto.tests
//
//import actor.proto.*
//import actor.proto.fixture.TestMailbox
//import actor.proto.fixture.TestMailboxStatistics
//import org.junit.Assert
//
//class DisposableActorTests {
//    fun whenActorStopped_DisposeIsCalled () {
//        var disposeCalled : Boolean = false
//        val props : Props = fromProducer{ DisposableActor{ disposeCalled = true} }.withMailbox{ TestMailbox() }
//        val pid : PID = spawn(props)
//        pid.stop()
//        Assert.assertTrue(disposeCalled)
//    }
//    fun whenActorRestarted_DisposeIsCalled () {
//        val childMailboxStats : TestMailboxStatistics = TestMailboxStatistics{it is Stopped}
//        var disposeCalled : Boolean = false
//        val strategy : OneForOneStrategy = OneForOneStrategy({ _, _ -> SupervisorDirective.Restart}, 0, null)
//        val childProps : Props = fromProducer{ DisposableActor{ disposeCalled = true} }.withMailbox{ UnboundedMailbox.create(childMailboxStats)}.withChildSupervisorStrategy(strategy)
//        val props : Props = fromProducer{ SupervisingActor(childProps) }.withMailbox{ TestMailbox()}.withChildSupervisorStrategy(strategy)
//        val parentPID : PID = spawn(props)
//        parentPID.send("crash")
//        childMailboxStats.reset.wait(1000)
//        Assert.assertTrue(disposeCalled)
//    }
//    fun whenActorResumed_DisposeIsNotCalled () {
//        val childMailboxStats : TestMailboxStatistics = TestMailboxStatistics{it is Stopped}
//        var disposeCalled : Boolean = false
//        val strategy : OneForOneStrategy = OneForOneStrategy({pid, reason -> SupervisorDirective.Resume}, 0, null)
//        val childProps : Props = fromProducer{ DisposableActor{ disposeCalled = true} }.withMailbox{ UnboundedMailbox.create(childMailboxStats)}.withChildSupervisorStrategy(strategy)
//        val props : Props = fromProducer{ SupervisingActor(childProps) }.withMailbox{ TestMailbox()}.withChildSupervisorStrategy(strategy)
//        val parentPID : PID = spawn(props)
//        parentPID.send("crash")
//        childMailboxStats.reset.wait(1000)
//        Assert.false(disposeCalled)
//    }
//    fun whenActorWithChildrenStopped_DisposeIsCalledInEachChild () {
//        var child1Disposed : Boolean = false
//        var child2Disposed : Boolean = false
//        val child1MailboxStats : TestMailboxStatistics = TestMailboxStatistics{it is Stopped}
//        val child2MailboxStats : TestMailboxStatistics = TestMailboxStatistics{it is Stopped}
//        val strategy : AllForOneStrategy = AllForOneStrategy({pid, reason -> SupervisorDirective.Stop}, 1, null)
//        val child1Props : Props = fromProducer{ DisposableActor{ child1Disposed = true} }.withMailbox{ UnboundedMailbox.create(child1MailboxStats)}
//        val child2Props : Props = fromProducer{ DisposableActor{ child2Disposed = true} }.withMailbox{ UnboundedMailbox.create(child2MailboxStats)}
//        val parentProps : Props = fromProducer{ ParentWithMultipleChildrenActor(child1Props, child2Props) }.withChildSupervisorStrategy(strategy)
//        val parent : PID = spawn(parentProps)
//        parent.send("crash")
//        child1MailboxStats.reset.wait(1000)
//        child2MailboxStats.reset.wait(1000)
//        Assert.assertTrue(child1Disposed)
//        Assert.assertTrue(child2Disposed)
//    }
//
//    open private class SupervisingActor : Actor {
//        private val _childProps : Props
//        private var _childPID : PID? = null
//        constructor(childProps : Props)  {
//            _childProps = childProps
//        }
//        suspend override fun receive (context : Context) {
//            if (context.message is Started)
//                _childPID = context.spawn(_childProps)
//
//            if (context.message is )
//                _childPID.send(context.message)
//
//
//        }
//    }
//
////    open private class DisposableActor : Actor, Disposable {
////        private val _onDispose : () -> Unit
////        constructor(onDispose : () -> Unit)  {
////            _onDispose = onDispose
////        }
////        suspend override fun receive (context : Context) {
////            val tmp = context.message
////            when (tmp) {
////                is String -> {
////                    val msg = tmp
////                    throw Exception()
////                }
////            }
////
////        }
////        override fun dispose () {
////            _onDispose()
////        }
////    }
//
//    open private class ParentWithMultipleChildrenActor : Actor {
//        private val _child1Props : Props
//        private val _child2Props : Props
//        constructor(child1Props : Props, child2Props : Props)  {
//            _child1Props = child1Props
//            _child2Props = child2Props
//        }
//        var child1 : PID
//        var child2 : PID
//        suspend override fun receive (context : Context) {
//            if (context.message is Started) {
//                child1 = context.spawn(_child1Props)
//                child2 = context.spawn(_child2Props)
//            }
//            if (context.message is ) {
//                child1.send(context.message)
//            }
//
//        }
//    }
//}
//
