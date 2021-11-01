package cc.cryptopunks.ui.poc.model

object Api {

    data class Model(
        val id: String,
        val methods: Map<String, Method>,
        val types: Map<String, Type>,
    )

    sealed interface Element

    data class Method(
        val id: String = "",
        val params: Map<String, Type> = emptyMap(),
        val result: Type = Type.Empty,
    ) : Element {
        companion object {
            val Empty = Method()
        }
    }

    data class Type(
        val type: String = "",
        val id: String = "",
        val properties: Map<String, Type> = emptyMap(),
        val options: List<String> = emptyList(),
    ) : Element {

        companion object {
            val Empty = Type()
        }
    }
}
