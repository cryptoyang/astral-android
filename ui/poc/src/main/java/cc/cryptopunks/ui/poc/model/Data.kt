package cc.cryptopunks.ui.poc.model

typealias UIArgs = Map<String, Any>

enum class UIDisplay { Panel, Data }

data class UIParam(val name: String, val type: Api.Type, val resolvers: Iterable<UIResolver>)

data class UIView(val source: Api.Method, val args: UIArgs, val data: Any)

data class UIMethodScore(val score: Int, val method: Api.Method, val matching: List<UIMatching>)

data class UIMatching(
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
