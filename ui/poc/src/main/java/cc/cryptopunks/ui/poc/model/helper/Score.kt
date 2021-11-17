package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.*
import kotlin.math.absoluteValue

fun UI.State.calculateScore(): List<UIMethodScore> {
    val latestIndex = matching.lastOrNull()?.index ?: -1
    val latestMatching = matching.filter { it.index == latestIndex }
    val chunks = text.splitChunks()
    return calculateScore(latestMatching, chunks)
}

private fun UI.State.calculateScore(
    matching: List<UIMatching>,
    chunks: List<String>,
): List<UIMethodScore> = matching
    .groupBy(UIMatching::method)
    .map { (method: Api.Method, matching: List<UIMatching>) ->
        UIMethodScore(
            method = method,
            matching = matching,
            score = methodScore(matching, chunks)
        )
    }
    .sortedBy { uiMethodScore ->
        uiMethodScore.score.absoluteValue
    }

private fun UI.State.methodScore(
    matching: List<UIMatching>,
    chunks: List<String>
): Int {
    val dataContext = selection.map(UIData::value)
    val additionalElements = chunks
        .minus(matching.map(UIMatching::chunk))
        .map { 1 shl 8 }.sum() + matching
        .mapNotNull { it.element as? UIMatching.Param.Arg }
        .filter { it.data !in dataContext }
        .map { 1 shl 16 }.sum()


    val method = matching.first().method
    val paramsCount = method.params.size
    val requiredElements = 1 + paramsCount * (2 shl 8) + paramsCount * (1 shl 16)

    val matchedElements = matching.map { matching ->
        when (matching.element) {
            is UIMatching.MethodName -> 1
            is UIMatching.Param.Name -> 1 shl 8
            is UIMatching.Param.Type -> 1 shl 8
            is UIMatching.Param.Arg -> 1 shl 16
        }
    }.sum()

    return matchedElements - requiredElements + additionalElements
}

fun CharSequence.splitChunks() = split(" ").filter { it.isNotBlank() }
