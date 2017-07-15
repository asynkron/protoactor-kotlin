package proto.actor

import java.time.Duration

interface Actor {
    suspend fun receiveAsync(context: Context)
}