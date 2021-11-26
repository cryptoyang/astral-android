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
        val newState = when (next) {
            is UIUpdate<*, *> -> state + next
            else -> state
        }
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
    is UI.Event.Configure -> listOf(
        UI.Element.Config + UIConfig(config + event.config)
    )
    is UI.Event.Text -> listOf(
        UI.Element.Text + event.value.orEmpty(),
    )
    is UI.Event.Method -> listOf(
        UI.Element.Method + event.method,
    )
    is UI.Event.Clicked -> listOf(
        UI.Element.Selection + generateSelection(event)
    )
    UI.Event.Action -> when {
        isReady -> listOf(
            UI.Element.Stack + resolveNextView()
        )
        isRequiredArg(text) -> listOf(
            UI.Element.Args + argsWith(text),
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
            UI.Element.Args + when (config.autoFill) {
                true -> matchedArgs()
                false -> defaultArgs()
            },
        )
        UI.Element.Args -> listOf(
            UI.Element.Param + nextParam()
        )
        UI.Element.Param -> when {
            param == null -> listOf(
                UI.Element.Ready + isReady()
            )
            config.autoFill -> when (val selectedArg = argDataFromSelection()) {
                null -> listOf(
                    UI.Element.Matching + calculateMatching(),
                )
                else -> listOf(
                    UI.Element.Args + argsWith(selectedArg),
                    UI.Element.Selection + selection.minus(selectedArg)
                )
            }
            else -> emptyList()
        }
        UI.Element.Selection -> when {
            selection.isEmpty() -> emptyList()
            else -> when {
                method != null -> listOf(
                    UI.Element.Param + param
                )
                method == null -> listOf(
                    UI.Element.Matching + calculateMatching(),
                )
                else -> emptyList()
            }
        }
        UI.Element.Text -> listOf(
            UI.Element.Matching + calculateMatching(),
        )
        UI.Element.Matching -> when {
            method == null
                && event is UI.Event.Clicked
                && config.autoFill
            -> {
                val selectionMethods = selectionMatchingMethods().filter(UIMatching::isReady)
                when {
                    selectionMethods.isEmpty() -> emptyList()
                    selectionMethods.size == 1 -> listOf(
                        UI.Element.Method + selectionMethods.first().method,
                    )
                    else -> listOf(
                        UI.Element.Display + UIDisplay.Panel
                    )
                }
            }
            else -> emptyList()

        }
        UI.Element.Ready -> when {
            isReady && config.autoExecute -> listOf(
                UI.Element.Stack + resolveNextView()
            )
            else -> emptyList()
        }
        else -> emptyList()
    }
