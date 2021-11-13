package cc.cryptopunks.ui.poc.model

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

fun Api.Model.defaultMatching(): List<UIMatching> =
    methods.values.map(::UIMatching)

fun List<UIMatching>.accumulateMatchingElements(
    chunks: List<String>,
    index: Int = nextIndex(),
): List<UIMatching> =
    chunks.foldIndexed(this) { i, acc, chunk ->
        acc.accumulateMatchingElements(chunk, i + index)
    }

private fun List<UIMatching>.accumulateMatchingElements(
    chunk: String,
    index: Int = nextIndex(),
): List<UIMatching> = this + this
    .filter { it.index == index - 1 }
    .map { it.method }
    .findMatchingElements(chunk)
    .flatMap { (method, elements) ->
        elements.map { element ->
            UIMatching(
                method = method,
                chunk = chunk,
                index = index,
                element = element,
            )
        }
    }

private fun Collection<Api.Method>.findMatchingElements(chunk: String): Map<Api.Method, List<UIMatching.Element>> {

    val elements = mutableListOf<Pair<UIMatching.Element, Api.Method>>()

    forEach { method ->

        if (method.id.contains(chunk, true))
            elements += UIMatching.MethodName to method

        method.params.forEach { (name, type) ->
            when {
                name.contains(chunk, true) ->
                    elements += UIMatching.Param.Name(name) to method
                // TODO type matcher
            }
        }
    }

    return elements.groupBy(
        keySelector = { (_, method) -> method },
        valueTransform = { (element, _) -> element }
    )
}

private fun List<UIMatching>.nextIndex() = lastOrNull()?.run { index + 1 } ?: 0
