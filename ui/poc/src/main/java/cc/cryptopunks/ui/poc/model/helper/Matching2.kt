package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.*
import com.fasterxml.jackson.databind.JsonNode

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
                            Api.Type.string -> true
                            Api.Type.bool -> data.value.toBooleanStrictOrNull() != null
                            Api.Type.int -> data.value.toIntOrNull() != null
                            Api.Type.num -> data.value.toDoubleOrNull() != null
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

private fun Api.Method.template(): List<UIMatching2.Type> = listOf(
    UIMatching2.Type.MethodName
) + params.flatMap { param ->
    listOf(
        UIMatching2.Type.ArgName(param.key),
        UIMatching2.Type.ArgType(param.key),
        UIMatching2.Type.ArgValue(param.key),
    )
}

private val UI.State.textChunks
    get() = text.splitChunks().map { chunk ->
        UIData(
            type = Api.Type("string"),
            value = chunk
        )
    }


fun UIMatching2.calculateScore(): Int =
    elements.mapNotNull { element ->
        when {
            element.type == UIMatching2.Type.Unknown -> when (element.value) {
                is JsonNode -> 1 shl 8
                else -> 1
            }
            element.value == UIMatching2.Type.Unknown -> when (element.type) {
                is UIMatching2.Type.ArgValue -> 1 shl 8
                else -> 1
            }
            else -> null
        }
    }.sum()

fun UIMatching2.args(): UIArgs = elements
    .filter { it.type is UIMatching2.Type.ArgValue }
    .filter { it.value !is UIMatching2.Type.Unknown }
    .associate { (it.type as UIMatching2.Type.ArgValue).name to it.value }

fun UI.State.firstMatchingMethod(): UIMatching2? =
    matching2.firstOrNull { it.args.isNotEmpty() }
