package cc.cryptopunks.ui.poc.model

import cc.cryptopunks.ui.poc.mapper.jsonschema.generateProteusLayouts
import cc.cryptopunks.ui.poc.mapper.openrpc.toModel
import cc.cryptopunks.ui.poc.schema.rpc.OpenRpc

object UI {

    data class Context(
        val doc: OpenRpc.Document,
        val model: Api.Model = doc.toModel(),
        val layouts: Map<String, Map<String, Any>> = doc.generateProteusLayouts(),
        val resolvers: Map<String, Iterable<Resolver>> = model.resolvers(),
        val data: MutableList<Data> = mutableListOf()
    )

    data class Data(
        val source: Api.Method,
        val args: Map<String, Any>,
        val result: Any
    )

    sealed interface Resolver {
        data class Path(val chunks: List<String>, val single: Boolean = true) : Resolver
        data class Method(val id: String, val path: Path) : Resolver
        data class Option(val list: List<String>) : Resolver
        data class Input(val type: String) : Resolver
    }
}
