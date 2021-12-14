package cc.cryptopunks.ui.model.internal

internal fun <T> MutableList<T>.removeFirst(predicate: (T) -> Boolean): T? =
    indexOfFirst(predicate).takeIf { it > -1 }?.let { removeAt(it) }
