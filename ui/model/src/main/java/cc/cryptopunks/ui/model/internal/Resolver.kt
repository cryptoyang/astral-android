package cc.cryptopunks.ui.model.internal

import cc.cryptopunks.ui.model.Service
import cc.cryptopunks.ui.model.UIResolver

internal fun Service.Schema.resolvers(): Map<String, Iterable<UIResolver>> = methods.values
    .flatMapTo(mutableSetOf()) { method -> method.params.values }
    .associateWith { type: Service.Type ->
        when (type.kind) {
            "int", "boolean", "string" -> listOf(
                when {
                    type.options.isNotEmpty() -> UIResolver.Option(type.options)
                    else -> UIResolver.Input(type.kind)
                }
            )
            "object" -> typeResolvers(type)
            "array" -> emptyList() // TODO
            else -> emptyList()
        }
    }
    .mapKeys { (type, _) -> type.hashId }

private fun Service.Schema.typeResolvers(
    target: Service.Type
): List<UIResolver.Method> =
    typeResolvers(
        targets = listOf(Chunk(target))
    ).map { chunk ->
        val chunks = chunk.toList()
        val path = chunks.mapNotNull { it.next?.first }
        UIResolver.Method(
            method = methods.values.first { it.result.id == chunk.type.id },
            path = UIResolver.Path(path, chunk.all { it.type.kind != "array" })
        )
    }

private data class Chunk(
    val type: Service.Type,
    val next: Pair<String, Chunk>? = null
) : Iterable<Chunk> {
    private class ChunkIterator(
        var current: Chunk?
    ) : Iterator<Chunk> {
        override fun hasNext(): Boolean = current != null
        override fun next(): Chunk = current!!.apply { current = next?.second }
    }

    override fun iterator(): Iterator<Chunk> = ChunkIterator(this)
}

private fun Service.Schema.typeResolvers(
    targets: List<Chunk>,
    types: List<Service.Type> = allTypes() - targets.types(),
): List<Chunk> = when {
    types.isEmpty() -> targets

    else -> {
        // for each target type
        val newTargets = targets.map { target ->
            // find types
            (types - target.type).flatMap { type ->
                // that has properties
                type.properties
                    // with type equal to the target type
                    .filterValues { propType -> target.type == propType }.keys
                    .map { key -> Chunk(type, key to target) }
            }
        }.flatten()

        val newTypes = types - newTargets.map(Chunk::type)

        when {
            newTargets.isEmpty() -> targets
            else -> typeResolvers(
                targets = newTargets,
                types = newTypes,
            )
        }
    }
}

private fun List<Chunk>.types() = map(Chunk::type)

private fun Service.Schema.allTypes(): List<Service.Type> =
    types.values + methods.values
        .map { it.result }
        .filter { it.kind == "array" }

val Service.Type.hashId: String
    get() = if (id.isBlank()) kind else id
