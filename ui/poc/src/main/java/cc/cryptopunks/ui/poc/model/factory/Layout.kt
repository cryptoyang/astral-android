package cc.cryptopunks.ui.poc.model.factory

import cc.cryptopunks.ui.poc.model.Service
import cc.cryptopunks.ui.poc.model.UILayout
import cc.cryptopunks.ui.poc.model.util.removeFirst

fun Service.Schema.generateLayouts(): Map<String, UILayout> =
    methods.mapValues { (_, method) ->
        method.result.mainLayout()
    }

private fun Service.Type.mainLayout(): UILayout =
    when (kind) {
        Service.Type.obj -> when {

            properties.size > 3 -> UILayout(header = single())

            else -> {
                properties
                    .filterValues { it.kind == Service.Type.arr }
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
        Service.Type.arr -> UILayout(
            content = many()
        )
        else -> UILayout(
            header = single(),
        )
    }.let { layout ->
        layout.copy(type = this)
    }


private fun Service.Type.element(path: List<String> = emptyList()) = when (kind) {
    Service.Type.arr -> many(path)
    else -> single(path)
}

private fun Service.Type.single(path: List<String> = emptyList()) = UILayout.Single(this, path)

private fun Service.Type.many(path: List<String> = emptyList()) =
    UILayout.Many(properties["items"]!!.single(), path)

