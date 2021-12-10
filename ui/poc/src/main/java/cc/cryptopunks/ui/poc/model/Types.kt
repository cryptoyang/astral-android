package cc.cryptopunks.ui.poc.model

import cc.cryptopunks.ui.poc.model.helper.args
import cc.cryptopunks.ui.poc.model.helper.calculateScore
import cc.cryptopunks.ui.poc.model.helper.hasSingleTextInput
import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.flow.Flow

typealias UIHandler = (UI.Event) -> UI.Change

typealias UIRequestData = UIRequest.() -> Flow<JsonNode>

data class UIRequest(val context: UI.Context, val method: Api.Method, val args: UIArgs)

enum class UIMode { View, Command }

enum class UIDisplay { Panel, Data, Input, Method }

data class UIMethod(
    val method: Api.Method,
    val elements: List<Element>,
) {
    val score by lazy { calculateScore() }
    val args by lazy { args() }
    val isReady by lazy { args.keys == method.params.keys }
    val singleTextArg by lazy { hasSingleTextInput() }

    data class Element(
        val type: Type,
        val value: Any,
    )

    sealed interface Type {
        object Unknown : Type
        object MethodName : Type
        data class ArgName(val name: String) : Type
        data class ArgType(val name: String) : Type
        data class ArgValue(val name: String, val complex: Boolean) : Type
    }
}

typealias UIArgs = Map<String, Any>

data class UIParam(
    val name: String,
    val type: Api.Type,
    val resolvers: Iterable<UIResolver>
)

sealed class UIResolver(private val ordinal: Int) : Comparable<UIResolver> {
    override fun compareTo(other: UIResolver): Int = ordinal.compareTo(other.ordinal)
    data class Data(val view: UIView) : UIResolver(1)
    data class Option(val list: List<String>) : UIResolver(2)
    data class Method(val method: Api.Method, val path: Path) : UIResolver(3)
    data class Path(val chunks: List<String>, val single: Boolean = true)
    data class Input(val type: String) : UIResolver(4)
}

class UIView(val source: Api.Method, val args: UIArgs, val data: () -> Flow<JsonNode>) {
    override fun toString(): String = source.javaClass.simpleName + args.toString()
}

data class UIConfig(
    val map: Map<String, Any> = mapOf(
        "autoFill" to true,
        "autoExecute" to true,
    )
) : Map<String, Any> by map {
    val autoFill: Boolean by map
    val autoExecute: Boolean by map
}

data class UIData(
    val type: Api.Type,
    val value: Any,
)
