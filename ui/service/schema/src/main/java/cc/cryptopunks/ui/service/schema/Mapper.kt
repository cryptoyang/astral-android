package cc.cryptopunks.ui.service.schema

import cc.cryptopunks.ui.model.Service
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.contains

fun OpenRpc.Document.toSchema(): Service.Schema =
    Parser(this).run {
        Service.Schema(
            version = Service.Schema.Version(
                id = info.title,
                name = info.version,
            ),
            methods = methods.associate { rpcMethod ->
                val id = shortName(rpcMethod.name)
                id to Service.Method(
                    id = id,
                    result = parseRefOrType(rpcMethod.result),
                    params = rpcMethod.params.associate { descriptor ->
                        descriptor.name to parseRefOrType(descriptor.schema)
                    }
                )
            },
            types = cache,
        )
    }

private class Parser(
    val doc: OpenRpc.Document,
    val cache: MutableMap<String, Service.Type> = mutableMapOf(),
)

private fun Parser.parseRefOrType(
    ref: OpenRpc.Ref<JsonSchema>
): Service.Type =
    when {
        ref.ref.isNotBlank() -> parseRef(ref.ref)
        ref.value != null -> parseRefOrType(ref.ref, ref.value)
        else -> Service.Type.Empty
    }

private fun Parser.parseRefOrType(
    shortName: String,
    node: JsonNode,
): Service.Type =
    when {
        "\$ref" in node -> parseRef(node["\$ref"].asText())
        "type" in node -> parseType(shortName, node, node["type"].asText())
        else -> Service.Type.Empty
    }

private fun Parser.parseRef(
    fullRef: String
): Service.Type =
    fullRef.dropPath().let { id ->
        val shortId = shortName(id)
        cache.getOrPut(shortId) {
            val schema = requireNotNull(doc.components.schemas[id]) { "No schema for $id" }
            parseRefOrType(shortId, schema)
        }
    }

private fun Parser.parseType(
    shortName: String,
    node: JsonNode,
    type: String
): Service.Type =
    when (type) {
        "integer", "boolean", "string" -> Service.Type(type, shortName, options = parseOptions(node))
        "array" -> parseArray(shortName, node, type)
        "object" -> Service.Type(type, shortName, parseProperties(node))
        else -> throw IllegalArgumentException("Cannot parse unknown type $type of $node")
    }

private fun Parser.parseArray(
    shortName: String,
    node: JsonNode,
    type: String
): Service.Type {
    val item = parseRefOrType(shortName, node["items"])
    val arrayId = (item.id.takeIf(String::isNotBlank) ?: item.kind) + ".array"
    return cache.getOrPut(arrayId) {
        Service.Type(type, arrayId, mapOf("items" to item))
    }
}

private fun parseOptions(
    node: JsonNode
): List<String> =
    when {
        "enum" in node -> node["enum"].map { it.asText() }
        "const" in node -> listOf(node["const"].asText())
        else -> emptyList()
    }

private fun Parser.parseProperties(
    node: JsonNode
): Map<String, Service.Type> =
    node["properties"]
        ?.run { fields().asSequence().associate { (key, value) -> key to parseRefOrType("", value) } }
        ?: throw NoSuchElementException("No properties for: " + node.toPrettyString())

// helpers

private fun String.dropPath() = split("/").last()

private fun Parser.shortName(id: String) = id //id.removePrefix(doc.info.title + "$")
