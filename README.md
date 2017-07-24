# Proto.Actor
Ultra-fast, distributed, cross-platform actors.

## Installing

...

## Source code
This is the Kotlin repository for Proto Actor.

Other implementations:
* C#  [https://github.com/AsynkronIT/protoactor-dotnet](https://github.com/AsynkronIT/protoactor-dotnet)
* Go: [https://github.com/AsynkronIT/protoactor-go](https://github.com/AsynkronIT/protoactor-go)
* Python (unstable/WIP): [https://github.com/AsynkronIT/protoactor-python](https://github.com/AsynkronIT/protoactor-python)
* JavaScript (unstable/WIP): [https://github.com/AsynkronIT/protoactor-js](https://github.com/AsynkronIT/protoactor-js)

## How to build

...

## Design principles

**Minimalistic API** - The API should be small and easy to use. Avoid enterprisey containers and configurations.

**Build on existing technologies** - There are already a lot of great technologies for e.g. networking and clustering. Build on those instead of reinventing them. E.g. gRPC streams for networking, Consul for clustering.

**Pass data, not objects** - Serialization is an explicit concern - don't try to hide it. Protobuf all the way.

**Be fast** - Do not trade performance for magic API trickery.

Inprocess Ping-Pong results:
```
Dispatcher		Elapsed		Msg/sec
300			273		116885925
400			217		147426522
500			150		213037390
600			85		375979638
700			87		364621820
800			83		381552772 <-- 380+ mil msg/sec
```

## Getting started

The best place currently for learning how to use Proto.Actor is the [examples](https://github.com/AsynkronIT/protoactor-kotlin/tree/dev/examples). Documentation and guidance is under way, but not yet complete, and can be found on the [website](http://proto.actor/docs/kotlin/).

### Hello world

Define a message type:

```kotlin
data class Hello(val who : String)
```

Define an actor:

```kotlin
class HelloActor : Actor
{
    suspend override fun receive(context : Context)
    {
        val msg = context.message
        when (msg)
        {
            is Hello -> println("Hello " + msg.who)
        }
    }
}
```

Spawn it and send a message to it:

```kotlin
val props = fromProducer({ HelloActor() })
val pid = spawn(props)
pid.send(Hello("Kotlin"))
```

You should see the output `Hello Kotlin`.

### Support

Many thanks to [JetBrains](https://www.jetbrains.com) for support!

Also thanks to [ej-technologies.com for their Java profiler - JProfiler](https://www.ej-technologies.com/products/jprofiler/overview.html)

