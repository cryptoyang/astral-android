package cc.cryptopunks.ui.poc.model

fun Api.Type.localize(
    target: Api.Type,
    path: List<String> = emptyList(),
): List<List<String>> = when (target) {
    this -> listOf(path + target.id)
    else -> properties.flatMap { (name, type) ->
        type.localize(target, path + name)
    }
}
