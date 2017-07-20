//package proto.tests
//
//import actor.proto.*
//import actor.proto.fixture.DoNothingActor
//import actor.proto.fixture.TestMailbox
//import org.junit.Assert
//
//open class MiddlewareTests {
//    fun given_ReceiveMiddleware_Should_Call_Middleware_In_Order_Then_Actor_Receive () {
//        val logs : MutableList<String> = mutableListOf()
//        val testMailbox : TestMailbox = TestMailbox()
//        val props : Props = fromFunc{ c ->
//            if (c.message is )
//                logs.add("actor")
//
//
//        }
//.withReceiveMiddleware({next -> {c ->
//            if (c.message is )
//                logs.add("middleware 1")
//
//            next(c)
//        }
//}, {next -> {c ->
//            if (c.message is )
//                logs.add("middleware 2")
//
//            next(c)
//        }
//}).withMailbox{ -> testMailbox}
//        val pid : PID = spawn(props)
//        pid.send("")
//        Assert.assertEquals(3, logs.count())
//        Assert.assertEquals("middleware 1", logs[0])
//        Assert.assertEquals("middleware 2", logs[1])
//        Assert.assertEquals("actor", logs[2])
//    }
//    fun given_SenderMiddleware_Should_Call_Middleware_In_Order () {
//        val logs : MutableList<String> = mutableListOf()
//        val pid1 : PID = spawn(fromProducer{ -> DoNothingActor() })
//        val props : Props = fromFunc{c ->
//            if (c.message is )
//                c.send(pid1, "hey")
//
//
//        }
//.withSenderMiddleware({next -> {c, t, e ->
//            if (c.message is )
//                logs.add("middleware 1")
//
//            return next(c, t, e)
//        }
//}, {next -> {c, t, e ->
//            if (c.message is )
//                logs.add("middleware 2")
//
//            return next(c, t, e)
//        }
//}).withMailbox{ -> TestMailbox()}
//        val pid2 : PID = spawn(props)
//        pid2.send("")
//        Assert.assertEquals(2, logs.count)
//        Assert.assertEquals("middleware 1", logs[0])
//        Assert.assertEquals("middleware 2", logs[1])
//    }
//}
//
