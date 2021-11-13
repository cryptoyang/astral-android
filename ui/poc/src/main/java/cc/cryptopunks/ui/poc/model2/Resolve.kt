package cc.cryptopunks.ui.poc.model2

import cc.cryptopunks.ui.poc.model.Api

sealed class UIResolver(
    val ordinal: Int,
) : Comparable<UIResolver> {

    override fun compareTo(other: UIResolver): Int = ordinal.compareTo(other.ordinal)

    data class Data(val view: UIView) : UIResolver(1)
    data class Option(val list: List<String>) : UIResolver(2)
    data class Method(val method: Api.Method, val path: Path) : UIResolver(3)
    data class Path(val chunks: List<String>, val single: Boolean = true)
    data class Input(val type: String) : UIResolver(4)
}

fun Api.Model.resolvers(): Map<String, Iterable<UIResolver>> = methods.values
    .flatMapTo(mutableSetOf()) { method -> method.params.values }
    .associateWith { type: Api.Type ->
        when (type.type) {
            "int", "boolean", "string" -> listOf(
                when {
                    type.options.isNotEmpty() -> UIResolver.Option(type.options)
                    else -> UIResolver.Input(type.type)
                }
            )
            "object" -> typeResolvers(type)
            "array" -> emptyList() // TODO
            else -> emptyList()
        }
    }
    .mapKeys { (type, _) -> type.hashId }

private fun Api.Model.typeResolvers(
    target: Api.Type
): List<UIResolver.Method> =
    typeResolvers(
        targets = listOf(Chunk(target))
    ).map { chunk ->
        val chunks = chunk.toList()
        val path = chunks.mapNotNull { it.next?.first }
        UIResolver.Method(
            method = methods.values.first { it.result.id == chunk.type.id },
            path = UIResolver.Path(path, chunk.all { it.type.type != "array" })
        )
    }

private data class Chunk(
    val type: Api.Type,
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

private fun Api.Model.typeResolvers(
    targets: List<Chunk>,
    types: List<Api.Type> = allTypes() - targets.types(),
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

private fun Api.Model.allTypes(): List<Api.Type> =
    types.values + methods.values
        .map { it.result }
        .filter { it.type == "array" }

val Api.Type.hashId: String
    get() = if (id.isBlank()) type else id
