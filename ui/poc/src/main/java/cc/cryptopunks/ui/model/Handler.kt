package cc.cryptopunks.ui.model

import cc.cryptopunks.ui.model.internal.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext

class Handler(initial: UI.State = UI.State()) {

    val state get() = stateFlow.value.state

    val changes: Flow<UI.Change> get() = stateFlow

    fun handle(action: UI.Input) = inputFlow.trySend(action)

    private val scope = CoroutineScope(newSingleThreadContext("UIModel"))

    private val stateFlow = MutableStateFlow(UI.Change(initial))

    private val inputFlow = Channel<UI.Input>(Channel.BUFFERED)

    init {
        scope.launch {
            inputFlow.send(UI.Action.Init)
            inputFlow.consumeAsFlow().mapNotNull { input ->
                when (input) {
                    is UI.Action -> input
                    is UI.Event -> state.interpret(input)
                }
            }.collect { action ->
                stateFlow.run {
                    value = value.state.handle(listOf(action)).printLog()
                    if (UI.Action.Exit in value.output) scope.launch {
                        inputFlow.send(UI.Action.Init)
                    }
                }
            }
        }
    }
}

internal fun UI.State.interpret(event: UI.Event): UI.Action? =
    when (event) {
        UI.Event.Init -> UI.Action.Init
        is UI.Event.Configure -> UI.Action.Configure(event.config)
        is UI.Event.Text -> when (event.value) {
            text -> null
            else -> UI.Action.SetText(event.value.orEmpty())
        }
        is UI.Event.Method -> UI.Action.SetMethod(event.method)
        is UI.Event.Clicked -> UI.Action.SelectData(event.id, event.value)
        UI.Event.Action -> when {
            isReady -> UI.Action.Execute
            textIsRequiredArg -> UI.Action.SetArg(param!!.name, text)
            else -> UI.Action.SwitchMode
        }
        UI.Event.Back -> when {
            UIDisplay.Data in display -> UI.Action.DropView
            args.isNotEmpty() -> UI.Action.DropArg
            method != null -> UI.Action.DropMethod
            stack.isEmpty() -> UI.Action.Exit
            else -> UI.Action.SwitchMode
        }
    }

internal tailrec fun UI.State.handle(
    actions: List<UIMessage>,
    messages: List<UIMessage> = emptyList(),
): UI.Change =
    when {
        actions.isEmpty() -> {
            val update = UI.Element.Display + calculateDisplay()
            UI.Change(plus(update), messages + update)
        }
        else -> {
            val action = actions.first()
            val newUpdates = when (action) {
                is UIUpdate<*, *> -> listOf(action)
                is UI.Action -> execute(action)
            }
            val newState = plus(newUpdates)
            val newMessages = when (action) {
                is UIUpdate<*, *> -> messages + newUpdates
                is UI.Action -> messages + action + newUpdates
            }
            val inferred = when (action) {
                is UI.Action -> newState.infer(action)
                else -> emptyList()
            }
            val newActions = actions.drop(1) + inferred
            newState.handle(newActions, newMessages)
        }
    }

