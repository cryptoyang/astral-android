package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.*
import cc.cryptopunks.ui.poc.model.util.removeFirst
import com.fasterxml.jackson.databind.JsonNode

fun UI.State.calculateMatching(): List<UIMethod> =
    context.model.methods.values.calculateMatching(
        availableData = generateUIDataFromStack() + selection + textChunks
    )

fun Collection<Api.Method>.calculateMatching(
    availableData: List<UIData> = emptyList()
): List<UIMethod> = map { method ->

    val template = method.template().toMutableList()
    val remaining = availableData.toMutableList()
    val elements = mutableListOf<UIMethod.Element>()

    while (remaining.isNotEmpty()) {
        val data = remaining.removeAt(0)

        val matchingType = template.removeFirst { type ->
            when (data.value) {
                is String -> when (type) {
                    UIMethod.Type.MethodName -> method.id.startsWith(data.value)
                    is UIMethod.Type.ArgName -> type.name.startsWith(data.value)
                    is UIMethod.Type.ArgType -> method.params[type.name]!!.id.startsWith(data.value)
                    is UIMethod.Type.ArgValue -> {
                        val paramType = method.params[type.name]!!
                        if (data.value.toString() in paramType.options) true
                        else when (paramType.type) {
                            Api.Type.str -> true
                            Api.Type.bool -> data.value.toBooleanStrictOrNull() != null
                            Api.Type.int -> data.value.toIntOrNull() != null
                            Api.Type.num -> data.value.toDoubleOrNull() != null
                            else -> false
                        }
                    }
                    else -> false
                }
                else -> when (type) {
                    is UIMethod.Type.ArgValue -> method.params[type.name]!!.id == data.type.id
                    else -> false
                }
            }
        } ?: UIMethod.Type.Unknown

        elements += UIMethod.Element(
            type = matchingType,
            value = data.value
        )
    }

    elements += template.map { type ->
        UIMethod.Element(
            type = type,
            value = UIMethod.Type.Unknown,
        )
    }

    UIMethod(
        method = method,
        elements = elements,
    )
}.sortedBy(UIMethod::score)

private fun Api.Method.template(): List<UIMethod.Type> = listOf(
    UIMethod.Type.MethodName
) + params.flatMap { (name, type) ->
    listOf(
        UIMethod.Type.ArgName(name),
        UIMethod.Type.ArgType(name),
        UIMethod.Type.ArgValue(name, type.type == Api.Type.obj),
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

fun UIMethod.calculateScore(): Int =
    elements.mapNotNull { element ->
        when {
            element.type == UIMethod.Type.Unknown -> when (element.value) {
                is JsonNode -> 2 shl 8
                else -> 1 shl 4
            }
            element.value == UIMethod.Type.Unknown -> when (element.type) {
                is UIMethod.Type.ArgValue -> when {
                    element.type.complex -> 2 shl 8
                    else -> 1 shl 8
                }
                else -> 1
            }
            else -> null
        }
    }.sum()

fun UIMethod.args(): UIArgs = elements
    .filter { it.type is UIMethod.Type.ArgValue }
    .filter { it.value !is UIMethod.Type.Unknown }
    .associate { (it.type as UIMethod.Type.ArgValue).name to it.value }

fun UI.State.selectionMatchingMethods(): List<UIMethod> {
    val selectedValues = selection.map(UIData::value)
    return methods.filter { (it.args.values intersect selectedValues).isNotEmpty() }
}

val UI.State.inputMethod: UIMethod?
    get() = methods.inputMatching()

fun List<UIMethod>.inputMatching(): UIMethod? =
    singleOrNull { uiMatching -> uiMatching.singleTextArg }

fun UIMethod.hasSingleTextInput(): Boolean = method.params.toList()
    .singleOrNull { (_, apiType) -> apiType.type == Api.Type.str }
    ?.let { (name, _) -> method.params.keys - args.keys - name }
    ?.isEmpty()
    ?: false
