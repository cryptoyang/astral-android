package cc.cryptopunks.ui.poc.model2

import cc.cryptopunks.ui.poc.api.MessengerApi
import cc.cryptopunks.ui.poc.api.handle
import cc.cryptopunks.ui.poc.mapper.Jackson
import cc.cryptopunks.ui.poc.mapper.model.generateProteusLayouts
import cc.cryptopunks.ui.poc.mapper.openrpc.toModel
import cc.cryptopunks.ui.poc.model.*
import cc.cryptopunks.ui.poc.schema.rpc.OpenRpc
import cc.cryptopunks.ui.poc.schema.rpc.Rpc
import java.util.concurrent.atomic.AtomicReference

// API

object UI {

    fun interface Handler : (Event) -> Change

    sealed interface Event {
        object Init : Event
        object Action : Event
        object Back : Event
        data class Clicked(val id: String, val value: Any) : Event
        data class Text(val value: String? = null) : Event
        data class Method(val method: Api.Method) : Event
    }

    data class Context(
        val doc: OpenRpc.Document,
        val model: Api.Model = doc.toModel(),
        val layouts: Map<String, Map<String, Any>> = model.generateProteusLayouts(),
        val resolvers: Map<String, Iterable<UIResolver>> = model.resolvers(),
    )

    class State(elements: UIElements = emptyMap()) : UIState(elements) {
        val context by +Element.Context
        val matching by +Element.Matching
        val stack by +Element.Stack
        val method by +Element.Method
        val methods by +Element.Methods
        val args by +Element.Args
        val display by +Element.Display
        val param by +Element.Param
        val text by +Element.Text
        val selection by +Element.Selection
        val isReady by +Element.Ready

        companion object
    }

    sealed interface Element<T> : Output {
        val defaultValue: T

        object Context : UIElement<UI.Context>(::TODO)

        object Stack : UIElement<List<UIView>>(emptyList())
        object Display : UIElement<UIDisplay>(UIDisplay.Panel)
        object Methods : UIElement<List<Score>>(emptyList())
        object Method : UIElement<Api.Method?>(null)
        object Args : UIElement<UIArgs>(emptyMap())
        object Param : UIElement<UIParam?>(null)
        object Selection : UIElement<List<Event.Clicked>>(emptyList())
        object Ready : UIElement<Boolean>(false)
        object Text : UIElement<String>("")
        object Matching : UIElement<List<UIMatching>>(emptyList())

        companion object Defaults : List<Element<*>> by listOf(
            Stack,
            Display,
            Methods,
            Method,
            Args,
            Param,
            Selection,
            Ready,
            Text,
            Matching,
        )
    }

    sealed interface Action : Output, UIMessage {
        object Exit : Action
    }

    data class Change(val event: Event, val state: State, val output: List<Output>)

    sealed interface Output
}

// Types

typealias UIArgs = Map<String, Any>
typealias UIElements = Map<UI.Element<*>, Any>
typealias UIMatching = Matching

// Functions

typealias UICreateRequest = UI.State.() -> UIRequest
typealias UIRequestView = UIRequest.() -> UIView

typealias UIGenerateMessages = UI.State.(UI.Event) -> List<UIMessage>
typealias UIUpdateState = UI.State.(UIMessage) -> UI.State
typealias UIProcessMessage = UI.State.(UI.State, UIMessage) -> List<UIMessage>
typealias UIProcessUpdate = UI.State.(UI.State, UIUpdate<*, *>) -> List<UIMessage>
typealias UIGetOutput = (UIMessage) -> UI.Output?

// Data

enum class UIDisplay { Panel, Data }
data class UIParam(val name: String, val type: Api.Type, val resolvers: Iterable<UIResolver>)
data class UIView(val source: Api.Method, val args: UIArgs, val data: Any)

// Internal

data class UIRequest(val context: UI.Context, val method: Api.Method, val args: UIArgs)
data class UIUpdate<E : UI.Element<T>, T>(val element: E, val value: T) : UIMessage

sealed interface UIMessage

