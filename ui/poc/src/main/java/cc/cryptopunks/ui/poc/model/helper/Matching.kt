package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.*
import cc.cryptopunks.ui.poc.model.util.removeFirst
import com.fasterxml.jackson.databind.JsonNode

fun UI.State.calculateMatching(): List<UIMatching> {
    val availableData = generateUIDataFromStack() + selection + textChunks

    return context.model.methods.values.map { method ->

        val template = method.template().toMutableList()
        val remaining = availableData.toMutableList()
        val elements = mutableListOf<UIMatching.Element>()

        while (remaining.isNotEmpty()) {
            val data = remaining.removeAt(0)

            val matchingType = template.removeFirst { type ->
                when (data.value) {
                    is String -> when (type) {
                        UIMatching.Type.MethodName -> method.id.startsWith(data.value)
                        is UIMatching.Type.ArgName -> type.name.startsWith(data.value)
                        is UIMatching.Type.ArgType -> method.params[type.name]!!.id.startsWith(data.value)
                        is UIMatching.Type.ArgValue -> {
                            val paramType = method.params[type.name]!!
                            if (data.value.toString() in paramType.options) true
                            else when (paramType.type) {
                                Api.Type.string -> true
                                Api.Type.bool -> data.value.toBooleanStrictOrNull() != null
                                Api.Type.int -> data.value.toIntOrNull() != null
                                Api.Type.num -> data.value.toDoubleOrNull() != null
                                else -> false
                            }
                        }
                        else -> false
                    }
                    else -> when (type) {
                        is UIMatching.Type.ArgValue -> method.params[type.name]!!.id == data.type.id
                        else -> false
                    }
                }
            } ?: UIMatching.Type.Unknown

            elements += UIMatching.Element(
                type = matchingType,
                value = data.value
            )
        }

        elements += template.map { type ->
            UIMatching.Element(
                type = type,
                value = UIMatching.Type.Unknown,
            )
        }

        UIMatching(
            method = method,
            elements = elements,
        )
    }.sortedBy(UIMatching::score)
}

private fun Api.Method.template(): List<UIMatching.Type> = listOf(
    UIMatching.Type.MethodName
) + params.flatMap { (name, type) ->
    listOf(
        UIMatching.Type.ArgName(name),
        UIMatching.Type.ArgType(name),
        UIMatching.Type.ArgValue(name, type.type == Api.Type.obj),
    )
}

private val UI.State.textChunks
    get() = text.splitChunks().map { chunk ->
        UIData(
            type = Api.Type("string"),
            value = chunk
        )
    }

fun CharSequence.splitChunks() = split(" ").filter { it.isNotBlank() }

fun UIMatching.calculateScore(): Int =
    elements.mapNotNull { element ->
        when {
            element.type == UIMatching.Type.Unknown -> when (element.value) {
                is JsonNode -> 2 shl 8
                else -> 1 shl 4
            }
            element.value == UIMatching.Type.Unknown -> when (element.type) {
                is UIMatching.Type.ArgValue -> when {
                    element.type.complex -> 2 shl 8
                    else -> 1 shl 8
                }
                else -> 1
            }
            else -> null
        }
    }.sum()

fun UIMatching.args(): UIArgs = elements
    .filter { it.type is UIMatching.Type.ArgValue }
    .filter { it.value !is UIMatching.Type.Unknown }
    .associate { (it.type as UIMatching.Type.ArgValue).name to it.value }

fun UI.State.firstMatchingMethod(): UIMatching? =
    matching.firstOrNull { it.args.isNotEmpty() }

fun UI.State.selectionMatchingMethods(): List<UIMatching> {
    val selectedValues = selection.map(UIData::value)
    return matching.filter { (it.args.values intersect selectedValues).isNotEmpty() }
}
