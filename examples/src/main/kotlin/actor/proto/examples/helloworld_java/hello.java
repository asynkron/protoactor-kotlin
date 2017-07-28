package actor.proto.examples.helloworld_java;

import actor.proto.Props;
import actor.proto.Protos;

import java.io.IOException;

import static actor.proto.Actors.*;

public class hello {
    public static void main(String[] args) throws IOException {
        Props p = fromFutureProducer(HelloActor::new);
        Protos.PID pid = spawn(p);
        send(pid,"Proto.Actor Java");

        System.in.read();
    }
}
