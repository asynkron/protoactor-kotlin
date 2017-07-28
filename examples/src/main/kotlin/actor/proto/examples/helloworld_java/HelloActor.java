package actor.proto.examples.helloworld_java;

import actor.proto.FutureActor;
import actor.proto.FutureContext;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static actor.proto.Actors.done;

public class HelloActor implements FutureActor {

    @NotNull
    @Override
    public CompletableFuture receive(@NotNull FutureContext context) {
        Object m = context.message();

        if (m instanceof String) {
            System.out.println("Hello " + m);
        }

        return done();
    }
}