fun eventHandler(
    state: UI.State,

    requestView: UIRequestView = uiRequestView,
    generateMessages: UIGenerateMessages = generateMessages(
        request = uiCreateRequest,
        view = requestView,
    ),

    processUpdate: UIProcessUpdate = uiProcessUpdate,
    processMessage: UIProcessMessage = processMessage(
        processUpdate = processUpdate
    ),

    updateState: UIUpdateState = uiUpdateState,
    getOutput: UIGetOutput = uiGetOutput

): UI.Handler = eventHandler(
    generateMessages = generateMessages,
    process = processMessage,
    update = updateState,
    getOutput = getOutput,
    stateRef = AtomicReference(state)
)

fun eventHandler(
    generateMessages: UIGenerateMessages,
    process: UIProcessMessage,
    update: UIUpdateState,
    getOutput: UIGetOutput,
    stateRef: AtomicReference<UI.State>,
): UI.Handler = UI.Handler { event: UI.Event ->

    var state: UI.State = stateRef.get()
    var remaining: List<UIMessage> = state.generateMessages(event)
    var acc: List<UIMessage> = remaining

    while (remaining.isNotEmpty()) {
        val next = remaining.first()
        val newState = state.update(next)
        val results = newState.process(state, next)
        state = newState
        acc = acc + results
        remaining = results + remaining.drop(1)
    }

    val output = acc.mapNotNull(getOutput)

    stateRef.set(state)

    UI.Change(event, state, output)
}

