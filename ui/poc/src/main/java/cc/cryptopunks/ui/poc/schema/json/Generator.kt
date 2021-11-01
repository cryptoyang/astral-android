package cc.cryptopunks.ui.poc.schema.json

import cc.cryptopunks.ui.poc.api.MessengerApi
import com.github.victools.jsonschema.generator.*
import kotlin.reflect.KClass


fun main() {
    schemaGenerator(MessengerApi::class.java.name).buildMultipleSchemaDefinitions()
        .also { builder ->
            MessengerApi.methods.map(KClass<*>::java).forEach(builder::createSchemaReference)
        }
        .collectDefinitions("")
        .run {
            println(toPrettyString())
        }
}

val schemaGenerator by lazy { schemaGenerator() }

fun schemaGenerator(
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
