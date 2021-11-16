package cc.cryptopunks.ui.poc.model

import cc.cryptopunks.ui.poc.api.uiRequestData
import cc.cryptopunks.ui.poc.mapper.model.generateProteusLayouts
import cc.cryptopunks.ui.poc.mapper.openrpc.toModel
import cc.cryptopunks.ui.poc.model.factory.resolvers
import cc.cryptopunks.ui.poc.schema.rpc.OpenRpc

object UI {

    sealed interface Event {
        object Init : Event
        object Action : Event
        object Back : Event
        data class Clicked(val id: String, val value: Any) : Event
        data class Text(val value: String? = null) : Event
        data class Method(val method: Api.Method) : Event
    }

    sealed interface Action : Output, UIMessage {
        object Exit : Action
    }

    sealed interface Element<T> : Output {
        val defaultValue: T

        object Context : UIElement<UI.Context>()
        object Stack : UIElement<List<UIView>>(emptyList())
        object Display : UIElement<UIDisplay>(UIDisplay.Panel)
        object Methods : UIElement<List<UIMethodScore>>(emptyList())
        object Method : UIElement<Api.Method?>(null)
        object Args : UIElement<UIArgs>(emptyMap())
        object Param : UIElement<UIParam?>(null)
        object Selection : UIElement<List<Event.Clicked>>(emptyList())
        object Ready : UIElement<Boolean>(false)
        object Text : UIElement<String>("")
        object Matching : UIElement<List<UIMatching>>(emptyList())
    }

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

    data class Context(
        val doc: OpenRpc.Document,
        val model: Api.Model = doc.toModel(),
        val layouts: Map<String, Map<String, Any>> = model.generateProteusLayouts(),
        val resolvers: Map<String, Iterable<UIResolver>> = model.resolvers(),
        val requestData: UIRequestData = uiRequestData,
    )

    data class Change(val event: Event, val state: State, val output: List<Output>)

    sealed interface Output
}

typealias UIHandler = (UI.Event) -> UI.Change

typealias UIRequestData = UIRequest.() -> Any

data class UIRequest(val context: UI.Context, val method: Api.Method, val args: UIArgs)

sealed class UIResolver(private val ordinal: Int) : Comparable<UIResolver> {
    override fun compareTo(other: UIResolver): Int = ordinal.compareTo(other.ordinal)
    data class Data(val view: UIView) : UIResolver(1)
    data class Option(val list: List<String>) : UIResolver(2)
    data class Method(val method: Api.Method, val path: Path) : UIResolver(3)
    data class Path(val chunks: List<String>, val single: Boolean = true)
    data class Input(val type: String) : UIResolver(4)
}
