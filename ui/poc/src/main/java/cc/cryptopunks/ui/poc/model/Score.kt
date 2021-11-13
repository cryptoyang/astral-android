package cc.cryptopunks.ui.poc.model

import kotlin.math.absoluteValue

data class UIMethodScore(
    val score: Int,
    val method: Api.Method,
    val matching: List<UIMatching>,
)

fun List<UIMatching>.calculateScore(
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
