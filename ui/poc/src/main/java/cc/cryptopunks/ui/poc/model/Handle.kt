package cc.cryptopunks.ui.poc.model

import kotlin.math.absoluteValue

fun CharSequence.splitChunks() = split(" ").filter { it.isNotBlank() }

fun Api.Model.defaultMatching(): List<Matching> =
    methods.values.map(::Matching)

fun List<Matching>.accumulateMatchingElements(
    chunks: List<String>,
    index: Int = nextIndex(),
): List<Matching> =
    chunks.foldIndexed(this) { i, acc, chunk ->
        acc.accumulateMatchingElements(chunk, i + index)
    }

private fun List<Matching>.accumulateMatchingElements(
    chunk: String,
    index: Int = nextIndex(),
): List<Matching> = this + this
    .filter { it.index == index - 1 }
    .map { it.method }
    .findMatchingElements(chunk)
    .flatMap { (method, elements) ->
        elements.map { element ->
            Matching(
                method = method,
                chunk = chunk,
                index = index,
                element = element,
            )
        }
    }

private fun Collection<Api.Method>.findMatchingElements(chunk: String): Map<Api.Method, List<Matching.Element>> {

    val elements = mutableListOf<Pair<Matching.Element, Api.Method>>()

    forEach { method ->

        if (method.id.contains(chunk, true))
            elements += Matching.MethodName to method

        method.params.forEach { (name, type) ->
            when {
                name.contains(chunk, true) ->
                    elements += Matching.Param.Name(name) to method
                // TODO type matcher
            }
        }
    }

    return elements.groupBy(
        keySelector = { (_, method) -> method },
        valueTransform = { (element, _) -> element }
    )
}

private fun List<Matching>.nextIndex() = lastOrNull()?.run { index + 1 } ?: 0

fun List<Matching>.calculateScore(
    chunks: CharSequence? = null
): List<Score> =
    groupBy { it.method }.map { (method, matching) ->
        Score(
            method = method,
            matching = matching,
            score = matching.score(chunks?.splitChunks() ?: emptyList())
        )
    }.sortedBy { it.score.absoluteValue }

private fun List<Matching>.score(chunks: List<String>): Int {
    val matchedParams = map { matching ->
        val element = matching.element
        val method = matching.method
        when (element) {
            is Matching.MethodName -> method
            is Matching.Param.Name -> method.params[element.name]!!
            is Matching.Param.Type -> TODO()
            is Matching.Param.Arg -> TODO()
        }
    }

    val matchedChunks = map { it.chunk }.toSet()
    val notMatchedChunks = chunks.filter { chunk -> chunk in matchedChunks }

    return matchedParams.size - (first().method.params.size + 1) - notMatchedChunks.size
}
