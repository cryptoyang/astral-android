package cc.cryptopunks.ui.poc.schema.rpc

import cc.cryptopunks.ui.poc.mapper.Jackson
import cc.cryptopunks.ui.poc.api.MessengerApi
import cc.cryptopunks.ui.poc.schema.rpc.OpenRpc.ContentDescriptor
import cc.cryptopunks.ui.poc.schema.rpc.OpenRpc.Ref
import cc.cryptopunks.ui.poc.schema.Schema
import cc.cryptopunks.ui.poc.schema.json.multipleDefinitionsSchema
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.contains
import com.fasterxml.jackson.module.kotlin.convertValue
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

fun main() {
//    val doc = MessengerApi.generateOpenRpcDocument()
//    Jackson.jsonPrettyWriter.writeValueAsString(doc).let {
//        println(it)
//        val parsed = Jackson.jsonSlimMapper.readValue(it, OpenRpc.Document::class.java)
//        println(parsed)
//    }

    Jackson.jsonPrettyWriter.writeValueAsString(
        TestListHolder(
            int = 1,
            list = ListUpdate(
                add = listOf(
                    "asd",
                    "dsa",
                    "1111"
                )
            )
        )
    ).let(::println)
}

data class TestListHolder(
    val int: Int,
    val list: ListUpdate<String>,
)

fun Rpc.Api.generateOpenRpcDocument(): OpenRpc.Document =
    generateOpenRpcDocument(
        title = this::class.java.name,
        types = methods,
    )

fun generateOpenRpcDocument(
    title: String,
    types: List<KClass<*>>,
): OpenRpc.Document {

    val schemas: Map<String, JsonNode> = types
        .multipleDefinitionsSchema("schemas").fields()
        .asSequence().associate { (key, value) -> key to value }

    val methods: List<OpenRpc.Method> = types
        .filter { type -> type.isSubclassOf(Rpc.Command::class) }
        .map { method ->

            val name = method.java.name

            val params = method.memberProperties
                .filterNot { it.name == "result" }
                .associate { it.name to it.returnType.classifier as KClass<*> }
                .map { (name, type) ->
                    ContentDescriptor(
                        name = name,
                        schema = type.parseSchemasRef(schemas.keys)
                    )
                }

            val result = schemas[name]!!["properties"]["result"]
                ?.also {
                    (schemas[name]!!["properties"] as? ObjectNode)?.remove("result")
                }
                ?.run {
                    if (contains("\$ref")) Ref(get("\$ref").asText())
                    else Ref(value = this)
                }
                ?: Ref(value = emptyMap<String, Any>().jacksonConvert())


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

private fun KClass<*>.parseSchemasRef(schemas: Set<String>): Ref<Schema> {
    return when {
        jsonPrimitives.any { isSubclassOf(it) } -> primitiveTypeRef()
        isSubclassOf(Unit::class) -> Ref(value = emptyMap<String, Any>().jacksonConvert())
        java.name in schemas -> Ref("#/schemas/${java.name}")
        else -> Ref("unknown type $java")
    }
}

private val jsonPrimitives = setOf(Number::class, String::class, Boolean::class)

private fun KClass<*>.primitiveTypeRef() =
    Ref<Schema>(value = mapOf("type" to java.simpleName.lowercase()).jacksonConvert())

inline fun <reified T> Any.jacksonConvert() = Jackson.jsonSlimMapper.convertValue<T>(this)
