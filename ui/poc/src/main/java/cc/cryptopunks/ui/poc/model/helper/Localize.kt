package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.Service

fun Service.Type.localize(
    target: Service.Type,
    path: List<String> = emptyList(),
): List<List<String>> = when (target) {
    this -> listOf(path + target.id)
    else -> properties.flatMap { (name, type) ->
        type.localize(target, path + name)
    }
}
