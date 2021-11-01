package cc.cryptopunks.ui.poc.model

import com.fasterxml.jackson.databind.JsonNode

object Command {

    sealed interface Output
    sealed interface Input

    object GetMethods : Input

    data class SelectMethod(val list: List<Score>) : Output

    data class Selected(val method: Api.Method) : Input

    data class Status(val method: Api.Method, val args: Map<String, String>) : Output {
        companion object {
            val Empty = Status(Api.Method.Empty, emptyMap())
        }
    }

    data class ProvideParam(
        val name: String,
        val type: Api.Type,
        val resolvers: Iterable<UI.Resolver>
    ) : Output

    data class SetParam(val name: String, val value: String) : Input

    object Ready : Output

    object Execute : Input

    data class Update(
        val context: UI.Context
    ) : Output

    data class InputText(val text: CharSequence?) : Input
}

data class Score(
    val score: Int,
    val method: Api.Method,
    val matching: List<Matching>,
)

data class Matching(
    val method: Api.Method,
    val chunk: String = "",
    val index: Int = -1,
    val element: Element = MethodName,
) {
    sealed interface Element {
        val name: String
    }

    object MethodName : Element {
        override val name = "method"
    }

    sealed interface Param : Element {

        data class Name(override val name: String) : Param
        data class Type(override val name: String) : Param
        data class Arg(override val name: String) : Param
    }
}
