package cc.cryptopunks.ui.poc.mapper

import cc.cryptopunks.ui.poc.ReferenceProblemHandler
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule

object Jackson {

    val jsonMapper: JsonMapper by lazy {
        val kotlinModule = KotlinModule.Builder().build()
        val simpleModule = SimpleModule()

        JsonMapper.builder()
            .addHandler(ReferenceProblemHandler())
            .addModule(kotlinModule)
            .addModule(simpleModule)
            .build()
    }

    val slimMapper: ObjectMapper by lazy {
        jsonMapper
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
            .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
    }

    val prettyWriter: ObjectWriter by lazy {
        slimMapper.writerWithDefaultPrettyPrinter()
    }
}