fun generateMessages(
    request: UICreateRequest,
    view: UIRequestView,
): UIGenerateMessages = { event ->
    when (event) {
        UI.Event.Init -> listOf(
            UI.Element.Stack + emptyList()
        )
        UI.Event.Back -> when {
            display == UIDisplay.Data -> listOf(
                UI.Element.Stack + stack.dropLast(1),
            )
            args.isNotEmpty() -> listOf(
                UI.Element.Args + (args - args.keys.last())
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
        UI.Event.Action -> when {
            isReady -> listOf(
                UI.Element.Stack + stack.plus(request().view())
            )
            isRequiredParam(text) -> listOf(
                UI.Element.Args + nextArg(text),
            )
            else -> listOf(
                UI.Element.Display + switchDisplay()
            )
        }
        is UI.Event.Text ->
            listOf(
                UI.Element.Text + event.value.orEmpty(),
            )
        is UI.Event.Method ->
            listOf(
                UI.Element.Method + event.method,
            )
        is UI.Event.Clicked -> when {
            isRequiredParam(event) -> listOf(
                UI.Element.Args + nextArg(event.value),
            )
            else -> listOf(
                UI.Element.Selection + listOf(event)
            )
        }
    }
}

val uiCreateRequest: UICreateRequest = {
    UIRequest(context, method!!, args)
}

val uiRequestView: UIRequestView = {
    val command = parseRpcMethod(context.model, method, args) as MessengerApi.Method
    val result = handle(command)
    UIView(method, args, result)
}


fun processMessage(
    processUpdate: UIProcessUpdate
): UIProcessMessage = { old, message ->
    when (message) {
        is UIUpdate<*, *> -> processUpdate(old, message)
        UI.Action.Exit -> emptyList()
    }
}

val uiProcessUpdate: UIProcessUpdate = { old, update ->
    when (update.element) {
        UI.Element.Context -> emptyList()
        UI.Element.Stack -> listOf(
            UI.Element.Display + when {
                stack.isEmpty() -> UIDisplay.Panel
                else -> UIDisplay.Data
            },
            UI.Element.Text.default(),
            UI.Element.Method.default(),
        )
        UI.Element.Display -> emptyList()
        UI.Element.Methods -> emptyList()
        UI.Element.Method -> listOf(
            UI.Element.Selection + emptyList(),
            UI.Element.Args + defaultArgs(),
        )
        UI.Element.Args -> listOf(
            UI.Element.Param + nextParam(),
            UI.Element.Ready + isReady()
        )
        UI.Element.Param -> emptyList()
        UI.Element.Ready -> emptyList()
        UI.Element.Selection,
        UI.Element.Text -> listOf(
            UI.Element.Matching + calculateMatching(old)
        )
        UI.Element.Matching -> listOf(
            UI.Element.Methods + calculateScore()
        )
        else -> emptyList()
    }
}

val uiUpdateState: UIUpdateState = { message ->
    when (message) {
        is UIUpdate<*, *> -> plus(message)
        else -> this
    }
}

val uiGetOutput: UIGetOutput = { message ->
    when (message) {
        is UI.Action -> message
        is UIUpdate<*, *> -> message.element
    }
}

// helpers

operator fun UI.State.Companion.invoke(context: UI.Context) = UI.State(
    UI.Element
        .filter { it.defaultValue != null }
        .associateWith { it.defaultValue as Any }
        .plus(UI.Element.Context to context)
)

fun UI.State.nextParam() =
    (method?.params ?: emptyMap())
        .minus(args.keys).toList().firstOrNull()
        ?.let { (name, type) ->
            UIParam(
                name = name,
                type = type,
                resolvers = context.resolvers[type.id]
                    ?: context.resolvers[type.type]
                    ?: throw IllegalArgumentException("no resolver ${context.resolvers} for $type")
            ).let { param ->
                val next = updateResolvers(param)
                next
            }
        }

fun UI.State.switchDisplay() = when (display) {
    UIDisplay.Panel -> UIDisplay.Data
    UIDisplay.Data -> UIDisplay.Panel
}

fun UI.State.isRequiredParam(text: String): Boolean = when (param?.type?.type) {
    "string" -> text
    "boolean" -> text.toBooleanStrictOrNull()
    "integer" -> text.toIntOrNull()
    "number" -> text.toDoubleOrNull()
    else -> null
} != null

fun UI.State.isRequiredParam(clicked: UI.Event.Clicked): Boolean =
    clicked.id == param?.type?.id || clicked.id == param?.type?.type

fun UI.State.nextArg(value: Any) = args + (param!!.name to value)

fun parseRpcMethod(
    model: Api.Model,
    method: Api.Method,
    args: Map<String, Any>
): Rpc.Method {
    val fullName = model.id + "$" + method.id
    val clazz = Class.forName(fullName) as Class<Rpc.Method>
    val rpcMethod: Rpc.Method = when {
        args.isEmpty() ->
            Jackson.jsonMapper.readValue(EmptyJson, clazz)
        else -> {
            val json = Jackson.prettyWriter.writeValueAsString(args)
            println(json)
            Jackson.jsonMapper.readValue(json, clazz)
        }
    }
    return rpcMethod
}

const val EmptyJson = "{}"

fun UI.State.defaultArgs(): Map<String, Any> =
    when (val method = method) {
        null -> emptyMap()
        else -> selection.toMutableList().run {
            method.params.toList().mapNotNull { (key, type) ->
                firstOrNull { clicked -> clicked.id == type.id }?.let { matching ->
                    remove(matching)
                    key to matching.value
                }
            }.toMap()
        }
    }

fun UI.State.isReady() = args.keys == method?.params?.keys

fun <E : UI.Element<T>, T> E.default() = UIUpdate(this, defaultValue)

private fun UI.State.updateResolvers(param: UIParam): UIParam {
    val view = stack.lastOrNull() ?: return param

    param.resolvers
        .filterIsInstance<UIResolver.Method>()
        .any { method -> method.method.id == view.source.id } || return param

    return param.copy(
        resolvers = param.resolvers + UIResolver.Data(view)
    )
}

fun UI.State.calculateMatching(old: UI.State): List<Matching> {
    val prev = old
        .text
        .splitChunks()
        .filter(String::isNotBlank)
    val next = text.splitChunks().filter(String::isNotBlank)

    val common = prev.withIndex().takeWhile { (index, value) ->
        value == next.getOrNull(index)
    }

    val additional = next.drop(common.size)

    return when {

        text.isBlank() -> context.model
            .defaultMatching()

        else -> matching
            .filter { it.index == common.lastIndex }
            .accumulateMatchingElements(additional)
    }
}

fun UI.State.calculateScore(): List<Score> {
    val latestIndex = matching.lastOrNull()?.index ?: -1
    val latestMatching = matching.filter { it.index == latestIndex }
    return latestMatching.calculateScore(text)
}
