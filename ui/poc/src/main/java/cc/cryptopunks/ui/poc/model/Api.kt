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

    data class Method(
        val id: String = "",
        val params: Map<String, Type> = emptyMap(),
        val result: Type = Type.Empty,
    ) {
        companion object {
            val Empty = Method()
        }
    }

    data class Type(
        val type: String = "",
        val id: String = "",
        val properties: Map<String, Type> = emptyMap(),
        val options: List<String> = emptyList(),
    ) {
        companion object {
            const val obj = "object"
            const val arr = "array"
            const val bool = "boolean"
            const val str = "string"
            const val int = "integer"
            const val num = "number"

            val Empty = Type()
            val String = Type(str)
        }
    }
}
