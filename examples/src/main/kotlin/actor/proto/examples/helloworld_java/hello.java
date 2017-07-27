package actor.proto.examples.helloworld_java;

import actor.proto.Props;
import actor.proto.Protos;
import actor.proto.java.Actor;

import java.io.IOException;

public class hello {
    public static void main(String[] args) throws IOException {
        Props p = Actor.fromProducer(HelloActor::new);
        Protos.PID pid = Actor.spawn(p);
        Actor.send(pid,"Proto.Actor Java");

        System.in.read();
    }
}
