package cc.cryptopunks.ui.poc.mapper.jsonschema

import cc.cryptopunks.ui.poc.mapper.Jackson
import cc.cryptopunks.ui.poc.stub.MessengerApi
import cc.cryptopunks.ui.poc.transport.schema.rpc.OpenRpc
import cc.cryptopunks.ui.poc.transport.schema.rpc.generateOpenRpcDocument
import cc.cryptopunks.ui.poc.transport.schema.JsonSchema
import cc.cryptopunks.ui.poc.transport.schema.Schema
import com.fasterxml.jackson.databind.JsonNode

fun main() {
    val methodNames = MessengerApi.methods.map { it.java.name }
    val doc = MessengerApi.generateOpenRpcDocument()

    doc.generateProteusLayouts()
        .let { Jackson.jsonPrettyWriter.writeValueAsString(it) }
        .let(::println)
}

fun OpenRpc.Document.generateProteusLayouts(): Map<String, Map<String, Any>> {
    val resultIds = methods
        .filter { it.result.ref.isNotBlank() }
        .map { it.result.ref.removePrefix("#/schemas/") }

    val subSchemas = components.schemas - resultIds

    val rootSchemas: Map<String, Schema /* = com.fasterxml.jackson.databind.JsonNode */> = methods
        .filter { it.result.ref.isBlank() }
        .associate { it.name to it.result.resolveSchema() }
        .plus(components.schemas - subSchemas.keys)


    val layouts = subSchemas.toProteusLayouts(listOf("item")) + rootSchemas.toProteusLayouts()

    return layouts
}

fun Map<String, JsonSchema>.toProteusLayouts(
    path: List<String> = emptyList()
): Map<String, Map<String, Any>> =
    mapValues { (_, scheme) -> scheme.toProteusLayout(path) }

fun JsonNode.toProteusLayout(
    path: List<String> = listOf("item"),
): Map<String, Any> = when {

    has("type") -> when (get("type").asText()) {

        "object" -> mutableMapOf(
            "type" to "LinearLayout",
            "orientation" to "vertical",
            "layout_width" to "match_parent",
            "layout_height" to "wrap_content",
            "padding" to "8dp",
            "children" to get("properties").fields().asSequence().map { (key, prop) ->
                prop.toProteusLayout(path + key)
            }.toList(),
        ).also { params ->
            if (path.isNotEmpty()) params += mapOf(
                "data" to mapOf(
                    "item" to path.formatRef()
                ),
                "onClick" to "item".formatRef(),
                "background" to "?android:selectableItemBackground",
            )
        }

        "array" -> mutableMapOf(
            "type" to "RecyclerView",
            "layout_width" to "match_parent",
            "layout_height" to "match_parent",
            "padding" to "8dp",
            "layout_manager" to mapOf(
                "type" to "LinearLayoutManager"
            ),
            "adapter" to mapOf(
                "@" to mapOf(
                    "type" to "SimpleListAdapter",
                    "item-count" to "@{items.\$length}",
                    "item-layout" to get("items").toProteusLayout(path + "items") +
                            mapOf(
                                "data" to mapOf(
                                    "item" to "@{items[\$index]}"
                                )
                            )
                ),
            )
        ).also { params ->
            if (path.isNotEmpty()) params += mapOf(
                "data" to mapOf(
                    "items" to path.formatRef()
                ),
            )
        }

        else -> mapOf(
            "type" to "TextView",
            "layout_width" to "wrap_content",
            "layout_height" to "wrap_content",
            "text" to path.formatRef(),
        )
    }

    has("\$ref") -> mutableMapOf<String, Any>(
        "type" to "include",
        "layout" to get("\$ref").asText()
            .removePrefix("#/schemas/")
            .removePrefix("#/definitions/"),
    ).also { map ->
        if (path.isNotEmpty()) map += "data" to mapOf(
            "item" to path.formatRef()
        )
    }

    else -> emptyMap()
}

private fun String.formatRef() = listOf(this).joinToString(".", "@{", "}")
private fun List<String>.formatRef() = joinToString(".", "@{", "}")