private fun UI.State.execute(action: UI.Action): UIUpdates = when (action) {
    is UI.Action.AddContext -> listOf(
        UI.Element.Context + addContext(action.context)
    )
    UI.Action.Init -> listOf(
        UI.Element.Stack.empty(),
        UI.Element.Method.empty(),
        UI.Element.Param.empty(),
        UI.Element.Args.empty(),
    )
    is UI.Action.Configure -> listOf(
        UI.Element.Config + UIConfig(config + action.config)
    )
    UI.Action.DropArg -> listOf(
        UI.Element.Args + dropLastArg()
    )
    UI.Action.DropMethod -> listOf(
        UI.Element.Method.empty(),
        UI.Element.Param.empty(),
        UI.Element.Args.empty(),
    )
    UI.Action.DropView -> listOf(
        UI.Element.Stack + dropLastView(),
        UI.Element.Method.empty(),
        UI.Element.Param.empty(),
        UI.Element.Args.empty(),
    )
    UI.Action.Execute -> listOf(
        UI.Element.Selection.empty(),
        UI.Element.Method.empty(),
        UI.Element.Text.empty(),
        UI.Element.Param.empty(),
        UI.Element.Args.empty(),
    ) + when {
        method!!.hasResult -> listOf(
            UI.Element.Stack + resolveNextView(),
        )
        else -> {
            executeCommand()
            listOf()
        }
    }
    is UI.Action.SetArg -> listOf(
        UI.Element.Args + argsWith(action.arg),
    )
    is UI.Action.SetMethod -> listOf(
        UI.Element.Method + action.method
    )
    is UI.Action.SetSelection -> listOf(
        UI.Element.Selection + action.data
    )
    is UI.Action.SelectData -> listOf(
        UI.Element.Selection + generateSelection(action)
    )
    is UI.Action.SetText -> listOf(
        UI.Element.Text + action.text
    )
    UI.Action.SwitchMode -> listOf(
        UI.Element.Mode + UIMode.values().toList().minus(mode).single()
    )
    UI.Action.CalculateMethods -> listOf(
        UI.Element.Methods + calculateMethods()
    )
    UI.Action.InferInputMethod -> when (val matching = inputMethod) {
        null -> emptyList()
        else -> listOfNotNull(
            UI.Element.Method + matching.method,
        )
    }
    UI.Action.SelectMethod -> selectionMatchingMethods().filter(UIMethod::isReady).run {
        when {
            isEmpty() -> emptyList()
            size == 1 -> listOf(
                UI.Element.Method + first().method,
            )
            else -> listOf(
            )
        }
    }
    UI.Action.SelectArg -> when (val selectedArg = argDataFromSelection()) {
        null -> listOf()
        else -> listOf(
            UI.Element.Args + argsWith(selectedArg),
            UI.Element.Selection.empty(),
        )
    }
    UI.Action.NextParam -> listOf(
        UI.Element.Param + nextParam()
    )
    is UI.Action.SetArgs -> listOf(
        UI.Element.Args + action.args
    )
    UI.Action.SwitchDisplay -> emptyList()
    UI.Action.Exit -> emptyList()
}

private fun UI.State.infer(action: UI.Action): List<UIMessage> = when (action) {
    UI.Action.Init,
    UI.Action.Execute,
    UI.Action.DropView,
    UI.Action.DropMethod,
    is UI.Action.AddContext,
    -> listOf(
        UI.Element.Mode + if (stack.isEmpty())
            UIMode.Command else
            UIMode.View,
        UI.Element.Selection.empty(),
        UI.Action.CalculateMethods,
        UI.Action.InferInputMethod,
    )

    is UI.Action.SetText
    -> listOf(
        UI.Action.CalculateMethods
    )

    is UI.Action.SetArg,
    is UI.Action.SetArgs,
    is UI.Action.SelectArg,
    -> listOf(
        UI.Action.NextParam
    ) + when {
        isReady() -> when {
            config.autoExecute -> listOf(
                UI.Action.Execute
            )
            else -> listOf(
                UI.Element.Ready + true
            )
        }
        else -> listOf(
            UI.Element.Ready + false
        )
    }

    is UI.Action.NextParam
    -> emptyList()

    is UI.Action.InferInputMethod,
    is UI.Action.SetMethod,
    is UI.Action.SelectMethod,
    -> when (method) {
        null -> listOf()
        else -> listOf(
            UI.Element.Selection.empty(),
            UI.Action.SetArgs(matchedArgs())
        )
    }

    is UI.Action.SetSelection,
    is UI.Action.SelectData,
    -> when {
        config.autoFill -> when (method) {
            null -> listOf(
                UI.Action.CalculateMethods,
                UI.Action.SelectMethod,
            )
            else -> listOf(
                UI.Action.CalculateMethods,
                UI.Action.SelectArg
            )
        }
        else -> listOf(
            UI.Action.CalculateMethods,
            UI.Action.SwitchMode,
        )
    }

    else -> emptyList()
}

private fun UI.State.calculateDisplay(): Set<UIDisplay> =
    when (mode) {
        UIMode.Command -> setOf(UIDisplay.Input) + when (method) {
            null -> setOf(UIDisplay.Panel)
            else -> setOfNotNull(
                UIDisplay.Method,
                when (val resolver = resolver) {
                    null,
                    is UIResolver.Option,
                    is UIResolver.Method -> UIDisplay.Panel
                    is UIResolver.Data -> UIDisplay.Data
                    is UIResolver.Input -> when (resolver.type) {
                        Service.Type.bool -> UIDisplay.Panel
                        else -> UIDisplay.Panel
                    }
                },
            )
        }
        UIMode.View -> setOfNotNull(
            UIDisplay.Data,
            UIDisplay.Input.takeIf { method != null && method == inputMethod?.method },
        )
    }

val UI.State.resolver get() = param?.resolvers?.minOrNull()
