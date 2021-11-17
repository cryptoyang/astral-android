package cc.cryptopunks.ui.poc.model

import cc.cryptopunks.ui.poc.model.helper.*
import java.util.concurrent.atomic.AtomicReference

fun eventHandler(state: UI.State): UIHandler =
    eventHandler(AtomicReference(state))

private fun eventHandler(
    stateRef: AtomicReference<UI.State>,
): UIHandler = { event: UI.Event ->

    var state: UI.State = stateRef.get()
    var remaining: List<UIMessage> = state.generateMessages(event)
    var acc: List<UIMessage> = remaining

    while (remaining.isNotEmpty()) {
        val next = remaining.first()
        val newState = state.update(next)
        val results = when (next) {
            is UIUpdate<*, *> -> newState.processUpdate(event, next)
            UI.Action.Exit -> emptyList()
        }
        state = newState
        remaining = results + remaining.drop(1)
        acc = acc + results
    }

    val output = acc.map { message ->
        when (message) {
            is UI.Action -> message
            is UIUpdate<*, *> -> message.element
        }
    }

    stateRef.set(state)

    UI.Change(event, state, output)
}

private fun UI.State.generateMessages(
    event: UI.Event
): List<UIMessage> = when (event) {

    UI.Event.Init -> listOf(
        UI.Element.Stack + emptyList()
    )

    is UI.Event.Text -> listOf(
        UI.Element.Text + event.value.orEmpty(),
    )

    is UI.Event.Method -> listOf(
        UI.Element.Method + event.method,
    )

    is UI.Event.Clicked -> when {
        isRequiredArg(event) -> listOf(
            UI.Element.Args + nextArg(event.value),
        )
        else -> listOf(
            UI.Element.Selection + listOf(
                UIData(
                    type = context.model.types[event.id]!!,
                    value = event.value
                )
            )
        )
    }

    UI.Event.Action -> when {
        isReady -> listOf(
            UI.Element.Stack + resolveNextView()
        )
        isRequiredArg(text) -> listOf(
            UI.Element.Args + nextArg(text),
        )
        else -> listOf(
            UI.Element.Display + switchDisplay()
        )
    }

    UI.Event.Back -> when {
        display == UIDisplay.Data -> listOf(
            UI.Element.Stack + dropLastView(),
        )
        args.isNotEmpty() -> listOf(
            UI.Element.Args + dropLastArg()
        )
        method != null -> listOf(
            UI.Element.Stack + stack,
            UI.Element.Display + UIDisplay.Panel,
        )
        stack.isEmpty() -> listOf(
            UI.Action.Exit
        )
        else -> listOf(
            UI.Element.Display + switchDisplay(),
        )
    }
}

private fun UI.State.processUpdate(
    event: UI.Event,
    update: UIUpdate<*, *>
): List<UIMessage> =
    when (update.element) {
        UI.Element.Context -> emptyList()
        UI.Element.Stack -> listOf(
            UI.Element.Display + displayDataOrPanel(),
            UI.Element.Text.default(),
            UI.Element.Method.default(),
        )
        UI.Element.Display -> emptyList()
        UI.Element.Method -> listOf(
            UI.Element.Selection + emptyList(),
            UI.Element.Args + when {
                config.autoFill -> matchedArgs()
                else -> defaultArgs()
            },
        )
        UI.Element.Args -> listOf(
            UI.Element.Param + nextParam(),
            UI.Element.Ready + isReady()
        )
        UI.Element.Param -> emptyList()
        UI.Element.Ready -> when {
            isReady && config.autoExecute -> listOf(
                UI.Element.Stack + resolveNextView()
            )
            else -> emptyList()
        }
        UI.Element.Selection,
        UI.Element.Text -> listOf(
            UI.Element.Matching2 + calculateMatching2(),
        )
        UI.Element.Matching2 -> when {
            event is UI.Event.Clicked && config.autoFill -> listOfNotNull(
                matching2.filter(UIMatching2::isReady).singleOrNull {
                    event.value in it.args.values
                }?.run {
                    UI.Element.Method + method
                }
            )
            else -> emptyList()

        }
        UI.Element.Matching -> listOf(
            UI.Element.Methods + calculateScore()
        )
        UI.Element.Methods -> emptyList()
        else -> emptyList()
    }

