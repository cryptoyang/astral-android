package cc.cryptopunks.ui.model.internal

internal fun String.isPath(): Boolean =
    PathRegex.path.matches(this)

private object PathRegex {
    val dot = "(\\.)".toRegex()
    val chunk = "(\\w+)".toRegex()
    val index = "((\\[)\\d+(\\]))?".toRegex()
    val path = "($chunk$index$dot)*$chunk$index".toRegex()
}
