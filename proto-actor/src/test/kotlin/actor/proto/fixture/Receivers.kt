package actor.proto.fixture

import actor.proto.Context


val EmptyReceive: suspend Context.(msg: Any) -> Unit = { }

