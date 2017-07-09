package proto.mailbox
class Task{
    companion object {
        fun fromResult(value:Any) : Task{
            return Task()
        }
    }
    fun wait() : Unit{

    }

    val isFaulted = false
    val exception : Exception? = null
}