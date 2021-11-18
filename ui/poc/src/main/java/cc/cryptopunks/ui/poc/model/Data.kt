package cc.cryptopunks.ui.poc.model

import cc.cryptopunks.ui.poc.model.helper.args
import cc.cryptopunks.ui.poc.model.helper.calculateScore
import com.fasterxml.jackson.databind.JsonNode

typealias UIArgs = Map<String, Any>

enum class UIDisplay { Panel, Data }

data class UIParam(val name: String, val type: Api.Type, val resolvers: Iterable<UIResolver>)

data class UIView(val source: Api.Method, val args: UIArgs, val data: JsonNode)

data class UIMethodScore(val score: Int, val method: Api.Method, val matching: List<UIMatching>)

data class UIConfig(
    val autoFill: Boolean = true,
    val autoExecute: Boolean = true,
)

data class UIData(
    val type: Api.Type,
    val value: Any,
)

data class UIMatching(
    val method: Api.Method,
    val elements: List<Element>,
) {
    val score by lazy { calculateScore() }

    val args by lazy { args() }

    val isReady by lazy { args.keys == method.params.keys }

    data class Element(
        val type: Type,
        val value: Any,
    )

    sealed interface Type {
        object Unknown : Type
        object MethodName : Type
        data class ArgName(val name: String) : Type
        data class ArgType(val name: String) : Type
        data class ArgValue(val name: String) : Type
    }
}
