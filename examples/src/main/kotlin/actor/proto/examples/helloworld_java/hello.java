package actor.proto.examples.helloworld_java;

import actor.proto.Props;

import java.io.IOException;

import static actor.proto.Protos.PID;
import static actor.proto.java.Actors.*;

class hello {
    public static void main(String[] args) throws IOException {
        Props props = fromProducer(HelloActor::new);
        PID pid = spawn(props);
        send(pid, "Proto.Actor Java");
        System.in.read();
    }
}
