package cc.cryptopunks.ui.poc.model

import cc.cryptopunks.ui.poc.mapper.jsonschema.generateProteusLayouts
import cc.cryptopunks.ui.poc.mapper.openrpc.toModel
import cc.cryptopunks.ui.poc.model2.UIResolver
import cc.cryptopunks.ui.poc.model2.resolvers
import cc.cryptopunks.ui.poc.schema.rpc.OpenRpc

object UI {

    data class Context(
        val doc: OpenRpc.Document,
        val model: Api.Model = doc.toModel(),
        val layouts: Map<String, Map<String, Any>> = doc.generateProteusLayouts(),
        val resolvers: Map<String, Iterable<UIResolver>> = model.resolvers(),
        val data: MutableList<Data> = mutableListOf(),
        var matching: List<Matching> = model.defaultMatching(),
        var text: CharSequence? = null,
        var method: Api.Method? = null,
        var args: Map<String, String> = emptyMap(),
        var status: Status = Status.SelectMethod(matching.calculateScore(text)),
        var show: Boolean = true,
    )

    data class Data(
        val source: Api.Method,
        val args: Map<String, Any>,
        val result: Any
    )

    sealed interface Status {

        data class SelectMethod(
            val list: List<Score>,
        ) : Status

    }
}

data class Score(
    val score: Int,
    val method: Api.Method,
    val matching: List<Matching>,
)

data class Matching(
    val method: Api.Method,
    val chunk: String = "",
    val index: Int = -1,
    val element: Element = MethodName,
) {
    sealed interface Element {
        val name: String
    }

    object MethodName : Element {
        override val name = "method"
    }

    sealed interface Param : Element {

        data class Name(override val name: String) : Param
        data class Type(override val name: String) : Param
        data class Arg(override val name: String) : Param
    }
}
