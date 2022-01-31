package cc.cryptopunks.astral.wrapdrive.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias ProvideAction = () -> Action
typealias Action = suspend () -> Unit

infix fun CoroutineScope.dispatch(action: Action) {
    launch { action() }
}
