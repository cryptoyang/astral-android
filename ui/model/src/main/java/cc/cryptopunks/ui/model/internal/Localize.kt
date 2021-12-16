package cc.cryptopunks.ui.model.internal

import cc.cryptopunks.ui.model.Service

internal fun Service.Type.localize(
    target: Service.Type,
    path: List<String> = emptyList(),
): List<List<String>> = when (target) {
    this -> listOf(path + target.id)
    else -> properties.flatMap { (name, type) ->
        type.localize(target, path + name)
    }
}
