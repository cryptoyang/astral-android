package cc.cryptopunks.ui.poc.model

import cc.cryptopunks.ui.poc.mapper.Jackson
import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

object Service {

    interface API {
        fun schemas(): Flow<Schema>
        fun status(): Flow<Status>
        fun execute(request: Request): Job
        fun subscribe(request: Request): Flow<JsonNode>
    }

    data class Schema(
        val id: String = "",
        val methods: Map<String, Method> = emptyMap(),
        val types: Map<String, Type> = emptyMap(),
    )

    data class Method(
        val id: String = "",
        val params: Map<String, Type> = emptyMap(),
        val result: Type = Type.Empty,
    )

    data class Type(
        val kind: String = "",
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

    data class Status(
        val id: String,
        val available: Boolean,
    )

    data class Request(
        val method: String,
        val arg: JsonNode = Jackson.emptyNode
    )
}