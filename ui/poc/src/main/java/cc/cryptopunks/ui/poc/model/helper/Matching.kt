package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.Api
import cc.cryptopunks.ui.poc.model.UI
import cc.cryptopunks.ui.poc.model.UIMatching

fun UI.State.calculateMatching(old: UI.State): List<UIMatching> {
    val prev = old
        .text
        .splitChunks()
        .filter(String::isNotBlank)
    val next = text.splitChunks().filter(String::isNotBlank)

    val common = prev.withIndex().takeWhile { (index, value) ->
        value == next.getOrNull(index)
    }

    val additional = next.drop(common.size)

    return when {

        text.isBlank() -> context.model
            .defaultMatching()

        else -> matching
            .filter { it.index == common.lastIndex }
            .accumulateMatchingElements(additional)
    }
}


private fun Api.Model.defaultMatching(): List<UIMatching> =
    methods.values.map(::UIMatching)

private fun List<UIMatching>.accumulateMatchingElements(
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
