package actor.proto.java

import java.util.concurrent.Future

interface JavaActor {
    fun receive(context: JavaContext): Future<Void>
}