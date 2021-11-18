package cc.cryptopunks.ui.poc.model.factory

import cc.cryptopunks.ui.poc.model.Api
import cc.cryptopunks.ui.poc.model.UILayout
import cc.cryptopunks.ui.poc.model.util.removeFirst

fun Api.Model.generateLayouts(): Map<String, UILayout> =
    methods.mapValues { (_, method) ->
        method.result.mainLayout()
    }

fun Api.Type.mainLayout(): UILayout =
    when (type) {
        Api.Type.obj -> when {

            properties.size > 3 -> UILayout(header = single())

            else -> {
                properties
                    .filterValues { it.type == Api.Type.array }
                    .apply { require(size < 2) }

                val remaining = properties.toList()
                    .map { (name, type) -> type.element(listOf(name)) }
                    .toMutableList()

                UILayout(
                    header = remaining.removeFirst { it is UILayout.Single } as? UILayout.Single
                        ?: UILayout.Single.Empty,
                    content = remaining.removeFirst { it is UILayout.Many } as? UILayout.Many
                        ?: remaining.removeFirst { it is UILayout.Single }
                            ?.let { it as? UILayout.Single }
                            ?.let { UILayout.Many(it) }
                        ?: UILayout.Many.Empty,
                    footer = remaining.removeFirst { it is UILayout.Single } as? UILayout.Single
                        ?: UILayout.Single.Empty,
                )
            }
        }
        Api.Type.array -> UILayout(
            content = many()
        )
        else -> UILayout(
            header = single(),
        )
    }.let { layout ->
        layout.copy(type = this)
    }


fun Api.Type.element(path: List<String> = emptyList()) = when (type) {
    Api.Type.array -> many(path)
    else -> single(path)
}

fun Api.Type.single(path: List<String> = emptyList()) = UILayout.Single(this, path)

fun Api.Type.many(path: List<String> = emptyList()) =
    UILayout.Many(properties["items"]!!.single(), path)

