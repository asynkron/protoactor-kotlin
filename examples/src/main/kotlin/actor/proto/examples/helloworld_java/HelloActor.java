package actor.proto.examples.helloworld_java;


import actor.proto.Actors;
import actor.proto.FutureActor;
import actor.proto.FutureContext;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;

public class HelloActor implements FutureActor {

    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    @NotNull
    @Override
    public CompletableFuture receive(@NotNull FutureContext context) {
        Object m = context.message();

        if (m instanceof String) {
            System.out.println("Hello " + m);

            CompletableFuture<Void> timeoutFuture = new CompletableFuture<>();
            scheduler.schedule(() -> timeoutFuture.complete(null), 2000, TimeUnit.MILLISECONDS);
            return timeoutFuture;
        }

        return Actors.done();
    }
}
