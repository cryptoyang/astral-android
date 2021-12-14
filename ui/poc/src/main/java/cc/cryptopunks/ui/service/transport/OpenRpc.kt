package cc.cryptopunks.ui.service.transport

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.ContextualDeserializer

object OpenRpc {

    data class Document(
        val openrpc: String,
        val info: Info,
        val servers: List<Server> = emptyList(),
        val methods: List<Method>,
        val components: Component = Component(),
        val externalDocs: ExternalDocument? = null,
    ) {

        fun Ref<JsonSchema>.resolveSchema(): JsonSchema = value
            ?: components.schemas[ref.removePrefix("#/schemas/")]
            ?: throw NoSuchElementException("Cannot resolve scheme for " + toString())
    }


    data class Info(
        val title: String,
        val description: String? = null,
        val termsOfService: String? = null,
        val contact: Contact? = null,
        val license: License? = null,
        val version: String,
    ) {
        data class Contact(
            val name: String,
            val url: String? = null,
            val email: String? = null,
        )

        data class License(
            val name: String,
            val url: String? = null,
        )
    }

    data class Server(
        val name: String,
        val url: String,
        val summary: String? = null,
        val description: String? = null,
        val variables: Map<String, Variable> = emptyMap(),
    ) {
        data class Variable(
            val enum: List<String> = emptyList(),
            val default: String,
            val description: String? = null,
        )
    }

    data class Method(
        val name: String,
        val tags: List<Ref<Tag>> = emptyList(),
        val summary: String? = null,
        val description: String? = null,
        val externalDocs: ExternalDocument? = null,
        val params: List<ContentDescriptor> = emptyList(),
        val result: Ref<JsonSchema>,
        val deprecated: Boolean = false,
        val servers: List<Server> = emptyList(),
        val errors: List<Ref<Error>> = emptyList(),
        val links: List<Ref<Link>> = emptyList(),
        val paramStructure: String? = null,
        val examples: List<Example.Pairing> = emptyList(),
    )

    data class ContentDescriptor(
        val name: String,
        val summary: String? = null,
        val description: String? = null,
        val required: Boolean = false,
        val schema: Ref<JsonSchema>,
        val deprecated: Boolean = false,
    )

    data class Example(
        val name: String? = null,
        val summary: String? = null,
        val description: String? = null,
        val value: Any? = null,
        val externalValue: String? = null,
    ) {
        data class Pairing(
            val name: String? = null,
            val summary: String? = null,
            val description: String? = null,
            val params: List<Example> = emptyList(),
            val result: Example? = null,
        )
    }

    data class Link(
        val name: String,
        val summary: String? = null,
        val description: String? = null,
        val method: String? = null,
        val params: Map<String, Any> = emptyMap(),
        val server: Server? = null,
    )

    data class Error(
        val code: Int,
        val message: String,
        val data: Any? = null,
    )

    data class Component(
        val contentDescriptors: Map<String, ContentDescriptor> = emptyMap(), // An object to hold reusable Content Descriptor Objects.
        val schemas: Map<String, JsonSchema> = emptyMap(), // An object to hold reusable Schema Objects.
        val examples: Map<String, Example> = emptyMap(), // An object to hold reusable Example Objects.
        val links: Map<String, Link> = emptyMap(), // An object to hold reusable Link Objects.
        val errors: Map<String, Error> = emptyMap(), // An object to hold reusable Error Objects.
        val examplePairingObjects: Map<String, Example.Pairing> = emptyMap(), // An object to hold reusable Example Pairing Objects.
        val tags: Map<String, Tag> = emptyMap(), // An object to hold reusable Tag Objects.
    )

    data class Tag(
        val name: String,
        val summary: String? = null,
        val description: String? = null,
        val externalDocs: ExternalDocument? = null,
    )

    data class ExternalDocument(
        val description: String? = null,
        val url: String,
    )

    @JsonDeserialize(using = Ref.Deserializer::class)
    @JsonSerialize(using = Ref.Serializer::class)
    data class Ref<T>(
        val ref: String = "",
        val value: T? = null,
    ) {
        class Serializer : JsonSerializer<Ref<*>>() {

            override fun serialize(
                value: Ref<*>,
                gen: JsonGenerator,
                serializers: SerializerProvider,
            ) {
                if (value.value != null) gen.writeObject(value.value)
                else gen.apply {
                    writeStartObject()
                    writeFieldName("\$ref")
                    writeString(value.ref)
                    writeEndObject()
                }
            }
        }

        class Deserializer(
            private val javaType: JavaType? = null,
        ) : JsonDeserializer<Ref<*>>(),
            ContextualDeserializer {

            override fun createContextual(
                ctxt: DeserializationContext,
                property: BeanProperty,
            ): JsonDeserializer<Ref<*>> = Deserializer(
                javaType = property.type.containedType(0)
            )

            override fun deserialize(
                p: JsonParser,
                ctxt: DeserializationContext,
            ): Ref<*> = p.readValueAsTree<JsonNode>().run {
                if (has("\$ref")) Ref(get("\$ref").asText())
                else Ref(value = ctxt.readTreeAsValue<Any>(this, javaType))
            }
        }
    }
}
