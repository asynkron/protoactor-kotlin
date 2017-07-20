//package proto.tests
//
//import actor.proto.ExponentialBackoffStrategy
//import actor.proto.RestartStatistics
//import org.junit.Assert
//import java.time.Duration
//
//open class SupervisionTests_ExponentialBackoff {
//    fun failureOutsideWindow_ZeroCount () {
//        val rs : RestartStatistics = RestartStatistics(10, DateTime.now.subtract(TimeSpan.fromSeconds(11)))
//        val strategy : ExponentialBackoffStrategy = ExponentialBackoffStrategy(Duration.ofSeconds(10), Duration.ofSeconds(1))
//        strategy.handleFailure(null, null, rs, null)
//        Assert.assertEquals(0, rs.failureCount)
//    }
//    fun failureInsideWindow_IncrementCount () {
//        val rs : RestartStatistics = RestartStatistics(10, DateTime.now.subtract(TimeSpan.fromSeconds(9)))
//        val strategy : ExponentialBackoffStrategy = ExponentialBackoffStrategy(TimeSpan.fromSeconds(10), TimeSpan.fromSeconds(1))
//        strategy.handleFailure(null, null, rs, null)
//        Assert.assertEquals(11, rs.failureCount)
//    }
//}
//
