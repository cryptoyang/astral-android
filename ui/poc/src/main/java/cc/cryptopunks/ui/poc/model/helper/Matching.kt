package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.*

fun UI.State.calculateMatching(old: UI.State): List<UIMatching> =
    defaultMatching() + dataMatching() + textMatching(old)


private fun UI.State.defaultMatching(): List<UIMatching> =
    context.model.methods.values.map(::UIMatching)

private fun UI.State.dataMatching(
    data: List<UIData> = selection,
    methods: Iterable<Api.Method> = context.model.methods.values,
): List<UIMatching> =
    methods.flatMap { method ->
        val remaining = data.toMutableList()
        method.params.mapNotNull { (name, type) ->
            remaining.removeFirst { uiData -> uiData.type.id == type.id }?.let { value ->
                UIMatching(
                    method = method,
                    element = UIMatching.Param.Arg(name, value)
                )
            }
        }
    }

fun <T> MutableList<T>.removeFirst(predicate: (T) -> Boolean): T? =
    indexOfFirst(predicate).takeIf { it > -1 }?.let { removeAt(it) }



private fun UI.State.textMatching(old: UI.State): List<UIMatching> = when {

    text.isBlank() -> emptyList()

    else -> {

        val prevChunks: List<String> = old.text.splitChunks().filter(String::isNotBlank)
        val nextChunks: List<String> = text.splitChunks().filter(String::isNotBlank)
        val commonChunks = prevChunks.withIndex().takeWhile { (index, value) ->
            value == nextChunks.getOrNull(index)
        }
        val additionalChunks: List<String> = nextChunks.drop(commonChunks.size)
        val latestMatching = matching.filter { it.index == commonChunks.lastIndex }
        val index = latestMatching.nextIndex()

        additionalChunks.foldIndexed(latestMatching) { i, acc, chunk ->
            acc.accumulateMatchingElements(chunk, i + index)
        }
    }
}


private fun List<UIMatching>.accumulateMatchingElements(
    chunk: String,
    index: Int = nextIndex(),
): List<UIMatching> = this + this
    .filter { uiMatching -> uiMatching.index == index - 1 }
    .map(UIMatching::method)
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
