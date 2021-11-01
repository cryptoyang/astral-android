package cc.cryptopunks.ui.poc.model


fun Api.Model.resolvers(): Map<String, Iterable<UI.Resolver>> = methods.values
    .flatMapTo(mutableSetOf()) { method -> method.params.values }
    .associateWith { type: Api.Type ->
        when (type.type) {
            "int", "boolean", "string" -> listOf(
                when {
                    type.options.isNotEmpty() -> UI.Resolver.Option(type.options)
                    else -> UI.Resolver.Input(type.type)
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
): List<UI.Resolver.Path> =
    typeResolvers(listOf(Chunk(target))).map { chunk ->
        val chunks = chunk.toList()
        val path = chunks.mapNotNull { it.next?.first }
        UI.Resolver.Path(listOf(chunk.type.id) + path, chunk.all { it.type.type != "array" })
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
    types: List<Api.Type> = this.types.values + methods.values
        .map { it.result }.filter { it.type == "array" } - targets.map(Chunk::type),
): List<Chunk> = when {
    types.isEmpty() -> targets

    else -> {
        val newTargets = targets.map { target ->
            (types - target.type).flatMap { type ->
                type.properties
                    .filterValues { propType -> target.type == propType }
                    .keys.map { key -> Chunk(type, key to target) }
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

val Api.Type.hashId: String
    get() = when {
        id.isNotBlank() -> id
        else -> hashCode().toString()
    }
