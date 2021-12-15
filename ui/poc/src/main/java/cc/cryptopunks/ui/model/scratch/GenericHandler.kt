package cc.cryptopunks.ui.model.scratch

import cc.cryptopunks.ui.model.UI
import cc.cryptopunks.ui.model.handle
import cc.cryptopunks.ui.model.interpret
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext


fun handler(
    initial: UI.State
) = GenericHandler(
    UI.Change(initial), { inputs: List<UI.Input> ->
        val actions = inputs.mapNotNull { input ->
            when (input) {
                is UI.Action -> input
                is UI.Event -> state.interpret(input)
            }
        }
        state.handle(actions)
    }, {
        when {
            state.methods.isEmpty() && state.context.methods.isNotEmpty() -> UI.Action.Init
            UI.Action.Exit in output -> UI.Action.Init
            else -> null
        }
    }
)


open class GenericHandler<Action, State>(
    initial: State,
    handle: State.(List<Action>) -> State,
    infer: State.() -> Action? = { null },
) {
    val current: State get() = state.value

    val changes: Flow<State> get() = state

    fun handle(action: Action) = actions.trySend(action)


    private val scope = CoroutineScope(newSingleThreadContext("UIModel"))

    private val actions = Channel<Action>(Channel.BUFFERED)

    private val state = MutableStateFlow(initial)

    init {
        scope.launch {
            state.value.infer()?.let { action ->
                actions.send(action)
            }
            actions.consumeAsFlow().collect { action ->
                state.run {
                    value = value.handle(listOf(action))
                    value.infer()?.let { action ->
                        scope.launch {
                            actions.send(action)
                        }
                    }
                }
            }
        }
    }
}
