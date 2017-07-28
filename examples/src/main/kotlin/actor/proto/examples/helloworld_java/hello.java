package actor.proto.examples.helloworld_java;

import actor.proto.Props;
import java.io.IOException;

import static actor.proto.java.Actors.*;
import static actor.proto.Protos.*;

public class hello {
    public static void main(String[] args) throws IOException {
        Props p = fromProducer(HelloActor::new);
        PID pid = spawn(p);
        send(pid,"Proto.Actor Java");

        System.in.read();
    }
}
