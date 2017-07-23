package actor.proto

enum class SupervisorDirective {
    Resume,
    Restart,
    Stop,
    Escalate
}