package actor.proto.examples.java.helloworld;

import actor.proto.Props;

import java.io.IOException;

import static actor.proto.Protos.PID;
import static actor.proto.java.Actors.fromProducer;
import static actor.proto.java.Actors.send;
import static actor.proto.java.Actors.spawn;

public class Hello {
    public static void main(String[] args) throws IOException {
        Props props = fromProducer(HelloActor::new);
        PID pid = spawn(props);
        send(pid, "Proto.Actor Java");
        System.in.read();
    }
}
