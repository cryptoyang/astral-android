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
                    result = parseRef(rpcMethod.result).run {
                        if (type == "array") copy(id = id) else this
                    },
                    params = rpcMethod.params.associate { descriptor ->
                        descriptor.name to parseRef(descriptor.schema)
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

private fun Parser.parseRef(
    ref: OpenRpc.Ref<Schema>
): Api.Type =
    when {
        ref.ref.isNotBlank() -> parseRef(ref.ref)
        ref.value != null -> parseNode(ref.ref, ref.value)
        else -> Api.Type.Empty
    }

private fun Parser.parseNode(
    id: String,
    node: JsonNode,
): Api.Type =
    when {
        "\$ref" in node -> parseRef(node["\$ref"].asText())
        "type" in node -> parseType(id, node, node["type"].asText())
        else -> Api.Type.Empty
    }

private fun Parser.parseRef(
    ref: String
): Api.Type =
    ref.split("/").last().let { id ->
        val shortId = shortName(id)
        cache.getOrPut(shortId) {
            val schema = requireNotNull(doc.components.schemas[id]) { "No schema for $id" }
            parseNode(shortId, schema)
        }
    }

private fun Parser.shortName(id: String) = id.removePrefix(doc.info.title + "$")

private fun Parser.parseType(
    id: String,
    node: JsonNode,
    type: String
): Api.Type =
    when (type) {
        "int", "boolean", "string" -> Api.Type(type, id, options = parseOptions(node))
        "array" -> Api.Type(type, id, mapOf("item" to parseNode(id, node["items"])))
        "object" -> Api.Type(type, id, parseProperties(node))
        else -> throw IllegalArgumentException("Cannot parse unknown type $type of $node")
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
        ?.run { fields().asSequence().associate { (key, value) -> key to parseNode("", value) } }
        ?: throw NoSuchElementException("No properties for: " + node.toPrettyString())

