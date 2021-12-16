package cc.cryptopunks.ui.service.base

import cc.cryptopunks.ui.mapper.Jackson
import cc.cryptopunks.ui.service.schema.OpenRpc
import cc.cryptopunks.ui.service.schema.JsonSchema
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.contains
import com.fasterxml.jackson.module.kotlin.convertValue
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

//private fun main() {
//    val doc = MessengerApi.generateOpenRpcDocument()
//    val json = Jackson.jsonPrettyWriter.writeValueAsString(doc)
//    val parsed = Jackson.jsonSlimMapper.readValue(json, OpenRpc.Document::class.java)
//    println(json)
//    println(doc)
//    println(parsed)
//    assert(doc == parsed)
//}

fun Rpc.Api.generateOpenRpcDocument(): OpenRpc.Document =
    generateOpenRpcDocument(
        title = title,
        types = methods,
    )

internal fun generateOpenRpcDocument(
    title: String,
    types: List<KClass<*>>,
): OpenRpc.Document {

    val schemas: Map<String, JsonNode> = types
        .generateJsonSchema("schemas").fields()
        .asSequence().associate { (key, value) -> key to value }

    val methods: List<OpenRpc.Method> = types
        .filter { type -> type.isSubclassOf(Rpc.Command::class) }
        .map { method ->

            val name = method.java.name

            val params = method.memberProperties
                .filterNot { it.name == "result" }
                .associate { it.name to it.returnType.classifier as KClass<*> }
                .map { (name, type) ->
                    OpenRpc.ContentDescriptor(
                        name = name,
                        schema = type.parseSchemasRef(schemas.keys)
                    )
                }

            val result = schemas[name]!!["properties"]["result"]
                ?.also {
                    (schemas[name]!!["properties"] as? ObjectNode)?.remove("result")
                }
                ?.run {
                    if (contains("\$ref")) OpenRpc.Ref(get("\$ref").asText())
                    else OpenRpc.Ref(value = this)
                }
                ?: OpenRpc.Ref(value = emptyMap<String, Any>().jacksonConvert())


            OpenRpc.Method(
                name = name,
                params = params,
                result = result
            )
        }

    return OpenRpc.Document(
        openrpc = "1.2.6",
        info = OpenRpc.Info(
            title = title,
            version = "1.0.0",
        ),
        methods = methods,
        components = OpenRpc.Component(
            schemas = schemas,
        )
    )
}

private fun KClass<*>.parseSchemasRef(schemas: Set<String>): OpenRpc.Ref<JsonSchema> {
    return when {
        jsonPrimitives.any { isSubclassOf(it) } -> primitiveTypeRef()
        isSubclassOf(Unit::class) -> OpenRpc.Ref(value = emptyMap<String, Any>().jacksonConvert())
        java.name in schemas -> OpenRpc.Ref("#/schemas/${java.name}")
        else -> OpenRpc.Ref("unknown type $java")
    }
}

private val jsonPrimitives = setOf(Number::class, String::class, Boolean::class)

private fun KClass<*>.primitiveTypeRef() =
    OpenRpc.Ref<JsonSchema>(value = mapOf("type" to java.simpleName.lowercase()).jacksonConvert())

private inline fun <reified T> Any.jacksonConvert() = Jackson.jsonSlimMapper.convertValue<T>(this)
