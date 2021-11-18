package cc.cryptopunks.ui.poc.model

object Api {

    data class Model(
        val id: String = "",
        val methods: Map<String, Method> = emptyMap(),
        val types: Map<String, Type> = emptyMap(),
    ) {
        companion object {
            val Empty = Model()
        }
    }

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
            const val obj = "object"
            const val array = "array"
            const val bool = "boolean"
            const val string = "string"
            const val int = "integer"
            const val num = "number"

            val Empty = Type()
            val String = Type(string)
        }
    }
}
