package cc.cryptopunks.ui.model.internal

import cc.cryptopunks.ui.model.*
import com.fasterxml.jackson.databind.JsonNode

internal fun UI.State.calculateMethods(): List<UIMethod> =
    context.methods.values.calculateMethods(
        availableData = generateUIDataFromStack() + selection + textChunks
    )

private fun Collection<Service.Method>.calculateMethods(
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
                        else when (paramType.kind) {
                            Service.Type.str -> true
                            Service.Type.bool -> data.value.toBooleanStrictOrNull() != null
                            Service.Type.int -> data.value.toIntOrNull() != null
                            Service.Type.num -> data.value.toDoubleOrNull() != null
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

private fun Service.Method.template(): List<UIMethod.Type> = listOf(
    UIMethod.Type.MethodName
) + params.flatMap { (name, type) ->
    listOf(
        UIMethod.Type.ArgName(name),
        UIMethod.Type.ArgType(name),
        UIMethod.Type.ArgValue(name, type.kind == Service.Type.obj),
    )
}

private val UI.State.textChunks
    get() = text.splitChunks().map { chunk ->
        UIData(
            type = Service.Type("string"),
            value = chunk
        )
    }

private fun CharSequence.splitChunks() = split(" ").filter { it.isNotBlank() }

internal fun UIMethod.calculateScore(): Int =
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

internal fun UI.State.selectionMatchingMethods(): List<UIMethod> {
    val selectedValues = selection.map(UIData::value)
    return methods.filter { (it.args.values intersect selectedValues).isNotEmpty() }
}

internal val UI.State.inputMethod: UIMethod?
    get() = methods.inputMatching()

private fun List<UIMethod>.inputMatching(): UIMethod? =
    singleOrNull { uiMatching -> uiMatching.singleTextArg }

internal fun UIMethod.hasSingleTextInput(): Boolean = method.params.toList()
    .singleOrNull { (_, apiType) -> apiType.kind == Service.Type.str }
    ?.let { (name, _) -> method.params.keys - args.keys - name }
    ?.isEmpty()
    ?: false
