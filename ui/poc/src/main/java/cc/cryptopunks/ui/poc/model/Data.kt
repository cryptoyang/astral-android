package cc.cryptopunks.ui.poc.model

import cc.cryptopunks.ui.poc.model.helper.removeFirst
import cc.cryptopunks.ui.poc.model.helper.splitChunks

typealias UIArgs = Map<String, Any>

enum class UIDisplay { Panel, Data }

data class UIParam(val name: String, val type: Api.Type, val resolvers: Iterable<UIResolver>)

data class UIView(val source: Api.Method, val args: UIArgs, val data: Any)

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
        data class Arg(override val name: String, val data: Any) : Param
    }
}

data class UIMatching2(
    val method: Api.Method,
    val elements: List<Element>,
) {
    val score = calculateScore()

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

fun UIMatching2.calculateScore() = elements.mapNotNull { element ->
    when {
        element.type == UIMatching2.Type.Unknown -> when (element.value) {
            is UIData -> 1 shl 8
            else -> 1
        }
        element.value == UIMatching2.Type.Unknown -> when (element.type) {
            is UIMatching2.Type.ArgValue -> 1 shl 8
            else -> 1
        }
        else -> null
    }
}.sum()

fun UIMatching2.args(): Map<String, Any> = elements
    .filter { it.type is UIMatching2.Type.ArgValue }
    .filter { it.value !is UIMatching2.Type.Unknown }
    .associate { (it.type as UIMatching2.Type.ArgValue).name to it.value }

fun UI.State.calculateMatching2(): List<UIMatching2> {
    val availableData = (selection + textChunks)

    return context.model.methods.values.map { method ->

        val template = method.template().toMutableList()
        val remaining = availableData.toMutableList()
        val elements = mutableListOf<UIMatching2.Element>()

        while (remaining.isNotEmpty()) {
            val data = remaining.removeAt(0)

            val matchingType = template.removeFirst { type ->
                when (data.value) {
                    is String -> when (type) {
                        UIMatching2.Type.MethodName -> method.id.startsWith(data.value)
                        is UIMatching2.Type.ArgName -> type.name.startsWith(data.value)
                        is UIMatching2.Type.ArgType -> method.params[type.name]!!.id.startsWith(data.value)
                        is UIMatching2.Type.ArgValue -> when (method.params[type.name]!!.type) {
                            Api.Type.String -> true
                            Api.Type.Boolean -> data.value.toBooleanStrictOrNull() != null
                            Api.Type.Integer -> data.value.toIntOrNull() != null
                            Api.Type.Number -> data.value.toDoubleOrNull() != null
                            else -> false
                        }
                        else -> false
                    }
                    else -> when (type) {
                        is UIMatching2.Type.ArgValue -> method.params[type.name]!!.id == data.type.id
                        else -> false
                    }
                }
            } ?: UIMatching2.Type.Unknown

            elements += UIMatching2.Element(
                type = matchingType,
                value = data.value
            )
        }

        elements += template.map { type ->
            UIMatching2.Element(
                type = type,
                value = UIMatching2.Type.Unknown,
            )
        }

        UIMatching2(
            method = method,
            elements = elements,
        )
    }.sortedBy(UIMatching2::score)
}

fun Api.Method.template(): List<UIMatching2.Type> = listOf(
    UIMatching2.Type.MethodName
) + params.flatMap { param ->
    listOf(
        UIMatching2.Type.ArgName(param.key),
        UIMatching2.Type.ArgType(param.key),
        UIMatching2.Type.ArgValue(param.key),
    )
}

val UI.State.textChunks
    get() = text.splitChunks().map { chunk ->
        UIData(
            type = Api.Type("string"),
            value = chunk
        )
    }
