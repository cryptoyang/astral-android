package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.UI
import cc.cryptopunks.ui.poc.model.UIMatching
import cc.cryptopunks.ui.poc.model.UIMethodScore
import kotlin.math.absoluteValue

fun UI.State.calculateScore(): List<UIMethodScore> {
    val latestIndex = matching.lastOrNull()?.index ?: -1
    val latestMatching = matching.filter { it.index == latestIndex }
    return latestMatching.calculateScore(text)
}

private fun List<UIMatching>.calculateScore(
    chunks: CharSequence? = null
): List<UIMethodScore> =
    groupBy { it.method }.map { (method, matching) ->
        UIMethodScore(
            method = method,
            matching = matching,
            score = matching.score(chunks?.splitChunks() ?: emptyList())
        )
    }.sortedBy { it.score.absoluteValue }

fun CharSequence.splitChunks() = split(" ").filter { it.isNotBlank() }

private fun List<UIMatching>.score(
    chunks: List<String>
): Int {

    val matchedParams = map { matching ->
        val element = matching.element
        val method = matching.method
        when (element) {
            is UIMatching.MethodName -> method
            is UIMatching.Param.Name -> method.params[element.name]!!
            is UIMatching.Param.Type -> TODO()
            is UIMatching.Param.Arg -> TODO()
        }
    }

    val matchedChunks = map { it.chunk }.toSet()
    val notMatchedChunks = chunks.filter { chunk -> chunk in matchedChunks }

    return matchedParams.size - (first().method.params.size + 1) - notMatchedChunks.size
}
