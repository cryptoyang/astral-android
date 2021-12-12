package cc.cryptopunks.ui.poc.transport.schema.json

import com.fasterxml.jackson.databind.node.ObjectNode
import kotlin.reflect.KClass

fun List<KClass<*>>.multipleDefinitionsSchema(
    definitionsPath: String,
): ObjectNode =
    schemaGenerator.buildMultipleSchemaDefinitions()
        .apply { forEach { type: KClass<*> -> createSchemaReference(type.java) } }
        .collectDefinitions(definitionsPath)
