package cc.cryptopunks.ui.service.mapper

import cc.cryptopunks.ui.service.stub.MessengerApi
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.victools.jsonschema.generator.*
import kotlin.reflect.KClass

private fun main() {
    MessengerApi.methods
        .generateJsonSchema("")
        .toPrettyString()
        .let(::println)
}

fun List<KClass<*>>.generateJsonSchema(
    definitionsPath: String,
): ObjectNode =
    schemaGenerator.buildMultipleSchemaDefinitions()
        .apply { forEach { type: KClass<*> -> createSchemaReference(type.java) } }
        .collectDefinitions(definitionsPath)


private val schemaGenerator by lazy {
    schemaGenerator()
}

private fun schemaGenerator(
    packagePrefix: String = "",
) = SchemaGenerator(
    SchemaGeneratorConfigBuilder(
        SchemaVersion.DRAFT_7,
        OptionPreset.PLAIN_JSON
    ).apply {
        with(Option.DEFINITIONS_FOR_ALL_OBJECTS)
        with(Option.GETTER_METHODS)
        with(Option.FIELDS_DERIVED_FROM_ARGUMENTFREE_METHODS)
        with(Option.VALUES_FROM_CONSTANT_FIELDS)

        forMethods()
            .withReadOnlyCheck { methodScope ->
                methodScope.declaredName == "result"
            }

        forTypesInGeneral()
            .withDefinitionNamingStrategy { key, _ ->
                key.type.briefDescription.removePrefix(packagePrefix)
            }
    }.build()
)
