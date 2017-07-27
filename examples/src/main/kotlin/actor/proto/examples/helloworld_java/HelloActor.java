package actor.proto.examples.helloworld_java;

import actor.proto.java.Actor;
import actor.proto.java.JavaActor;
import actor.proto.java.JavaContext;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;

public class HelloActor implements JavaActor {

    @NotNull
    @Override
    public Future<?> receive(@NotNull JavaContext context) {
        Object m = context.message();

        if (m instanceof String) {
            System.out.println("Hello " + m);
        }

        return Actor.done();
    }
}
