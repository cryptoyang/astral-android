package cc.cryptopunks.ui.poc.mapper

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule

object Jackson {

    private val kotlinModule by lazy {
        KotlinModule.Builder().build()
    }

    val jsonMapper: JsonMapper by lazy {
        val simpleModule = SimpleModule()

        JsonMapper.builder()
            .addHandler(ReferenceProblemHandler())
            .addModule(kotlinModule)
            .addModule(simpleModule)
            .build()
    }

    val jsonSlimMapper: ObjectMapper by lazy {
        jsonMapper
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
            .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
    }

    val jsonPrettyWriter: ObjectWriter by lazy {
        jsonSlimMapper.writerWithDefaultPrettyPrinter()
    }

    val yamlMapper: ObjectMapper by lazy {
        ObjectMapper((YAMLFactory()))
            .registerModule(kotlinModule)
    }

    val emptyNode: ObjectNode by lazy {
        jsonMapper.createObjectNode()
    }
}

private class ReferenceProblemHandler : DeserializationProblemHandler() {

    override fun handleUnknownProperty(
        ctxt: DeserializationContext?,
        p: JsonParser?,
        deserializer: JsonDeserializer<*>?,
        beanOrClass: Any?,
        propertyName: String,
    ): Boolean = propertyName == "\$ref"

    override fun handleInstantiationProblem(
        ctxt: DeserializationContext?,
        instClass: Class<*>?,
        argument: Any?,
        t: Throwable?,
    ): Any {
        return super.handleInstantiationProblem(ctxt, instClass, argument, t)
    }
}
