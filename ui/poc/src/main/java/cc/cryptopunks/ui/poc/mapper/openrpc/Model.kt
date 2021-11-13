package cc.cryptopunks.ui.poc.mapper.openrpc

import cc.cryptopunks.ui.poc.model.Api
import cc.cryptopunks.ui.poc.schema.rpc.OpenRpc
import cc.cryptopunks.ui.poc.schema.Schema
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.contains

fun String.splitPath(): List<String> = split(".", "[", "]", "].").filter { it.isNotEmpty() }

fun OpenRpc.Document.toModel(): Api.Model =
    Parser(this).run {
        Api.Model(
            id = info.title,
            methods = methods.associate { rpcMethod ->
                val id = shortName(rpcMethod.name)
                id to Api.Method(
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
    val cache: MutableMap<String, Api.Type> = mutableMapOf(),
)

private fun Parser.parseRefOrType(
    ref: OpenRpc.Ref<Schema>
): Api.Type =
    when {
        ref.ref.isNotBlank() -> parseRef(ref.ref)
        ref.value != null -> parseRefOrType(ref.ref, ref.value)
        else -> Api.Type.Empty
    }

private fun Parser.parseRefOrType(
    shortName: String,
    node: JsonNode,
): Api.Type =
    when {
        "\$ref" in node -> parseRef(node["\$ref"].asText())
        "type" in node -> parseType(shortName, node, node["type"].asText())
        else -> Api.Type.Empty
    }

private fun Parser.parseRef(
    fullRef: String
): Api.Type =
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
): Api.Type =
    when (type) {
        "int", "boolean", "string" -> Api.Type(type, shortName, options = parseOptions(node))
        "array" -> parseArray(shortName, node, type)
        "object" -> Api.Type(type, shortName, parseProperties(node))
        else -> throw IllegalArgumentException("Cannot parse unknown type $type of $node")
    }

private fun Parser.parseArray(
    shortName: String,
    node: JsonNode,
    type: String
): Api.Type {
    val item = parseRefOrType(shortName, node["items"])
    val arrayId = (item.id.takeIf(String::isNotBlank) ?: item.type) + ".array"
    return cache.getOrPut(arrayId) {
        Api.Type(type, arrayId, mapOf("items" to item))
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
): Map<String, Api.Type> =
    node["properties"]
        ?.run { fields().asSequence().associate { (key, value) -> key to parseRefOrType("", value) } }
        ?: throw NoSuchElementException("No properties for: " + node.toPrettyString())

// helpers

private fun String.dropPath() = split("/").last()
private fun Parser.shortName(id: String) = id.removePrefix(doc.info.title + "$")
