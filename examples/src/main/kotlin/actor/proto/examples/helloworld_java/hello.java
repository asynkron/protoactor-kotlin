package actor.proto.examples.helloworld_java;

import actor.proto.Actor;
import actor.proto.Actors;
import actor.proto.Props;
import actor.proto.Protos;


import java.io.IOException;

public class hello {
    public static void main(String[] args) throws IOException {
        Props p = Actors.fromFutureProducer(HelloActor::new);
        Protos.PID pid = Actors.spawn(p);
        Actors.send(pid,"Proto.Actor Java");
        Actors.send(pid,"After some time...");

        System.in.read();
    }
}
