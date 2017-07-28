package actor.proto.examples.helloworld_java;


import actor.proto.java.Actor;
import actor.proto.java.Context;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.CompletableFuture;
import static actor.proto.java.Actors.done;

public class HelloActor implements Actor {

    @NotNull
    @Override
    public CompletableFuture receive(@NotNull Context context) {
        Object m = context.message();

        if (m instanceof String) {
            System.out.println("Hello " + m);
        }

        return done();
    }
}
