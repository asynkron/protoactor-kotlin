//package proto.tests
//
//import actor.proto.*
//import actor.proto.fixture.TestMailboxStatistics
//import actor.proto.mailbox.ResumeMailbox
//import org.junit.Assert
//
//class SupervisionTests_AllForOne {
//    companion object {
//        private val Exception : Exception = Exception("boo hoo")
//    }
//
//    class ParentActor(child1Props: Props, child2Props: Props) : Actor {
//        private val _child1Props : Props = child1Props
//        private val _child2Props : Props = child2Props
//        lateinit var child1 : PID
//        lateinit var child2 : PID
//        suspend override fun receive (context : Context) {
//            if (context.message is Started) {
//                child1 = context.spawnChild(_child1Props)
//                child2 = context.spawnChild(_child2Props)
//            }
//            if (context.message is ) {
//                child1.send(context.message)
//            }
//        }
//    }
//
//    class ChildActor : Actor {
//        suspend override fun receive (context : Context) {
//            val tmp = context.message
//            when (tmp) {
//                is String -> {
//                    throw Exception
//                }
//            }
//
//        }
//    }
//    fun allForOneStrategy_Should_ResumeChildOnFailure () {
//        val child1MailboxStats : TestMailboxStatistics = TestMailboxStatistics{it is ResumeMailbox }
//        val child2MailboxStats : TestMailboxStatistics = TestMailboxStatistics{it is ResumeMailbox}
//        val strategy : AllForOneStrategy = AllForOneStrategy({_, _ -> SupervisorDirective.Resume}, 1, null)
//        val child1Props : Props = fromProducer{ ChildActor()}.withMailbox{ UnboundedMailbox.create(child1MailboxStats)}
//        val child2Props : Props = fromProducer{ ChildActor()}.withMailbox{ UnboundedMailbox.create(child2MailboxStats)}
//        val parentProps : Props = fromProducer{ ParentActor(child1Props, child2Props)}.withChildSupervisorStrategy(strategy)
//        val parent : PID = spawn(parentProps)
//        parent.send("hello")
//        child1MailboxStats.reset.wait(1000)
//        Assert.contains(ResumeMailbox, child1MailboxStats.posted)
//        Assert.contains(ResumeMailbox, child1MailboxStats.received)
//        Assert.doesNotContain(ResumeMailbox, child2MailboxStats.posted)
//        Assert.doesNotContain(ResumeMailbox, child2MailboxStats.received)
//    }
//    fun allForOneStrategy_Should_StopAllChildrenOnFailure () {
//        val child1MailboxStats : TestMailboxStatistics = TestMailboxStatistics{it is Stopped}
//        val child2MailboxStats : TestMailboxStatistics = TestMailboxStatistics{it is Stopped}
//        val strategy : AllForOneStrategy = AllForOneStrategy({_, _ -> SupervisorDirective.Stop}, 1, null)
//        val child1Props : Props = fromProducer{ ChildActor()}.withMailbox{ UnboundedMailbox.create(child1MailboxStats)}
//        val child2Props : Props = fromProducer{ ChildActor()}.withMailbox{ UnboundedMailbox.create(child2MailboxStats)}
//        val parentProps : Props = fromProducer{ ParentActor(child1Props, child2Props)}.withChildSupervisorStrategy(strategy)
//        val parent : PID = spawn(parentProps)
//        parent.send("hello")
//        child1MailboxStats.reset.wait(1000)
//        child2MailboxStats.reset.wait(1000)
//        Assert.contains(Stop.Instance, child1MailboxStats.posted)
//        Assert.contains(Stop.Instance, child1MailboxStats.received)
//        Assert.contains(Stop.Instance, child2MailboxStats.posted)
//        Assert.contains(Stop.Instance, child2MailboxStats.received)
//    }
//    fun allForOneStrategy_Should_RestartAllChildrenOnFailure () {
//        val child1MailboxStats : TestMailboxStatistics = TestMailboxStatistics{it is Stopped}
//        val child2MailboxStats : TestMailboxStatistics = TestMailboxStatistics{it is Stopped}
//        val strategy : AllForOneStrategy = AllForOneStrategy({_, _ -> SupervisorDirective.Restart}, 1, null)
//        val child1Props : Props = fromProducer{ ChildActor()}.withMailbox{ UnboundedMailbox.create(child1MailboxStats)}
//        val child2Props : Props = fromProducer{ ChildActor()}.withMailbox{ UnboundedMailbox.create(child2MailboxStats)}
//        val parentProps : Props = fromProducer{ ParentActor(child1Props, child2Props)}.withChildSupervisorStrategy(strategy)
//        val parent : PID = spawn(parentProps)
//        parent.send("hello")
//        child1MailboxStats.reset.wait(1000)
//        child2MailboxStats.reset.wait(1000)
//        Assert.contains(child1MailboxStats.posted, {it is Restart})
//        Assert.contains(child1MailboxStats.received, {it is Restart})
//        Assert.contains(child2MailboxStats.posted, {it is Restart})
//        Assert.contains(child2MailboxStats.received, {it is Restart})
//    }
//    fun allForOneStrategy_Should_PassExceptionOnRestart () {
//        val child1MailboxStats : TestMailboxStatistics = TestMailboxStatistics{it is Stopped}
//        val child2MailboxStats : TestMailboxStatistics = TestMailboxStatistics{it is Stopped}
//        val strategy : AllForOneStrategy = AllForOneStrategy({_, _ -> SupervisorDirective.Restart}, 1, null)
//        val child1Props : Props = fromProducer{ ChildActor()}.withMailbox{  UnboundedMailbox.create(child1MailboxStats)}
//        val child2Props : Props = fromProducer{ ChildActor()}.withMailbox{  UnboundedMailbox.create(child2MailboxStats)}
//        val parentProps : Props = fromProducer{ ParentActor(child1Props, child2Props)}.withChildSupervisorStrategy(strategy)
//        val parent : PID = spawn(parentProps)
//        parent.send("hello")
//        child1MailboxStats.reset.wait(1000)
//        child2MailboxStats.reset.wait(1000)
//        Assert.contains(child1MailboxStats.posted, {msg -> (msg is Restart ) && msg.reason == Exception})
//        Assert.contains(child1MailboxStats.received, {msg -> (msg is Restart ) && msg.reason == Exception})
//        Assert.contains(child2MailboxStats.posted, {msg -> (msg is Restart ) && msg.reason == Exception})
//        Assert.contains(child2MailboxStats.received, {msg -> (msg is Restart ) && msg.reason == Exception})
//    }
//    fun allForOneStrategy_Should_EscalateFailureToParent () {
//        val parentMailboxStats : TestMailboxStatistics = TestMailboxStatistics{it is Stopped}
//        val strategy : AllForOneStrategy = AllForOneStrategy({_, _ -> SupervisorDirective.Escalate}, 1, null)
//        val childProps : Props = fromProducer{ ChildActor()}
//        val parentProps : Props = fromProducer{ ParentActor(childProps, childProps)}.withChildSupervisorStrategy(strategy).withMailbox{ UnboundedMailbox.create(parentMailboxStats)}
//        val parent : PID = spawn(parentProps)
//        parent.send("hello")
//        parentMailboxStats.reset.wait(1000)
//        val failure : Failure =
//        Assert.isType<Exception>(failure.reason)
//    }
//}
//
