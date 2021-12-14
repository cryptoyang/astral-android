package cc.cryptopunks.ui.mapper

val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
val snakeRegex = "_[a-zA-Z]".toRegex()

// String extensions
fun String.camelToSnakeCase(): String =
    camelRegex.replace(this) { "_${it.value}" }.lowercase()

fun String.snakeToLowerCamelCase(): String =
    snakeRegex.replace(this) { it.value.replace("_", "").uppercase() }

fun String.snakeToUpperCamelCase(): String =
    snakeToLowerCamelCase().replaceFirstChar(Char::titlecase)
