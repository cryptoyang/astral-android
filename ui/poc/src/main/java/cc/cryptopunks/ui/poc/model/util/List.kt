package cc.cryptopunks.ui.poc.model.util

fun <T> MutableList<T>.removeFirst(predicate: (T) -> Boolean): T? =
    indexOfFirst(predicate).takeIf { it > -1 }?.let { removeAt(it) }
